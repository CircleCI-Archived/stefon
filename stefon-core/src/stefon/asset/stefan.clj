(ns stefon.asset.stefon
  (:require [clojure.java.io :as io]
            [clojure.string :as s]
            [stefon.asset :as asset])
  (:use [stefon.util :only [slurp-into string-builder]])
  (:import [java.io FileReader PushbackReader FileNotFoundException]))

(defn load-stefon
  "a manifest file must be a valid clojure data structure,
namely a vector or list of file names or directory paths."
  [file]
  (let [stream (PushbackReader. (FileReader. file))]
    (read stream)))

(defn stefon-files
  "return a sequence of files specified by the given stefon."
  [stefon-file]
  (->> (load-stefon stefon-file)
       (map (fn [filename]
              (let [base-dir (.getParent (io/file stefon-file))
                    file (io/file base-dir filename)]
                (when-not (.exists file)
                  (throw (FileNotFoundException. (str "Could not find " filename " from " stefon-file))))
                (->> file
                     file-seq
                     (sort-by #(.getCanonicalPath %))))))
       flatten
       (remove #(or (re-matches #".*\.swp$" (.getCanonicalPath %)) ; vim swap files
                    (re-matches #".*/\.#[^\/]+$" (.getCanonicalPath %)) ; emacs swap files
                    (re-matches #".*/\.DS_Store$" (.getCanonicalPath %)) ; OSX
                    (.isDirectory %)))))

(defn compile-stefon [file content]
  (let [builder (string-builder)]
    (doseq [sf (stefon-files file)]
      (->> sf
           asset/compile
           first ; content
           (.append builder)))
    (.toString builder)))

(asset/register "stefon" compile-stefon)
