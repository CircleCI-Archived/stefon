(ns stefon.test.precompile
  (:require [stefon.core :as core]
            [stefon.precompile :as precompile]
            [stefon.settings :as settings]
            [stefon.cache :as cache]
            [clojure.java.io :as io])
  (:use clojure.test
        ring.mock.request))

(deftest precompile-roundtrip-works
  (let [options {:engine :v8
                 :compress false
                 :log-level :quiet
                 :asset-root "test/fixtures"
                 :cache-root "test/precompile-cache"
                 :cache-mode :production
                 :precompiles ["javascripts/app.js"]}]
    (swap! cache/cached-uris (constantly {})) ; clear first
    (settings/with-options options
      (precompile/precompile options)
      (precompile/load-precompiled-assets)
      (is (= (keys @cache/cached-uris)
             (map #(str "/assets/" %)
                  (:precompiles options)))))))