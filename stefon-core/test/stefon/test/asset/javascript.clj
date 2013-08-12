(ns stefon.test.asset.javascript
  (:require [stefon.test.helpers :as h]
            [stefon.asset.coffeescript]
            [stefon.util :refer (dump)])
  (:use clojure.test))

(deftest test-javascript
  (h/test-expected "test/fixtures/assets"
                   "javascripts/app.js"
                   "javascripts/app.js"
                   "var file = \"/app.js\"\n"))
