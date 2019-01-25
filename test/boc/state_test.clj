(ns boc.state-test
  (:require
   [boc.be.state.users :as users]
   [com.rpl.specter :as s]
   [clojure.test :as t :refer [deftest is testing]]
   [axw.util :refer [deep-merge]]
   ))

(defn s-assert [data select expected]
  (is (= expected (s/select select data)))
  data)

(defn s-setval [structure apath aval]
  (s/setval apath aval structure))

(deftest login
  (-> {:users [{:username "alex" :password "123"}]
       :sessions [{:data {:session "uuid" :username {:value "andrej"}}}]}
      (users/login "uuid")
      (s-assert [:sessions s/FIRST :data (s/multi-path [:username :error] [:password :error])] ["User andrej not found" nil])
      (s-setval [:sessions s/FIRST :data :username :value] "alex")
      (users/login "uuid")
      (s-assert [:sessions s/FIRST :data (s/multi-path [:username :error] [:password :error])] [nil "Wrong password for user alex"])
      (s-setval [:sessions s/FIRST :data :password :value] "123")
      (users/login "uuid")
      (s-assert [:sessions s/FIRST :data (s/multi-path [:username :error] [:password :error])] [nil nil])))


