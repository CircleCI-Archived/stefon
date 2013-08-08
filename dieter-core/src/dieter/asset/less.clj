(ns dieter.asset.less
  (:require [dieter.pools :as pools]
            dieter.asset.css
            [dieter.settings :as settings]
            [dieter.asset :as asset])
  (:use [dieter.jsengine :only (run-compiler)]))

(def pool (pools/make-pool))

(defn preprocess-less [file]
  (run-compiler pool
                ["less-wrapper.js" "less-rhino-1.3.3.js"]
                ;; TODO: pass options
                file))

(defrecord Less [file]
  dieter.asset.Asset
  (read-asset [this]
    (dieter.asset.css.Css. (:file this) (preprocess-less (:file this)))))

(asset/register "less" map->Less)
