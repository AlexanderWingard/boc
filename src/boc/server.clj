(ns boc.server
  (:require
   [boc.state :as state]
   [axw.ws-server :as server]
   ))

(defn broadcast [state]
  (let [state-str (with-out-str (clojure.pprint/pprint state))]
    (doseq [[data channels] (state/data-and-channels state)]
      (let [string (pr-str (assoc data :debug state-str))]
        (doseq [c channels] (server/send! c string))))))

(defn on-msg [channel state msg]
  (-> state
      (swap! state/handle-msg channel msg)
      (broadcast)))

(defn on-close [channel state]
  (on-msg channel state {:intent :leave}))

(defonce state (atom {:users [
                              {:id 1 :username "andrej" :password "123"}
                              {:id 2 :username "alex" :password "123"}
                              ]}))

(defn -main [& args]
  (-> (server/new :port 8080 :on-msg (var on-msg) :on-close (var on-close) :state state)
      (server/start)
      (server/print-url)))
