(ns dieter.asset.hamlcoffee
  (:require [dieter.pools :as pools]
            [dieter.asset :as asset]
            [dieter.asset.javascript])
  (:use [dieter.jsengine :only (run-compiler)]))

(def pool (pools/make-pool))

(defn compile-coffeescript [file]
  (run-compiler pool
                ["coffee-script.js"
                 ;; imported direct from https://raw.github.com/netzpirat/haml-coffee/master/dist/compiler/hamlcoffee.js
                 "hamlcoffee.js"
                 "haml_coffee_assets-rhino-fix.js"
                 ;; imported direct from https://raw.github.com/netzpirat/haml_coffee_assets/master/lib/js/haml_coffee_assets.js
                 "haml_coffee_assets.js"
                 "hamlcoffee-wrapper.js"]
                "compileHamlCoffee"
                file))

(defn preprocess-hamlcoffee [file]
  (asset/memoize-file file compile-coffeescript))

(defrecord HamlCoffee [file]
  dieter.asset.Asset
  (read-asset [this]
    (dieter.asset.javascript.Js. (:file this) (preprocess-hamlcoffee (:file this)))))

(asset/register "hamlc" map->HamlCoffee)