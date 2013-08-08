(ns stefon.cache.disk
  (:require [clojure.string :as cstr]
            [stefon.path :as path]
            [clojure.java.io :as io]))


(derive (class (make-array Byte/TYPE 0)) ::bytes)
(derive java.lang.String ::string-like)
(derive java.lang.StringBuilder ::string-like)

(defmulti write-file (fn [c f] (class c)))
(defmethod write-file ::string-like [content file]
  (spit file content))

(defmethod write-file ::bytes [content file]
  (with-open [out (java.io.FileOutputStream. file)]
    (.write out content)))

(defn cached-file-path
  "given the adrf, generate the filename of where the file
will be cached. Cache is rooted at cache-root/assets/ so that
static file middleware can be rooted at cache-root"
  [adrf content]
  (add-md5 (path/adrf->filename (settings/precompile-root) adrf)
           content))

(defn write-to-cache [content adrf]
  (let [dest (io/file (cached-file-path adrf content))]
    (io/make-parents dest)
    (write-file content dest)
    dest))
