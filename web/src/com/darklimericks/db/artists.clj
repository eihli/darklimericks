(ns com.darklimericks.db.artists
  (:require [next.jdbc :as jdbc]
            [honeysql.core :as honey.sql]
            [honeysql.helpers :as honey.helpers]))

(defn insert-artist-sql [name]
  (let [[s & params] (-> (honey.helpers/insert-into :artist)
                         (honey.helpers/columns :name)
                         (honey.helpers/values
                          [[name]])
                         honey.sql/format)
        s (format "%s RETURNING id" s)]
    (concat [s] params)))

(defn insert-artist [db name]
  (jdbc/execute-one! db (insert-artist-sql name)))

(defn most-recent-artist-sql []
  (-> {:select [:*]
       :from [:artist]
       :order-by [[:id :desc]]
       :limit 1}
      (honey.sql/format)))

(defn most-recent-artist [db]
  (jdbc/execute-one! db (most-recent-artist-sql)))

(defn artists-by-letter [db letter]
  (jdbc/execute!
   db
   (honey.sql/format
    {:select [:*]
     :from [:artist]
     :where [:like :name (str letter "%")]})))

(defn artist [db id]
  (jdbc/execute-one!
   db
   (honey.sql/format
    {:select [:*]
     :from [:artist]
     :where [:= :id id]})))

(defn num-artists [db]
  (:count
   (jdbc/execute-one!
    db
    (honey.sql/format
     {:select [:%count.*]
      :from [:artist]}))))
