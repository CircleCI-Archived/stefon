(ns stefon.asset.static
  (:require [stefon.asset :as asset]))

(defrecord Static [file content]
  stefon.asset.Asset
  (read-asset [this]
    (assoc this :content
           (with-open [in (java.io.BufferedInputStream. (java.io.FileInputStream. (:file this)))]
             (let [buf (make-array Byte/TYPE (.length (:file this)))]
               (.read in buf)
               buf)))))
