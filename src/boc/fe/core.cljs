(ns boc.fe.core
  (:require
   [cljs.reader :as reader]
   [axw.ws :as ws]
   [cljsjs.semantic-ui :as sem]
   [reagent.core :as r]
   [com.rpl.specter :as s]
   [clojure.walk :refer [prewalk-replace]]
   [json-html.core :as json-html]
   ))

(defonce state (r/atom {:history '() :history-item 0}))

(extend-protocol json-html/Render
  cljs.core/PersistentVector
  (render [this]
    (cond
      (empty? this)
      [:div.jh-type-object [:span.jh-empty-collection]]

      (every? number? this)
      [:table.jh-type-object {:style {:table-layout "fixed"}}
       [:tbody
        [:tr
         (for [n (range (count this))]
           [:th n])]
        [:tr
         (for [v this]
           [:td.jh-value.jh-array-value (json-html/render-html v)])]]]

      :else
      [:table.jh-type-object
       [:tbody
        (for [[i v] (map-indexed vector this)]
          [:tr [:th.jh-key.jh-array-key i]
           [:td.jh-value.jh-array-value (json-html/render-html v)
            ]])]])))

(declare ws)

(defn render-clojure [data]
  (-> (prewalk-replace
       {:table.jh-type-object :table.jh-type-object.ui.unstackable.selectable.celled.compact.table}
       (json-html/edn->hiccup (:data data)))
      (assoc 0 (cond
                 (not-empty (:acks data))
                 :table.jh-type-object.ui.unstackable.selectable.celled.compact.green.table

                 (:from-me data)
                 :table.jh-type-object.ui.unstackable.selectable.celled.compact.red.table

                 :default
                 :table.jh-type-object.ui.unstackable.selectable.celled.compact.blue.table))))

(defn log [& data]
  (apply js/console.log data))

(defn send-edn [data]
  (->> data
      (clj->js)
      (js/JSON.stringify)
      (ws/send ws)))

(defn send-input []
  (let [data (-> (:input @state)
                 (reader/read-string)
                 (assoc :id (str(random-uuid))))]
    (send-edn data)
    (swap! state #(-> %
                      (update :messages conj {:from-me true :data data})
                      (update :history conj (:input @state))
                      (assoc :history-item 0)
                      (assoc :input "")))))

(defn ws-receive [s]
  (let [data (-> s
                 (js/JSON.parse)
                 (js->clj :keywordize-keys true))]
    (when (some? (:ack data))
      (swap! state #(s/setval [:messages s/ALL (s/selected? :data :id (s/pred= (:ack data))) :acks s/NIL->SET s/NONE-ELEM] data %)))
    (when (not= "ack" (:msg data))
      (swap! state update :messages conj {:data data}))))

(defn ws-open [])

(defn ws-close [])

(defonce ws (ws/new "ws"
                    #((var ws-receive) %)
                    #((var ws-open))
                    #((var ws-close))))

(defn nav-history [step]
  (let [history (:history @state)
        len (count history)]
    (swap! state update :history-item (fn [old]
                                        (-> (+ old step)
                                            (max 0)
                                            (min len)))))
  (swap! state assoc :input
         (let [{:keys [history-item history]} @state]
           (if (= 0 history-item)
             ""
             (nth history (- history-item 1))))))

(defn main-component []
  [:div.ui.container
   [:div.ui.fluid.action.large.input
    [:input {:type "text" :placeholder "EDN..."
             :value (:input @state)
             :on-change #(swap! state assoc :input (-> % .-target .-value))
             :on-key-press (fn [e] (case (.-key e)
                                     "Enter" (send-input)
                                     nil))
             :on-key-down (fn [e] (case (.-key e)
                                    "ArrowUp" (nav-history -1)
                                    "ArrowDown" (nav-history 1)
                                    nil))}]
    [:div.ui.button {:on-click #(send-input)} "Send"]]
   (for [m (:messages @state)]
     [:div
      [:hr]
      [render-clojure m]])])

(r/render [main-component] (js/document.getElementById "app"))
