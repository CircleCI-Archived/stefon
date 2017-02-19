(ns stefon.settings
  (:require [clojure.string :as s]
            [stefon.util :refer [temp-dir]])
  (:import [java.io File]))

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

(defonce tmp-dir-path-delay
  (delay (if-let [^File tmp-dir (temp-dir "stefon")]
           (.getAbsolutePath tmp-dir)
           (throw (Exception. "Could not create tmp dir for serving-root.")))))

(defn serving-root
  "Determine what the serving root of the application should be. In production
   this is the serving root key of *settings*. In development it creates a
   tempory directory and uses it."
  []
  (if (production?)
    (:serving-root *settings*)
    ; It is possible, though unlikely, that creating a tmp dir will fail
    @tmp-dir-path-delay))

(defn uri-root
  "Determine what the uri-root should be based on the uri-root option in
  settings. The uri-root option should not contain any leading or trailing
  slashes."
  [& [prefix suffix]]
  (when (production?)
    (let [uri-root-str (:uri-root *settings*)]
      (when-not (empty? uri-root-str)
        (str prefix uri-root-str suffix)))))

(defn serving-asset-root []
  (str (serving-root) "/assets"))

(defn manifest-file []
  (:manifest-file *settings*))

(defn precompiles []
  (:precompiles *settings*))

(defn hamlcoffee-options []
  (:hamlcoffee-options *settings*))

(defn asset-roots []
  (let [result  (:asset-roots *settings*)]
    (doseq [root result]
      (when-not (re-matches #".*assets.*" root)
        (throw (Exception. "Root must contain 'assets'"))))
    result))
