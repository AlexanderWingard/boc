(ns boc.server
  (:require [clojure.edn :as edn]
            [clojure.string :refer [upper-case]]
            [compojure.core :refer [defroutes GET]]
            [compojure.handler :refer [site]]
            [compojure.route :refer [not-found resources]]
            [org.httpkit.server :refer [on-close on-receive send! run-server with-channel]]
            [ring.util.response :refer [resource-response response]]
            [ring.middleware.cljsjs :refer [wrap-cljsjs]]))

(defonce channels (atom #{}))
(defonce state (atom {}))
(def stop-server (atom nil))
; (broadcast (reset! state (select-keys @state [:seq-nr]))) 

(defn broadcast [data]
  (let [s (pr-str data)]
    (doseq [c @channels] (send! c s))))

(defn deep-merge [a b]
  (if (map? a)
    (merge-with deep-merge a b)
    b))

(defn handle-intent [data]
  (case (:intent data)
    "login" (-> data
                (assoc-in [:username :error] "Wrong username")
                (assoc-in [:login :error] true)
                (dissoc :intent))
    data))

(defn ws-receive [channel string]
  (broadcast
   (swap! state #(-> %
                     (deep-merge (edn/read-string string))
                     (handle-intent)))))

(defn ws-handler [req]
  (with-channel req channel
    (swap! channels conj channel)
    (broadcast @state)
    (on-close channel (fn [status] (swap! channels disj channel)))
    (on-receive channel (partial (var ws-receive) channel))))

(defroutes routes
  (GET "/" [] (resource-response "main.html" {:root "public"}))
  (GET "/ws" [] ws-handler)
  (wrap-cljsjs (resources "/"))
  (not-found "Page not found"))

(defn -main
  [& args]
  (reset! stop-server (run-server (site (var routes)) {:port 8080}))
  (println "http://localhost:8080"))
