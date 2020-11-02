(ns com.darklimericks.server.limericks
  (:require [clojure.string :as string]
            [clojure.core.async :as async]
            [reitit.core :as reitit]
            [com.darklimericks.db.artists :as artists]
            [com.darklimericks.db.albums :as albums]
            [com.darklimericks.db.limericks :as db.limericks]
            [com.darklimericks.linguistics.core :as linguistics]
            [com.owoga.prhyme.limerick :as limerick]
            [com.owoga.prhyme.data.dictionary :as dict]
            [com.owoga.prhyme.data.darklyrics :refer [darklyrics-markov-2]]))

(defn parse-scheme-element [[tokens ctx]]
  (cond
    (or (= 5 (count tokens))
        (= 3 (count tokens)))
    (do (assert (re-matches #"\w+\d" (first tokens))
                "Expected a letter followed by an integer.")
        (let [text (re-find #"[a-zA-Z]+" (first tokens))
              count (Integer/parseInt (re-find #"\d+" (first tokens)))]
          [(rest tokens) (conj ctx [text count])]))

    (or (= 4 (count tokens))
        (= 1 (count tokens)))
    (do (assert (= (first tokens) (apply str (first ctx))) (format "Expected %s" (first ctx)))
        [(rest tokens) ctx])

    (= 2 (count tokens))
    (do (assert (= (first tokens) (apply str (second ctx))) (format "Expected %s" (second ctx)))
        [(rest tokens) ctx])

    :else
    (throw (ex-info "Invalid scheme" {:scheme tokens}))))

(comment
  (parse-scheme-element [["A9" "A9" "B5" "B5" "A9"] []]))

(defn parse-scheme [scheme-string]
  (-> (string/split scheme-string #"\s+")
      (#(vector % []))
      (parse-scheme-element)
      (parse-scheme-element)
      (parse-scheme-element)
      (parse-scheme-element)
      (parse-scheme-element)
      ((fn [[tokens [a b]]]
         [a a b b a]))))

(comment
  (re-find #"\d+" "alpha 234 aset 34a")
  (re-find #"[a-zA-Z]+" "apla352 spac")
  (parse-scheme "a9 a9 b6 b6 a9")
  (require '[integrant.repl.state :as state])
  (reitit/match-by-path (:app/router state/system) "/limerick-generation-task")
  ((:app/handler state/system) {:request-method :post
                                :uri "/limerick-generation-task"
                                :body-params {:scheme 23}})
  )

(defn get-artist-and-album-for-new-limerick [db]
  (let [artist (artists/most-recent-artist db)
        albums (albums/artist-albums db (:artist/id artist))
        limericks (db.limericks/album-limericks db (:album/id (first albums)))]
    (cond
      (< (count limericks) 10)
      [(:artist/id artist) (:album/id (first albums))]

      (< (count albums) 5)
      (let [album-name (linguistics/gen-album)
            {album-id :album/id} (albums/insert-album db album-name (:id artist))]
        [(:id artist) album-id])

      :else
      (let [artist-name (linguistics/gen-artist)
            {artist-id :artist/id} (artists/insert-artist db artist-name)
            album-name (linguistics/gen-album)
            {album-id :album/id} (albums/insert-album db album-name artist-id)]
        [artist-id album-id]))))

(defn get-limerick-name [lines]
  (->> lines
       (string/join " ")
       (#(string/split % #"\s+"))
       shuffle
       (take 2)
       (map string/capitalize)
       (string/join " ")))

(defn generate-limerick [db scheme task-id]
  (let [scheme (parse-scheme scheme)]
    (async/thread
      (try
        (let [rhyme-id (java.util.UUID/randomUUID)
              rhyme (limerick/rhyme-from-scheme
                     dict/prhyme-dict
                     darklyrics-markov-2
                     scheme)]
          (swap! db update-in [:tasks task-id] assoc :status :finished)
          (swap! db assoc-in [:limericks rhyme-id] {:text rhyme :task-id task-id})
          rhyme)
        (catch Throwable e
          (println "Exception" e)
          (swap! db update-in [:tasks task-id] assoc :status :failed)
          (swap! db assoc-in [:tasks task-id] {:text (.getMessage e)}))))))

(defn generate-limerick-worker [db scheme]
  (let [limerick (limerick/rhyme-from-scheme
               dict/prhyme-dict
               darklyrics-markov-2
               scheme)
        [artist-id album-id] (get-artist-and-album-for-new-limerick db)]
    (db.limericks/insert-limerick
     db
     (get-limerick-name limerick)
     (string/join "\n" limerick)
     album-id)))
