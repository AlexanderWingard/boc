(ns boc.general-components
  (:require
   [cljs.pprint :refer [pprint]]
   [boc.data :as data]
   ))

(defn online-status []
  [:i.ws-status {:class (if (data/is-online) ["blue" "cloud" "icon"]["red" "x" "icon"])}])

(defn state-debug []
  [:pre {:style {:position "absolute" :top "0px" :left "0px"}} (with-out-str (pprint @data/state))])

(defn input-field [key label]
  (let [state @data/state
        error (get-in state [key :error])]
    [:div.field {:class (when (some? error) "error")}
     [:label label]
     [:input {:type "text"
              :value (get-in state [key :value])
              :on-change #(data/update-state-field [key :value] (-> % .-target .-value))}]
     (when (some? error) [:div.ui.pointing.red.basic.label error])]))

(defn intent-button [key label]
  [:button.ui.button {:on-click #(data/update-state-field [:intent] key)
                      :class (when (some? (get-in @data/state [key :error])) "red")}
   label])
