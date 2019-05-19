(ns boc.be.state.sessions
  (:require
   [com.rpl.specter :as s]
   ))

(defn join [state channel session]
  (s/multi-transform
   [:sessions
    s/NIL->VECTOR
    (s/multi-path [(s/cond-path (s/not-selected? [s/ALL :data :session (s/pred= session)])
                                [s/AFTER-ELEM (s/terminal-val {:data {:session session}})])]
                  [s/ALL :channels s/NIL->SET (s/subset #{channel}) (s/terminal-val #{})]
                  [s/ALL (s/selected? [:data :session (s/pred= session)]) (s/multi-path [:channels s/NIL->SET (s/subset #{channel})(s/terminal-val #{channel})]
                                                                                     [(s/collect-one :channels) :data :clients (s/terminal (fn [channels _] (count channels)))])]
                  )]
   state))

(defn leave [state channel]
  (s/multi-transform [:sessions s/MAP-VALS (s/multi-path
                                            [:channels (s/subset #{channel}) (s/terminal-val #{})]
                                            [(s/collect-one :channels) :data :clients (s/terminal (fn [channels _] (count channels)))])]
                     state))

(defn data-and-channels [state]
  (reduce (fn [acc [session data channels]]
            (assoc acc session {:data data :channels channels}))
          {}
          (s/select [:sessions s/ALL
                     (s/collect-one [:data :session])
                     (s/collect-one :data)
                     :channels] state)))
