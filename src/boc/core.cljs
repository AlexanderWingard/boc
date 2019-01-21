(ns boc.core
  (:require
   [axw.ws :as ws]
   [boc.general-components :as gc]
   [cljsjs.semantic-ui :as sem]
   [reagent.core :as r]
   ))

(defn log [& data]
  (apply js/console.log data))

(defn test-component []
  [:div
   [gc/state-debug]
   [gc/online-status]
   [:div.big-logo
    [:h1"Bank of Charlie"]
    [:div "Est. 2017"]]
   [:div {:style {:max-width "400px" :margin "auto"}}
    [:div.ui.form
     [gc/input-field :username "Username"]
     [gc/input-field :password "Password"]
      [:div {:style {:text-align "center"}}
       [gc/intent-button :login "Login"]
       [:br]
       [:a {:href "#register"} "Register new user"]]]]])

(r/render [test-component] (js/document.getElementById "app"))
