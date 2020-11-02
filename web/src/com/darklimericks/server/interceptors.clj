(ns com.darklimericks.server.interceptors
  (:require [reitit.http.interceptors.parameters :refer [parameters-interceptor]]
            [muuntaja.interceptor :as muuntaja-interceptor]
            [reitit.interceptor.sieppari :as sieppari]
            [ring.middleware.keyword-params :refer [keyword-params-request]]
            [taoensso.timbre :as timbre]
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
            (timbre/info
             (str "Entering " (dissoc request ::reitit/match)))
            ctx)
   :exit (fn [{:keys [response] :as ctx}]
           (timbre/info
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
