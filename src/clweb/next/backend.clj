(ns clweb.next.backend
  (:require
   [org.httpkit.server :refer [run-server with-channel]]
   [compojure.handler :refer [site]]
   [compojure.route :refer [not-found resources]]
   [compojure.core :refer [GET routes]]
   [ring.util.response :refer [resource-response]]
   [ring.middleware.cljsjs :refer [wrap-cljsjs]]
   ))

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

(server :port 8080)

(defn start [{:keys [::port] :as server}]
  (-> server
      (update ::stop (fn [stop] (if (nil? stop)
                                 (run-server (site (create-routes server)) {:port port})
                                 stop)))))

(defn stop [server]
  (-> server
      (update ::stop (fn [stop] (when (some? stop)
                                  (stop))))))
