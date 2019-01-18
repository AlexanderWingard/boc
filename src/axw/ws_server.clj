(ns axw.ws-server
  (:require
   [axw.keyword-constructor :as kwc]
   [clojure.edn :as edn]
   [compojure.core :refer [GET routes]]
   [compojure.handler :refer [site]]
   [compojure.route :refer [not-found resources]]
   [org.httpkit.server :refer [run-server with-channel on-close on-receive]]
   [ring.middleware.cljsjs :refer [wrap-cljsjs]]
   [ring.util.response :refer [resource-response]]
   ))

(def send! org.httpkit.server/send!)

(defn ws-handler [server req]
  (with-channel req channel
    (when-some [cb (::on-connect @server)]
      (cb channel (::state @server)))
    (when-some [cb (::on-close @server)]
      (on-close channel (fn [status] (cb channel (::state @server)))))
    (when-some [cb (::on-msg @server)]
      (on-receive channel (fn [string] (cb channel (::state @server) (edn/read-string string)))))))

(defn create-routes [server]
  (routes
   (GET "/" [] (resource-response "main.html" {:root "public"}))
   (GET "/ws" [] (partial ws-handler server))
   (wrap-cljsjs (resources "/"))
   (not-found "Page not found")))

(defn server [ & {:as args}]
  (atom (kwc/create args
                    #{::port ::on-connect ::on-close ::on-msg}
                    #{::port}
                    {::state (atom nil)})))

(defn start [s]
  (swap! s update ::stop (fn [stop] (if (nil? stop)
                                      (run-server (site (create-routes s)) {:port (::port @s)})
                                      stop))))

(defn stop [s]
  (swap! s update ::stop (fn [stop] (when (some? stop)
                              (stop)))))
