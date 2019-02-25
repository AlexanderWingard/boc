(ns boc.fe.components.main
  (:require
   [boc.fe.components.general :as gc]
   ))

(defn component []
  [gc/intent-button :logout "Logout"])

