(ns com.darklimericks.server.limericks
  (:require [clojure.string :as string]
            [clojure.java.io :as io]
            [reitit.core :as reitit]
            [com.darklimericks.server.models :as models]
            [com.darklimericks.db.artists :as artists]
            [com.darklimericks.db.albums :as albums]
            [com.darklimericks.db.limericks :as db.limericks]
            [com.darklimericks.util.identicon :as identicon]
            [com.darklimericks.linguistics.core :as linguistics]
            [com.owoga.trie :as trie]
            [com.owoga.tightly-packed-trie :as tpt]
            [com.owoga.corpus.markov :as markov]
            [com.owoga.prhyme.core :as prhyme]
            [com.owoga.phonetics :as phonetics]
            [com.owoga.prhyme.data-transform :as data-transform]
            [com.owoga.tightly-packed-trie.encoding :as encoding]
            [taoensso.timbre :as timbre]))

(defn parse-scheme-element [[tokens ctx]]
  (cond
    (or (= 5 (count tokens))
        (= 3 (count tokens)))
    (do (assert (re-matches #"\w+\d" (first tokens))
                "Expected a letter followed by an integer.")
        (let [text (re-find #"[a-zA-Z]+" (first tokens))
              count (Integer/parseInt (re-find #"\d+" (first tokens)))]
          (assert (and (< 5 count) (< count 14))
                  "Expected syllable counts between 5 and 14.")
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
      (and artist
           albums
           (< (count limericks) 10))
      [(:artist/id artist) (:album/id (first albums))]

      (and artist
           (< (count albums) 5))
      (let [album-name (linguistics/gen-album)
            {album-id :album/id} (albums/insert-album db album-name (:artist/id artist))]
        (when (or (nil? (:artist/id artist))
                  (nil? album-id))
          (throw (ex-info "Nil artist or album" {:artist artist
                                                 :album album-id})))
        ^:new-album [(:artist/id artist) album-id])

      :else
      (let [artist-name (linguistics/gen-artist)
            {artist-id :artist/id} (artists/insert-artist db artist-name)
            album-name (linguistics/gen-album)
            {album-id :album/id} (albums/insert-album db album-name artist-id)]
        (when (or (nil? artist-id)
                (nil? album-id))
          (throw (ex-info "Nil artist or album" {:artist artist-id
                                                 :album album-id})))
        ^:new-album ^:new-artist [artist-id album-id]))))

(defn get-limerick-name [lines]
  (->> lines
       (string/join " ")
       (#(string/split % #"\s+"))
       shuffle
       (take 2)
       (map string/capitalize)
       (string/join " ")))

(comment
  (->> (markov/rhyme-from-scheme
        '[[A 9] [A 9] [B 6] [B 6] [A 9]]
        database
        markov-trie
        rhyme-trie)
       (map reverse)
       (map (partial map second))
       (map data-transform/untokenize))

  )

(def rhyme-trie-unstressed-trailing-consonants
  (markov/->RhymeTrie
   models/rhyme-trie
   (fn [phones]
     (->> phones
          prhyme/take-vowels-and-tail-consonants
          prhyme/remove-all-stress))
   (fn [phones choices]
     (every? phonetics/consonant (butlast phones)))))


(comment
  (let [result {'[a 8]
                [[[["P" "AE1" "S"] "pass"]
                  [["P" "ER0" "EH1" "N" "IY0" "AH0" "L" "IY0"] "perennially"]
                  [["Y" "UW1"] "you"]
                  [["T" "UW1"] "to"]]
                 [[["OW1" "V" "ER0" "P" "AE2" "S"] "overpass"]
                  [["AH0" "N"] "an"]
                  [["AO1" "N"] "on"]
                  [["M" "AH0" "N" "IH2" "P" "Y" "AH0" "L" "EY1" "SH" "AH0" "N"]
                   "manipulation"]]
                 [[["M" "IH1" "D" "AH0" "L" "K" "L" "AE1" "S"] "middle-class"]
                  [["HH" "AY1" "D" "IH0" "NG"] "hiding"]
                  [["M" "AA1" "N" "S" "T" "ER0"] "monster"]
                  [["K" "R" "UW1" "AH0" "L"] "cruel"]]],
                '[b 5]
                [[[["R" "EY1" "S"] "race"]
                  [["M" "AH0" "T" "IH1" "R" "IY0" "AH0" "L"] "material"]]
                 [[["B" "AO1" "R" "G" "EY0" "S"] "borges"]
                  [["IY2" "K" "W" "AH0" "L" "IH1" "B" "R" "IY0" "AH0" "M"] "equilibrium"]]]}
        [[a1 a2 a2] [b1 b2]] (vals result)]
    (->> [a1 a2 b1]
         (map reverse)
         (map (partial map second))))

  )

(defn generate-limerick-worker [db message]
  (timbre/info "Begin generate limerick worker.")
  (let [{:keys [scheme session-id]} message

        [[a1 a2 a3] [b1 b2]]
        (vals
         (markov/rhyme-from-scheme-v2
          scheme
          models/database
          models/markov-trie
          rhyme-trie-unstressed-trailing-consonants))

        limerick (->> [a1 a2 b1 b2 a3]
                      (map reverse)
                      (map (partial map second))
                      (map data-transform/untokenize))
        _ (timbre/info "Limerick: " limerick)
        album-artist (get-artist-and-album-for-new-limerick db)
        [artist-id album-id] album-artist
        album (albums/album db album-id)]
    (when (:new-album (meta album-artist))
      (identicon/generate (-> (:album/name album)
                              string/lower-case
                              (string/replace #" " "-")) 128))
    (db.limericks/insert-user-limerick
     db
     session-id
     (get-limerick-name limerick)
     (string/join "\n" limerick)
     album-id)))
