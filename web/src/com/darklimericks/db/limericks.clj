(ns com.darklimericks.db.limericks
  (:require [next.jdbc :as jdbc]
            [taoensso.timbre :as timbre]
            [honeysql.core :as honey.sql]
            [honeysql.helpers :as honey.helpers]))

(defn insert-limerick-sql [name text album-id]
  (-> (honey.helpers/insert-into :limerick)
      (honey.helpers/columns :name :text :album-id)
      (honey.helpers/values
       [[name text album-id]])
      honey.sql/format))

(comment
  (insert-limerick-sql "foo" "bar" 1)
  ;; => ["INSERT INTO limerick (name, text, album_id) VALUES (?, ?, ?)" "foo" "bar" 1]
  (-> (insert-limerick-sql "foo" "bar" 1)
      (update 0 #(str % " RETURNING id")))
  )

(defn insert-limerick [db name text album-id]
  (jdbc/execute! db (insert-limerick-sql name text album-id)))

(defn insert-user-limerick [db session-id name text album-id]
  (timbre/info (format "Saving %s." session-id))
  (jdbc/with-transaction [tx db]
    (let [{limerick-id :limerick/id}
          (jdbc/execute-one!
           tx
           (-> (insert-limerick-sql name text album-id)
               (update 0 #(str % " RETURNING id"))))]
      (timbre/info (format "Saving %s to %s." limerick-id session-id))
      (jdbc/execute! tx (-> (honey.helpers/insert-into :session_limerick)
                            (honey.helpers/columns :limerick_id :session_id)
                            (honey.helpers/values [[limerick-id session-id]])
                            honey.sql/format)))))

(comment
  (require '[integrant.repl.state :as state])
  )

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

(defn limericks-by-session [db session]
  (->> (honey.sql/build
        :select :*
        :from :limerick
        :join [:session_limerick [:= :session_limerick.limerick_id :limerick.id]]
        :where [:= :session_limerick.session_id session])
       honey.sql/format
       (jdbc/execute! db)))

(defn album-limericks-sql [album-id]
  (honey.sql/format
   (honey.sql/build
    :select :*
    :from :limerick
    :where [:= :album_id album-id])))

(defn album-limericks [db album-id]
  (jdbc/execute! db (album-limericks-sql album-id)))
