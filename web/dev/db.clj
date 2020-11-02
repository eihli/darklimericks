(ns db
  (:require [next.jdbc.sql :as jdbc.sql]
            [next.jdbc :as jdbc]
            [honeysql.core :as honey.sql]
            [integrant.repl.state :refer [system]]
            [com.darklimericks.db.artists :as artists]
            [com.darklimericks.db.albums :as albums]
            [com.darklimericks.server.limericks :as server.limericks]
            [com.darklimericks.db.limericks :as limericks]))

(def conn (:database.sql/connection system))

(comment
  (->> (honey.sql/build :select :* :from :artist)
      (honey.sql/format)
      (jdbc.sql/query conn))
  (artists/insert-artist conn "Optimus Prhyme")
  (server.limericks/get-artist-and-album-for-new-limerick conn)
  (artists/artist conn 1)
  (albums/artist-albums-sql 1)
  (albums/artist-albums conn 1)
  (albums/insert-album conn "Limericks Illicit" 1)
  (albums/insert-album-sql  "Limericks Illicit" 1)
  (artists/artists-by-letter conn "O")
  (limericks/album-limericks-sql 1)
  (limericks/album-limericks conn 1)
  (albums/artist-most-resent-album conn 1)
  )
