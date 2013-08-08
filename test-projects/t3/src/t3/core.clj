(ns t3.core
  (:require
   [ring.middleware.file :as ringfile]
   [ring.adapter.jetty :as jetty]
   [stefon.core :as stefon]))

(defn handler [req]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    (str "Hello World from Ring" (stefon/link-to-asset "scripts.js"))})

(defn boot []
  (stefon/init {:cache-mode :production})
  (jetty/run-jetty
   (-> (-> #'handler (ringfile/wrap-file "filez"))
       (stefon/asset-pipeline {:cache-mode :production}))
   {:port 8080}))
