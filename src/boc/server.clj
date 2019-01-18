(ns boc.server
  (:require
   [boc.state :as state]
   [axw.ws-server :as be]
   ))

(defn on-msg [channel state msg]
  (let [session (:session msg)
        intent (:intent msg)
        msg (dissoc msg :session :intent)]
    (swap! state #(-> %
                      (state/handle-intent intent msg channel session)
                      (state/update-data session msg)
                      (state/broadcast session be/send!)))))

(defn on-close [channel state]
  (swap! state state/leave channel))

(defonce server (be/server :port 8080
                           :on-msg (var on-msg)
                           :on-close (var on-close)))
(defn -main
  [& args]
  (be/start server)
  (println "http://localhost:8080"))
