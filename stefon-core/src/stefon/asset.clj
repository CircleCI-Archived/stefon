(ns stefon.asset
  (:require [clj-time.core :as time]
            [clj-time.coerce :as time-coerce]
            [clojure.string :as cstr]
            [clojure.java.io :as io]
            [stefon.settings :as settings]
            [stefon.util :refer (inspect)]
            [stefon.path :as path]))

(defprotocol Asset
  "Protocol for pre-processing assets"
  (read-asset [this]
    "Perform all pre-processing on the object. Must return an Asset."))

;;;;;;;;;;;;;;;;;;;;;;;
;;; Memoizing already compiled assets
;;;;;;;;;;;;;;;;;;;;;;;

(def memoized (atom {}))
(defn memoize-file [file f]
  "Ability to cache precomputed files using timestamps (avoiding the term \"cache\" since it'ss already overloaded here)"
  (let [filename (.getCanonicalPath file)
        val (get @memoized filename)
        current-timestamp (-> file .lastModified time-coerce/from-long)
        saved-timestamp (:timestamp val)
        saved-content (:content val)]
    (if (and saved-content
             (time/before? current-timestamp saved-timestamp))

      ;; return already memory
      saved-content

      ;; compute new value and save it
      (let [new-content (f file)]
        (dosync
         (swap! memoized assoc filename {:content new-content
                                         :timestamp (time/now)}))
        new-content))))

;;;;;;;;;;;;;;;;;;;;;;;
;;; Register assets
;;;;;;;;;;;;;;;;;;;;;;;

"mapping of file types to constructor functions"
(defonce types
  (atom {}))

(defn register [ext constructor-fn]
  "register a new asset constructor for files with the file extension ext"
  (swap! types assoc ext constructor-fn)
  nil)

(defn file-ext [file]
  (last (cstr/split (str file) #"\.")))

(defn make-asset [file]
  "returns a newly constructed asset of the proper type as determined by the file extension.
defaults to Static if extension is not registered."
  ((get @types (file-ext file) (:default @types)) {:file file}))

(defn find-asset [adrf]
  {:post [(or (nil? %) (-> % io/file .exists))]}
  (or (reduce #(or %1 (path/find-file (path/adrf->filename %2 adrf)))
               nil
               (settings/asset-roots))
      (throw (java.io.FileNotFoundException.
              (str "could not find " adrf " in any of " (settings/asset-roots))))))

(defn build-asset [adrf]
  (when-let [asset (-> adrf
                       find-asset
                       make-asset
                       read-asset)]
    (let [undigested-uri (path/adrf->uri adrf)
          digested-uri (path/path->digested undigested-uri (:content asset))]
      (-> asset
          (assoc :digested digested-uri)
          (assoc :undigested undigested-uri)))))


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
