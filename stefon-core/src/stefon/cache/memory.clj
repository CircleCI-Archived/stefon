(ns stefon.cache.memory
 "Stores the assets in between compiling them or loading them and actually requesting them"
  (:require [clojure.string :as cstr]
            [stefon.settings :as settings]
            [stefon.path :as path]
            [stefon.asset :as asset]
            [stefon.util :refer (inspect)]
            [clojure.java.io :as io]))

(defonce assets (atom {}))

(defn cache-set! [asset]
  (let [digested (:digested asset)
        undigested (:undigested asset)]
    (swap! assets assoc digested asset)
    (asset/write-asset asset)))

;; TODO: can we get rid of this?
;; TODO: or maybe get rid of asset content entirely from assets
(defn cache-set-empty!
  "Add an empty asset with for a path. Useful for loading precompiled assets, and testing"
  [undigested digested]
  (swap! assets assoc digested {:digested digested
                                :undigested undigested}))

(defn cache-get [uri]
  (get @assets uri))

(defn cache-reset! []
  (reset! assets {}))