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
        digested (dump (if (= (dump mode) :production)
                         (-> opts precompile/precompile first)
                         (core/link-to-asset (dump undigested) opts)))
        pipeline (core/asset-pipeline app opts)]
    (pipeline (request/request :get digested))))


(defn test-expected [root file expected-file expecteds]
  (settings/with-options {:asset-roots [root]}
    (let [[result-file content] (-> file asset/compile dump)
          content (digest/->str content)]
      (doseq [expected expecteds]
        (is (.contains (dump content) expected)))
      (is (= expected-file result-file)))))


(defn test-syntax [root file expecteds]
  (settings/with-options {:asset-roots [root]}
    (try
      (asset/compile file)
      (is false)
      (catch Exception e
        (doseq [expected expecteds]
          (is (has-text? (.toString e) expected)))))))