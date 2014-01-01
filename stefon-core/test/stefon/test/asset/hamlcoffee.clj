(ns stefon.test.asset.hamlcoffee
  (:require [stefon.test.helpers :as h]
            [stefon.asset :as asset]
            [stefon.asset.hamlcoffee :as hc]
            [stefon.settings :as settings]
            [stefon.digest :as digest]
            [stefon.util :refer (dump)])
  (:use clojure.test))

(defn wrap [name output]
  "The hamlcoffee compiler wraps the code, so we need to wrap our expected output"
  (str "(function() {\n  var _ref;\n\n  if ((_ref = window.HAML) == null) {\n    window.HAML = {};\n  }\n\n  window.HAML['"
       name
       "'] = function(context) {\n    return (function() {\n      var $o;\n      $o = [];\n      $o.push(\""

       output
       "\");\n      return $o.join(\"\\n\");\n    }).call(context);\n  };\n\n}).call(this);\n"))

(deftest test-basic
  (h/test-expected "test/fixtures/assets"
                   "javascripts/basic.hamlc"
                   "/assets/javascripts/basic-07da6554d81caef2b995caf987514c35"
                   [(wrap "basic"
                          "<!DOCTYPE html>\\n<html>\\n  <head>\\n    <title>\\n      Title\\n    </title>\\n  </head>\\n  <body>\\n    <h1>\\n      Header\\n    </h1>\\n  </body>\\n</html>")]))

(deftest test-hamlcoffee-options
  ;; reregister the hamlc compiler; make sure we're using the defaults
  (settings/with-options {}
    (require '[stefon.asset.hamlcoffee :as hc] :reload-all)
    (testing "escapes html by default"
      (h/test-expected "test/fixtures/assets"
                       "javascripts/html.hamlc"
                       "/assets/javascripts/html-195780c3874e7799182df13406bb419d"
                       [".replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/'/g, '&#39;').replace(/\\//g, '&#47;').replace(/\"/g, '&quot;')"])))

  ;; reregister the hamlc compiler; uses the options we specify
  (settings/with-options {:hamlcoffee-options {:escapeHtml false}}
    (require '[stefon.asset.hamlcoffee :as hc] :reload-all)
    (testing "accepts setting to not escape html"
      (h/test-expected "test/fixtures/assets"
                       "javascripts/html.hamlc"
                       "/assets/javascripts/html-c87cbe0effa5609d4834504dc40f7b95"
                       [(wrap "html"
                              "\" + \"<a href='#'>test</a>")]))))

;; (deftest bad-haml-syntax
;;   (h/test-syntax "test/fixtures/assets"
;;                  "javascripts/badhaml1.hamlc"
;;                  ["ERROR: Syntax Error on line 1"]))

;;   (is (has-text?
;;        (preprocess-hamlcoffee
;;         (io/file "test/fixtures/assets/javascripts/badhaml2.hamlc"))
;;        "@import \"includeme.less\"")))

;; (testing "bad coffee syntax"
;;   (is (has-text?
;;        (preprocess-hamlcoffee
;;         (io/file "test/fixtures/assets/javascripts/badcoffee.hamlc"))
;;        "@import \"includeme.less\""))))
