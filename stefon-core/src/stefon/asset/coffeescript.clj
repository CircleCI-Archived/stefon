(ns stefon.asset.coffeescript
  (:require [stefon.asset :as asset]
            stefon.asset.javascript
            [stefon.pools :as pools])
  (:use [stefon.jsengine :only (run-compiler)]))

(def pool (pools/make-pool))

(defn compile-coffeescript [file]
  (run-compiler pool
                ["coffee-script.js" "coffee-wrapper.js"]
                "compileCoffeeScript"
                file))

(defn preprocess-coffeescript [file]
  (asset/memoize-file file compile-coffeescript))

(defrecord Coffee [file]
  stefon.asset.Asset
  (read-asset [this]
    (stefon.asset.javascript.Js. (:file this) (preprocess-coffeescript (:file this)))))
