(ns boc.fe.components.general
  (:require
   [boc.fe.state :as state]
   ))

(defn online-status []
  [:i.ws-status {:class (if (state/is-online) ["blue" "cloud" "icon"]["red" "x" "icon"])}])

(defn state-debug []
  [:pre {:class "debug"} (:debug @state/state)])

(defn input-field [key label]
  (let [state @state/state
        error (get-in state [key :error])]
    [:div.field {:class (when (some? error) "error")}
     [:label label]
     [:div.ui.large.input
      [:input {:type "text"
               :value (get-in state [key :value])
               :on-change #(state/update-state-field [key :value] (-> % .-target .-value))}]]
     (when (some? error) [:div.ui.pointing.red.basic.label error])]))

(defn intent-button [key label]
  [:button.ui.button {:on-click #(state/send-intent key)
                      :class (when (not-empty (get-in @state/state [key :error])) "red")}
   label])

