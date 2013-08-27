(ns stefon.test.asset.less
  (:require [stefon.test.helpers :as h]
            [stefon.asset.less :as less]
            [stefon.util :refer (dump)])
  (:use clojure.test))

(deftest test-basic
  (h/test-expected "test/fixtures/assets"
                   "stylesheets/basic.css.less"
                   "/assets/stylesheets/basic-2c2167c0f4a90e20413715e33b17bb82.css"
                   ["#header{color:#4d926f;}\n"]))

(deftest test-with-imports
  (h/test-expected "test/fixtures/assets"
                   "stylesheets/includes.css.less"
                   "/assets/stylesheets/includes-b9650f9c695dc4a463e5b1c5e3643c09.css"
                   ["#includee{color:white;}\n#includee-three{color:white;}\n#includee-two{color:white;}\n#includer{color:black;}\n"]))

(deftest test-preprocess-less
  (h/test-syntax "test/fixtures/assets"
                 "stylesheets/bad.css.less"
                 ["Syntax Error on line 1" "@import \"includeme.css.less\""]))
