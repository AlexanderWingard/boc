(ns boc.be.state.users
  (:require
   [boc.be.state.paths :as paths]
   [com.rpl.specter :as s]
   [clojure.string :refer [lower-case trim]]
   ))

(defn trim-lower-compare [a b]
  (= (lower-case (trim a))
     (lower-case (trim b))))

(defn user-by-name [state name]
  (s/select-one [:users s/ALL (s/selected? [:username #(trim-lower-compare % name)])] state))

(defn field-values [fields state session-path]
  (->> state
       (s/select [session-path (s/submap fields) s/ALL (s/collect-one s/FIRST) s/LAST :value])
       (into {})))

(defn field-errors [fields key]
  [(s/collect-one (s/submap fields))
   key :error (s/terminal (fn [data _] (s/select [s/MAP-VALS :error #(some? %)] data)))])

(defmacro validate [& body]
  `(s/terminal (fn [prev#] (do ~@body))))

(defn ensure-allowed-view [state session]
  (s/transform [(paths/data session) (s/collect-one [:private :user]) :view]
               (fn [user old]
                 (cond
                   (some? user) old
                   (contains? #{:login :register} old) old
                   :else :login))
               state))

(defn login [state session]
  (let [fields [:username :password]
        {:keys [username password]} (field-values fields state (paths/data session))
        found-user (user-by-name state username)
        correct (and (some? found-user) (= password (:password found-user)))]
    (s/multi-transform
     [(paths/data session)
      (s/multi-path
       [:username :error
        (validate (cond (nil? found-user)
                        (str "User " username " not found")))]
       [:password :error
        (validate (cond (and (some? found-user) (not correct))
                        (str "Wrong password for user " username)))]
       (field-errors fields :login)
       [:private :user
        (validate (cond correct
                        (select-keys found-user [:id :username])))]
       [:view (s/terminal (fn [prev] (if correct :main prev)))])]
     state)))

(defn logout [state session]
  (s/setval (paths/data session) {:session session :view :login} state))

(defn register [state session]
  (let [fields [:username :password :password-repeat]
        {:keys [username password password-repeat]} (field-values fields state (paths/data session))]
    (s/multi-transform
     (s/multi-path
      [(paths/data session)
       (s/multi-path
        [:username :error
         (validate (cond (empty? username)
                         (str "Please supply username")
                         (some? (user-by-name state username))
                         (str "User " username " already exists")))]
        [:password :error
         (validate (cond (empty? password)
                         (str "Please supply password")))]
        [:password-repeat :error
         (validate (cond (not= password password-repeat)
                         (str "Passwords must match")))]
        (field-errors fields :register))]
      [(s/if-path [(paths/data session) :register :error #(empty? %)]
                  (s/multi-path
                   [:users s/NONE-ELEM (s/terminal-val {:username username :password password})]
                   [(paths/data session) (s/multi-path [:private :user :username (s/terminal-val username)]
                                                       [:view (s/terminal-val :main)])]))])
     state)))
