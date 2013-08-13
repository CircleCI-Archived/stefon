(ns stefon.test.asset.reference
  (:require [stefon.test.helpers :as h]
            [stefon.asset.reference]
            [stefon.util :refer (dump)])
  (:use clojure.test))


;; throws on non-existant refernce


;; mutliple ones per line


(deftest test-asset-path-in-js
  (h/test-expected "test/fixtures/reference/assets"
                   "javascripts/testpath.js.coffee.ref"
                   "/assets/javascripts/testpath.js"
                   ["(function() {\n  var x;\n\n  x = '/assets/images/image-102c15cd1a2dfbe24b8a5f12f2671fc8.jpeg';\n\n}).call(this);\n"]))

(deftest test-asset-uri-in-css
  (h/test-expected "test/fixtures/reference/assets"
                   "javascripts/testuri.css.less.ref"
                   "/assets/javascripts/testuri.css"
                   [".outer {\n  background-image: url(/assets/images/image-102c15cd1a2dfbe24b8a5f12f2671fc8.jpeg);\n}"]))

(deftest test-data-uri-in-js
  (h/test-expected "test/fixtures/reference/assets"
                   "javascripts/testdata.js.coffee.ref"
                   "/assets/javascripts/testdata.js"
                   ["var x = \"/assets/image-somedigest.jpeg\";"]))

(deftest test-multiple-per-line
  (h/test-expected "test/fixtures/reference/assets"
                   "javascripts/testmultiple.js.coffee.ref"
                   "/assets/javascripts/testmultiple.js"
                   ["var x = \"assets/image-somedigest.jpeg\";"]))

(deftest test-syntax-error
   (h/test-syntax "test/fixtures/reference/assets"
                  "javascripts/bad.js.coffee.ref"
                  ["some unknown error"]))