(ns boc.test-util
  (:require
   [clojure.test :as t]
   [com.rpl.specter :as s]
   [boc.be.state.paths :as paths]
   ))

(defn s-assert [data select expected]
  (t/is (contains? (s/select-one (butlast select) data) (last select)))
  (t/is (= expected (s/select-one select data)))
  data)

(defn s-setval [structure apath aval]
  (s/setval apath aval structure))

(defn apply-assert [state func & args]
  (t/is (= (last args) (apply func state (butlast args))))
  state)

(defn mk-error-assert [session state key expected]
  (s-assert state [(paths/data session) key :error] expected))

(defn mk-data-assert [session state keys expected]
  (s-assert state (concat (paths/data session) keys) expected))

(defn mk-set-value [session state key value]
  (s-setval state [(paths/data session) key :value] value))

(defn mk-set-data [session state keys value]
  (s-setval state (concat (paths/data session) keys) value))
