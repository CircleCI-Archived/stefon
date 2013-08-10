(ns stefon.core
  (:require [stefon.settings :as settings]
            [stefon.asset :as asset]
            [stefon.path :as path]
            [stefon.digest :as digest]
            [stefon.cache.memory :as mem]
            [stefon.util]
            [stefon.asset.coffeescript :as coffee]
            [stefon.asset.css :as css]
            [stefon.asset.hamlcoffee :as hamlc]
            [stefon.asset.javascript :as js]
            [stefon.asset.less :as less]
            [stefon.asset.manifest :as manifest]
            [stefon.asset.static :as static]
            [stefon.precompile :as precompile]
            [stefon.util :refer (inspect wrap-inspect)]
            [ring.util.response :as response]
            [ring.middleware.file      :refer (wrap-file)]
            [ring.middleware.file-info :refer (wrap-file-info)]
            [stefon.middleware.expires :refer (wrap-file-expires-never wrap-expires-never)]
            [stefon.middleware.mime    :refer (wrap-stefon-mime-types)]))


(defn find-or-build-asset [adrf]
  (if-let [asset (mem/cache-get adrf)]
    asset
    (when-let [asset (asset/build adrf)]
      (mem/cache-set! asset)
      asset)))

(defn wrap-cache [app]
  (fn [req]
    (if-let [asset (mem/cache-get (:uri req))]
      (response/response (mem/file-from-asset asset))
      (app req))))


;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Entry points
;;;;;;;;;;;;;;;;;;;;;;;;;

(def known-mime-types {:hbs "text/javascript"
                       "less" "text/css"
                       "hamlc" "text/javascript"
                       "coffee" "text/javascript"
                       "cs" "text/javascript"})


(defn link-to-asset [adrf & [options]]
  "path should start under assets and not contain a leading slash
ex. (link-to-asset \"javascripts/app.js\") => \"/assets/javascripts/app-12345678901234567890123456789012.js\""
  (settings/with-options options
    (-> adrf find-or-build-asset :digested)))

(defn asset-pipeline
  "Construct the Stefon asset pipeline depending on the :cache-mode option, eventually
   either loading the data from the cache directory, rendering a new resource and
   returning that, or passing on the request to the previously existing request
   handlers in the pipeline."
  [app & [options]]
  (settings/with-options options
    (if (settings/production?)
      (-> app
          ;; serve directly from disk, never from memory
          (wrap-file (settings/precompile-root))
          (wrap-file-expires-never (settings/precompile-root))
          (wrap-file-info known-mime-types)
          wrap-stefon-mime-types)
      (-> app
          wrap-cache ;; serve directly from memory
          wrap-expires-never
          (wrap-file-info known-mime-types)
          wrap-stefon-mime-types))))

(defn precompile [options] ;; lein stefon-precompile uses this name
  (precompile/precompile options))

(defn init [options]
  (settings/with-options options
    (precompile/load-precompiled-assets)))
