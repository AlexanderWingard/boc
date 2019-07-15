(ns boc.fe.components.accounts
  (:require
   [boc.fe.components.main-frame :as main-frame]
   [boc.fe.components.general :as gc]
   [boc.fe.state :as state]))

(defn add-account-form []
  [gc/modal
   [:div.header "Create new account"]
   [:div.ui.form {:style {:margin "20px"}}
    [gc/input-field :account-name "Name"]]
   [:div.actions
    [:div.ui.right.labeled.icon.button {:on-click #(state/update-state-field [:add-account-view] nil)} "Cancel" [:i.x.icon]]
    [:div.ui.green.right.labeled.icon.button {:on-click #(state/send-intent :accounts/add)} "Save" [:i.save.icon]]]])

(defn add-transaction-form []
  [gc/modal
   [:div.header "Add transaction to " (:accounts/edit @state/state)]
   [:div.ui.form {:style {:margin "20px"}}
    [gc/input-field :transaction-name "Name"]
    [gc/calendar-input-field :transaction-date "Date"]
    [gc/input-field :transaction-amount "Amount"]]
   [:div.actions
    [:div.ui.right.labeled.icon.button {:on-click #(state/update-state-field [:add-transaction-view] nil)} "Cancel" [:i.x.icon]]
    [:div.ui.green.right.labeled.icon.button {:on-click #(state/send-intent :transactions/add)} "Save" [:i.save.icon]]]])

(defn delete-account-form [account]
  [gc/modal
   [:div.header "Delete account " (:name account)]
   [:div.actions
    [:div.ui.right.labeled.icon.button {:on-click #(state/update-state-field [:delete-account-view] nil)} "Cancel" [:i.x.icon]]
    [:div.ui.red.right.labeled.icon.button {:on-click #(state/send-intent :accounts/delete {:accounts/delete-id (:id account)})} "Delete" [:i.trash.icon]]]])

(defn component []
  [main-frame/component
   [:div
    (if (:add-account-view @state/state)
      [add-account-form]
      [gc/state-href [:add-account-view] true "Add new account"])
    [:br]
    [:div.ui.segment
     [:div.ui.relaxed.divided.list
      (for [account (:accounts @state/state)]
        ^{:key (:id account)}[:div.item
         [:div.content
          [:a.header {:on-click #(state/update-state-field [:accounts/edit] (:id account))} (:name account)]
          (when (= (:id account) (:accounts/edit @state/state))
            [:div
             (when (:delete-account-view @state/state)
               [delete-account-form account])
             (gc/state-href [:delete-account-view] true "delete")])]])]]
    (when (:accounts/edit @state/state)
      [:div ;;"List transactions for " (:accounts/edit @state/state) "here"
       [:table.ui.collapsing.table
        [:thead
         [:tr
          [:th "Date"]
          [:th "Comment"]
          [:th "Amount"]]]
        [:tbody
         (for [transaction (:transactions @state/state)]
           [:tr
            [:td (:date transaction)]
            [:td (:name transaction)]
            [:td (:amount transaction)]])]]
       (when (:add-transaction-view @state/state)
         [add-transaction-form])
       (gc/state-href [:add-transaction-view] true "Add transaction")])
    [gc/state-href [:view] :main "Back"]]])
