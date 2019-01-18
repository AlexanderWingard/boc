(ns boc.core
  (:require
   [cljs.pprint :refer [pprint]]
   [cljsjs.semantic-ui :as sem]
   [reagent.core :as r]
   [axw.ws :as ws]
   [clojure.set :as sets]))

(defonce state (r/atom {}))
(defonce ws-state (r/atom "offline"))
(declare ws)

(defn log [& data]
  (apply js/console.log data))

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

(defn ws-receive [data]
  (when (>= (:seq-nr data) (:seq-nr @state))
    (reset! state data)))

(defn ws-open []
  (reset! ws-state "online")
  (update-state-reset {:intent :join-session :session "random-uuid"}))

(defn ws-close []
  (reset! ws-state "offline"))

(defonce ws (ws/new "ws"
                    #((var ws-receive) %)
                    #((var ws-open))
                    #((var ws-close))))

(defn input-field [key label]
  (let [error (get-in @state [key :error])]
    [:div.field {:class (when (some? error) "error")}
     [:label label]
     [:input {:type "text"
              :value (get-in @state [key :value])
              :on-change #(update-state-field [key :value] (-> % .-target .-value))}]
     (when (some? error) [:div.ui.pointing.red.basic.label error])]))

(defn test-component []
  [:div
   [:code (with-out-str (pprint @state))]
   [:i.ws-status {:class (if (= "online" @ws-state) ["blue" "cloud" "icon"]["red" "x" "icon"])}]
   [:div.big-logo
    [:h1"Bank of Charlie"]
    [:div "Est. 2017"]]
   [:div {:style {:max-width "400px" :margin "auto"}}
    [:div.ui.form
     [input-field :username "Username"]
     [input-field :password "Password"]
      [:div {:style {:text-align "center"}}
       [:button.ui.button {:on-click #(update-state-field [:intent] :login) :class (when (some? (get-in @state [:login :error])) "red")} "Login"]
       [:br]
       [:a {:href "#register"} "Register new user"]]]]])

(r/render [test-component] (js/document.getElementById "app"))
