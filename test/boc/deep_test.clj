(ns boc.deep-test
  (:require
   [clojure.test :as t :refer [deftest is testing]]
   [axw.deep :refer [deep-merge deep-diff deep-diff-keep]]
   ))

(deftest deep-diff-test
  (is (= {}
         (deep-diff
          false
          {:a {:aa 20}}
          {:a {:aa 20}})))
  (is (= {:a {:aa 20} :b 20}
         (deep-diff-keep
          false
          [:b]
          {:a {:aa 10
               :ab 10}
           :b 20}
          {:a {:aa 20
               :ab 10}
           :b 20})))
  (is (= {:a {:aa 20}}
         (deep-diff
          false
          {:a {:aa 10
               :ab 10}
           :b 20}
          {:a {:aa 20
               :ab 10}
           :b 20})))
  (is (= {:b nil :a {:aa 20 :ab nil}}
         (deep-diff
          true
          {:a {:aa 10 :ab 10} :b 10}
          {:a {:aa 20}})))
  (is (= {:b nil :a 20}
         (deep-diff
          true
          {:a 10 :b 10}
          {:a 20}))))

(deftest deep-merge-test
  (is (= {:a {:aa 20} :b {:bb 20}}
         (deep-merge
          {:a {:aa 10} :b 10}
          {:a {:aa 20} :b {:bb 20}}))))

(deftest deep-swap-test
  (let [state (atom {:a {:aa 10}})
        request {:b {:bb 20}}
        [old new] (swap-vals! state #(-> %
                                         (deep-merge request)
                                         (assoc-in [:a :aa] 20)))
        response (deep-diff false request new)]
    (is (= {:a {:aa 20}} response))))
