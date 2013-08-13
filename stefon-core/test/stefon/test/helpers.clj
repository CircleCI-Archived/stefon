(ns stefon.test.helpers
  (:require [stefon.settings :as settings]
            [stefon.core :as core]
            [stefon.asset :as asset]
            [stefon.digest :as digest]
            [stefon.precompile :as precompile]
            [stefon.util :refer (dump)]
            [ring.mock.request :as request]
            [clojure.java.io :as io]
            [clojure.test :refer (is)]))

(defn contains-file? [seq filename]
  (->> seq
       (filter #(= % filename))
       count
       pos?))

(defn contains-file-containing? [seq substr]
  (->> seq
       (filter #(.contains % substr))
       count
       pos?))


(defn has-text?
  "returns true if expected occurs in text exactly n times (one or more times if not specified)"
  ([text expected]
     (not= -1 (.indexOf text expected)))
  ([text expected times]
     (= times (count (re-seq (re-pattern expected) text)))))


(defn asset [undigested {:as opts :keys [mode]}]
  (let [app (fn [req] (throw (Exception. "should never be reached")))
        digested (if (= mode :production)
                   (-> opts precompile/precompile first)
                   (core/link-to-asset undigested opts))
        pipeline (core/asset-pipeline app opts)]
    (pipeline (request/request :get digested))))


(defn test-expected [root file expected-file expecteds & {:keys [debug]}]
  (settings/with-options {:asset-roots [root]}
    (let [[result-file content] (-> file asset/find-and-compile-and-save)
          content (digest/->str content)]
      (doseq [expected expecteds]
        (when debug
          (dump content)
          (dump expected))
        (is (.contains content expected)))
      (is (= expected-file result-file)))))


(defn test-syntax [root file expecteds]
  (settings/with-options {:asset-roots [root]}
    (try
      (asset/find-and-compile-and-save file)
      (is false)
      (catch Exception e
        (doseq [expected expecteds]
          (is (has-text? (.toString e) expected)))))))