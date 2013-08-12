(ns stefon.test.end-to-end
  (:require [stefon.core :as core]
            [stefon.settings :as settings]
            [stefon.path :as path]
            [stefon.test.helpers :as h]
            [stefon.precompile :as precompile]
            [stefon.util :refer (dump)])
  (:use clojure.test))

(deftest wrap-cache-works-with-wrap-file-info
  (let [dev-asset (h/asset "test.js" {:asset-roots ["test/fixtures/middleware/resources/assets"]
                                      :cache-mode :development})

        prod-asset (h/asset "test.js "{:asset-roots ["test/fixtures/middleware/resources/assets"]
                                       :precompiles ["test.js"]
                                       :serving-root "test/fixtures/middleware/public"
                                       :cache-mode :production})]


    (is dev-asset)
    (is (= (-> dev-asset :status)
           (-> prod-asset :status)
           200))

    (is (= (-> dev-asset :headers (get "Content-Length"))
           (-> prod-asset :headers (get "Content-Length"))
           "11"))

    (is (= (-> dev-asset :headers (get "Content-Type"))
           (-> prod-asset :headers (get "Content-Type"))
           "text/javascript"))

    (is (= (-> prod-asset :headers (get "Expires"))
           (-> dev-asset :headers (get "Expires"))))
    (is (-> prod-asset :headers (get "Expires")))))