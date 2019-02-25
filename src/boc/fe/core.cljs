(ns boc.fe.core
  (:require
   [axw.ws :as ws]
   [boc.fe.components.login :as login]
   [boc.fe.components.register :as register]
   [boc.fe.components.main :as main]
   [boc.fe.state :as state]
   [cljsjs.semantic-ui :as sem]
   [reagent.core :as r]
   ))

(defn log [& data]
  (apply js/console.log data))

(defn main-component []
  (case (:view @state/state)
    :login [login/component]
    :register [register/component]
    :main [main/component]
    [:div]))

(r/render [main-component] (js/document.getElementById "app"))
