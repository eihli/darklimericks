(ns com.darklimericks.db.core
  (:require [integrant.core :as ig]
            [environ.core :refer [env]]
            [next.jdbc :as jdbc]))



(defmethod ig/prep-key ::connection [_ _]
  {:jdbcUrl (str "jdbc:postgresql://localhost:5432/?user="
                 (or (env :postgres-user)
                     "dev")
                 "&password="
                 (or (env :postgres-password)
                     "dev"))})

(defmethod ig/init-key ::connection [_ db-spec]
  (jdbc/get-datasource db-spec))
