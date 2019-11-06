(ns boc.be.core
  (:require
   [axw.deep :refer [deep-merge deep-diff]]
   [axw.ws-server :as server]
   [clojure.pprint :refer [pprint]]
   [clojure.data.json :as json]
   [com.rpl.specter :as s]
   [clojure.edn :as edn]
   ))

(defonce state (atom {:channels #{}}))

(defn broadcast [msg & [sender]]
  (doseq [c (:channels @state)]
    (when (not= c sender)
      (server/send! c msg))))

(defn on-msg [channel state msg]
  (broadcast msg channel))

(defn on-connect [channel state]
  (swap! state update :channels conj channel)
  (broadcast (json/write-str {:msg "join" :channel (str channel)})))

(defn on-close [channel state]
  (swap! state update :channels disj channel)
  (broadcast (json/write-str {:msg "leave" :channel (str channel)})))

(defn -main [& args]
  (-> (server/new :port 8080 :on-msg (var on-msg) :on-connect (var on-connect) :on-close (var on-close) :state state)
      (server/start)
      (server/print-url)))
