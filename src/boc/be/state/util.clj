(ns boc.be.state.util
  (:require
   [com.rpl.specter :as s]
   ))

(defmacro validate [& body]
  `(s/terminal (fn [prev#] (do ~@body))))

(defn field-values [fields state session-path]
  (->> state
       (s/select [session-path (s/submap fields) s/ALL (s/collect-one s/FIRST) s/LAST :value])
       (into {})))

(defn field-errors [fields key]
  [(s/collect-one (s/submap fields))
   key :error (s/terminal (fn [data _] (s/select [s/MAP-VALS :error #(some? %)] data)))])

(defn uuid []
  (.toString (java.util.UUID/randomUUID)))
