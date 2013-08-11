(ns stefon.asset.less
  (:require [stefon.jsengine :as jsengine]
            [stefon.asset :as asset]))

(def compile (jsengine/compiler "compileLessNoCompress"
                                ["less-wrapper.js" "less-rhino-1.3.3.js"]))

(asset/register "less" compile)
