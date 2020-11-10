(ns com.darklimericks.kv.core
  (:require [taoensso.timbre :as timbre]
            [integrant.core :as ig]
            [environ.core :refer [env]]))


(defmethod ig/prep-key ::connection [_ config]
  (let [pass (or (env :redis-pass) "dev")]
    (assoc-in
     config
     [:spec :uri]
     (format "redis://localhost:6379/"))))

;; Point of this? We aren't and maintaining a connection here.
;; I guess carmine does that behind the scenes with it's "pool"
;; option. But it's nice to have all boundary config in one place.
(defmethod ig/init-key ::connection [_ config]
  config)
