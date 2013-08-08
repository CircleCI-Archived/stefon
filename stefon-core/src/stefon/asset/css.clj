(ns stefon.asset.css
  (:require [stefon.asset :as asset]
            [stefon.settings :as settings])
  (:use [stefon.util :only [slurp-into string-builder]])
  (:require [clojure.string :as s]))

(defn compress-css [text]
  (-> text
      (s/replace "\n" "")
      (s/replace #"\s+" " ")
      (s/replace #"^\s" "")))

(defrecord Css [file content]
  stefon.asset.Asset
  (read-asset [this]
    (assoc this :content
           (slurp-into
            (string-builder "/* Source: " (:file this) " */\n")
            (:file this))))

  stefon.asset.Compressor
  (compress [this]
    (compress-css (:content this))))

(asset/register "css" map->Css)