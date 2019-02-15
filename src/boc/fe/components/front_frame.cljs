(ns boc.fe.components.front-frame
  (:require
   [boc.fe.components.general :as gc]
   ))

(defn component [content]
  [:div
   [gc/state-debug]
   [gc/online-status]
   [:div.big-logo
    [:h1"Bank of Charlie"]
    [:div "Est. 2017"]]
   [:div {:style {:max-width "400px" :margin "auto"}}
    content]])
