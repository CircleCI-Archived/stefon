(ns stefon.test.manifest
  (:require [stefon.core :as core]
            [stefon.manifest :as manifest]
            [stefon.test.helpers :as h]
            [stefon.util :refer (dump)]
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