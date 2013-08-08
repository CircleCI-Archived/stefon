(ns dieter.cache.memory
  (:require [clojure.string :as cstr]
            [dieter.settings :as settings]
            [dieter.path :as path]
            [clojure.java.io :as io]))

(defonce assets (atom {}))

(defn cache-set! [asset]
  (let [digested (:digested asset)
        undigested (:undigested asset)]
    (swap! assets assoc digested asset)))

(defn cache-get [uri]
  (get @assets uri))
