(ns stefon.test.middleware
  (:require [stefon.core :as core]
            [stefon.settings :as settings]
            [stefon.precompile :as precompile]
            [stefon.util :refer (inspect)])
  (:use clojure.test
        ring.mock.request))


(deftest wrap-cache-works-with-wrap-file-info
  (let [app (fn [req] (throw (Exception. "should never be reached")))
        dev-opts {:asset-roots ["test/fixtures/middleware/resources/assets"]
                  :cache-mode :development}
        sha1-name (core/link-to-asset "test.js" dev-opts)
        dev-pipeline (core/asset-pipeline app dev-opts)
        dev-asset (dev-pipeline (request :get sha1-name))

        prod-opts {:asset-roots ["test/fixtures/middleware/resources/assets"]
                   :precompiles ["test.js"]
                   :precompile-root "test/fixtures/middleware/public"
                   :cache-mode :production}
        _ (precompile/precompile prod-opts)
        prod-pipeline (core/asset-pipeline app prod-opts)
        prod-asset (prod-pipeline (request :get sha1-name))]

    (is (= sha1-name "/assets/test-f20372f6903e89fbd11fb2d2684922d0.js"))
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
    (is (-> prod-asset :headers (get "Expires")))

    ;; TODO: body file is wrong and probably wont server the asset - replace
    ;; with string itself - use read
    (is (= (inspect dev-asset) (inspect prod-asset)))))