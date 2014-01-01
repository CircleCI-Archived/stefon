(ns stefon.asset.hamlcoffee
  (:refer-clojure :exclude [compile])
  (:require [stefon.asset :as asset]
            [stefon.jsengine :as jsengine]
            [stefon.settings :as settings]))

;; imported from
;; https://raw.github.com/netzpirat/haml-coffee/master/dist/compiler/hamlcoffee.js
;; https://raw.github.com/netzpirat/haml_coffee_assets/master/lib/js/haml_coffee_assets.js
(def processor
  (jsengine/compiler "compileHamlCoffee"
                     ["coffee-script.js"
                      "hamlcoffee.js"
                      "haml_coffee_assets-rhino-fix.js"
                      "haml_coffee_assets.js"
                      "hamlcoffee-wrapper.js"]
                     :options (settings/hamlcoffee-options)))

(asset/register "hamlc" processor)
