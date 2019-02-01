(ns boc.deep-test
  (:require
   [clojure.test :as t :refer [deftest is testing]]
   [axw.deep :refer [deep-diff deep-diff-keep]]
   ))

(deftest deep-diff-test
  (is (= {}
         (deep-diff
          {:a {:aa 20}}
          {:a {:aa 20}})))
  (is (= {:a {:aa 20} :b 20}
         (deep-diff-keep
          [:b]
          {:a {:aa 10
               :ab 10}
           :b 20}
          {:a {:aa 20
               :ab 10}
           :b 20})))
  (is (= {:a {:aa 20}}
         (deep-diff
          {:a {:aa 10
               :ab 10}
           :b 20}
          {:a {:aa 20
               :ab 10}
           :b 20}))))
