(ns boc.state
  (:require [com.rpl.specter :as s]))

(defn join-session [state channel uuid]
  (s/select [:sessions s/ALL :data (s/subselect :session #(= % "uuid8"))] state))

(defn leave [state channel]
  (s/setval [:sessions s/MAP-VALS :channels (s/subset #{channel})] #{} state))

(defn update-data [state session data]
  (s/setval [:sessions session :data] data state))

(defn broadcast [state session]
  (s/select-one [:sessions session (s/collect-one :data) :channels] state)
  state)

(defn test [state session]
  (s/select [:sessions s/MAP-VALS :data #(= (:session %) session)] state))
