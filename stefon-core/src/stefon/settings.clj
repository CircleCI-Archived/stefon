(ns stefon.settings
  (:require [clojure.java.io :as io]
            [stefon.util :refer (inspect)]))

(defonce ^:dynamic *settings*
  {:asset-roots ["resources/assets"] ; returns first one it finds
;   :serving-root "public" or "/tmp/stefon"
   :cache-mode :development
   :precompiles [#".*"]}) ;; TODO: make this work

(defmacro with-options [options & body]
  `(binding [*settings* (merge *settings* ~options)]
     (do ~@body)))

(defn production? []
  (-> *settings* :cache-mode (= :development) not))

(defn serving-root []
  (cond
   (:serving-root *settings*) (:serving-root *settings*)
   (production?) "public"
   :else "/tmp/stefon"))

(defn serving-asset-root []
  (str (serving-root) "/assets"))

(defn precompiles []
  (:precompiles *settings*))

(defn asset-roots []
  (let [result  (:asset-roots *settings*)]
    (doseq [root result]
      (when-not (re-matches #".*assets.*" root)
        (throw (Exception. "Root must contain 'assets'"))))
    result))
