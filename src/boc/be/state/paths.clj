(ns boc.be.state.paths
  (:require [com.rpl.specter :as s]))

(defn session [s]
  [:sessions s/ALL (s/selected? [:data :session (s/pred= s)])])

(defn channels [s]
  [(session s) :channels s/ALL])

(defn data [s]
  [(session s) :data])

(defn user-id [s]
  [(data s) :private :user :id])

(defn session-ids []
  [:sessions s/ALL :data :session])
