(ns stefon.test.end-to-end
  (:require [stefon.core :as core]
            [stefon.settings :as settings]
            [stefon.path :as path]
            [stefon.cache.memory :as mem]
            [stefon.precompile :as precompile]
            [stefon.util :refer (inspect)])
  (:use clojure.test
        ring.mock.request))

(defn asset [undigested {:as opts :keys [cache-mode]}]
  (let [app (fn [req] (throw (Exception. "should never be reached")))
        digested (if (= cache-mode :production)
                   (-> opts precompile/precompile first)
                   (core/link-to-asset undigested opts))
        pipeline (core/asset-pipeline app opts)]
    (pipeline (request :get digested))))

(deftest wrap-cache-works-with-wrap-file-info
  (let [dev-asset (asset "test.js" {:asset-roots ["test/fixtures/middleware/resources/assets"]
                                    :cache-mode :development})

        prod-asset (asset "test.js "{:asset-roots ["test/fixtures/middleware/resources/assets"]
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