(ns boc.broadcast-test
  (:require
   [boc.be.core :as be]
   [clojure.test :as t :refer [deftest is testing]]
   ))

(deftest broadcast-test
  (let [data-old {:sessions [{:data {:session "1" :a 10 :b 10 :c 10} :channels #{1 2}}
                             {:data {:session "2" :a 10 } :channels #{3}}]}
        data-new {:sessions [{:data {:session "1" :a 20 :c 10} :channels #{1 2}}
                             {:data {:session "2" :a 10 } :channels #{3}}]}
        no-debug (->> (be/broadcast-int [data-old data-new] {:a 20} 1)
                      (map #(update %1 1 (fn [m] (dissoc m :debug)))))]
    (is (= '([1 {:b nil}] [2 {:b nil :a 20}] [3 {}]) no-debug))))
