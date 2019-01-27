(ns boc.state-test
  (:require
   [boc.be.state.users :as users]
   [boc.be.state.paths :as paths]
   [clojure.test :as t :refer [deftest is testing]]
   [boc.test-util :refer :all]
   [boc.be.state.paths :as paths]))

(deftest login
  (let [initial {:users [{:username "alex" :password "123"}]
                 :sessions [{:data {:session "uuid"}}]}
        session-data (paths/data "uuid")]
    (-> initial
        (s-setval [session-data :username :value] "andrej")
        (users/login "uuid")
        (s-assert [session-data :username :error] ["User andrej not found"])
        (s-assert [session-data :password :error] [nil])

        (s-setval [session-data :username :value] "alex")
        (users/login "uuid")
        (s-assert [session-data :password :error] ["Wrong password for user alex"])

        (s-setval [session-data :password :value] "123")
        (users/login "uuid")
        (s-assert [session-data :username :error] [nil])
        (s-assert [session-data :password :error] [nil])
        )))

(deftest register
  (let [initial {:users [{:username "alex" :password "123"}]
                 :sessions [{:data {:session "uuid"}}]}
        session-data (paths/data "uuid")]
    (-> initial
        (users/register "uuid")
        (s-assert [session-data :username :error] ["Please supply username"])

        (s-setval [session-data :username :value] "alex")
        (users/register "uuid")
        (s-assert [session-data :username :error] ["User alex already exists"])

        (s-setval [session-data :username :value] "andrej")
        (users/register "uuid")
        (s-assert [session-data :password :error] ["Please supply password"])
        (s-assert [session-data :username :error] [nil])

        (s-setval [session-data :password :value] "123")
        (users/register "uuid")
        (s-assert [session-data :password-repeat :error] ["Passwords must match"])
        (s-assert [session-data :register :error] [["Passwords must match"]])

        (s-setval [session-data :password-repeat :value] "123")
        (users/register "uuid")
        (s-assert [session-data :register :error] [[]])
        (apply-assert users/user-by-name "andrej" {:username "andrej" :password "123"})
        )))
