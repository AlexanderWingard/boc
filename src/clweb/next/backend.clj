(ns clweb.next.backend
  (:require
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

(defn kw-constructor [args & [known required internal]]
  (let [req (if (= required :all) known required)]
    (reduce
     (fn [acc kw]
       (let [ukw (keyword (name kw))
             v (get args ukw)]
         (if (contains? req kw)
           (assert (some? v) (str "Missing " ukw)))
         (-> acc
             (dissoc ukw)
             (assoc kw v)))) (merge internal args) known)))

(defn server [ & {:as args}]
  (kw-constructor args
                  #{::port ::on-connect ::on-close ::on-msg}
                  #{::port}
                  {::state (atom nil)}))

(def s (server :port 1986
               :on-connect
               (fn [channel state]
                 (swap! state assoc-in [::sessions channel] nil))

               :on-close
               (fn [channel state]
                 (swap! state update-in [::sessions] dissoc channel))

               :on-msg
               (fn [channel state msg])))

(defn start [{:keys [::port] :as server}]
  (-> server
      (update ::stop (fn [stop] (if (nil? stop)
                                 (run-server (site (create-routes server)) {:port port})
                                 stop)))))

(defn stop [server]
  (-> server
      (update ::stop (fn [stop] (when (some? stop)
                                  (stop))))))
