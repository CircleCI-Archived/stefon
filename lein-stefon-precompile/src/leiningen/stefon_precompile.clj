(ns leiningen.stefon-precompile
  "Precompile stefon assets"
  (:require [clojure.string :as string]
            [leiningen.core.eval :as eval]))

(defn split-ns
  "Given a string "
  [ns-string]
  (let [pair (map symbol (string/split ns-string #"/"))
         ns (first pair)
         sym (second pair)]
     [ns sym]))

(defn stefon-precompile
  [project]
  (let [[ns sym] (-> project :stefon-options split-ns)]
    (eval/eval-in-project
     project
     `(let [options# (ns-resolve (quote ~ns) (quote ~sym))]
        (println "options=" @options#)
        (stefon.core/precompile @options#))
     `(do (require 'stefon.core)
          (require (quote ~ns))))))