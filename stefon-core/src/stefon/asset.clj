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
  (:import [java.io BufferedInputStream FileInputStream]))

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

(defn find-file [adrf]
  {:post [(or (nil? %) (-> % io/file .exists))]}
  (or (reduce #(or %1 (path/find-file (path/adrf->filename %2 adrf)))
               nil
               (settings/asset-roots))
      (throw (java.io.FileNotFoundException.
              (str "could not find " adrf " in any of " (settings/asset-roots))))))

(defn read-file [file]
  (with-open [in (-> file FileInputStream. BufferedInputStream.)]
    (let [buf (-> file .length byte-array)]
      (.read in buf)
      buf)))

(derive (class (make-array Byte/TYPE 0)) ::bytes)
(derive java.lang.String ::string-like)
(derive java.lang.StringBuilder ::string-like)

(defmulti write-to-disk (fn [f c] (class c)))
(defmethod write-to-disk ::string-like [file content]
  (spit file content))

(defmethod write-to-disk ::bytes [file content]
  (with-open [out (java.io.FileOutputStream. file)]
    (.write out content)))

(defn write-asset [asset]
  (let [f (->> asset
               :digested
               path/uri->adrf
               (io/file (settings/serving-asset-root)))]
    (io/make-parents f)
    (write-to-disk f (:content asset))
    (:digested asset)))


(defn apply-pipeline [file content]
  (let  [name (fs/name file)
         ext (fs/extension file)
         precompiler (get @types ext)]
    ;; TODO there is a way to skip stages too, if they've been precompiled
    (if ext
      (do
        ;; TODO: md5 source + options
        ;; TODO: check-disk
        (infof "[%10s] %s -> %s" ext file name)
        (apply-pipeline name (precompiler content)))
      (do
        [file ext content]))))

(defn compile
  "returns [content filename]"
  [adrf]
   (->> adrf
        find-file
        read-file
        (apply-pipeline (fs/file adrf))))

(defn build [adrf]
  (let [[undigested content] (compile adrf)
        digested (path/path->digested undigested content)]
    (infof "%s -> %s" adrf digested)
    (manifest/set! undigested digested)
    (write-asset content digested)))