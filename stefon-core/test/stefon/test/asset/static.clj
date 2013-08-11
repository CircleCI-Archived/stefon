(ns stefon.test.asset.static
  (:require [clojure.java.io :as io]
            [stefon.asset :as asset]
            [stefon.settings :as settings]
            [stefon.asset.static :as static])
  (:use clojure.test)
  (:import stefon.asset.static.Static))

(deftest test-static-assets
  (let [file (io/file "test/fixtures/assets/images/stefon.jpeg")
        asset (asset/read-asset (Static. file))]

    (testing "read-asset"
      (is (= (.length file)
             (count (:content asset)))))

    ;; (testing "compress returns unmodified content"
    ;;   (settings/with-options {:compress true}
    ;;     (is (= (:content asset)
    ;;            (asset/compress asset)))))
    ))