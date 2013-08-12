(ns stefon.test.asset.coffeescript
  (:require [stefon.test.helpers :as h]
            [stefon.asset.coffeescript]
            [stefon.util :refer (dump)])
  (:use clojure.test))


(deftest test-coffeescript
  (h/test-expected "test/fixtures/assets"
                   "javascripts/test.js.coffee"
                   "javascripts/test.js"
                   "(function() {\n\n  (function(param) {\n    return alert(\"x\");\n  });\n\n}).call(this);\n")

  (h/test-syntax "test/fixtures/assets"
                 "javascripts/bad.js.coffee"
                 ["on line 2"  "unmatched ]"]))