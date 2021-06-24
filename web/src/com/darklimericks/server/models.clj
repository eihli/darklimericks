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
