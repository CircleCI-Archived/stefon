(ns dieter.precompile
  (:require [clojure.java.io :as io]
            [dieter.path :as path]
            [dieter.asset :as asset]
            [dieter.digest :as digest]
            [dieter.settings :as settings]))

(defn relative-path [root file]
  (let [absroot (.getCanonicalPath (io/file root))
        absfile (.getCanonicalPath (io/file file))
        root-length (count absroot)]
    (.substring absfile (inc root-length))))

(defn build-asset [& args]
  (apply (ns-resolve 'dieter.core 'build-asset) args))

(defn delete-dir [directory]
  (->> directory
       io/file
       file-seq
       flatten
       (remove #(= directory (.getPath %1)))
       reverse
       (map #(.delete %1))
       dorun))

(defn precompile [options]
  (settings/with-options options
    (-> (settings/precompile-root) delete-dir)
    (if (settings/precompiles)

      ;; just the listed files
      ;; TODO add dirs
      (doseq [filename (settings/precompiles)]
        (build-asset filename))

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
                          (build-asset))
                     (print ".")
                     (catch Exception e
                       (println "Not built" filename)))))
         dorun)))))