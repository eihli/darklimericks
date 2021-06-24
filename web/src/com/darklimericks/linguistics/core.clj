(ns com.darklimericks.linguistics.core
  (:require [com.owoga.prhyme.data.dictionary :as dict]
            [com.owoga.prhyme.core :as prhyme]
            [com.owoga.prhyme.util :as util]
            [com.darklimericks.server.models :as models]
            [com.owoga.corpus.markov :as markov]
            [clojure.string :as string]
            [com.owoga.phonetics :as phonetics]
            [com.owoga.phonetics.stress-manip :as stress-manip]))

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

(defn perfect-rhyme
  [phones]
  (->> phones
       reverse
       (util/take-through stress-manip/primary-stress?)
       first
       reverse
       (#(cons (first %)
               (stress-manip/remove-any-stress-signifiers (rest %))))))

(comment
  (perfect-rhyme (first (phonetics/get-phones "technology")))
  ;; => ("AA1" "L" "AH" "JH" "IY")
  )

(defn perfect-rhyme-sans-consonants
  [phones]
  (->> phones
       perfect-rhyme
       (remove phonetics/consonant)))

(comment
  (perfect-rhyme-sans-consonants (first (phonetics/get-phones "technology")))
  ;; => ("AA1" "AH" "IY")
  )

(defn perfect-rhyme?
  [phones1 phones2]
  (apply = (map perfect-rhyme [phones1 phones2])))

(defn perfect-rhyme-sans-consonants?
  [phones1 phones2]
  (apply = (map perfect-rhyme-sans-consonants [phones1 phones2])))

(comment
  (apply perfect-rhyme? (map (comp first phonetics/get-phones) ["technology" "ecology"]));; => true
  (apply perfect-rhyme? (map (comp first phonetics/get-phones) ["technology" "economy"]));; => false
  (apply perfect-rhyme-sans-consonants? (map (comp first phonetics/get-phones) ["technology" "economy"]));; => true
  (apply perfect-rhyme-sans-consonants? (map (comp first phonetics/get-phones) ["technology" "trilogy"]));; => false
  (apply perfect-rhyme? (map (comp first phonetics/get-phones) ["bother me" "poverty"]))
  (apply perfect-rhyme? (map (comp first phonetics/phrase-phones) ["bother me" "poverty"]))
  (phonetics/phrase-phones "bother me");; => [["B" "AA1" "DH" "ER0" "M" "IY1"]]
  (phonetics/phrase-phones "poverty");; => [["P" "AA1" "V" "ER0" "T" "IY0"]]
  )

(defn number-of-matching-vowels-with-stress
  [phones1 phones2]
  (let [[vowels1 vowels2] (map (partial filter phonetics/vowel?) [phones1 phones2])]
    (->> [vowels1 vowels2]
         (apply map vector)
         (filter (partial apply =))
         (filter (comp (partial re-find #"1") first))
         count)))

(comment
  (apply
   number-of-matching-vowels-with-stress
   (map first (map phonetics/get-phones ["technology" "ecology"])))
  (apply
   number-of-matching-vowels-with-stress
   (map first (map phonetics/get-phones ["biology" "ecology"])))
  )

(defn number-of-matching-vowels-any-stress
  [phones1 phones2]
  (let [[vowels1 vowels2] (map (partial filter phonetics/vowel?) [phones1 phones2])]
    (->> [vowels1 vowels2]
         (map (partial map phonetics/remove-stress))
         (apply map vector)
         (filter (partial apply =))
         count)))

(comment
  (apply
   number-of-matching-vowels-any-stress
   (map first (map phonetics/get-phones ["economy" "ecology"])))
  (apply
   number-of-matching-vowels-any-stress
   (map first (map phonetics/get-phones ["biology" "ecology"])))
  )

(defn quality-of-rhyme-phones
  "Points for:
  - Perfect rhyme
  - Perfect rhyme sans consonants
  - Number of matching stressed vowels
  - Number of matching any-stressed vowels
  "
  [phones1 phones2]
  (let [perfect? (if (perfect-rhyme? phones1 phones2) 1 0)
        perfect-sans-consonants? (if (perfect-rhyme-sans-consonants? phones1 phones2) 1 0)
        num-matching-stressed (number-of-matching-vowels-with-stress phones1 phones2)
        num-matching-any-stress (number-of-matching-vowels-any-stress phones1 phones2)]
    (println perfect? perfect-sans-consonants? num-matching-stressed num-matching-any-stress)
    (+ perfect?
       perfect-sans-consonants?
       num-matching-stressed
       num-matching-any-stress)))

(comment
  (->> [["economy" "ecology"]
        ["biology" "ecology"]
        ["bother me" "poverty"]
        ["property" "properly"]]
       (map (partial map (comp first phonetics/phrase-phones)))
       (map (partial apply quality-of-rhyme-phones)))

  )
(defn rhymes
  "All rhymes. Slightly flexible. Ordered by number of rhyming syllables.
  Most generic and likely desired rhyming algorithm."
  [target]
  (->> target
       (phonetics/get-phones)
       (mapcat (partial
                markov/rhymes
                models/rhyme-trie-unstressed-trailing-consonants))
       (mapcat second)))

(defn rhymes-with-frequencies
  [target trie database]
  (let [rhymes- (rhymes target)
        freqs (map
               (comp
                (fnil int 0)
                second
                (partial get models/markov-trie)
                (partial conj [1 1 1])
                database)
               rhymes-)]
    (distinct (sort-by (comp - second) (map vector rhymes- freqs)))))

(comment
  (markov/rhymes models/rhyme-trie-unstressed-trailing-consonants (phonetics/get-phones "food"))
  (rhymes "food")

  (get models/markov-trie [(models/database "food")])
  (rhymes-with-frequencies "technology" models/markov-trie models/database)

  )
