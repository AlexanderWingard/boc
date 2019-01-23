(ns boc.server
  (:require
   [boc.state :as state]
   [axw.ws-server :as server]
   ))

(defn on-msg [channel state msg]
  (swap! state state/handle-msg channel msg))

(defn on-close [channel state]
  (swap! state state/handle-msg channel {:intent :leave}))

(defonce state (atom {:users [
                              {:id 1 :username "andrej" :password "123"}
                              {:id 2 :username "alex" :password "123"}
                              ]}))

(defn -main [& args]
  (-> (server/new :port 8080 :on-msg (var on-msg) :on-close (var on-close) :state state)
      (server/start)
      (server/print-url)))
