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
      (state/join-session "channel" "uuid")
      (s-assert [:sessions s/ALL :channels (s/set-elem "channel")] ["channel"])
      (state/join-session "channel" "uuid2")
      (s-assert [:sessions s/ALL (s/selected? [:data :session (s/pred= "uuid")]) :channels (s/set-elem "channel")] []))
  )

