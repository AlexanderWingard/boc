(ns boc.fe.components.main-frame
  (:require
   [boc.fe.components.general :as gc]
   [boc.fe.state :as state]
   ))

(defn menu []
  [:div.ui.menu
   [:div.right.menu
    (gc/intent-button :logout (str "Logout " (get-in @state/state [:private :user :username])))]])

(defn component [content]
  [:div
   [menu]
   [gc/state-debug]
   [gc/online-status]
   content])
