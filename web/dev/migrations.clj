(ns migrations
  (:require [migratus.core :as migratus]
            [integrant.repl.state :as state]))

(def config {:store :database
             :migration-dir "migrations/"
             :init-script "init.sql"
             :db {:connection-uri (:jdbcUrl (-> state/config :com.darklimericks.db.core/connection))}})

(comment


  config

  (migratus/init config)

  (migratus/migrate config)

  (migratus/create config "User Limericks")
  state/config
  (:database.sql/connection state/system)
  (migratus/init config))
