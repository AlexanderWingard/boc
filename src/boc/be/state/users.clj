(ns boc.be.state.users
  (:require
   [boc.be.state.paths :as paths]
   [com.rpl.specter :as s]
   ))

(defn user-by-name [state name]
  (s/select-one [:users s/ALL (s/selected? [:username (s/pred= name)])] state))

(defn login [state session]
  (let [user-state (s/select-one (paths/data session) state)
        username (s/select-one [:username :value] user-state)
        password (s/select-one [:password :value] user-state)
        found-user (user-by-name state username)
        correct (and (some? found-user) (= password (:password found-user)))]
    (s/multi-transform
     [(paths/data session)
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

(defn field-values [fields state session-path]
  (->> state
       (s/select [session-path (s/submap fields) s/ALL (s/collect-one s/FIRST) s/LAST :value])
       (into {})))

(defn field-errors [fields key]
  [(s/collect-one (s/submap fields))
   key :error (s/terminal (fn [data _] (s/select [s/MAP-VALS :error #(some? %)] data)))])

(defmacro validate [& body]
  `(s/terminal (fn [prev#] (or (do ~@body) s/NONE))))

(defn register [state session]
  (let [fields [:username :password :password-repeat]
        {:keys [username password password-repeat]} (field-values fields state (paths/data session))]
    (s/multi-transform
     (s/multi-path
      [(paths/data session)
       (s/multi-path
        [:username :error
         (validate (cond (empty? username) "Please supply username"
                         (some? (user-by-name state username)) (str "User " username " already exists")))]
        [:password :error
         (validate (cond (empty? password) "Please supply password"))]
        [:password-repeat :error
         (validate (cond (not= password password-repeat) "Passwords must match"))]
        (field-errors fields :register))]
      [(s/if-path [(paths/data session) :register :error #(empty? %)] [:users s/NONE-ELEM (s/terminal-val {:username username :password password})])])
     state)))
