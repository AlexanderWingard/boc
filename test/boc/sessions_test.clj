(ns boc.sessions-test
  (:require
   [boc.be.state.sessions :as sessions]
   [clojure.test :refer [deftest is testing]]
   [boc.test-util :refer :all]
   [com.rpl.specter :as s]
   [boc.be.state.paths :as paths]
   ))

(deftest sessions
  (-> {}
      (sessions/join 1 "uuid")
      (s-assert [(paths/session "uuid") :channels] #{1})
      (sessions/join 1 "uuid2")
      (s-assert [(paths/session "uuid") :channels] #{})
      (s-assert [(paths/session "uuid2") :channels] #{1})
      (sessions/leave 1)
      (s-assert [(paths/session "uuid2") :channels] #{})
      ))
