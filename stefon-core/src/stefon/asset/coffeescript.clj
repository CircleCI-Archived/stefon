(ns stefon.asset.coffeescript
  (:refer-clojure :exclude [compile])
  (:require [stefon.jsengine :as jsengine]
            [stefon.asset :as asset]))

(def compile (jsengine/compiler "compileCoffeeScript"
                                ["coffee-script.js" "coffee-wrapper.js"]))

(asset/register "cs" compile)
(asset/register "coffee" compile)
