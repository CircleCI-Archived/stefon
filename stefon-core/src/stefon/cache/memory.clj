(ns stefon.cache.memory
  (:require [clojure.string :as cstr]
            [stefon.settings :as settings]
            [stefon.path :as path]
            [clojure.java.io :as io]))

(defonce assets (atom {}))

(defn cache-set! [asset]
  (let [digested (:digested asset)
        undigested (:undigested asset)]
    (swap! assets assoc digested asset)))

(defn cache-get [uri]
  (get @assets uri))
