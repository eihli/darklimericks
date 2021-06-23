(ns com.darklimericks.linguistics.core
  (:require [com.owoga.prhyme.data.dictionary :as dict]
            [com.owoga.prhyme.core :as prhyme]
            [com.darklimericks.server.models :as models]
            [com.owoga.corpus.markov :as markov]
            [clojure.string :as string]))

(defn gen-artist []
  (->> [(rand-nth (seq dict/adjectives))
        (rand-nth (seq dict/nouns))]
       (map string/capitalize)
       (string/join " ")))

(defn gen-album []
  (->> [(rand-nth (seq dict/adverbs))
        (rand-nth (seq dict/verbs))]
       (map string/capitalize)
       (string/join " ")))


(defn rhymes
  "All rhymes. Slightly flexible. Ordered by number of rhyming syllables.
  Most generic and likely desired rhyming algorithm."
  [target]
  (->>  (prhyme/phrase->all-flex-rhyme-tailing-consonants-phones target)
        (map first)
        (map reverse)
        (mapcat (partial markov/rhyme-choices models/rhyme-trie))
        (sort-by (comp - count first))
        (mapcat second)))

(comment
  (rhymes "food")

  )
