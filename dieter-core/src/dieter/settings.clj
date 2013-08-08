(ns dieter.settings
  (:require [clojure.java.io :as io]))

(defonce ^:dynamic *settings*
  {:asset-roots ["resources/assets"] ; returns first one it finds
   :precompile-root "public/assets"
;; TODO: remind me how to do polymorphism!
;;   :cache dieter.cache.memory
;;   :cache (dieter.cache.disk/cache "public/assets")
   :cache-mode :development
   :precompiles [#".*"]})

(defmacro with-options [options & body]
  `(binding [*settings* (merge *settings* ~options)]
     (do ~@body)))

(defn precompile-root []
  (:precompile-root *settings*))

(defn precompiles []
  (:precompiles *settings*))

(defn asset-roots []
  (:asset-roots *settings*))

(defn production? []
  (-> *settings* :cache-mode (= :development) not))