(ns boc.be.state.accounts
  (:require
   [boc.be.state.paths :as paths]
   [boc.be.state.users :as users]
   [com.rpl.specter :as s]
   [boc.be.state.util :refer :all]
   ))

(defn view [state session]
  (s/transform [(s/collect-one [:accounts])
                (paths/data session) :accounts]
               (fn [accounts old]
                 accounts)
               state))

(defn delete-account  [state session]
  (let [id (s/select-one [(paths/data session) :accounts/delete-id] state)]
    (s/multi-transform
     (s/multi-path
      [:accounts s/ALL (s/selected? :id (s/pred= id)) (s/terminal-val s/NONE)]
      [(paths/data session) :accounts/delete-id (s/terminal-val s/NONE)])
     state)))

(defn add-account [state session]
  (let [fields [:account-name]
        {:keys [account-name]} (field-values fields state (paths/data session))]
    (s/multi-transform
     (s/multi-path
      [(paths/data session)
       (s/multi-path
        [:account-name :error
         (validate (cond (empty? account-name)
                         (str "Please supply account name")))]
        (field-errors fields :accounts/add))]
      [(s/if-path [(paths/data session) :accounts/add :error #(empty? %)]
                  (s/multi-path
                   [:accounts s/NONE-ELEM (s/terminal-val {:id (uuid)
                                                           :name account-name
                                                           :owner (users/current state session)})]
                   [(paths/data session) (s/multi-path
                                          [:account-name (s/terminal-val s/NONE)]
                                          [:add-account-view (s/terminal-val s/NONE)])]
                   ))]
      )
     state)))
