(ns com.darklimericks.db.albums
  (:require [next.jdbc :as jdbc]
            [next.jdbc.sql :as jdbc.sql]
            [honeysql.core :as honey.sql]
            [honeysql.helpers :as honey.helpers]))

(defn insert-album-sql [name artist-id]
  (let [[s & params] (-> (honey.helpers/insert-into :album)
                         (honey.helpers/columns :name :artist-id)
                         (honey.helpers/values
                          [[name artist-id]])
                         honey.sql/format)
        s (format "%s RETURNING id" s)]
    (concat [s] params)))

(defn insert-album [db name artist-id]
  (jdbc/execute-one! db (insert-album-sql name artist-id)))

(defn artist-albums-sql [artist-id]
  (honey.sql/format
   (honey.sql/build
    :select :*
    :from :album
    :where [:= :artist_id artist-id]
    :order-by [[:album.id :desc]])))

(defn artist-albums [db artist-id]
  (jdbc/execute! db (artist-albums-sql artist-id)))

(defn artist-most-recent-album-sql [artist-id]
  (honey.sql/format
   (honey.sql/build
    :select :*
    :from :album
    :join [:artist [:= :album.artist_id :artist.id]]
    :order-by [[:album.id :desc]]
    :where [:= :artist.id artist-id])))

(defn artist-most-resent-album [db artist-id]
  (jdbc/execute! db (artist-most-recent-album-sql artist-id)))

(defn album [db id]
  (jdbc.sql/get-by-id db :album id))

(defn most-recent-albums [db]
  (->> {:select [:*]
        :from [:album]
        :order-by [[:album.id :desc]]}
       honey.sql/format
       (jdbc/execute! db)))

(defn num-albums [db]
  (:count
   (jdbc/execute-one!
    db
    (honey.sql/format
     {:select [:%count.*]
      :from [:album]}))))

(comment
  (honey.sql/format
   {:select :*
    :from [:album]
    :order-by [[:album.id :desc]]}))
