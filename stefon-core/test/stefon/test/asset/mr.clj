(ns stefon.test.asset.mr
  (:require [stefon.test.helpers :as h]
            [stefon.settings :as settings]
            [stefon.asset.mr :as mr]
            [stefon.asset :as asset]
            [stefon.util :refer (dump)])
  (:use [clojure.test]))

(deftest test-simple-example
  (let [root "test/fixtures/assets"
        adrf "javascripts/content.js.mr"
        options {:map (fn [root adrf compiled content options] {adrf compiled})
                 :reduce (fn [all root adrf content options] [(count all) all])}
        result (mr/mr root adrf (asset/read-file root adrf) options)]
    (is (= result [2 (list {"javascripts/basic.hamlc" "/assets/javascripts/basic-07da6554d81caef2b995caf987514c35"}
                           {"javascripts/lib/dquery.js" "/assets/javascripts/lib/dquery-d33b26a52b757a46198ef87351da3752.js"})]))))
