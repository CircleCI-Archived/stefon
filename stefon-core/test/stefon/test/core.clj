(ns stefon.test-core
  (:require [stefon.core :as core]
            [stefon.settings :as settings]
            [stefon.cache.memory :as mem]
            [stefon.path :as path]
            [stefon.asset :as asset]
            [stefon.test.helpers :as h]
            [stefon.util :refer (inspect)]
            [clojure.java.io :as io])
  (:use clojure.test
        ring.mock.request))

(deftest test-link-to-asset
  (testing "development mode"
    (let [opts {:cache-mode :development :asset-roots ["test/fixtures/assets"]}]
      (is (thrown? java.io.FileNotFoundException (core/link-to-asset "javascripts/dontfindme.js" opts)))
      (is (= "/assets/javascripts/app-895a9f207aea908554d644c9bd160d5f.js" (core/link-to-asset "javascripts/app.js" opts)))
      (is (= "/assets/javascripts/manifest-e4e2f0c89cbafc0ee96ea6e355f8ec4f.js.stefon" (core/link-to-asset "javascripts/manifest.js.stefon" opts)))))

  (testing "production mode"
    (let [opts {:cache-mode :production
                :asset-roots ["test/fixtures/assets"]}]

      (testing "no previous file generated"
        (is (path/digest-path? (core/link-to-asset "javascripts/app.js" opts))))

      (testing "file previously generated"
        (mem/cache-set-empty! "/assets/javascripts/app.js"
                              "/assets/javascripts/app-12345678901234567890af1234567890.js")

        (is (= "/assets/javascripts/app-895a9f207aea908554d644c9bd160d5f.js"
               (core/link-to-asset "javascripts/app.js" opts)))))))


(deftest test-core-link-to-asset-in-secondary-dir
  (testing "development mode"
    (let [opts {:cache-mode :development
                :asset-roots ["test/fixtures/assets" "test/fixtures/more_assets/assets"]}]
      (is (thrown? Exception (core/link-to-asset "javascripts/dontfindme.js" opts)))
      (is (= "/assets/javascripts/app-895a9f207aea908554d644c9bd160d5f.js"
             (core/link-to-asset "javascripts/app.js" opts)))
      (is (= "/assets/images/Elsa-445866c6257dfd01886f4d7968181f14.jpg"
             (core/link-to-asset "images/Elsa.jpg" opts)))
      (is (= "/assets/javascripts/manifest-e4e2f0c89cbafc0ee96ea6e355f8ec4f.js.stefon"
             (core/link-to-asset "javascripts/manifest.js.stefon" opts))))))

(deftest test-asset-builder
  (settings/with-options {:asset-roots ["test/fixtures/assets"]}
    (testing "sha'd file paths"
      (mem/cache-reset!)
      (is (= "/assets/javascripts/app-895a9f207aea908554d644c9bd160d5f.js"
             (-> "javascripts/app.js" asset/build-asset :digested)))
      (.delete (io/file "test/fixtures/asset-cache/assets/javascripts/app-895a9f207aea908554d644c9bd160d5f.js")))

    (testing "binary files"
      (is (= "/assets/images/stefon-102c15cd1a2dfbe24b8a5f12f2671fc8.jpeg"
             (-> "images/stefon.jpeg" asset/build-asset :digested)))
      (.delete (io/file "test/fixtures/asset-cache/assets/images/stefon-102c15cd1a2dfbe24b8a5f12f2671fc8.jpeg")))))

(deftest test-asset-pipeline
  (let [app (fn [req] (:uri req))
        opts {:cache-mode :development
              :asset-roots ["test/fixtures/assets" "test/fixtures/more_assets/assets"]}
        pipeline (core/asset-pipeline app opts)
        mime-req (fn [uri] (-> (request :get (core/link-to-asset uri opts))
                              pipeline
                              :headers
                              (get "Content-Type")))]
    (testing "development mode"
      (testing "mime types"
        (mem/cache-reset!)
        (is (= "text/javascript" (mime-req "javascripts/app.js")))
        (is (= "image/jpeg"      (mime-req "images/stefon.jpeg")))
        (is (= "text/css"        (mime-req "stylesheets/main.css")))
        (is (= "text/css"        (mime-req "stylesheets/basic.less")))
        (is (= "text/javascript" (mime-req "javascripts/manifest.js.stefon")))))
    (testing "production mode"
      (testing "mime types"
        (mem/cache-reset!)
        (is (= "text/javascript" (mime-req "javascripts/app.js")))
        (is (= "image/jpeg"      (mime-req "images/stefon.jpeg")))
        (is (= "text/css"        (mime-req "stylesheets/main.css")))
        (is (= "text/css"        (mime-req "stylesheets/basic.less")))
        (is (= "text/javascript" (mime-req "javascripts/manifest.js.stefon")))))))
