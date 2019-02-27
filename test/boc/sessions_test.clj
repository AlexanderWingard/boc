(ns boc.sessions-test
  (:require
   [boc.be.state.sessions :as sessions]
   [clojure.test :refer [deftest is testing]]
   [boc.test-util :refer :all]
   ))

(deftest sessions
  (-> {}
      (sessions/join 1 "uuid")
      (s-assert [] nil)))
