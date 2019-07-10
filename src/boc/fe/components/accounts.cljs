(ns boc.fe.components.accounts
  (:require
   [boc.fe.components.main-frame :as main-frame]
   [boc.fe.components.general :as gc]
   [boc.fe.state :as state]))

(defn add-account-form []
  [:div.ui.modal
   [:div.header "Create new account"]
   [:div.ui.form {:style {:margin "20px"}}
    [gc/input-field :account-name "Name"]]
   [:div.actions
    [:div.ui.right.labeled.icon.button {:on-click #(state/update-state-field [:add-account-view] nil)} "Cancel" [:i.x.icon]]
    [:div.ui.green.right.labeled.icon.button {:on-click #(state/send-intent :accounts/add)} "Save" [:i.save.icon]]]])

(defn delete-account-form [account]
  [:div.ui.modal
   [:div.header "Delete account " (:name account)]
   [:div.actions
    [:div.ui.right.labeled.icon.button {:on-click #(state/update-state-field [:delete-account-view] nil)} "Cancel" [:i.x.icon]]
    [:div.ui.red.right.labeled.icon.button {:on-click #(state/send-intent :accounts/delete {:accounts/delete-id (:id account)})} "Delete" [:i.trash.icon]]]])

(defn component []
  [main-frame/component
   [:div
    (if (:add-account-view @state/state)
      [gc/modal [add-account-form]]
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
               [gc/modal [delete-account-form account]])
             (gc/state-href [:delete-account-view] true "delete")])]])]]
    [gc/state-href [:view] :main "Back"]]])
