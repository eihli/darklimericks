(ns com.darklimericks.server.system
  (:require [integrant.core :as ig]
            [taoensso.timbre :as timbre]
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



