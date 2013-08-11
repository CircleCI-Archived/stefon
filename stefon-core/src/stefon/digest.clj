(ns stefon.digest
  (:require [stefon.util :refer (dump)])
  (:import [java.security MessageDigest]))

(derive (class (make-array Byte/TYPE 0)) ::bytes)
(derive java.lang.String ::string-like)
(derive java.lang.StringBuilder ::string-like)

(defn md5 [bytes]
  (-> "MD5"
      MessageDigest/getInstance
      (.digest bytes)))

(defmulti digest class)
(defmethod digest ::bytes [bytes]
  (->> bytes
       md5
       (BigInteger. 1)
       (format "%032x")))

(defmethod digest ::string-like [string]
  (-> string
      str
      (.getBytes "UTF-8")
      digest))
