(ns stefon.asset.reference
  (:refer-clojure :exclude [compile])
  (:require [stefon.asset :as asset]
            [stefon.digest :as digest]
            [clojure.java.shell :as shell]
            [clojure.string :as str]
            [ring.util.mime-type :as mime]
            [stefon.util :refer (dump)])
  (:import org.apache.commons.codec.binary.Base64))

(declare process-all)

(defn process [line root fn-name build-fn callback-fn]
  (let [p1 (re-pattern (str "(.*)\\(" fn-name " (.*?)\\)(.*)"))
        p2 (re-pattern (str "(.*)\\(" fn-name " (.*?)\\)(.*)"))
        match (or (re-matches p1 line) (re-matches p2 line))]

    (if match
      (let [[_ start args end] match
            [url width height] (str/split args #"\s+")
            url (str/replace url #"['\"]" "") ;; strip quotes
            width (when width (Integer/parseInt width))
            height (when height (Integer/parseInt height))
            [d c] (build-fn root url)]

        ;; recurse to handle multiple entries per line
        (process-all (str start (callback-fn d c width height) end) root))
      line)))

(defn base64 [data]
  (if (string? data)
    (Base64/encodeBase64String (.getBytes data "UTF-8"))
    (Base64/encodeBase64String data)))

(defn resize-image [data width height]
  (->
   (shell/sh "bash" "-c" (format "convert - -resize %sx%s -" width height)
             :in (clojure.java.io/input-stream data)
             :out-enc :bytes)
   :out))

(defn data-uri [d c & width-height]
  (let [[width height] width-height
        mime-type (mime/ext-mime-type d)]
    (if (and width height)
      (data-uri d (resize-image c width height))
      (str "data:" (mime/ext-mime-type d) ";base64," (base64 c)))))

(defn process-all [line root]
  (-> line
      (process root "asset-path" asset/compile-and-save (fn [d c & _] d))
      (process root "asset-uri" asset/compile-and-save (fn [d c & _] (str "url(\"" d "\")")))
      (process root "data-uri" asset/compile data-uri)))

(defn compile [root adrf content]
  (->> content
       digest/->str
       (#(str/split % #"\n"))
       (map #(process-all % root))
       (str/join "\n")))

(asset/register "ref" compile)
