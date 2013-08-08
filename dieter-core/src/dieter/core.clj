(ns dieter.core
  (:require [dieter.settings :as settings]
            [dieter.asset :as asset]
            [dieter.path :as path]
            [dieter.digest :as digest]
            [dieter.cache.memory :as mem]
            [dieter.util]
            [dieter.precompile :as precompile]
            [dieter.asset.coffeescript]
            [dieter.asset.css]
            [dieter.asset.hamlcoffee]
            [dieter.asset.javascript]
            [dieter.asset.less]
            [dieter.asset.manifest]
            [dieter.asset.static]
            [ring.util.response :as response]
            [ring.middleware.file      :refer (wrap-file)]
            [ring.middleware.file-info :refer (wrap-file-info)]
            [dieter.middleware.expires :refer (wrap-file-expires-never wrap-expires-never)]
            [dieter.middleware.mime    :refer (wrap-dieter-mime-types)]))


(defn find-or-build-asset [adrf]
  (if-let [asset (mem/cache-get adrf)]
    asset
    (when-let [asset (asset/build adrf)]
      (mem/cache-set! asset)
      asset)))

(defn wrap-cache [app]
  (fn [req]
    (if-let [asset (mem/cache-get (:uri req))]
      (response/response :body (:content asset))
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
  "Construct the Dieter asset pipeline depending on the :cache-mode option, eventually
   either loading the data from the cache directory, rendering a new resource and
   returning that, or passing on the request to the previously existing request
   handlers in the pipeline."
  [app & [options]]
  (settings/with-options options
    (when (settings/production?)
      (-> app
          ;; server directly from memory
          (wrap-file (settings/precompile-root))
          (wrap-file-expires-never (settings/precompile-root))
          (wrap-file-info known-mime-types)
          (wrap-dieter-mime-types))
      (-> app
          (wrap-cache)
          (wrap-expires-never)
          (wrap-file-info known-mime-types)
          (wrap-dieter-mime-types)))))

(defn precompile [options] ;; lein dieter-precompile uses this name
  (precompile/precompile options))
