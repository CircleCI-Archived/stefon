(ns stefon.asset
  (:require [clj-time.core :as time]
            [clj-time.coerce :as time-coerce]
            [clojure.string :as cstr]
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
  (if-let [f (get @types (file-ext file) (:default @types))]
    (f {:file file})
    (throw (Exception. (str "No registered asset-type for " file)))))

(defn build [adrf]
  (when-let [asset (-> adrf
                       inspect
                       path/find-asset
                       make-asset
                       read-asset
                       ;; TODO add back compression
                       )]
    (let [undigested-uri (path/adrf->uri adrf)
          digested-uri (path/path->digested undigested-uri (:content asset))]
      (-> asset
          (assoc :digested digested-uri)
          (assoc :undigested undigested-uri)))))