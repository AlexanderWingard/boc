(ns boc.core
  (:require
   [cljs.pprint :refer [pprint]]
   [cljsjs.semantic-ui :as sem]
   [reagent.core :as r]
   [axw.ws :as ws]
   [clojure.set :as sets]))

(defonce state (r/atom {}))
(defonce ws-state (r/atom "offline"))

(defn log [& data]
  (apply js/console.log data))

(defn dict-diff [old new]
  (into {} (sets/difference (set new) (set old))))

(defn ws-receive [data]
  (when (>= (:seq-nr data) (:seq-nr @state))
    (reset! state data)))

(defonce ws (ws/new "ws"
                   #((var ws-receive) %)
                   #(reset! ws-state "online")
                   #(reset! ws-state "offline")))

(defn client-side-update [key value]
  (->> (swap-vals! state #(-> % (update :seq-nr inc) (assoc key value)))
       (apply dict-diff)
       (ws/send ws)))

(defn input-field [key]
  [:input {:type "text"
           :value (key @state)
           :on-change #(client-side-update key (-> % .-target .-value))}])

(defn login-form []
  [:div
   [input-field :first-name]
   [input-field :last-name]])

(defn test-component []
  [:div
   [:div @ws-state]
   [login-form]
   [:code (with-out-str (pprint @state))]])

(r/render [test-component] (js/document.getElementById "app"))
