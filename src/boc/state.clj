(ns boc.state
  (:require [com.rpl.specter :as s]))

(defn log [arg]
  (println arg)
  arg)

(defn session-path [uuid]
  [:sessions s/ALL (s/selected? [:data :session (s/pred= uuid)])])

(defn channels-path [uuid]
  [(session-path uuid) :channels s/ALL])

(defn data-path [uuid]
  [(session-path uuid) :data])

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

(defn leave [state channel]
  (s/setval [:sessions s/MAP-VALS :channels (s/subset #{channel})] #{} state))

(defn deep-merge [a b]
  (if (and (map? a) (map? b))
    (merge-with deep-merge a b)
    b))

(defn login [state uuid user]
  (s/setval [(data-path uuid) :user] user state))

(defn logout [state uuid ]
  (s/setval [(data-path uuid) :user] s/NONE state))

(defn update-data [state uuid data]
  (s/transform (data-path uuid) #(deep-merge % (dissoc data :session :user)) state))

(defn broadcast [state uuid]
  (s/select-one [(session-path uuid) (s/collect-one :data) :channels] state))
