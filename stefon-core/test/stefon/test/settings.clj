(ns stefon.test.settings
  (:require [stefon.util :refer (dump)]
            [stefon.settings :as settings]
            [stefon.core :as core]
            [clojure.test :refer :all]))


(deftest disallow-asset-root-without-assets
  (is (thrown? Exception
               (settings/with-options {:asset-roots ["aasd"]}
                 (settings/asset-roots)))))

(deftest works-fine-with-assets
  (is (settings/with-options {:asset-roots ["aasd/assets"]}
        (settings/asset-roots))))