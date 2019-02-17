(ns boc.fe.components.login
  (:require
   [boc.fe.components.general :as gc]
   [boc.fe.components.front-frame :as front-frame]
   ))

(defn component []
  [front-frame/component
   [:div.ui.form
    [gc/input-field :username "Username"]
    [gc/input-field :password "Password"]
    [:div {:style {:text-align "center"}}
     [gc/intent-button :login "Login"]
     [:br]
     [gc/state-href [:view] :register "Register new user"]]]])
