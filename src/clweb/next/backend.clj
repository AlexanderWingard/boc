(ns clweb.next.backend
  (:require
   [org.httpkit.server :refer [run-server with-channel]]
   [compojure.handler :refer [site]]
   [compojure.route :refer [not-found resources]]
   [compojure.core :refer [GET routes]]
   [ring.util.response :refer [resource-response]]
   [ring.middleware.cljsjs :refer [wrap-cljsjs]]
   ))

(defrecord Server [stop port])

(defn ws-handler [server req]
  (with-channel req channel
    ;; (swap! channels conj channel)
    ;; (broadcast @state)
    ;; (on-close channel (fn [status] (swap! channels disj channel)))
    ;; (on-receive channel (partial (var ws-receive) channel))
    ))

(defn create-routes [server]
  (routes
   (GET "/" [] (resource-response "main.html" {:root "public"}))
   (GET "/ws" [] (partial ws-handler server))
   (wrap-cljsjs (resources "/"))
   (not-found "Page not found")))

(defn server [port]
  (map->Server {:port port}))

(defn start [server]
  (-> server
      (assoc :stop (run-server (site (create-routes server)) {:port (:port server)}))))

(defn stop [server]
  ((:stop server))
  (-> server
      (assoc :stop nil)))
