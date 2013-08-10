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

(defn build-asset [& args]
  (apply (ns-resolve 'stefon.asset 'build) args))

(defn write-asset [asset]
  (let [f (->> asset
               :digested
               path/uri->adrf
               (io/file (settings/precompile-asset-root)))]
    (spit f (:content asset))))

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
  (->> (settings/precompile-asset-root)
       io/file
       file-seq
       flatten
       (remove #(.isDirectory %))
       (map (fn [filename]
              (let [digested (->> filename
                                  (relative-path (settings/precompile-root))
                                  (str "/"))
                    undigested (path/path->undigested digested)]
                (mem/cache-set! {:undigested undigested
                                 :digested digested}))))
       dorun))


(defn precompile [options]
  (settings/with-options options
    (-> (settings/precompile-asset-root) delete-dir)
    (-> (settings/precompile-asset-root) io/make-parents)
    (if (settings/precompiles)

      ;; just the listed files
      ;; TODO add dirs
      (doseq [filename (settings/precompiles)]
        (->> filename
             build-asset
             write-asset))

      ;; all files
      (doseq [asset-root (settings/asset-roots)]
        (->>
         (io/file asset-root "assets")
         file-seq
         flatten
         (remove #(.isDirectory %))
         (map (fn [filename]
                (try (->> filename
                          (relative-path asset-root)
                          (str "./")
                          build-asset
                          write-asset)
                     (print ".")
                     (catch Exception e
                       (println "Not built" filename)))))
         dorun)))))