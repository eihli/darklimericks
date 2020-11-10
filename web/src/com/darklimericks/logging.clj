(ns com.darklimericks.logging
  (:require [integrant.core :as ig]
            [taoensso.timbre :as timbre]))

(defmethod ig/init-key ::logging [_ config]
  (timbre/merge-config! config))

(defmethod ig/halt-key! ::logging [_ _])
