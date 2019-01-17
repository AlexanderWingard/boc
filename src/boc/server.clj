(ns boc.server
  (:require
   [boc.state :as state]
   [clweb.next.backend :as be]
   [org.httpkit.server :refer [send!]]
   ))

(defn on-msg [channel state {:keys [session] :as msg}]
  (case (:intent msg)
    :join-session (swap! state state/join-session channel session)
    nil)
  (swap! state state/update-data session (assoc msg :intent nil))
  (let [[data channels] (state/broadcast @state session)
        string (pr-str data)]
    (doseq [c channels] (send! c string))))

(defn on-close [channel state]
  (swap! state state/leave channel))

(defonce server (atom (be/server :port 8080
                                 :on-msg (var on-msg)
                                 :on-close (var on-close))))
(defn -main
  [& args]
  (swap! server be/start)
  (println "http://localhost:8080"))
