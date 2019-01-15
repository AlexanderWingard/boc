(ns boc.state
  (:require [com.rpl.specter :as s]))

(defn log [arg]
  (println arg)
  arg)

(defn join-session [state channel uuid]
  (s/multi-transform
   [:sessions
    s/NIL->VECTOR
    (s/multi-path [(s/cond-path (s/not-selected? [s/ALL :data :session (s/pred= uuid)])
                                [s/AFTER-ELEM (s/terminal-val {:data {:session uuid}})])]
                  [s/ALL :channels s/NIL->SET (s/subset #{channel}) (s/terminal-val #{})]
                  [s/ALL (s/selected? [:data :session (s/pred= uuid)]) :channels s/NIL->SET (s/subset #{channel})(s/terminal-val #{channel})]
                  )]
   state))

(-> {}
    (log)
    (join-session "chan" "uuid")
    (log)
    (join-session "chan" "uuid")
    (log)
    (join-session "chan" "uuid2")
    (log))

(defn leave [state channel]
  (s/setval [:sessions s/MAP-VALS :channels (s/subset #{channel})] #{} state))

(defn update-data [state session data]
  (s/setval [:sessions session :data] data state))

(defn broadcast [state session]
  (s/select-one [:sessions session (s/collect-one :data) :channels] state)
  state)

(defn test [state session]
  (s/select [:sessions s/MAP-VALS :data #(= (:session %) session)] state))



(s/setval [:sessions  s/ALL :data :session (s/pred= 5) (s/nil->val 20)]
          10
          {:sessions [{:data {:session 1}}{:data {:session 2}}]})

(s/multi-transform
 [(s/if-path [s/ALL (s/pred= :a)]
           [s/ALL (s/pred= :a) (s/terminal-val :c)]
           [s/AFTER-ELEM (s/terminal-val :DEFAULT)]
           )]
 [:a :d :a])
