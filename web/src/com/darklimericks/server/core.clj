(ns com.darklimericks.server.core
  (:gen-class)
  (:require [integrant.core :as ig]
            [reitit.coercion]
            [reitit.coercion.spec]
            [taoensso.timbre :as timbre]
            [clojure.java.io :as io]
            [org.httpkit.server :as kit]))

(defmethod ig/init-key ::server [_ {:keys [handler] :as opts}]
  (timbre/info (format "Starting server on port %d" (:port opts)))
  (kit/run-server handler (dissoc opts :handler)))

(defmethod ig/halt-key! ::server [_ server]
  (timbre/info "Stopping server.")
  (server))


(defn -main []
  (try
    (let [config (->> "server/config.edn"
                      io/resource
                      slurp
                      ig/read-string)
          _ (ig/load-namespaces config)
          system (-> config
                     ig/prep
                     ig/init)]
      (timbre/info "Running with config: server/config.edn" )
      system)
    (catch Throwable e
      (.printStackTrace e)
      (System/exit 1))))

(comment
  (def m (-main))
  (->> "server/config.edn"
       io/resource
       slurp
       ig/read-string
       ig/load-namespaces
       )
  )
