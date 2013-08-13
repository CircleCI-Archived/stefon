(ns stefon.asset
  (:require [clj-time.core :as time]
            [clojure.string :as cstr]
            [clojure.java.io :as io]
            [clojure.tools.logging :refer (infof)]
            [me.raynes.fs :as fs]
            [stefon.settings :as settings]
            [stefon.jsengine :as jsengine]
            [stefon.manifest :as manifest]
            [stefon.util :refer (dump)]
            [stefon.path :as path])
  (:import [java.io BufferedInputStream FileInputStream FileNotFoundException]))

;;;;;;;;;;;;;;;;;;;;;;;
;;; Register assets
;;;;;;;;;;;;;;;;;;;;;;;
"mapping of file types to constructor functions"
(defonce types
  (atom {}))

(defn register [extension processor]
  (swap! types assoc extension processor))

;;;;;;;;;;;;;;;;;;;;;;;
;;; Compiler
;;;;;;;;;;;;;;;;;;;;;;;

(defn file-not-found [root adrf]
  (throw (FileNotFoundException. (format "Couldn't find %s in %s" adrf root))))

(defn find-file [adrf]
  (or (reduce #(or %1 (path/find-file %2 adrf))
               nil
               (settings/asset-roots))
      (file-not-found (settings/asset-roots) adrf)))

(defn read-file [root adrf]
  (let [file (io/file root adrf)]
    (when-not (.exists file)
      (file-not-found root adrf))
    (with-open [in (-> file FileInputStream. BufferedInputStream.)]
      (let [buf (-> file .length byte-array)]
        (.read in buf)
        buf))))

(derive (class (make-array Byte/TYPE 0)) ::bytes)
(derive java.lang.String ::string-like)
(derive java.lang.StringBuilder ::string-like)

(defmulti write-to-disk (fn [f c] (class c)))
(defmethod write-to-disk ::string-like [file content]
  (spit file content))

(defmethod write-to-disk ::bytes [file content]
  (with-open [out (java.io.FileOutputStream. file)]
    (.write out content)))

(defn write-asset [content digested]
  (let [f (->> digested
               path/uri->adrf
               (io/file (settings/serving-asset-root)))]
    (io/make-parents f)
    (write-to-disk f content)
    digested))

(defn split [filename]
  "split around the last '."
  (let [index (.lastIndexOf filename ".")]
    (if (pos? index)
      [(.substring filename 0 index) (.substring filename (+ 1 index))]
      [filename ""])))

(defn name [filename]
  (-> filename split first))

(defn extension [filename]
  (-> filename split second))

;; TODO there is a way to skip stages too, if they've been precompiled
;; TODO: md5 source + options
;; TODO: check-disk cache
;; track dependencies
(defn apply-pipeline [root adrf content]
  (let [name (name adrf)
        ext (extension adrf)
        precompiler (get @types ext)]
    (if precompiler
      (let [content (precompiler root adrf content)]
        (printf "%-9s %s -> %s\n" (str "[" ext "]") adrf name)
        (apply-pipeline root name content))
      [adrf content])))


(defn compile
  "returns [digested content]"
  [root adrf]
  (let [[name content] (->> (read-file root adrf)
                            (apply-pipeline root adrf))
        digested (-> name
                     path/adrf->uri
                     (path/path->digested content))]
    [digested content]))

(defn build [adrf]
  (let [found (find-file adrf)
        [digested content] (apply compile found)]
    ;; TODO not all built assets will need to be written. They will for
    ;; asset-path and asset-uri. They won't for data-uri, less, and stefon files.
    (manifest/set! adrf digested)
    (write-asset content digested)
    [digested content]))