(ns boc.be.core
  (:require
   [axw.deep :refer [deep-merge deep-diff deep-diff-2]]
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

(defn broadcast [[old new] msg from]
  (let [state-str (with-out-str (pprint new))
        old-data (sessions/data-and-channels old)]
    (doseq [[session {:keys [data channels]}] (sessions/data-and-channels new)]
      (let [data (assoc data :debug state-str)]
        (doseq [c channels]
          (->> (if (and (= c from) (= :join-session (:intent msg)))
                 data
                 (let [diff (deep-diff-2 (get-in old-data [session :data])
                                         data)]
                   (if (= c from)
                     (deep-diff msg diff)
                     diff)))
               (pr-str)
               (server/send! c)))))))

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
