(ns stefon.cache.disk
  (:require [clojure.string :as cstr]
            [stefon.path :as path]
            [clojure.java.io :as io]))

(derive (class (make-array Byte/TYPE 0)) ::bytes)
(derive java.lang.String ::string-like)
(derive java.lang.StringBuilder ::string-like)

(defmulti write-file (fn [c f] (class c)))
(defmethod write-file ::string-like [content file]
  (spit file content))

(defmethod write-file ::bytes [content file]
  (with-open [out (java.io.FileOutputStream. file)]
    (.write out content)))
