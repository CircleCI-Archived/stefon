(ns stefon.asset.reference
  (:require [stefon.asset :as asset]
            [stefon.digest :as digest]
            [clojure.string :as str]
            [ring.util.mime-type :as mime]
            [stefon.util :refer (dump)])
  (:import org.apache.commons.codec.binary.Base64))

(declare process-all)

(defn process [line root fn-name build-fn callback-fn]
  (let [p1 (re-pattern (str "(.*)\\(" fn-name " '(.*?)'\\)(.*)"))
        p2 (re-pattern (str "(.*)\\(" fn-name " \"(.*?)\"\\)(.*)"))
        match (or (re-matches p1 line) (re-matches p2 line))]

    (if match
      (let [[_ start adrf end] match
            [d c] (build-fn root adrf)]

        ;; recurse to handle multiple entries per line
        (process-all (str start (callback-fn d c) end) root))
      line)))

(defn base64 [data]
  (Base64/encodeBase64String (.getBytes data "UTF-8")))

(defn data-uri [d c]
  (str "data:" (mime/ext-mime-type d) ";base64," (base64 d)))

(defn process-all [line root]
  (-> line
      (process root "asset-path" asset/compile-and-save (fn [d c] d))
      (process root "asset-uri" asset/compile-and-save (fn [d c] (str "url(\"" d "\")")))
      (process root "data-uri" asset/compile data-uri)))

(defn compile [root adrf content]
  (->> content
       digest/->str
       (#(str/split % #"\n"))
       (map #(process-all % root))
       (str/join "\n")))

(asset/register "ref" compile)