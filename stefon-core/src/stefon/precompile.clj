(ns stefon.precompile
  (:require [clojure.java.io :as io]
            [stefon.util :refer (dump)]
            [stefon.asset :as asset]
            [stefon.path :as path]
            [stefon.manifest :as manifest]
            [stefon.settings :as settings]))

(defn relative-path [root file]
  (let [absroot (.getCanonicalPath (io/file root))
        absfile (.getCanonicalPath (io/file file))
        root-length (count absroot)]
    (.substring absfile (inc root-length))))

(defn delete-dir [directory]
  (->> directory
       io/file
       file-seq
       flatten
       (remove #(= directory (.getPath %1)))
       reverse
       (map #(.delete %1))
       dorun))

(defn load-precompiled-assets
  "Put precompiled files into the manifest"
  []
  (manifest/load!))


(defn precompile [options]
  (settings/with-options options
    (delete-dir (settings/serving-asset-root))
    (-> (settings/serving-asset-root) io/file .mkdirs)
    (doall
     (for [filename (settings/precompiles)]
       (asset/build filename)))
    (manifest/save!)))