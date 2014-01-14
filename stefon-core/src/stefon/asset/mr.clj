(ns stefon.asset.mr
  "extract, transform and load, kinda"
  (:require [stefon.asset :as asset]
            [stefon.asset.stefon :as stefon]
            [stefon.path :as path]))


(defn mr [root adrf content options]
  (let [mapper (or (:map options) identity)
        reducer (or (:reduce options) identity)
        mapped (for [sf (stefon/stefon-files root adrf content)]
                 (let [[compiled content] (asset/compile root sf)]
                   (mapper root sf compiled content options)))]
    (reducer mapped root adrf content options)))

(asset/register "mr" mr)
