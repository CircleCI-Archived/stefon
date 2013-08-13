(ns stefon.asset.reference
  (:require [stefon.asset :as asset]
            [stefon.digest :as digest]
            [clojure.string :as str]
            [ring.util.mime-type :as mime]
            [stefon.util :refer (dump)])
  (:import org.apache.commons.codec.binary.Base64))

(defn process [line fn-name callback]
  (let [p1 (re-pattern (str "(.*)\\(" fn-name " '(.*?)'\\)(.*)"))
        p2 (re-pattern (str "(.*)\\(" fn-name " \"(.*?)\"\\)(.*)"))
        match (or (re-matches p1 line) (re-matches p2 line))]

    (if match
      (let [[_ start adrf end] match
            [d c] (asset/build adrf)]

        ;; recurse to handle multiple entries per line
        (process (str start (callback d c) end) fn-name callback))
      line)))

(defn base64 [data]
  (Base64/encodeBase64String (.getBytes data "UTF-8")))

(defn data-uri [d c]
  (str "data:" (mime/ext-mime-type d) ";base64," (base64 d)))

(defn process-all [line]
  (-> line
      (process "asset-path" (fn [d c] d))
      (process "asset-uri" (fn [d c] (str "url(\"" d "\")")))
      (process "data-uri" data-uri)))

(defn compile [root adrf content]
  (->> content
       digest/->str
       (#(str/split % #"\n"))
       (map process-all)
       (str/join "\n")))

(asset/register "ref" compile)