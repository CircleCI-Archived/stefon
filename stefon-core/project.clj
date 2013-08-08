(defproject stefon "0.4.0"
  :description "Asset pipeline ring middleware"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :url "http://github.com/edgecase/stefon"
  :dependencies [[ring/ring-core "1.1.8"]
                 [clj-time "0.4.4"]
                 [org.clojure/core.incubator "0.1.1"]
                 [com.google.javascript/closure-compiler "r1592"]
                 [clj-v8 "0.1.4"]
                 [clj-v8-native "0.1.4"]
                 [org.mozilla/rhino "1.7R4"]
                 [org.clojure/clojure "1.4.0"]]
  :dev-dependencies [[org.clojure/clojure "1.5.0"]]
  :profiles {:dev {:dependencies [[ring-mock "0.1.4"]]}})
