(ns boc.state-test
  (:require
   [boc.state :as state]
   [com.rpl.specter :as s]
   [clojure.test :as t :refer [deftest is testing]]
   ))

(defn s-assert [data select expected]
  (is (= expected (s/select select data)))
  data)

(deftest sessions
  (-> {}
      (state/join-session "c-1" "s-1")
      (s-assert (state/channels-path "s-1") ["c-1"])
      (state/join-session "c-1" "s-2")
      (s-assert (state/channels-path "s-1") [])
      (s-assert (state/channels-path "s-2") ["c-1"])
      (state/leave "c-1")
      (s-assert (state/channels-path "s-2") [])))

(deftest users
  (-> {}
      (state/join-session "c-1" "s-1")
      (state/login "s-1" "alex")
      (state/update-data "s-1" {:user "fraud"})
      (state/logout "s-1")
      ))
