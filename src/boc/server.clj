(ns boc.server
  (:require
   [boc.state :as state]
   [axw.ws-server :as server]
   ))

(defn on-msg [channel state msg]
  (let [session (:session msg)
        intent (:intent msg)
        msg (dissoc msg :session :intent)]
    (swap! state #(-> %
                      (state/handle-intent intent msg channel session)
                      (state/update-data session msg)
                      (state/broadcast session server/send!)))))

(defn on-close [channel state]
  (swap! state state/leave channel))

(defn -main [& args]
  (-> (server/new :port 8080 :on-msg (var on-msg) :on-close (var on-close))
      (server/start)
      (server/print-url)))
