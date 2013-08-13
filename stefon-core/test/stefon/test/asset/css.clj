(ns stefon.test.asset.css
  (:require [stefon.test.helpers :as h]
            [stefon.util :refer (dump)])
  (:use clojure.test))

(deftest test-css
  (h/test-expected "test/fixtures/assets"
                   "stylesheets/main.css"
                   "/assets/stylesheets/main-b63a41ce251e2b1ffbb5c6aa32d984d2.css"
                   [".fancy {\n    text-decoration: blink;\n}"]))


;; (deftest test-compress-css
;;   (let [uncompressed-css "   .content .p {\n color: #fff;\n }"
;;         asset (Css. "filename.css" uncompressed-css)]
;;     (is (= ".content .p { color: #fff; }"
;;            (asset/compress asset)))))