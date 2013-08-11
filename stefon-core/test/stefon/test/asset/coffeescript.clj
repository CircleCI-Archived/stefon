(ns stefon.test.asset.coffeescript
  (:require [stefon.asset :as asset]
            [stefon.settings :as settings]
            [stefon.test.helpers :as h])
  (:use clojure.test))

(defn test-expected [root file expected]
  (settings/with-options {:asset-roots [root]}
    (is (= expected
            (-> file
                asset/compile
                first)))))

(defn test-syntax [root file expecteds]
  (settings/with-options {:asset-roots [root]}
    (try
      (asset/compile file)
      (is false)
      (catch Exception e
        (doseq [expected expecteds]
          (is (h/has-text? (.toString e) expected)))))))


(deftest test-coffeescript
  (test-expected "test/fixtures/assets"
                 "javascripts/test.js.coffee"
                 "(function() {\n\n  (function(param) {\n    return alert(\"x\");\n  });\n\n}).call(this);\n")

  (test-syntax "test/fixtures/assets"
               "javascripts/bad.js.coffee"
               ["on line 2"  "unmatched ]"]))