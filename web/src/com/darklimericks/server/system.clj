(ns com.darklimericks.server.system
  (:require [integrant.core :as ig]
            [taoensso.timbre :as timbre]
            [org.httpkit.server :as kit]
            [next.jdbc :as jdbc]
            [environ.core :refer [env]]
            [taoensso.carmine.message-queue :as car-mq]
            [reitit.http :as http]
            [reitit.ring :as ring]
            [reitit.coercion.spec]
            [reitit.interceptor.sieppari :as sieppari]
            [reitit.http.interceptors.parameters :refer [parameters-interceptor]]
            [com.darklimericks.server.handlers :as handlers]
            [com.darklimericks.server.limericks :as limericks]
            [com.darklimericks.server.interceptors :as interceptors]
            [com.darklimericks.server.db :as db]))

(def worker (atom nil))

(defmethod ig/init-key :worker/limerick-gen [_ {:keys [db kv]}]
  (when (nil? @worker)
    (reset!
     worker
     (car-mq/worker
      kv
      "limericks"
      {:handler
       (fn [{:keys [message attempt]}]
         (timbre/info "Received" message)
         (limericks/generate-limerick-worker db message)
         {:status :success})}))))

(defmethod ig/halt-key! :worker/limerick-gen [_ worker]
  #_(.stop worker))

(defmethod ig/prep-key :database.kv/connection [_ config]
  (let [pass (or (env :redis-pass) "dev")]
    (assoc-in
     config
     [:spec :uri]
     (format "redis://localhost:6379/"))))

;; Point of this? We aren't and maintaining a connection here.
;; I guess carmine does that behind the scenes with it's "pool"
;; option. But it's nice to have all boundary config in one place.
(defmethod ig/init-key :database.kv/connection [_ config]
  config)

(defmethod ig/prep-key :database.sql/connection [_ _]
  {:jdbcUrl (str "jdbc:postgresql://localhost:5432/?user="
                 (or (env :postgres-user)
                     "dev")
                 "&password="
                 (or (env :postgres-password)
                     "dev"))})

(defmethod ig/init-key :database.sql/connection [_ db-spec]
  (jdbc/get-datasource db-spec))

(defmethod ig/init-key :app/logging [_ config]
  (timbre/merge-config! config))

(defmethod ig/halt-key! :app/logging [_ _])

(defmethod ig/init-key :app/cache [_ _]
  (timbre/debug "Initializing cache.")
  db/db)

(defmethod ig/halt-key! :app/db [_ db]
  #_(reset! db {}))

(defmethod ig/init-key :app/router [_ {:keys [db cache]}]
  (let [routes [["/" {:name ::home
                      :get {:handler (handlers/home-handler db)}}]
                ["/{letter}.html"
                 {:name ::artists-by-letter
                  :handler (handlers/artists-by-letter db)}]
                ["/limerick-generation-task"
                 {:name ::limerick-generation-task
                  :post {:handler (handlers/limerick-generation-post-handler db cache)}
                  :get {:handler (handlers/limerick-generation-get-handler db cache)}}]
                ["/limericks"
                 ["/:artist-id/:album-id"
                  {:name ::album
                   :coercion reitit.coercion.spec/coercion
                   :parameters {:path {:artist-id int?
                                       :album-id int?}}
                   :get {:handler (handlers/limericks-get-handler db cache)}}]
                 ["/:artist-id"
                  {:name ::artist
                   :coercion reitit.coercion.spec/coercion
                   :parameters {:path {:artist-id int?}}
                   :get {:handler (handlers/artist-get-handler db)}}]]
                ["/assets/*" handlers/resource-handler]]]
    (http/router
     routes
     {:data {:interceptors [interceptors/coerce-request-interceptor
                            interceptors/logging-interceptor
                            interceptors/format-interceptor
                            (parameters-interceptor)
                            interceptors/keywordize-params-interceptor]}})))

(defmethod ig/init-key :app/handler [_ {:keys [router]}]
  (http/ring-handler
   router
   (ring/create-default-handler)
   {:executor sieppari/executor}))

(defmethod ig/init-key :app/server [_ {:keys [handler] :as opts}]
  (timbre/info (format "Starting server on port %d" (:port opts)))
  (kit/run-server handler (dissoc opts :handler)))

(defmethod ig/halt-key! :app/server [_ server]
  (timbre/info "Stopping server.")
  (server))
