(ns com.darklimericks.server.worker
  (:require [integrant.core :as ig]
            [taoensso.timbre :as timbre]
            [taoensso.carmine.message-queue :as car-mq]
            [com.darklimericks.server.limericks :as limericks]))

(defmethod ig/init-key ::limerick-gen [_ {:keys [db kv]}]
  (car-mq/worker
   kv
   "limericks"
   {:handler
    (fn [{:keys [message attempt]}]
      (timbre/info "Received" message)
      (limericks/generate-limerick-worker db message)
      {:status :success})}))

(defmethod ig/halt-key! ::limerick-gen [_ w]
  (.stop w))
