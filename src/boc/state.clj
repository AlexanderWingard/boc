(ns boc.state
  (:require [com.rpl.specter :as s]))

(defn join-session [state channel uuid]
  (s/setval [:sessions uuid (s/nil->val {:data {:tmp 20 :session uuid}}) :channels (s/subset #{})] #{channel} state))

(defn leave [state channel]
  (s/setval [:sessions s/MAP-VALS :channels (s/subset #{channel})] #{} state))

(defn update-data [state session data]
  (s/setval [:sessions session :data] data state))

(defn broadcast [state session]
  (log(s/select-one [:sessions session (s/collect-one :data) :channels] state))
  state)

(defn test [state session]
  (s/select [:sessions s/MAP-VALS :data #(= (:session %) session)] state))

(defn log [data]
  (println data)
  data)

(-> {}
    (join-session "chan" "1")
    (test "1")
    ;; (join-session "chan2" "1")
    ;; (log)
    ;; (join-session "chan" "2")
    ;; (log)
    ;; ;; (update-data "1" {:fe-data 10})
    ;; (broadcast "1")
    ;; ;; (leave "chan")
    ;; (log)
    )
