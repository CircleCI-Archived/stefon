(ns stefon.asset.less
  (:require [stefon.jsengine :as jsengine]
            [stefon.asset :as asset]))

;; compile with compression to prevent triggering a bug in clj-v8
(def compile (jsengine/compiler "compileLessCompress"
                                ["less-wrapper.js" "less-rhino-1.3.3.js"]
                                :memoize false))

(asset/register "less" compile)
