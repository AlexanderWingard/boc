(ns boc.broadcast-test
  (:require
   [boc.be.core :as be]
   [clojure.test :as t :refer [deftest is testing]]
   ))

(deftest broadcast-test
  (let [data-old {:sessions [{:data {:session "1" :a 10 :b 10} :channels #{1}}]}
        data-new {:sessions [{:data {:session "1" :a 20} :channels #{1}}]}
        result (atom [])]
    (with-redefs [axw.ws-server/send! (fn [ch data] (swap! result conj data))]
      (be/broadcast [data-old data-new] {:a 20} 1)
      (println @result))))
