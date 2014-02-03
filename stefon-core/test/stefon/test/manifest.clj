(ns stefon.test.manifest
  (:require [stefon.core :as core]
            [stefon.manifest :as manifest]
            [stefon.test.helpers :as h]
            [stefon.util :refer (dump temp-dir)]
            [clojure.java.io :as io])
  (:use clojure.test))

(defn d
  "digested"
  [s]
  (str "/assets/" s "-01234567890123456789012345678901"))

(deftest round-tripping-works
  (manifest/clear!)

  ;; check basic functionality
  (manifest/set! "a" (d  "b"))
  (manifest/set! "c" (d "d"))
  (manifest/set! "c" (d "e"))
  (is (= (manifest/fetch "c") (d "e")))
  (is (= (manifest/fetch "a") (d "b")))

  (manifest/save!)
  (is (not (= (manifest/save-string) "{\n}")))
  (manifest/clear!)
  (is (= (dump (manifest/save-string)) "{\n}"))

  ;; verify the loaded values overwrite old values
  (manifest/set! "x" (d "y"))
  (manifest/load!)
  (is (not (= (manifest/save-string) "{\n}")))
  (is (= (manifest/fetch "c") (d "e")))
  (is (= (manifest/fetch "a") (d "b")))
  (is (= (manifest/fetch "x") nil)))

(deftest save!-creates-parent-dir-when-necessary
  (manifest/set! "a" (d  "b"))
  (let [tmp-dir (temp-dir "stefon-save")
        manifest-file (io/file tmp-dir "a" "deep" "path" "manifest.json")
        parent-file (.getParentFile manifest-file)]
    (testing "with a deep path"
      (is (not (.exists parent-file)))
      (is (not (.exists manifest-file)))
      (with-redefs [stefon.settings/manifest-file (fn [] (.getPath manifest-file))]
        (manifest/save!))
      (is (.exists parent-file))
      (is (.exists manifest-file))))
  (manifest/clear!))
