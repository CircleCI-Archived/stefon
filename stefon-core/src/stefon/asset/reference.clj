(ns stefon.asset.reference
  (:require [stefon.asset :as asset]
            [stefon.digest :as digest]
            [clojure.string :as str]
            [stefon.util :refer (dump)]))

(defn process [line]
  (dump line)
  (if-let [match (or (re-matches #"(.*)\(asset-path '(.*?)'\)(.*)" line)
                     (re-matches #"(.*)\(asset-path \"(.*?)\"\)(.*)" line))]
    (let [[_ start adrf end] match
          digested  (asset/build adrf)]
      (dump (process (str start digested end)))); recurse to handle multiple entries per line
    (dump line)))

(defn compile [root adrf content]
  (->> content
       digest/->str
       (#(str/split % #"\n"))
       (map process)
       (str/join "\n")))

(asset/register "ref" compile)

;; "data:#{asset.content_type};base64,#{Rack::Utils.escape(base64)}"


;; possible things we might allow
;; def asset_path (path)
;; def asset_url (path)
;; def image_path (path)
;; def image_url (path)
;; def video_path (path)
;; def video_url (path)
;; def audio_path (path)
;; def audio_url (path)
;; def font_path (path)
;; def font_url (path)
;; def javascript_path (path)
;; def javascript_url (path)
;; def stylesheet_path (path)
;; def stylesheet_url (path)
