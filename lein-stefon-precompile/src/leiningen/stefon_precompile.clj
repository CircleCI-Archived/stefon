(ns leiningen.stefon-precompile
  "Precompile stefon assets"
  (:require [clojure.string :as string]
            [leiningen.core.eval :as eval]))

(defn ns-resolve-string
  "Given a string "
  [ns-string]
  `(let [pair# (map symbol (string/split ~ns-string #"/"))
         ns# (first pair#)
         sym# (second pair#)]
     (require ns#)
     @(ns-resolve ns# sym#)))

(defn resolve-stefon-options [opt]
  `(let [opt# ~opt]
     (cond
      (map? opt#) opt#
      (string? opt#) (let [val# ~(ns-resolve-string opt)]
                       (cond
                        (map? val#) val#
                        (fn? val#) (val#))))))

(defn stefon-precompile
  [project]
  (let [options (:stefon-options project)
        options (->> project
                     :stefon-options
                     resolve-stefon-options)]
    (println "options=" options)
    (eval/eval-in-project
     project
     `(let []
        (stefon.core/precompile ~options))
     `(require 'stefon.core))))