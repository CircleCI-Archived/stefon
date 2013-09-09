(ns stefon.settings
  (:require [clojure.java.io :as io]
            [clojure.string :as s]
            [stefon.util :refer (dump)]))

(defonce ^:dynamic *settings*
  {:asset-roots ["resources/assets"] ; returns first one it finds
;   :serving-root "public" or "/tmp/stefon"
   :mode :development
   :manifest-file "resources/manifest.json" ;; you dont necesarily want this in the assets dir
   :precompiles [#".*"]}) ;; TODO: make this work

(def ^:private validations
  (let [sequence-of-strings? (fn [sequence-of-strings]
                               (and (sequential? sequence-of-strings)
                                    (every? string? sequence-of-strings)))
        in-mode (fn [mode predicate]
                  (fn [settings]
                    (or (not= mode (:mode settings))
                        (predicate settings))))]
    [[map? "options must be a map"]
     [(in-mode :production (comp string? :serving-root))
      "when in production serving-root must be a string representing a path"]
     [(comp string? :manifest-file)
      "manifest-file must be a string representing a path"]
     [(comp (partial contains? #{:production :development}) :mode)
      "mode must be either"]
     [(comp sequence-of-strings? :asset-roots)
      "asset-roots must be a sequence of strings representing paths"]
     [(in-mode :production (comp sequence-of-strings? :precompiles))
      "when in production precompiles must be an sequence of strings "
      "representing paths"]]))

(defn- attempt-predicate
  "Take a vector containing a predicate and an associated error message
   returning the message if the predicate is false or if it fails."
  [[predicate & message]]
  (when-not (try (predicate *settings*) (catch Exception _ false))
    (apply str message)))

(defn validate
  "Run validations on *settings* and throw an error when issues are found."
  []
  (let [errors (->> validations
                    (map attempt-predicate)
                    (remove nil?))]
    (when-not (empty? errors)
      (throw (Exception. (str "Options (" *settings* ") are invalid: "
                              (s/join ", " errors) \.))))))

(defmacro with-options [options & body]
  `(binding [*settings* (merge *settings* ~options)]
     (validate)
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
