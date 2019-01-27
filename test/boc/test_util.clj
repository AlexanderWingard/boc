(ns boc.test-util
  (:require
   [clojure.test :as t]
   [com.rpl.specter :as s]
   ))

(defn s-assert [data select expected]
  (t/is (= expected (s/select select data)))
  data)

(defn s-setval [structure apath aval]
  (s/setval apath aval structure))

(defn apply-assert [state func & args]
  (t/is (= (last args) (apply func state (butlast args)))))
