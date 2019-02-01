(ns boc.fe.state
  (:require
   [axw.ws :as ws]
   [axw.deep :refer [deep-diff-keep]]
   [reagent.core :as r]
   ))

(defonce state (r/atom {}))
(defonce ws-state (r/atom "offline"))
(declare ws)

(defn update-state-reset [new]
  (->> (reset! state new)
       (ws/send ws)))

(defn update-state-field [ks value]
  (->> (swap-vals! state #(-> % (update :seq-nr inc) (assoc-in ks value)))
       (apply deep-diff-keep [:session])
       (ws/send ws)))

(defn is-online []
  (= "online" @ws-state))

(defn ws-receive [data]
  (when (>= (:seq-nr data) (:seq-nr @state))
    (reset! state data)))

(defn ws-open []
  (reset! ws-state "online")
  (update-state-reset {:intent :join-session}))

(defn ws-close []
  (reset! ws-state "offline"))

(defonce ws (ws/new "ws"
                    #((var ws-receive) %)
                    #((var ws-open))
                    #((var ws-close))))
