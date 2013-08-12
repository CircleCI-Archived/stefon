(ns stefon.asset.stefon
  (:require [clojure.java.io :as io]
            [clojure.string :as s]
            [stefon.asset :as asset]
            [stefon.util :refer (dump)])
  (:use [stefon.util :only [slurp-into string-builder]])
  (:import [java.io ByteArrayInputStream InputStreamReader PushbackReader FileNotFoundException]))

(defn load-stefon
  "a manifest file must be a valid clojure data structure,
namely a vector or list of file names or directory paths."
  [content]
  (-> content ByteArrayInputStream. InputStreamReader. PushbackReader. read))

(defn stefon-files
  "return a sequence of files specified by the given stefon."
  [root adrf content]
  (let [parent (.getParent (io/file root adrf))
        root-length (-> root io/file .getCanonicalPath .length)]
    (->> content
         load-stefon
         (map (fn [asset-filename]
                (-> (io/file parent asset-filename)
                    file-seq)))
         flatten
         (remove #(or (re-matches #".*\.swp$" (.getPath %)) ; vim swap files
                      (re-matches #".*/\.#[^\/]+$" (.getPath %)) ; emacs swap files
                      (re-matches #".*/\.DS_Store$" (.getPath %)) ; OSX
                      (.isDirectory %)))
         (map #(.getCanonicalPath %))
         sort
         (map #(.substring % root-length)))))

(defn compile-stefon [root adrf content]
  (let [builder (string-builder)]
    (doseq [sf (stefon-files root adrf content)]
      (let [content (asset/read-file (io/file root adrf))]
        (->> (asset/apply-pipeline root sf content)
             first ; content
             (.append builder))))
    (.toString builder)))

(asset/register "stefon" compile-stefon)
