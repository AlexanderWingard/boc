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
