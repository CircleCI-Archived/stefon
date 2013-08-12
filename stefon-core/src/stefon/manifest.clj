(ns stefon.manifest
 "Maps adrfs to the compiled form. Can be saved from or loaded to a manifest-file"
  (:require [stefon.settings :as settings]
            [stefon.path :as path]
            [stefon.util :refer (dump)]
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

(defn save! []
  (-> @mapping
      (#(json/generate-string % {:pretty true}))
      (spit (settings/manifest-file))))

(defn load! []
  (->> (settings/manifest-file)
       slurp
       json/parse-string
       (swap! mapping identity)))
