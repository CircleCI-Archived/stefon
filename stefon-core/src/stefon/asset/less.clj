(ns stefon.asset.less
  (:refer-clojure :exclude [compile])
  (:require [stefon.jsengine :as jsengine]
            [stefon.asset :as asset]))

;; compile with compression to prevent triggering a bug in clj-v8
(def processor (jsengine/compiler "compileLessCompress"
                                ["less-wrapper.js" "less-rhino-1.3.3.js"]
                                :memoize false))

(asset/register "less" processor)
