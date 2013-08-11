(ns stefon.precompile
  (:require [clojure.java.io :as io]
            [stefon.path :as path]
            [stefon.asset :as asset]
            [stefon.cache.memory :as mem]
            [stefon.digest :as digest]
            [stefon.util :refer (inspect)]
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
  "Load any assets already in the cache directory"
  []
  (->> (settings/serving-asset-root)
       io/file
       file-seq
       flatten
       (remove #(.isDirectory %))
       (map (fn [filename]
              (let [digested (->> filename
                                  (relative-path (settings/serving-root))
                                  (str "/"))
                    undigested (path/path->undigested digested)]
                (mem/cache-set! {:undigested undigested
                                 :digested digested}))))
       dorun))


(defn precompile [options]
  (settings/with-options options
    (delete-dir (settings/serving-asset-root))
    (-> (settings/serving-asset-root) io/file .mkdirs)

    (doall
     (for [filename (settings/precompiles)]
       (->> filename
            asset/build-asset
            asset/write-asset)))))