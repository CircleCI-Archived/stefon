(ns stefon.test.core
  (:require [stefon.core :as core]
            [stefon.settings :as settings]
            [stefon.path :as path]
            [stefon.manifest :as manifest]
            [stefon.asset :as asset]
            [stefon.test.helpers :as h]
            [stefon.util :refer (dump)]
            [ring.util.response :as response]
            [clojure.java.io :as io])
  (:use clojure.test
        ring.mock.request))

(deftest test-link-to-asset
  (testing "development mode"
    (let [opts {:mode :development :asset-roots ["test/fixtures/assets"]}]
      (is (thrown? java.io.FileNotFoundException (core/link-to-asset "javascripts/dontfindme.js" opts)))
      (is (= "/assets/javascripts/app-895a9f207aea908554d644c9bd160d5f.js" (core/link-to-asset "javascripts/app.js" opts)))
      (is (= "/assets/javascripts/stefon-e4e2f0c89cbafc0ee96ea6e355f8ec4f.js" (core/link-to-asset "javascripts/stefon.js.stefon" opts)))

      (testing "file previously generated"
        (manifest/clear!)
        (manifest/set! "javascripts/app.js"
                       "/assets/javascripts/app-12345678901234567890af1234567890.js")

        (is (= "/assets/javascripts/app-895a9f207aea908554d644c9bd160d5f.js"
               (core/link-to-asset "javascripts/app.js" opts))))))

  (testing "production mode"
    (let [opts {:mode :production
                :asset-roots ["test/fixtures/assets"]}]

      (testing "no previous file generated"
        (is (path/digest-path? (core/link-to-asset "javascripts/app.js" opts))))

      (testing "file previously generated"
        (manifest/clear!)
        (manifest/set! "javascripts/app.js"
                       "/assets/javascripts/app-12345678901234567890af1234567890.js")

        (is (= "/assets/javascripts/app-12345678901234567890af1234567890.js"
               (core/link-to-asset "javascripts/app.js" opts)))))))


(deftest test-core-link-to-asset-in-secondary-dir
  (testing "development mode"
    (let [opts {:mode :development
                :asset-roots ["test/fixtures/assets" "test/fixtures/more_assets/assets"]}]
      (is (thrown? Exception (core/link-to-asset "javascripts/dontfindme.js" opts)))
      (is (= "/assets/javascripts/app-895a9f207aea908554d644c9bd160d5f.js"
             (core/link-to-asset "javascripts/app.js" opts)))
      (is (= "/assets/images/Elsa-445866c6257dfd01886f4d7968181f14.jpg"
             (core/link-to-asset "images/Elsa.jpg" opts)))
      (is (= "/assets/javascripts/stefon-e4e2f0c89cbafc0ee96ea6e355f8ec4f.js"
             (core/link-to-asset "javascripts/stefon.js.stefon" opts))))))

(deftest test-asset-builder
  (settings/with-options {:asset-roots ["test/fixtures/assets"]}
    (testing "sha'd file paths"
      (manifest/clear!)
      (is (= "/assets/javascripts/app-895a9f207aea908554d644c9bd160d5f.js"
             (-> "javascripts/app.js" asset/find-and-compile-and-save first)))
      (.delete (io/file "test/fixtures/asset-cache/assets/javascripts/app-895a9f207aea908554d644c9bd160d5f.js")))

    (testing "binary files"
      (is (= "/assets/images/stefon-102c15cd1a2dfbe24b8a5f12f2671fc8.jpeg"
             (-> "images/stefon.jpeg" asset/find-and-compile-and-save first)))
      (.delete (io/file "test/fixtures/asset-cache/assets/images/stefon-102c15cd1a2dfbe24b8a5f12f2671fc8.jpeg")))))

(deftest caching-and-simple-links-work-in-development
  (let [app (fn [req] {:status 404})
        opts {:mode :development
              :asset-roots ["test/fixtures/assets" "test/fixtures/more_assets/assets"]}
        pipeline (core/asset-pipeline app opts)
        resp (fn [adrf] (-> (request :get (path/adrf->uri adrf))
                              pipeline))
        adrf "javascripts/app.js"]
    (manifest/clear!)
    (testing "checking no files"
      (is (= {:status 404} (resp "filenotfound.js"))))
    (testing "simple links redirect without caching"
      (let [resp (resp adrf)]
        (is (= 302 (:status resp)))
        (is (= (core/link-to-asset adrf opts) (-> resp :headers (get "Location"))))
        (is (not (-> resp :headers (get "Expires"))))))
    (testing "digest links work and cached"
      (let [resp (-> (request :get (core/link-to-asset adrf opts)) pipeline)]
        (is (= 200 (:status resp)))
        (is (-> resp :body))
        (is (-> resp :headers (get "Expires")))))))

(deftest caching-and-simple-links-in-production
  (let [app (fn [req] {:status 404})
        opts {:mode :production
              :serving-root "/tmp/stefon"
              :asset-roots ["test/fixtures/assets" "test/fixtures/more_assets/assets"]}
        pipeline (core/asset-pipeline app opts)
        resp (fn [adrf] (-> (request :get (path/adrf->uri adrf))
                              pipeline))
        adrf "javascripts/app.js"]
    (manifest/clear!)
    (testing "checking no files"
      (is (= {:status 404} (resp "filenotfound.js"))))
    (testing "simple links don't work in production"
      (is (= {:status 404} (resp adrf))))))
