(ns stefon.jsengine
  (:require [clj-time.core :as time]
            [clj-time.coerce :as time-coerce]
            cheshire.core
            [clojure.java.io :as io]
            [stefon.settings :as settings]
            [stefon.digest :as digest]
            [stefon.pools :as pools]
            [stefon.v8 :as v8]
            [stefon.util :refer (dump)]))

(def memoized (atom {}))
(defn- memoize-file [filename f]
  "Ability to cache precomputed files using timestamps (avoiding the term \"cache\" since it'ss already overloaded here)"
  (let [val (get @memoized filename)
        current-timestamp (-> filename io/file .lastModified time-coerce/from-long)
        saved-timestamp (:timestamp val)
        saved-content (:content val)]
    (if (and saved-content
             (time/before? current-timestamp saved-timestamp))

      ;; return already memory
      saved-content

      ;; compute new value and save it
      (let [new-content (f)]
        (dosync
         (swap! memoized assoc filename {:content new-content
                                         :timestamp (time/now)}))
        new-content))))

;; TODO: take an asset to avoid slurping here
(defn- run-compiler [pool preloads fn-name filename content options]
  (try
    (let [file (io/file filename)
          content (digest/->str content)
          absolute (.getAbsolutePath file)
          options (cheshire.core/encode options)]
      (v8/with-scope pool preloads
        (v8/call fn-name [content absolute filename options])))
    (catch Exception e
      (let [ste (StackTraceElement. "jsengine"
                                    fn-name filename -1)
            st (.getStackTrace e)
            new-st (into [ste ] st)
            new-st-array (into-array StackTraceElement new-st)]
        (.setStackTrace e new-st-array)
        (throw e)))))

(defn memoizable? [adrf]
  (->> adrf (re-find #"\.ref$") nil?))

(defn compiler [fn-name preloads & {:as args :keys [memoize options]
                                    :or {memoize true}}]
  (let [pool (pools/make-pool)]
    (fn [root adrf content]
      (let [abs (.getCanonicalPath (io/file root adrf))
            f #(run-compiler pool preloads fn-name abs content options)]
        (if (and memoize (memoizable? adrf))
          (memoize-file abs f)
          (f))))))
