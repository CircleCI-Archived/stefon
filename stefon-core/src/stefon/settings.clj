(ns stefon.settings
  (:require [clojure.java.io :as io]
            [stefon.util :refer (inspect)]))

(defonce ^:dynamic *settings*
  {:asset-roots ["resources/assets"] ; returns first one it finds
   :precompile-root "public/assets"
;; TODO: remind me how to do polymorphism!
;;   :cache stefon.cache.memory
;;   :cache (stefon.cache.disk/cache "public/assets")
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
  (let [result  (:asset-roots (inspect *settings*))]
    (doseq [root result]
      (when-not (inspect (re-matches #".*assets.*" (inspect root)))
        (throw (Exception. "Root must contain 'assets'"))))
    result))

(defn production? []
  (-> *settings* :cache-mode (= :development) not))