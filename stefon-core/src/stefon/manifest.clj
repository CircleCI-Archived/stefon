(ns stefon.manifest
 "Maps adrfs to the compiled form. Can be saved from or loaded to a manifest-file"
  (:require [stefon.settings :as settings]
            [stefon.path :as path]
            [stefon.util :refer (dump)]
            [clojure.java.io :as io]
            [cheshire.core :as json]))

(defonce mapping (atom {}))

(defn set!
  [undigested digested]
  {:pre [(not (path/asset-uri? undigested))
         (path/asset-uri? digested)
         (path/digest-path? digested)
         (not (path/digest-path? undigested))]}
  (swap! mapping assoc undigested digested))

(defn fetch [adrf]
  (get @mapping adrf))

(defn clear! []
  (reset! mapping {}))

(defn save-string []
  (-> @mapping
      (json/generate-string {:pretty true})))

(defn save!
  "Write the current mapping to the manifest file, creating the directory
  structure if necessary."
  []
  (let [manifest-file (io/file (settings/manifest-file))
        parent-dir (.getParentFile manifest-file)]
    (when parent-dir (.mkdirs parent-dir))
    (spit manifest-file (save-string))))

(defn load-map! [map]
  (swap! mapping (constantly map)))

(defn load! []
  (-> (settings/manifest-file)
      slurp
      json/parse-string
      load-map!))
