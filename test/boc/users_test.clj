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
(def data-assert (partial mk-data-assert "uuid"))
(def set-value (partial mk-set-value "uuid"))
(def set-data (partial mk-set-data "uuid"))
(def do-login #(users/login % "uuid"))
(def do-register #(users/register % "uuid"))

(deftest s-test
  (s-assert {:a {:aa nil}} [:a :aa] nil))

(deftest case-test
  (-> initial
      (users/user-by-name "AleX")
      (s-assert [:id] 1)))

(deftest login
  (-> initial
      (set-data [:view] :login)
      (set-value :username "andrej")
      (do-login)
      (error-assert :username "User andrej not found")
      (error-assert :password nil)

      (set-value :username "alex")
      (do-login)
      (error-assert :password "Wrong password for user alex")
      (data-assert [:view] :login)

      (set-value :password "123")
      (do-login)
      (error-assert :username nil)
      (error-assert :password nil)
      (data-assert [:private :user] {:id 1 :username "alex"})
      (data-assert [:view] :main)

      (set-value :password "1234")
      (do-login)
      (data-assert [:private :user] nil)))

(deftest logout-test
  (-> initial
      (set-data [:private :user] {:id 1 :username "alex"})
      (data-assert [:private :user] {:id 1 :username "alex"})
      (users/logout "uuid")
      (data-assert [:view] :login)))

(deftest allowed-views-test
  (-> initial
      (set-data [:view] :accounts)
      (users/ensure-allowed-view "uuid")
      (data-assert [:view] :login)

      (set-data [:view] :register)
      (users/ensure-allowed-view "uuid")
      (data-assert [:view] :register)

      (set-value :username "alex")
      (set-value :password "123")
      (do-login)

      (set-data [:view] :any)
      (users/ensure-allowed-view "uuid")
      (data-assert [:view] :any)))

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
