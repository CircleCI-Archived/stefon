(ns stefon.util
  (:require [clojure.java.io :as io]
            [clojure.pprint :as pprint]))

(defn slurp-into [#^StringBuilder builder f]
  "read file contents into an existing string builder"
  (with-open [#^java.io.Reader r (io/reader f)]
    (loop [c (.read r)]
      (if (neg? c)
        builder
        (do
          (.append builder (char c))
          (recur (.read r)))))))

(defn string-builder [& args]
  (let [builder (StringBuilder.)]
    (doseq [arg args]
      (.append builder arg))
    builder))

(defmacro dump
  "prints the expression '<name> is <value>', and returns the value"
  [value]
  `(let [value# (quote ~value)
         result# ~value]
     (println value# "is" (with-out-str (pprint/pprint result#)))
     result#))

(defn wrap-dump
  "prints the expression '<name> is <value>', and returns the value"
  [handler]
  (fn [req]
    (dump (handler (dump req)))))


;; The following code is borrowed from me.raynes/fs with a few minor edits.
;;
;; Copyright (C) 2010-2013 Miki Tebeka, Anthony Grimes
;; Distributed under the Eclipse Public License, the same as Clojure.
(defn- mkdir
  "Create a directory."
  [path]
  (.mkdir (io/file path)))

(defn- tmpdir
  "The temporary file directory looked up via the java.io.tmpdir
   system property. Does not create a temporary directory."
  []
  (System/getProperty "java.io.tmpdir"))

(defn- temp-name
  "Create a temporary file name like what is created for temp-file
   and temp-dir."
  ([prefix] (temp-name prefix ""))
  ([prefix suffix]
     (format "%s%s-%s%s" prefix (System/currentTimeMillis)
             (long (rand 0x100000000)) suffix)))

(defn- temp-create
  "Create a temporary file or dir, trying n times before giving up."
  ([prefix suffix tries f]
     (loop [tries (range tries)]
       (let [tmp (io/file (tmpdir) (temp-name prefix suffix))]
         (if (and (seq tries) (f tmp))
           tmp
           (recur (rest tries)))))))

(defn temp-dir
  "Create a temporary directory. Returns nil if dir could not be created
   even after n tries (default 10)."
  ([prefix]              (temp-dir prefix "" 10))
  ([prefix suffix]       (temp-dir prefix suffix 10))
  ([prefix suffix tries] (temp-create prefix suffix tries mkdir)))
