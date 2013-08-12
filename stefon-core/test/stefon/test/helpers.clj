(ns stefon.test.helpers
  (:require [stefon.settings :as settings]
            [stefon.asset :as asset]
            [stefon.util :refer (dump)]
            [clojure.java.io :as io]
            [clojure.test :refer (is)]))

(defn contains-file? [seq filename]
  (->> seq
       (filter #(= (.getCanonicalPath %)
                   (-> filename io/file .getCanonicalPath)))
       count
       pos?))

(defn contains-file-containing? [seq substr]
  (->> seq
       (filter #(.contains (-> % .getCanonicalPath) substr))
       count
       pos?))


(defn has-text?
  "returns true if expected occurs in text exactly n times (one or more times if not specified)"
  ([text expected]
     (not= -1 (.indexOf text expected)))
  ([text expected times]
     (= times (count (re-seq (re-pattern expected) text)))))

(defn test-expected [root file expected-file expected]
  (settings/with-options {:asset-roots [root]}
    (let [[result-file content] (-> file asset/compile)
          content (if (isa? String content) content (String. content "UTF-8"))]
      (is (= expected (dump content)))
      (is (= expected-file result-file)))))


(defn test-syntax [root file expecteds]
  (settings/with-options {:asset-roots [root]}
    (try
      (asset/compile file)
      (is false)
      (catch Exception e
        (doseq [expected expecteds]
          (is (has-text? (.toString e) expected)))))))