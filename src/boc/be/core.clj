(ns boc.be.core
  (:require
   [boc.be.state.core :as state]
   [axw.ws-server :as server]
   [axw.deep :refer [deep-merge deep-diff deep-diff-2]]
   [clojure.pprint :refer [pprint]]
   ))

(defn broadcast [[old new] msg from]
  (let [state-str (with-out-str (pprint new))
        old-data (state/data-and-channels old)]
    (doseq [[session {:keys [data channels]}] (state/data-and-channels new)]
      (let [data (assoc data :debug state-str)]
        (doseq [c channels]
          (->> (if (and (= c from) (= :join-session (:intent msg)))
                 data
                 (let [diff (deep-diff-2 (get-in old-data [session :data])
                                         data)]
                   (if (= c from)
                     (deep-diff msg diff)
                     diff)))
               (pr-str)
               (server/send! c)))))))

(defn on-msg [channel state msg]
  (-> state
      (swap-vals! state/handle-msg channel msg)
      (broadcast msg channel)))

(defn on-close [channel state]
  (on-msg channel state {:intent :leave-session}))

(defonce state (atom {:users [
                              {:id 1 :username "andrej" :password "123"}
                              {:id 2 :username "alex" :password "123"}
                              ]}))

(defn -main [& args]
  (-> (server/new :port 8080 :on-msg (var on-msg) :on-close (var on-close) :state state)
      (server/start)
      (server/print-url)))
