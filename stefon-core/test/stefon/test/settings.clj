(ns stefon.test.settings
  (:require [stefon.util :refer (dump)]
            [stefon.settings :as settings]
            [stefon.core :as core]
            [clojure.test :refer :all]))

(deftest validate-works
  (testing "throws an exception when errors are found"
    (is (thrown-with-msg? Exception #"must be a map"
                          (binding [settings/*settings* "not a map"]
                            (settings/validate))))
    (is (thrown-with-msg? Exception #"serving-root"
                          (settings/with-options {:mode :production
                                                  :serving-root nil}))))
  (testing "throws no exception with default options (which are valid)"
    (is (nil? (settings/validate))))
  (testing "when mode is production precompiles must be set"
    (is (thrown-with-msg? Exception #"^Options .* are invalid:.*precompiles"
                          (settings/with-options {:mode :production
                                                  :serving-root "public"})))
    (is (nil? (settings/with-options {:mode :production
                                      :precompiles []
                                      :serving-root "public"})))))

(deftest disallow-asset-root-without-assets
  (is (thrown? Exception
               (settings/with-options {:asset-roots ["aasd"]}
                 (settings/asset-roots)))))

(deftest works-fine-with-assets
  (is (settings/with-options {:asset-roots ["aasd/assets"]}
        (settings/asset-roots))))
