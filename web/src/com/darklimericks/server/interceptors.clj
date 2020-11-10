(ns com.darklimericks.server.interceptors
  (:require [reitit.http.interceptors.parameters :refer [parameters-interceptor]]
            [muuntaja.interceptor :as muuntaja-interceptor]
            [reitit.interceptor.sieppari :as sieppari]
            [ring.middleware.keyword-params :refer [keyword-params-request]]
            [ring.middleware.session]
            [ring.middleware.session.memory :as mem]
            [taoensso.timbre :as timbre]
            [taoensso.carmine.ring :as carmine.ring]
            [reitit.core :as reitit]
            [reitit.coercion :as coercion]
            [reitit.impl :as impl]))


(def format-interceptor (muuntaja-interceptor/format-interceptor))

(def keywordize-params-interceptor
  {:enter
   (fn [{:keys [request] :as ctx}]
     (update ctx :request keyword-params-request))})

(def logging-interceptor
  {:enter (fn [{:keys [request] :as ctx}]
            #_(timbre/info
             (str "Entering " (dissoc request ::reitit/match)))
            ctx)
   :leave (fn [{:keys [response] :as ctx}]
           #_(timbre/info
            (str "Exiting " (dissoc response ::reitit/match)))
           ctx)})

(def coerce-request-interceptor
  {:enter
   (fn [{:keys [request] :as ctx}]
     (let [{{{:keys [coercion parameters]} :data :as opts} :reitit.core/match} request]
       (cond
         (not coercion) ctx
         (not parameters) ctx
         :else
         (if-let [coercers (coercion/request-coercers coercion parameters opts)]
           (let [coerced (coercion/coerce-request coercers request)]
             (assoc-in ctx [:request :parameters] coerced))
           ctx))))})

(defn- session-options
  [cache options]
  {:store        (options :store (carmine.ring/carmine-store cache))
   :cookie-name  (options :cookie-name "ring-session")
   :cookie-attrs (merge {:path "/"
                         :http-only true}
                        (options :cookie-attrs)
                        (if-let [root (options :root)]
                          {:path root}))})

(defn session-interceptor
  ([cache]
   (session-interceptor cache {}))
  ([cache options]
   (let [options (session-options cache options)]
     {:enter
      (fn [ctx]
        (update
         ctx
         :request
         ring.middleware.session/session-request
         options))
      :leave
      (fn [{:keys [response request] :as ctx}]
        (update
         ctx
         :response
         ring.middleware.session/session-response
         request
         options))}))
  )
