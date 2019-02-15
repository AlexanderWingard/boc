(ns boc.users-test
  (:require
   [boc.be.state.users :as users]
   [boc.be.state.paths :as paths]
   [clojure.test :as t :refer [deftest is testing]]
   [boc.test-util :refer :all]
   [boc.be.state.paths :as paths]
   [axw.deep :as deep]))

(def initial {:users [{:id 1 :username "alex" :password "123"}]
              :sessions [{:data {:session "uuid"}}]})
(def error-assert (partial mk-error-assert "uuid"))
(def set-value (partial mk-set-value "uuid"))
(def do-login #(users/login % "uuid"))
(def do-register #(users/register % "uuid"))

(deftest s-test
  (s-assert {:a {:aa nil}} [:a :aa] nil))

(deftest apa
  (let [a {:intent :login, :session "default-1"}
        b {:session "default-1",
           :clients 1,
           :seq-nr 16,
           :username {:value "alex", :error nil},
           :password {:error "Wrong password for user alex"},
           :login {:error ["Wrong password for user alex"]},
           :private {:user {:id 2, :username "alex", :password "123"}}}]
    (is (= (dissoc b :session) (deep/deep-diff a b)))))

(deftest login
  (-> initial
      (set-value :username "andrej")
      (do-login)
      (error-assert :username "User andrej not found")
      (error-assert :password nil)

      (set-value :username "alex")
      (do-login)
      (error-assert :password "Wrong password for user alex")

      (set-value :password "123")
      (do-login)
      (error-assert :username nil)
      (error-assert :password nil)
      (s-assert [(paths/data "uuid") :private :user] {:id 1 :username "alex"})

      (set-value :password "1234")
      (do-login)
      (s-assert [(paths/data "uuid") :private :user] nil)))

(deftest allowed-views-test
  (-> initial
      (s-setval [(paths/data "uuid") :view] "accounts")
      (users/ensure-allowed-view "uuid")
      (s-assert [(paths/data "uuid") :view] "login")

      (s-setval [(paths/data "uuid") :view] "register")
      (users/ensure-allowed-view "uuid")
      (s-assert [(paths/data "uuid") :view] "register")

      (set-value :username "alex")
      (set-value :password "123")
      (do-login)

      (s-setval [(paths/data "uuid") :view] "any")
      (users/ensure-allowed-view "uuid")
      (s-assert [(paths/data "uuid") :view] "any")))

(deftest register
  (-> initial
      (do-register)
      (error-assert :username "Please supply username")

      (set-value :username "alex")
      (do-register)
      (error-assert :username "User alex already exists")

      (set-value :username "andrej")
      (do-register)
      (error-assert :password "Please supply password")
      (error-assert :username nil)

      (set-value :password "123")
      (do-register)
      (error-assert :password-repeat "Passwords must match")
      (error-assert :register ["Passwords must match"])

      (set-value :password-repeat "123")
      (do-register)
      (error-assert :register [])
      (apply-assert users/user-by-name "andrej" {:username "andrej" :password "123"})))
