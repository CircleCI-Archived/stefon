(ns stefon.middleware.mime
  "Middleware for sending mime types of stefon files"
  (:require [ring.util.response :as response]
            [stefon.util :refer (dump)]))

(defn stefon-file-type [filename]
  (cond
   (re-matches #".*css.stefon$" filename) "text/css"
   (re-matches #".*js.stefon$" filename) "text/javascript"))

(defn wrap-stefon-mime-types
  [app]
  (fn [req]
    (let [{:keys [headers body] :as response} (app req)]
      (if (instance? java.io.File body)
        (let [filename (.getPath body)
              file-type (stefon-file-type filename)]
          (if file-type
            (response/content-type response file-type)
            response))
        response))))
