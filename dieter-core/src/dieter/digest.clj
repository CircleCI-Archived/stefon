(ns dieter.digest
  (:import [java.security MessageDigest]))

(defn md5 [bytes]
  (.digest (MessageDigest/getInstance "MD5") bytes))

(defmulti digest class)
(defmethod digest ::bytes [bytes]
  (format "%032x" (BigInteger. 1 (md5 bytes))))

(defmethod digest ::string-like [string]
  (md5 (.getBytes (str string) "UTF-8")))
