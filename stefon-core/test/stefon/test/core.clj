(ns stefon.test.core
  (:require [stefon.core :as core]
            [stefon.settings :as settings]
            [stefon.cache.memory :as mem]
            [stefon.path :as path]
            [stefon.asset :as asset]
            [stefon.test.helpers :as h]
            [clojure.java.io :as io])
  (:use clojure.test
        ring.mock.request))

(deftest test-link-to-asset
  (testing "development mode"
    (let [opts {:cache-mode :development :asset-root "test/fixtures"}]
      (is (nil? (core/link-to-asset "javascripts/dontfindme.js" opts)))
      ;; (is (= "/assets/javascripts/app.js" (core/link-to-asset "javascripts/app.js" opts)))
      ;; (is (= "/assets/javascripts/manifest.js.stefon" (core/link-to-asset "javascripts/manifest.js.stefon" opts)))
      ))

  ;; (testing "production mode"
  ;;   (let [opts {:cache-mode :production
  ;;               :asset-root "test/fixtures"}]

  ;;     (testing "no previous file generated"
  ;;       (is (path/digest-path? (core/link-to-asset "javascripts/app.js" opts))))

  ;;     (testing "file previously generated"
  ;;       (mem/cache-set-empty! "/assets/javascripts/app.js"
  ;;                             "/assets/javascripts/app-12345678901234567890af1234567890.js")

  ;;       (is (= "/assets/javascripts/app-12345678901234567890af1234567890.js"
  ;;              (core/link-to-asset "javascripts/app.js" opts))))))
  )


(deftest test-core-link-to-asset-in-secondary-dir
  (testing "development mode"
    (let [opts {:cache-mode :development
                :asset-roots ["test/fixtures" "test/fixtures/more_assets"]}]
      (is (nil? (core/link-to-asset "javascripts/dontfindme.js" opts)))
      (is (= "/assets/javascripts/app.js"
             (core/link-to-asset "javascripts/app.js" opts)))
      (is (= "/assets/images/Elsa.jpg"
             (core/link-to-asset "images/Elsa.jpg" opts)))
      (is (= "/assets/javascripts/manifest.js.stefon"
             (core/link-to-asset "javascripts/manifest.js.stefon" opts))))))

(deftest test-asset-builder
  (settings/with-options {:asset-root "test/fixtures"}
    (testing "plain file paths"
      (mem/cache-reset!)
      (is (= "/assets/javascripts/app-0dbd0f18020cf56c28846c40b56b5baa.js"
             (-> "javascripts/app.js" asset/build :digested-uri)))
      (is (= "/assets/javascripts/app-0dbd0f18020cf56c28846c40b56b5baa.js"
             (-> "/assets/javascripts/app.js" mem/cache-get :digested)))
      (.delete (io/file "test/fixtures/asset-cache/assets/javascripts/app-0dbd0f18020cf56c28846c40b56b5baa.js")))

    (testing "md5'd file paths"
      (is (= "/assets/javascripts/app-0dbd0f18020cf56c28846c40b56b5baa.js"
             (-> "/assets/javascripts/app.js" mem/cache-get :digested)))
      (is (= "/assets/javascripts/app-0dbd0f18020cf56c28846c40b56b5baa.js"
             (-> "/assets/javascripts/app.js" mem/cache-get :digested)))
      (.delete (io/file "test/fixtures/asset-cache/assets/javascripts/app-0dbd0f18020cf56c28846c40b56b5baa.js")))

    (testing "binary files"
      (is (= "/assets/images/stefon-102c15cd1a2dfbe24b8a5f12f2671fc8.jpeg"
             (-> "images/stefon.jpeg" mem/cache-get :digested)))
      (.delete (io/file "test/fixtures/asset-cache/assets/images/stefon-102c15cd1a2dfbe24b8a5f12f2671fc8.jpeg")))))

(deftest test-asset-pipeline
  (let [app (fn [req] (:uri req))
        pipeline (fn [opts] (core/asset-pipeline app opts))
        mime-req (fn [opts uri] ((((pipeline opts) (request :get uri)) :headers) "Content-Type"))]
    (testing "development mode"
      (let [opts {:cache-mode :development
                  :asset-roots ["test/fixtures" "test/fixtures/more_assets"]}]
        (testing "mime types"
          (mem/cache-reset!)
          (is (= "text/javascript" (mime-req opts "/assets/javascripts/app.js")))
          (is (= "image/jpeg"      (mime-req opts "/assets/images/stefon.jpeg")))
          (is (= "text/css"        (mime-req opts "/assets/stylesheets/main.css")))
          (is (= "text/css"        (mime-req opts "/assets/stylesheets/basic.less")))
          (is (= "text/javascript" (mime-req opts "/assets/javascripts/manifest.js.stefon")))
          )))
    (testing "production mode"
      (let [opts {:cache-mode :production
                  :asset-roots ["test/fixtures" "test/fixtures/more_assets"]}]
        (testing "mime types"
          (mem/cache-reset!)
          (is (= "text/javascript" (mime-req opts "/assets/javascripts/app.js")))
          (is (= "image/jpeg"      (mime-req opts "/assets/images/stefon.jpeg")))
          (is (= "text/css"        (mime-req opts "/assets/stylesheets/main.css")))
          (is (= "text/css"        (mime-req opts "/assets/stylesheets/basic.less")))
          (is (= "text/javascript" (mime-req opts "/assets/javascripts/manifest.js.stefon")))
          )))))
