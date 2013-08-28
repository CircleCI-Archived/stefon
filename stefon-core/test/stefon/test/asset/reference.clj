(ns stefon.test.asset.reference
  (:require [stefon.test.helpers :as h]
            [stefon.asset.less :as less]
            [stefon.asset.hamlcoffee :as hamlc]
            [stefon.asset.coffeescript :as coffee]
            [stefon.asset.reference :as ref]
            [stefon.util :refer (dump)])
  (:use clojure.test))


;; throws on non-existant refernce
;; mutliple ones per line

(deftest test-asset-path-in-js
  (h/test-expected "test/fixtures/reference/assets"
                   "javascripts/testpath.js.coffee.ref"
                   "/assets/javascripts/testpath-bcba95e202390838c2ef17d7b3885839.js"
                   ["(function() {\n  var x;\n\n  x = '/assets/images/image-102c15cd1a2dfbe24b8a5f12f2671fc8.jpeg';\n\n}).call(this);\n"]))

(deftest test-asset-uri-in-css
  (h/test-expected "test/fixtures/reference/assets"
                   "javascripts/testuri.css.less.ref"
                   "/assets/javascripts/testuri-35e5ffbd04bbac344b9aad35e48fa782.css"
                   [".outer{background-image:url(\"/assets/images/image-102c15cd1a2dfbe24b8a5f12f2671fc8.jpeg\");}\n"]))

(deftest test-data-uri-in-js
  (h/test-expected "test/fixtures/reference/assets"
                   "javascripts/testdata.js.coffee.ref"
                   "/assets/javascripts/testdata-dc68eed2f0ed1b90b175acc1e337cff6.js"
                   ["x = \"data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD/2wCEAAk"
                    "x = 'data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD/2wCEAAk"
                    "x = \"data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD/2wCEAAk"
                    "x = 'data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD/2wCEAAk"]))

(deftest test-multiple-per-line
  (h/test-expected "test/fixtures/reference/assets"
                   "javascripts/testmultiple.js.ref"
                   "/assets/javascripts/testmultiple-1fe91acb475290c2dbc2729abdc197ab.js"
                   ["x = \"/assets/images/image-102c15cd1a2dfbe24b8a5f12f2671fc8.jpeg\"; y = \"/assets/images/image-102c15cd1a2dfbe24b8a5f12f2671fc8.jpeg\";"]))

(deftest test-syntax-error
   (h/test-syntax "test/fixtures/reference/assets"
                  "javascripts/bad.js.coffee.ref"
                  ["java.io.FileNotFoundException: Couldn't find some-file-which-doesnt-exit.jpeg in test/fixtures/reference/assets"]))
