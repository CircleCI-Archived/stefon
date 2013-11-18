(defproject stefon "0.5.0-SNAPSHOT"
  :description "Asset pipeline ring middleware"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :url "http://github.com/circleci/stefon"
  :dependencies [[ring/ring-core "1.2.0"]
                 [clj-time "0.4.4"]
                 [org.clojure/core.incubator "0.1.1"]
                 [org.clojure/tools.logging "0.2.3"]
                 [cheshire "4.0.0"]
                 [commons-codec "1.5"]
                 [com.google.javascript/closure-compiler "r1592"]
                 [clj-v8 "0.1.4"]
                 [clj-v8-native "0.1.4"]
                 [pathetic "0.5.1"]]
  :profiles {:dev {:dependencies [[ring-mock "0.1.4"]
                                  [org.clojure/clojure "1.5.1"]
                                  [bond "0.2.5" :exclusions [org.clojure/clojure]]]}})
