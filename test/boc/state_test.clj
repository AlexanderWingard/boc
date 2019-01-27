(ns boc.state-test
  (:require
   [boc.be.state.users :as users]
   [boc.be.state.paths :as paths]
   [clojure.test :as t :refer [deftest is testing]]
   [boc.test-util :refer :all]
   [boc.be.state.paths :as paths]))

(def initial {:users [{:username "alex" :password "123"}]
              :sessions [{:data {:session "uuid"}}]})
(def error-assert (partial mk-error-assert "uuid"))
(def set-value (partial mk-set-value "uuid"))
(def do-login #(users/login % "uuid"))
(def do-register #(users/register % "uuid"))

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
      ))

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
      (apply-assert users/user-by-name "andrej" {:username "andrej" :password "123"})
      ))
