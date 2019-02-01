(ns boc.be.state.core
  (:require
   [boc.be.state.sessions :as sessions]
   [boc.be.state.users :as users]
   [boc.be.state.paths :as paths]
   [com.rpl.specter :as s]
   [axw.deep :refer [deep-merge]]))

(def data-and-channels sessions/data-and-channels)

(defn update-data [state session data]
  (s/transform (paths/data session) #(deep-merge % data) state))

(defn handle-intent [state intent channel session]
  (case intent
    :join-session (sessions/join state channel session)
    :leave-session (sessions/leave state channel)
    :login (users/login state session)
    state))

(defn handle-msg [state channel msg]
  (let [session (or (:session msg) (rand-nth ["default-1" "default-2"]))
        intent (:intent msg)
        msg (dissoc msg :session :intent :private)]
    (-> state
        (update-data session msg)
        (handle-intent intent channel session))))
