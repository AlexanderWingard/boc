(ns boc.fe.core
  (:require
   [axw.ws :as ws]
   [cljsjs.semantic-ui :as sem]
   [reagent.core :as r]
   [clojure.walk :refer [prewalk-replace]]
   [json-html.core :as json-html]
   ))

(defonce state (r/atom {}))

(declare ws)

(defn render-clojure [data]
  (prewalk-replace
   {:table.jh-type-object :table.jh-type-object.ui.selectable.celled.compact.table}
   (json-html/json->hiccup data)))

(defn log [& data]
  (apply js/console.log data))

(defn ws-receive [data]
  (swap! state update :messages conj data))

(defn ws-open []
  )

(defn ws-close []
  )

(defonce ws (ws/new "ws"
                    #((var ws-receive) %)
                    #((var ws-open))
                    #((var ws-close))))


(defn main-component []
  [:div.ui.container
   [:div.ui.fluid.action.input
    [:input {:type "text" :placeholder "Json..."
             :value (:input @state)
             :on-change #(swap! state assoc :input (-> % .-target .-value))}]
    [:div.ui.button {:on-click #(ws/send ws (:input @state))} "Send"]]
   (for [m (:messages @state)]
     (try [render-clojure (js/JSON.parse m)]
          (catch js/Error e [:div m])))])


(r/render [main-component] (js/document.getElementById "app"))
