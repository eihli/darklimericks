(ns com.darklimericks.server.models
  (:require [taoensso.nippy :as nippy]
            [com.owoga.trie :as trie]
            [com.owoga.tightly-packed-trie :as tpt]
            [com.owoga.prhyme.core :as prhyme]
            [com.owoga.phonetics :as phonetics]
            [clojure.java.io :as io]
            [com.owoga.corpus.markov :as markov]))

(def database (nippy/thaw-from-resource
               "models/markov-database-4-gram-backwards.bin"))

(def rhyme-trie (into (trie/make-trie)
                      (nippy/thaw-from-resource
                       "models/rhyme-trie-unstressed-vowels-and-trailing-consonants.bin")))

(def markov-trie (tpt/load-tightly-packed-trie-from-file
                  (io/resource "models/markov-tightly-packed-trie-4-gram-backwards.bin")
                  (markov/decode-fn database)))

(def rhyme-trie-unstressed-trailing-consonants
  (markov/->RhymeTrie
   rhyme-trie
   (fn [phones]
     (->> phones
          prhyme/take-vowels-and-tail-consonants
          prhyme/remove-all-stress))
   (fn [phones choices]
     (every? phonetics/consonant (butlast phones)))))

(comment
  (->> ["AA1" "ER0" "IY0"]
       (markov/rhymes rhyme-trie-unstressed-trailing-consonants)
       (map (fn append-quality-of-rhyme [[phones1 words]]
              [phones1 (->> (mapcat prhyme/phrase->all-phones words)
                            (map (fn [[phones2 word]]
                                   [phones2 word (prhyme/quality-of-rhyme-phones phones1 phones2)])))]))
       (mapcat (fn sort-by-quality-of-rhyme [[phones1 words]]
                 [phones1 (sort-by (fn [[_ _ quality]]
                                     (- quality))
                                   words)]))
       (take 20))

  (->> "bother me"
       (prhyme/phrase->all-phones)
       (map first)
       (map
        (fn [phones]
          [phones (markov/rhymes rhyme-trie-unstressed-trailing-consonants phones)]))
       (map (fn append-quality-of-rhyme [[phones1 words]]
              [phones1 (->> (mapcat prhyme/phrase->all-phones (reduce into #{} (map second words)))
                            (map (fn [[phones2 word]]
                                   [phones2 word (prhyme/quality-of-rhyme-phones phones1 phones2)])))]))
       (map (fn sort-by-quality-of-rhyme [[phones1 words]]
              [phones1 (sort-by (fn [[_ _ quality]]
                                  (- quality))
                                words)]))
       (mapcat second)
       (sort-by #(- (nth % 2)))
       (take 20))

  )
