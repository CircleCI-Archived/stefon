(ns stefon.test.asset.javascript
  (:require [stefon.test.helpers :as h]
            [stefon.util :refer (dump)])
  (:use clojure.test))

(deftest test-javascript
  (h/test-expected "test/fixtures/assets"
                   "javascripts/app.js"
                   "/assets/javascripts/app-895a9f207aea908554d644c9bd160d5f.js"
                   ["var file = \"/app.js\"\n"]))

;; (deftest test-compress-js
;;   (testing "valid javascript"
;;     (let [uncompressed-js " var foo = 'bar'; "
;;           asset (Js. "filename.js" uncompressed-js)]
;;       (is (= "var foo=\"bar\";" (asset/compress asset)))))

;;   (testing "with compile errors"
;;     (let [uncompressed-with-errors "var foo = [1, 2, 3, ];"
;;           asset (Js. "haz-errors.js" uncompressed-with-errors)]
;;       (settings/with-options {:compress true :log-level :quiet}
;;         (is (= uncompressed-with-errors (asset/compress asset)))))))