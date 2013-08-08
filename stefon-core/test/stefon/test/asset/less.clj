(ns stefon.test.asset.less
  (:require [clojure.java.io :as io]
            [stefon.asset.less :as less]
            [stefon.settings :as settings]
            [stefon.test.helpers :as h])
  (:use clojure.test))

(deftest test-preprocess-less
  (settings/with-options
   {:compress false}
   (testing "basic less file"
    (is (= "#header {\n  color: #4d926f;\n}\n" (less/preprocess-less (io/file "test/fixtures/assets/stylesheets/basic.less")))))
   (testing "file with imports"
    (is (= "#includee {\n  color: white;\n}\n#includee-three {\n  color: white;\n}\n#includee-two {\n  color: white;\n}\n#includer {\n  color: black;\n}\n"
           (less/preprocess-less (io/file "test/fixtures/assets/stylesheets/includes.less")))))
   (testing "bad less syntax"
    (try
      (less/preprocess-less (io/file "test/fixtures/assets/stylesheets/bad.less"))
      (is false) ; test it throws
      (catch Exception e
        (is (h/has-text? (.toString e) "Syntax Error on line 1"))
        (is (h/has-text? (.toString e) "@import \"includeme.less\"")))))))
