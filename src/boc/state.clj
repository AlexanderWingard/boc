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
                  [s/ALL (s/selected? [:data :session (s/pred= uuid)]) (s/multi-path [:channels s/NIL->SET (s/subset #{channel})(s/terminal-val #{channel})]
                                                                                     [(s/collect-one :channels) :data :clients (s/terminal (fn [channels _] (count channels)))])]
                  )]
   state))
(def state {:users [{:id 1 :username "andrej"} {:id 2 :username "alex" :password "apa"}]})

(defn user-by-name [state name]
  (s/select-one [:users s/ALL (s/selected? [:username (s/pred= name)])] state))

(defn login [state uuid]
  (let [user-state (s/select-one (data-path uuid) state)
        username (s/select-one [:username :value] user-state)
        password (s/select-one [:password :value] user-state)
        found-user (user-by-name state username)
        correct (and (some? found-user) (= password (:password found-user)))]
    (s/multi-transform
     [(data-path uuid)
      (s/multi-path
       [:username :error
        (s/terminal
         (fn [_] (if (nil? found-user) (str "User " username " not found") s/NONE)))]
       [:password :error
        (s/terminal
         (fn [_] (if (and (some? found-user) (not correct)) (str "Wrong password for user " username) s/NONE)))]
       [:login :error
        (s/terminal
         (fn [_] (if (or (nil? found-user) (not correct)) true s/NONE)))]
       [:private :user (s/terminal (fn [_] (if correct found-user s/NONE)))])]
     state)))

(defn leave [state channel]
  (s/multi-transform [:sessions s/MAP-VALS (s/multi-path
                                            [:channels (s/subset #{channel}) (s/terminal-val #{})]
                                            [(s/collect-one :channels) :data :clients (s/terminal (fn [channels _] (count channels)))])]
                     state))

(defn deep-merge [a b]
  (if (and (map? a) (map? b))
    (merge-with deep-merge a b)
    b))

(defn logout [state uuid ]
  (s/setval [(data-path uuid) :user] s/NONE state))

(defn update-data [state uuid data]
  (s/transform (data-path uuid) #(deep-merge % data) state))

(defn data-and-channels [state]
  (s/select [:sessions s/ALL (s/collect-one :data) :channels] state))

(defn handle-intent [state intent channel session]
  (case intent
    :join-session (join-session state channel session)
    :login (login state session)
    :leave (leave state channel)
    state))

(defn handle-msg [state channel msg]
  (let [session (or (:session msg) (rand-nth ["default-1" "default-2"]))
        intent (:intent msg)
        msg (dissoc msg :session :intent :private)]
    (-> state
        (update-data session msg)
        (handle-intent intent channel session))))
