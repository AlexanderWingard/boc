(ns boc.data
  (:require
   [axw.ws :as ws]
   [reagent.core :as r]
   ))

(defonce state (r/atom {}))
(defonce ws-state (r/atom "offline"))
(declare ws)

(defn deep-diff [old new]
  (reduce-kv
   (fn [acc k new-val]
     (let [old-val (get old k)]
       (if (= old-val new-val)
         acc
         (if (map? new-val)
           (if-some [child (deep-diff old-val new-val)] (assoc acc k child) acc)
           (assoc acc k new-val)))))
   (select-keys new [:session])
   new))

(defn update-state-reset [new]
  (->> (reset! state new)
       (ws/send ws)))

(defn update-state-field [ks value]
  (->> (swap-vals! state #(-> % (update :seq-nr inc) (assoc-in ks value)))
       (apply deep-diff)
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
