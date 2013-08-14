(ns stefon.path
  (:require [clojure.string :as cstr]
            [clojure.core.incubator :refer (-?>)]
            [clojure.java.io :as io]
            [stefon.settings :as settings]
            [stefon.util :refer (dump)]
            [stefon.digest :as digest]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; String-types used
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;; Stefon uses many different types of paths, and it can be confusing at times.
;;; We try to use a sort-of hungarian notation, where every path has a type
;;; included in its variable name somehow.

;;; A "path" refers to paths of any kind, including filenames, uris, adrfs, etc.

;;; An "Asset-directory-relative filename" (adrf) represents a path, relative
;;; to the asset directory. It is used as a canonical representation, and can
;;; easily by created from URIs and filenames from directory traversals. It can
;;; also easily be converted into any of these types of file. It does not
;;; include the string "/assets/".

;;; A URI represents the part of the URI that we use in stefon. If a whole URI
;;; is prototcol://hostname/path, then we use URI to represent just the "path"
;;; portion. In stefon, all URIs will start with "/assets/", as otherwise they
;;; won't be handled by stefon.

;;; A filename represents an actual file on the filesystem. We'll try and keep
;;; them as absolute strings names, because relative ones are easy to confuse
;;; with other types.

(defn asset-uri? [uri]
  (re-matches #"^/assets/.*" uri))

(defn split-digested-path [path]
  "return [match path digest extenstion], or nil"
  (if (re-find #"[^\.]\.[^\.]" path)
    (re-matches #"^(.+)-([\da-f]{32})\.((\w|\.)+)$" path)
    (re-matches #"^(.+)-([\da-f]{32})$" path)))

(defn split-path [path]
  "returns [match path extenston] or nil"
  (if (re-find #"[^\.]\.[^\.]" path)
    (re-matches #"^(.+?)\.((\w|\.)+)$" path)
    [path path nil]))

(defn digest-path? [path]
  (-> path split-digested-path boolean))

(defn path->undigested
  "Returns the path with the content digest stripped"
  [path]
  {:pre [(digest-path? path)]
   :post [(not (digest-path? %))]}
  (let [[_ fname digest ext] (split-digested-path path)]
    (if ext
      (str fname "." ext)
      fname)))

(defn path->digested
  "Adds a digest to the path based on the content"
  [path content]
  {:pre [(not (digest-path? path))]
   :post [(digest-path? %)]}
  (let [[_ fname ext] (split-path path)
        digested (digest/digest content)]
    (if ext
      (str fname "-" digested "." ext)
      (str path "-" digested))))

(defn uri->adrf [uri]
  {:pre [(asset-uri? uri)]
   :post [(not (asset-uri? %))]}
  (.substring uri 8))

(defn adrf->uri [adrf]
  {:post [(asset-uri? %)]} ;; uris start with "/assets"
  (str "/assets/" adrf))

(defn adrf->filename [root adrf]
  (str root "/" adrf))

(defn find-file [root adrf]
  (let [file (io/file (adrf->filename root adrf))]
    (when (.exists file)
      [root adrf])))
