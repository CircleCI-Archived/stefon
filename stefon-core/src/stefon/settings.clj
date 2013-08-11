(ns stefon.settings
  (:require [clojure.java.io :as io]
            [stefon.util :refer (dump)]))

(defonce ^:dynamic *settings*
  {:asset-roots ["resources/assets"] ; returns first one it finds
;   :serving-root "public" or "/tmp/stefon"
   :mode :development
   :manifest-file "resources/manifest.json" ;; you dont necesarily want this in the assets dir
   :precompiles [#".*"]}) ;; TODO: make this work

(defmacro with-options [options & body]
  `(binding [*settings* (merge *settings* ~options)]
     (do ~@body)))

(defn production? []
  (-> *settings* :mode (= :development) not))

(defn serving-root []
  (cond
   (:serving-root *settings*) (:serving-root *settings*)
   (production?) "public"
   :else "/tmp/stefon"))

(defn serving-asset-root []
  (str (serving-root) "/assets"))

(defn manifest-file []
  (:manifest-file *settings*))

(defn precompiles []
  (:precompiles *settings*))

(defn asset-roots []
  (let [result  (:asset-roots *settings*)]
    (doseq [root result]
      (when-not (re-matches #".*assets.*" root)
        (throw (Exception. "Root must contain 'assets'"))))
    result))
