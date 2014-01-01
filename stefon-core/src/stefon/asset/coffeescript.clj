(ns stefon.asset.coffeescript
  (:refer-clojure :exclude [compile])
  (:require [stefon.jsengine :as jsengine]
            [stefon.asset :as asset]))

(def processor (jsengine/compiler "compileCoffeeScript"
                                ["coffee-script.js" "coffee-wrapper.js"]))

(asset/register "cs" processor)
(asset/register "coffee" processor)
