(ns stefon.test.asset.less
  (:require [stefon.test.helpers :as h]
            [stefon.asset.less :as less]
            [stefon.util :refer (dump)])
  (:use clojure.test))

(deftest test-basic
  (h/test-expected "test/fixtures/assets"
                   "stylesheets/basic.css.less"
                   "/assets/stylesheets/basic.css"
                   ["#header {\n  color: #4d926f;\n}\n"]))

(deftest test-with-imports
  (h/test-expected "test/fixtures/assets"
                   "stylesheets/includes.css.less"
                   "/assets/stylesheets/includes.css"
                   ["#includee {\n  color: white;\n}\n#includee-three {\n  color: white;\n}\n#includee-two {\n  color: white;\n}\n#includer {\n  color: black;\n}\n"]))

(deftest test-preprocess-less
  (h/test-syntax "test/fixtures/assets"
                 "stylesheets/bad.css.less"
                 ["Syntax Error on line 1" "@import \"includeme.css.less\""]))
