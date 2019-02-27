(ns boc.be.core
  (:require
   [axw.deep :refer [deep-merge deep-diff]]
   [axw.ws-server :as server]
   [boc.be.state.paths :as paths]
   [boc.be.state.sessions :as sessions]
   [boc.be.state.users :as users]
   [clojure.pprint :refer [pprint]]
   [com.rpl.specter :as s]
   ))

(defn update-data [state session data]
  (s/transform (paths/data session) #(deep-merge % data) state))

(defn handle-intent [state intent channel session]
  (case intent
    :join-session (sessions/join state channel session)
    :leave-session (sessions/leave state channel)
    :login (users/login state session)
    :logout (users/logout state session)
    :register (users/register state session)
    :register-view (update-data state session {:view :register})
    :login-view (update-data state session {:view :login})
    state))

(defn handle-view [state session]
  (users/ensure-allowed-view state session))

(defn handle-msg [state channel msg]
  (let [session (or (:session msg) (rand-nth ["default-1" "default-2"]))
        intent (:intent msg)
        msg (dissoc msg :session :intent :private)]
    (-> state
        (update-data session msg)
        (handle-intent intent channel session)
        (handle-view session))))

(defn broadcast-int [[old new] msg from]
  (let [state-str (with-out-str (pprint new))
        old-channels-data (sessions/data-and-channels old)
        new-channels-data (sessions/data-and-channels new)]
    (mapcat
     (fn [[session {new-data :data  channels :channels}]]
       (let [new-data (assoc new-data :debug state-str)
             old-data (get-in old-channels-data [session :data])
             mk-diff (fn [return-to-sender]
                       (if (and return-to-sender (= :join-session (:intent msg)))
                         new-data
                         (let [diff (deep-diff true old-data new-data)]
                           (if return-to-sender
                             (deep-diff false msg diff)
                             diff))))]
         (map (fn [channel] [channel (mk-diff (= channel from))]) channels)))
     new-channels-data)))

(defn broadcast [old-new msg from]
  (doseq [[chan data] (broadcast-int old-new msg from)]
    (server/send! chan (pr-str data))))

(defn on-msg [channel state msg]
  (-> state
      (swap-vals! handle-msg channel msg)
      (broadcast msg channel)))

(defn on-close [channel state]
  (on-msg channel state {:intent :leave-session}))

(defonce state (atom {:users [
                              {:id 1 :username "andrej" :password "123"}
                              {:id 2 :username "alex" :password "123"}
                              ]}))

(defn -main [& args]
  (-> (server/new :port 8080 :on-msg (var on-msg) :on-close (var on-close) :state state)
      (server/start)
      (server/print-url)))
