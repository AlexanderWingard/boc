(ns boc.fe.components.register
  (:require
   [boc.fe.components.general :as gc]
   [boc.fe.components.front-frame :as front-frame]
   ))

(defn component []
  [front-frame/component
   [:div.ui.form
    [:h2 "Register new user"]
    [gc/input-field :username "Username"]
    [gc/input-field :password "Password"]
    [gc/input-field :password-repeat "Repeat password"]
    [:div {:style {:text-align "center"}}
     [gc/intent-button :register "Register"]
     [:br]
     [gc/state-href :login-view "Back to login"]]]])
