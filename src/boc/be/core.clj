(ns boc.be.core
  (:require
   [axw.deep :refer [deep-merge deep-diff]]
   [axw.ws-server :as server]
   [boc.be.state.util :refer :all]
   [boc.be.state.paths :as paths]
   [boc.be.state.sessions :as sessions]
   [boc.be.state.users :as users]
   [boc.be.state.accounts :as accounts]
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
    :accounts/delete  (accounts/delete-account state session)
    :accounts/add  (accounts/add-account state session)
    :transactions/add (accounts/add-transaction state session)
    nil state
    (do (println "Unknown intent: " intent) state)))

(defn update-views [state]
  (reduce (fn [state session]
            (let [state (users/ensure-allowed-view state session)]
              (case (s/select-one [(paths/data session) :view] state)
                :accounts (accounts/view state session)
                state)))
          state
          (s/select (paths/session-ids) state)))

(defn handle-msg [state channel msg]
  (let [session (or (:session msg) (rand-nth ["default-1" "default-2"]))
        intent (:intent msg)
        msg (dissoc msg :session :intent :private)]
    (-> state
        (update-data session msg)
        (handle-intent intent channel session)
        (update-views))))

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

(defn persist [old-new]
  (let [[old new] (map #(dissoc % :sessions) old-new)]
    (if-not (= old new)
      (spit "data.log" (str (pr-str new) "\n") :append true)))
  old-new)

(defn unpersist []
  (try
    (with-open [rdr (clojure.java.io/reader "data.log")]
      (-> rdr (line-seq) (last) (read-string)))
    (catch Exception e {})))

(defn on-msg [channel state msg]
  (-> state
      (swap-vals! handle-msg channel msg)
      (persist)
      (broadcast msg channel)))

(defn on-close [channel state]
  (on-msg channel state {:intent :leave-session}))

(defonce state (atom (unpersist)))

(defn -main [& args]
  (-> (server/new :port 8080 :on-msg (var on-msg) :on-close (var on-close) :state state)
      (server/start)
      (server/print-url)))
