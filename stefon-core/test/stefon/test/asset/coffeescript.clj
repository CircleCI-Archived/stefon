(ns stefon.test.asset.coffeescript
  (:require [stefon.test.helpers :as h]
            [stefon.asset.coffeescript]
            [stefon.util :refer (dump)])
  (:use clojure.test))


(deftest test-coffeescript
  (h/test-expected "test/fixtures/assets"
                   "javascripts/test.js.coffee"
                   "/assets/javascripts/test-b74cbe00c1ccfd87aab2f921da52fe4a.js"
                   ["(function() {\n\n  (function(param) {\n    return alert(\"x\");\n  });\n\n}).call(this);\n"]))

(deftest test-syntax-error
   (h/test-syntax "test/fixtures/assets"
                  "javascripts/bad.js.coffee"
                  ["on line 2" "unmatched ]"]))