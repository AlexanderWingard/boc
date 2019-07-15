(ns boc.be.state.accounts
  (:require
   [boc.be.state.paths :as paths]
   [boc.be.state.users :as users]
   [com.rpl.specter :as s]
   [boc.be.state.util :refer :all]
   ))

(defn view [state session]
  (s/multi-transform [(s/collect-one [:accounts])
                      (s/collect-one [:transactions])
                      (paths/data session)
                      (s/multi-path
                       [:accounts (s/terminal (fn [accounts _ _] accounts))]
                       [:transactions (s/terminal (fn [_ transactions _] transactions))])]
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

(defn read-num [s]
  (try (let [num (read-string s)]
         (if (number? num)
           num))
       (catch Exception e nil)))

(defn add-transaction [state session]
  (let [fields [:transaction-name :transaction-date :transaction-amount]
        {:keys [transaction-name transaction-date transaction-amount]} (field-values fields state (paths/data session))
        account (s/select-one [(paths/data session) :accounts/edit] state)]
    (s/multi-transform
     (s/multi-path
      [(paths/data session)
       (s/multi-path
        [:transaction-name :error
         (validate (cond (empty? transaction-name)
                         (str "Please give this transaction a name")))]
        [:transaction-date :error
         (validate (cond (empty? transaction-date)
                         (str "Please supply the transaction date")))]
        [:transaction-amount :error
         (validate (cond (empty? transaction-amount)
                         (str "Please supply the transaction amount")
                         (nil? (read-num transaction-amount))
                         (str "Not a valid number")))]
        (field-errors fields :transactions/add))]
      [(s/if-path [(paths/data session) :transactions/add :error #(empty? %)]
                  (s/multi-path
                   [:transactions s/NONE-ELEM (s/terminal-val {:id (uuid)
                                                               :account account
                                                               :name transaction-name
                                                               :date transaction-date
                                                               :amount transaction-amount})]
                   [(paths/data session) (s/multi-path
                                          [:add-transaction-view (s/terminal-val s/NONE)])])
                  )])
     state)))
