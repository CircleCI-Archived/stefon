(ns stefon.asset
  (:require [clj-time.core :as time]
            [clj-time.coerce :as time-coerce]
            [clojure.string :as cstr]
            [stefon.path :as path]))

(defprotocol Asset
  "Protocol for pre-processing assets"
  (read-asset [this]
    "Perform all pre-processing on the object. Must return an Asset."))

(defprotocol Compressor
  "Protocol for compressing assets"
  (compress [this]
    "Perform any required compression / minification.
Must return final contents of the file for output.
Contents can be a String, StringBuilder, or byte[]"))

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

(def types "mapping of file types to constructor functions"
  (atom {}))

(defn register [ext constructor-fn]
  "register a new asset constructor for files with the file extension ext"
  (swap! types assoc ext constructor-fn))

(defn file-ext [file]
  (last (cstr/split (str file) #"\.")))

(defn make-asset [file]
  "returns a newly constructed asset of the proper type as determined by the file extension.
defaults to Static if extension is not registered."
  ((get @types
        (file-ext file)
        (:default @types))
   {:file file}))

(defn build [adrf]
  (let [asset (-> adrf
                  path/find-asset
                  (make-asset)
                  (read-asset)
                  ;; TODO add back compression
                  )
        undigested-uri (path/adrf->uri adrf)
        digested-uri (path/path->digested undigested-uri (:content asset))]
    (assoc :digested digested-uri)
    (assoc :undigested undigested-uri)))