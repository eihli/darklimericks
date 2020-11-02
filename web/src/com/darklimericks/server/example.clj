(ns com.darklimericks.server.example
  (:require [integrant.core :as ig]
            [clojure.tools.namespace.repl :refer [set-refresh-dirs]]
            [integrant.repl :as repl]
            [org.httpkit.server :as kit]
            [reitit.http :as http]
            [hiccup.core :as hiccup]
            [hiccup.page :as page]
            [taoensso.timbre :as timbre]
            [reitit.interceptor.sieppari :as sieppari]))

(defn home []
  (page/html5
   [:head
    [:meta {:charset "utf-8"}]
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]]
   [:title "Hello World"]
   [:body "Goodbye, world!"]))

(defn home-handler [request]
  {:status 200
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :body (hiccup/html (home))})

(def routes
  [["/" {:name ::home
         :get {:handler home-handler}}]])

(def config
  {:app/handler {:router (ig/ref :app/router)}
   :app/router {:routes routes}
   :app/server {:port 8000 :handler (ig/ref :app/handler)}})

(defmethod ig/init-key :app/router [_ {:keys [routes]}]
  (http/router routes))

(defmethod ig/init-key :app/handler [_ {:keys [router]}]
  (http/ring-handler router {:executor sieppari/executor}))

(defmethod ig/init-key :app/server [_ opts]
  (timbre/info "Starting server with " opts)
  (kit/run-server (:handler opts) (dissoc opts :handler)))

(defmethod ig/halt-key! :app/server [_ server]
  (timbre/info "Stopping server")
  (server))

(comment
  (set-refresh-dirs "src" "dev")
  (repl/set-prep! (constantly config))
  (repl/prep)
  (repl/go)
  (repl/reset)
  (repl/halt)
  )
