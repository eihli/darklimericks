(ns migrations
  (:require [migratus.core :as migratus]
            [integrant.repl.state :as state]))

(def config {:store :database
             :migration-dir "migrations/"
             :init-script "init.sql"
             :db {:connection-uri (-> state/config :database.sql/connection :jdbcUrl)}})

(comment
  state/config
  (:database.sql/connection state/system)
  (migratus/init config))
