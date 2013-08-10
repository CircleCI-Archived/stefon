(ns stefon.jsengine
  (:require [stefon.settings :as settings]
            [stefon.pools :as pools]
            [stefon.v8 :as v8]))


;; TODO: take an asset to avoid slurping here
(defn run-compiler [pool preloads fn-name file]
  (try
    (let [input (slurp file)
          absolute (.getAbsolutePath file)
          filename (.getCanonicalPath file)
          args [input absolute filename]]
      (v8/with-scope pool preloads
        (v8/call fn-name args)))
    (catch Exception e
      (let [ste (StackTraceElement. "jsengine"
                                    "compileHamlCoffee" (.getPath file) -1)
            st (.getStackTrace e)
            new-st (into [ste ] st)
            new-st-array (into-array StackTraceElement new-st)]
        (.setStackTrace e new-st-array)
        (throw e)))))