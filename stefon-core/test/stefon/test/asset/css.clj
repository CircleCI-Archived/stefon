(ns stefon.test.asset.css
  (:require [stefon.test.helpers :as h]
            [stefon.util :refer (dump)])
  (:use clojure.test))

(deftest test-css
  (h/test-expected "test/fixtures/assets"
                   "stylesheets/main.css"
                   "/assets/stylesheets/main.css"
                   [".fancy {\n    text-decoration: blink;\n}"]))


;; (deftest test-compress-css
;;   (let [uncompressed-css "   .content .p {\n color: #fff;\n }"
;;         asset (Css. "filename.css" uncompressed-css)]
;;     (is (= ".content .p { color: #fff; }"
;;            (asset/compress asset)))))