(ns com.darklimericks.db.limericks
  (:require [next.jdbc :as jdbc]
            [honeysql.core :as honey.sql]
            [honeysql.helpers :as honey.helpers]))

(defn insert-limerick-sql [name text album-id]
  (-> (honey.helpers/insert-into :limerick)
      (honey.helpers/columns :name :text :album-id)
      (honey.helpers/values
       [[name text album-id]])
      honey.sql/format))

(defn insert-limerick [db name text album-id]
  (jdbc/execute! db (insert-limerick-sql name text album-id)))

(defn limerick-sql [limerick-id]
  (honey.sql/format
   (honey.sql/build
    :select :*
    :from :limerick
    :where [:= :limerick_id limerick-id])))

(defn limerick [db limerick-id]
  (jdbc/execute! db (limerick-sql limerick-id)))

(defn limericks [db]
  (jdbc/execute!
   db
   (honey.sql/format
    (honey.sql/build
     :select :*
     :from :limerick))))

(defn album-limericks-sql [album-id]
  (honey.sql/format
   (honey.sql/build
    :select :*
    :from :limerick
    :where [:= :album_id album-id])))

(defn album-limericks [db album-id]
  (jdbc/execute! db (album-limericks-sql album-id)))
