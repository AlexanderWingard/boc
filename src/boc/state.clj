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
(def state {:users [{:id 1 :username "andrej"} {:id 2 :username "alex" :password "apa"}]})

(defn user-by-name [state name]
  (s/select-one [:users s/ALL (s/selected? [:username (s/pred= name)])] state))

(defn validate-user [state username _error]
  (if-some [user (user-by-name state username)]
    s/NONE
    (str "No user " username " found")))

(defn validate-password [state username password _error]
  (let [user (user-by-name state username)]
    (cond
      (nil? user) s/NONE
      (= password (:password user)) s/NONE
      :else (str "Wrong password for user " username ))))

(defn login [state uuid]
  (->> state
       (s/multi-transform [(s/collect-one s/STAY)
                           (data-path uuid)
                           (s/multi-path
                            [(s/collect-one [:username :value]):password (s/collect-one :value) :error (s/terminal validate-password)]
                            [:username (s/collect-one :value) :error (s/terminal validate-user)]
                            [(s/collect[(s/submap [:username :password]) s/MAP-VALS :error #(some? %)])
                             :login :error (s/terminal (fn [_ errors _] (if (empty? errors) s/NONE errors)))])])))

(->> {:session "random-uuid",
     :seq-nr 22,
     :username {:value "dddasd", :error "apa"},
     :password {:error 9, :value "dadssdddd"}}
     (s/transform [
                   (s/collect[(s/submap [:username :password]) s/MAP-VALS :error #(some? %)])
                   :login :error]
                  (fn [errors _] errors)))

(defn leave [state channel]
  (s/setval [:sessions s/MAP-VALS :channels (s/subset #{channel})] #{} state))

(defn deep-merge [a b]
  (if (and (map? a) (map? b))
    (merge-with deep-merge a b)
    b))

(defn logout [state uuid ]
  (s/setval [(data-path uuid) :user] s/NONE state))

(defn update-data [state uuid data]
  (s/transform (data-path uuid) #(deep-merge % data) state))

(defn broadcast [state uuid cb]
  (let [[data channels] (s/select-one [(session-path uuid) (s/collect-one :data) :channels] state)
        string (pr-str data)]
    (doseq [c channels] (cb c string)))
  state)

(defn handle-intent [state intent channel session]
  (case intent
    :join-session (join-session state channel session)
    :login (login state session)
    state))
