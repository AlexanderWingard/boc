(ns boc.fe.components.main
  (:require
   [boc.fe.components.main-frame :as main-frame]
   [boc.fe.components.general :as gc]
   ))

(defn component []
  [main-frame/component
   "Welcome"])
