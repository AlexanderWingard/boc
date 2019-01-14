(ns boc.state-test
  (:require
   [boc.state :as state]
   [clojure.test :as t :refer [deftest is testing]]
   ))

(deftest sessions
  (is (= nil (-> {:sessions [{:data {:session "uuid2"}} {:data {:session "uuid4"}}]}
                 (state/join-session "channel" "uuid")))))

