(ns stefon.jsengine
  (:require [stefon.settings :as settings]
            [clj-time.core :as time]
            [clj-time.coerce :as time-coerce]
            [stefon.pools :as pools]
            [stefon.v8 :as v8]))

(def memoized (atom {}))
(defn memoize-file- [file f]
  "Ability to cache precomputed files using timestamps (avoiding the term \"cache\" since it'ss already overloaded here)"
  (let [filename (.getCanonicalPath file)
        val (get @memoized filename)
        current-timestamp (-> file .lastModified time-coerce/from-long)
        saved-timestamp (:timestamp val)
        saved-content (:content val)]
    (if (and saved-content
             (time/before? current-timestamp saved-timestamp))

      ;; return already memory
      saved-content

      ;; compute new value and save it
      (let [new-content (f file)]
        (dosync
         (swap! memoized assoc filename {:content new-content
                                         :timestamp (time/now)}))
        new-content))))

;; TODO: take an asset to avoid slurping here
(defn run-compiler- [pool preloads fn-name content file]
  (try
    (let [absolute (.getAbsolutePath file)
          filename (.getCanonicalPath file)]
      (v8/with-scope pool preloads
        (v8/call fn-name [content absolute filename])))
    (catch Exception e
      (let [ste (StackTraceElement. "jsengine"
                                    "compileHamlCoffee" (.getPath file) -1)
            st (.getStackTrace e)
            new-st (into [ste ] st)
            new-st-array (into-array StackTraceElement new-st)]
        (.setStackTrace e new-st-array)
        (throw e)))))

(defn compiler [fn-name preloads]
  (let [pool (pools/make-pool)]
    (fn [file content]
      (memoize-file- file)
      (run-compiler- pool preloads fn-name file))))
