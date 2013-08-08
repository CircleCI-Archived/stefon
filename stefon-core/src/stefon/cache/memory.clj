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


(defn cache-set-empty!
  "Add an empty asset with for a path. Useful for loading precompiled assets, and testing"
  [undigested digested]
  (swap! assets assoc digested {:digested digested
                                :undigested undigested}))

(defn cache-get [uri]
  (get @assets uri))

(defn cache-reset! []
  (reset! assets {}))
