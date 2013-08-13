(ns stefon.test.asset.stefon
  (:require [stefon.test.helpers :as h]
            [clojure.java.io :as io]
            [stefon.settings :as settings]
            [stefon.asset :as asset]
            [stefon.path :as path]
            [stefon.asset.stefon :as stefon]
            [stefon.util :refer (dump)])
  (:use [clojure.test]))

(defn all [adrf]
  (let [root "test/fixtures/assets"]
    (settings/with-options {:asset-roots [root]}
      (let [file (io/file root adrf)]
        (stefon/stefon-files root adrf (asset/read-file file))))))

(deftest test-stefon-files
  (let [files (all "javascripts/stefon.js.stefon")]
    (is (h/contains-file? files "javascripts/app.js"))
    (is (h/contains-file? files "javascripts/lib/framework.js"))
    (is (not (h/contains-file? files "javascripts/lib")))
    (is (h/contains-file? files "javascripts/lib/dquery.js"))
    (is (h/contains-file? files "javascripts/models/feature.js"))

    (testing "load javascript file with same name as directory to be loaded"
      (is (h/contains-file? files "javascripts/lib.js")))))

(deftest test-directories-are-sorted
  (let [root "test/fixtures/assets"
        path "javascripts/sorted/"
        filename (str path "stefon.js.stefon")
        subdir (str root "/" path "subdir")
        files (stefon/stefon-files root filename (-> (io/file root filename) asset/read-file))]
    ;; To ensure this test is actually effective, we added enough files to have
    ;; a high degree of certainty that its sorted on Linux. We also chose files
    ;; which sort differently on than in clojure (OSX sorts alphabetically, but
    ;; is case-insensitive, so below E.js will be in the middle of the list on
    ;; OSX, but at the front when sorted)
    (is (= (map #(-> % io/file .getName) files)
           (seq ["E.js" "a.js" "b.js" "c.js" "d.js" "f.js"
                 "g.js" "h.js" "i.js" "j.js" "k.js" "l.js"
                 "m.js" "n.js"])))))

(deftest test-stefon-asset
  (h/test-expected
   "test/fixtures/assets"
   "javascripts/stefon.js.stefon"
   "/assets/javascripts/stefon.js"
   [
    ;; relative file path
    "var file = \"/app.js\""

    ;; non-specific file paths
    "var file = \"/lib/framework.js\""

    ;; trailing slash requires all files under that directory
    "var file = \"/lib/dquery.js\""

    ;; multiple requires are included only once, the first occurrence
    "var file = \"/lib/framework.js\""]))


(deftest test-emacs-file
  (let [files (all "javascripts/emacs_test/emacs.js.stefon")]
    (is (not (h/contains-file-containing? files ".#")))
    (is (h/contains-file-containing? files "nested/testfile"))))

(deftest test-vim-file
  (let [files (all "javascripts/vim_test/vim.js.stefon" )]
    (is (not (h/contains-file-containing? files ".testfile.coffee.swp")))
    (is (h/contains-file-containing? files "nested/testfile.coffee"))))

(deftest test-dsstore-file
  (let [files (all "javascripts/dsstore_test/dsstore.js.stefon")]
    (is (not (h/contains-file-containing? files ".DS_Store")))
    (is (h/contains-file-containing? files "nested/existing"))))

(deftest test-nested-directories
  (let [files (all "javascripts/nested-dirs.js.stefon")]
    (is (h/contains-file? files "javascripts/nested-dirs/nested1/a.js"))
    (is (h/contains-file? files "javascripts/nested-dirs/nested1/b.js"))
    (is (h/contains-file? files "javascripts/nested-dirs/nested2/c.js"))))

(deftest test-error-on-missing-file
  (let [root "test/fixtures/assets"
        adrf "javascripts/missing_test/missing.stefon"
        filename (path/adrf->filename root adrf)]
    (try
      (stefon/compile-stefon root adrf (asset/read-file filename))
      (is false) ; shouldnt hit
      (catch Exception e
        (is (h/has-text? (.toString e) (str "Couldn't find javascripts/missing_test/some-file-which-doesnt-exist.js in " root)))))))

(deftest test-files-named-same-as-dir
  ;; test for incorrect behaviour. When a dir A should contain a file A but doesn't, stefon returned the dir instead of the file.
  (let [root "test/fixtures/assets"
        adrf "javascripts/missing_test/missing-in-dir.stefon"
        filename (path/adrf->filename root adrf)]
    (try
      (stefon/compile-stefon root adrf (asset/read-file filename))
      (is false) ; shouldnt hit
      (catch Exception e
        (is (h/has-text? (.toString e) (str "Couldn't find javascripts/missing_test/missing_test in " root)))))))