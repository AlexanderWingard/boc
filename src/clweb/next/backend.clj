(ns clweb.next.backend
  (:require
   [axw.keyword-constructor :as kwc]
   [clojure.edn :as edn]
   [org.httpkit.server :refer [run-server with-channel send! on-close on-receive]]
   [compojure.handler :refer [site]]
   [compojure.route :refer [not-found resources]]
   [compojure.core :refer [GET routes]]
   [ring.util.response :refer [resource-response]]
   [ring.middleware.cljsjs :refer [wrap-cljsjs]]
   ))

(defn ws-handler [server req]
  (with-channel req channel
    (when-some [cb (::on-connect server)]
      (cb channel (::state server)))
    (when-some [cb (::on-close server)]
      (on-close channel (fn [status] (cb channel (::state server)))))
    (when-some [cb (::on-msg server)]
      (on-receive channel (fn [string] (cb channel (::state server) (edn/read-string string)))))))

(defn create-routes [server]
  (routes
   (GET "/" [] (resource-response "main.html" {:root "public"}))
   (GET "/ws" [] (partial ws-handler server))
   (wrap-cljsjs (resources "/"))
   (not-found "Page not found")))

(defn server [ & {:as args}]
  (kwc/create args
              #{::port ::on-connect ::on-close ::on-msg}
              #{::port}
              {::state (atom nil)}))

(defn start [{:keys [::port] :as server}]
  (-> server
      (update ::stop (fn [stop] (if (nil? stop)
                                 (run-server (site (create-routes server)) {:port port})
                                 stop)))))

(defn stop [server]
  (-> server
      (update ::stop (fn [stop] (when (some? stop)
                                  (stop))))))
