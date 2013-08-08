(ns stefon.middleware.mime
  "Middleware for sending mime types of stefon files"
  (:require [ring.util.response :as response]))

(defn stefon-file-type [filename]
  (cond
   (re-matches #".*css-[\da-f]{32}\.stefon$" filename) "text/css"
   (re-matches #".*js-[\da-f]{32}\.stefon$" filename) "text/javascript"))

(defn wrap-stefon-mime-types
  [app]
  (fn [req]
    (let [{:keys [headers body] :as response} (app req)]
      (if (instance? java.io.File body)
        (let [filename (.getPath body)
              file-type (stefon-file-type filename)]
          (if file-type
            (-> response
                (response/content-type file-type))
            response))
        response))))
