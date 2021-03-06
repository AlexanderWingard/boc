(ns boc.fe.components.general
  (:require
   [cljs.pprint :refer [pprint]]
   [boc.fe.state :as state]
   [reagent.core :as r]
   [calendar-ui]))

(defn online-status []
  [:i.ws-status {:class (if (state/is-online) ["blue" "cloud" "icon"]["red" "x" "icon"])}])

(defn dbg [d]
  (with-out-str (pprint d)))

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

(defn calendar-input-field [key label]
  (let [state @state/state
        me (r/current-component)
        error (get-in state [key :error])]
    [:div.field {:class (when (some? error) "error")}
     [:label label]
     [:div.ui.large.input
      [:input {:type "text"
               :value (get-in state [key :value])
               :on-change #(state/update-state-field [key :value] (-> % .-target .-value))
               :on-focus (fn [this] (doto (js/$ (r/dom-node me))
                                      (.calendar #js {:onChange (fn [date text mode] (state/update-state-field [key :value] text))})))}]]
     (when (some? error) [:div.ui.pointing.red.basic.label error])]))

(defn intent-button [key label]
  [:button.ui.button {:on-click #(state/send-intent key)
                      :class (when (not-empty (get-in @state/state [key :error])) "red")}
   label])

(defn state-button [ks value label]
  [:button.ui.button {:on-click #(state/update-state-field ks value)}
   label])

(defn state-href [ks value label]
  [:a {:href "#" :on-click #(state/update-state-field ks value)} label])

(defn modal [& body]
  (r/create-class
   {:display-name "A modal"
    :component-did-mount (fn [this] (doto (js/$ "#modal")
                                      (.modal #js {:closable false})
                                      (.modal "show")))
    :component-will-unmount (fn [this] (doto (js/$ "#modal")
                                         (.modal "hide")
                                         (.remove)))
    :reagent-render (fn [& body] [:div [:div.ui.modal {:id "modal"} body]])}))


