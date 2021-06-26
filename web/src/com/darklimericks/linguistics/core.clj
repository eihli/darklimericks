(ns com.darklimericks.linguistics.core
  (:require [com.owoga.prhyme.data.dictionary :as dict]
            [com.owoga.prhyme.core :as prhyme]
            [com.owoga.prhyme.util :as util]
            [com.darklimericks.server.models :as models]
            [com.owoga.corpus.markov :as markov]
            [clojure.string :as string]
            [com.owoga.phonetics :as phonetics]
            [com.owoga.phonetics.syllabify :as syllabify]
            [com.owoga.phonetics.stress-manip :as stress-manip]
            [clojure.math.combinatorics :as combinatorics]))

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
         (map reverse)
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
         (map reverse)
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

(defn same-number-of-syllables?
  [phones1 phones2]
  (apply = (map (comp count syllabify/syllabify) [phones1 phones2])))

(comment
  (apply
   same-number-of-syllables?
   (map first (map phonetics/get-phones ["economy" "ecology"])))

  (apply
   same-number-of-syllables?
   (map first (map phonetics/get-phones ["numerology" "ecology"])))
  )

(defn quality-of-rhyme-phones
  "Points for:
  - Perfect rhyme
  - Perfect rhyme sans consonants
  - Number of matching stressed vowels
  - Number of matching any-stressed vowels
  - Same number of syllables
  "
  [phones1 phones2]
  (let [perfect? (if (perfect-rhyme? phones1 phones2) 1 0)
        perfect-sans-consonants? (if (perfect-rhyme-sans-consonants? phones1 phones2) 1 0)
        num-matching-stressed (number-of-matching-vowels-with-stress phones1 phones2)
        num-matching-any-stress (number-of-matching-vowels-any-stress phones1 phones2)
        same-number-of-syllables (same-number-of-syllables? phones1 phones2)]
    (+ perfect?
       perfect-sans-consonants?
       num-matching-stressed
       num-matching-any-stress
       same-number-of-syllables)))

(comment
  (->> [["economy" "ecology"]
        ["biology" "ecology"]
        ["bother me" "poverty"]
        ["property" "properly"]
        ["bother me" "invincibility"]
        ["invincibility" "bother me"]]
       (map (partial map (comp first phonetics/phrase-phones)))
       (map (partial apply quality-of-rhyme-phones)))

  (phonetics/phrase-phones "bother me")
  (phonetics/phrase-phones "invincibility")

  (let [phones1 ["B" "AA1" "DH" "ER0" "M" "IY1"]
        phones2 ["IH2" "N" "V" "IH2" "N" "S" "AH0" "B" "IH1" "L" "IH0" "T" "IY0"]]
    (perfect-rhyme-sans-consonants? phones1 phones2))

  )

(defn rhymes
  "All rhymes. Slightly flexible. Ordered by number of rhyming syllables.
  Most generic and likely desired rhyming algorithm."
  [target]
  (->> target
       (phonetics/phrase-phones)
       (mapcat (partial
                markov/rhymes
                models/rhyme-trie-unstressed-trailing-consonants))
       (mapcat second)))

(comment
  (rhymes "bother me")

  )

(defn rhymes-with-phones
  "All rhymes. Slightly flexible. Ordered by number of rhyming syllables.
  Most generic and likely desired rhyming algorithm."
  [target]
  (let [pronunciations (phonetics/phrase-phones target)]
    (->> pronunciations
         (mapcat
          (partial markov/rhymes models/rhyme-trie-unstressed-trailing-consonants))
         (mapcat second)
         (mapcat
          (fn [word]
            (map #(vector word %) (phonetics/phrase-phones word)))))))

(comment
  (rhymes-with-phones
   "technology")

  )
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

(defn assoc-phrases-with-phones
  [phrases]
  (mapcat
   (fn [phrase]
     (map #(vector phrase %) (phonetics/get-phones phrase)))
   phrases))

(comment
  (assoc-phrases-with-phones ["foo" "bar"]);; => (["foo" ["F" "UW1"]] ["bar" ["B" "AA1" "R"]])
  )

(defn append-freqs
  [database trie prefix phrase-phones]
  (map
   (fn [phrase-phone]
     (let [id (database (first phrase-phone))]
       (conj phrase-phone (second (get trie (conj prefix id) ['_ 0])))))
   phrase-phones))

(comment
  (->> ["top" "bar"]
       (assoc-phrases-with-phones)
       (append-freqs models/database models/markov-trie [1 1 1]))
  ;; => (["top" ["T" "AA1" "P"] 888]
  ;;     ["top" ["T" "AO1" "P"] 888]
  ;;     ["bar" ["B" "AA1" "R"] 220])
  )

(defn append-rhyme-quality
  [target-phrase phrase-phones]
  (mapcat
   (fn [phrase-phone]
     (map
      (fn [phones]
        (into phrase-phone [target-phrase phones (quality-of-rhyme-phones
                                                  (second phrase-phone)
                                                  phones)]))
      (phonetics/phrase-phones target-phrase)))
   phrase-phones))

(comment
  (->> ["hog" "bog"]
       (assoc-phrases-with-phones)
       (append-freqs models/database models/markov-trie [1 1 1])
       (append-rhyme-quality "log"))
  ;; => (["hog" ["HH" "AA1" "G"] 18 "log" ["L" "AO1" "G"] 0]
  ;;     ["bog" ["B" "AA1" "G"] 42 "log" ["L" "AO1" "G"] 0]
  ;;     ["bog" ["B" "AO1" "G"] 42 "log" ["L" "AO1" "G"] 4])
  )

(defn rhymes-with-frequencies-and-rhyme-quality
  [target trie database]
  (let [rhymes- (rhymes target)
        rhymes-with-freqs-and-qualities
        (->> rhymes-
             (assoc-phrases-with-phones)
             (append-freqs models/database models/markov-trie [1 1 1])
             (append-rhyme-quality target))]
    (distinct
     (sort-by
      (fn [[w1 p1 f w2 p2 q]]
        [(- q) (- f)])
      rhymes-with-freqs-and-qualities))))

(comment
  (time
   (rhymes-with-frequencies-and-rhyme-quality
    "bother me"
    models/markov-trie
    models/database))

  (apply quality-of-rhyme-phones
         '(["B" "AA1" "DH" "ER0" "M" "IY1"]
           ["IH2" "N" "V" "IH2" "N" "S" "AH0" "B" "IH1" "L" "IH0" "T" "IY0"]))

  (apply quality-of-rhyme-phones
         '(["B" "AA1" "DH" "ER0" "M" "IY1"]
           ["IH2" "N" "V" "IH2" "Z" "AH0" "B" "IH1" "L" "AH0" "T" "IY0"]))

  )
(comment
  (markov/rhymes models/rhyme-trie-unstressed-trailing-consonants (phonetics/get-phones "food"))
  (rhymes "food")

  (get models/markov-trie [(models/database "food")])
  (rhymes-with-frequencies "technology" models/markov-trie models/database)

  )
