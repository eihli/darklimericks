(ns com.darklimericks.server.models
  (:require [taoensso.nippy :as nippy]
            [com.owoga.trie :as trie]
            [com.owoga.tightly-packed-trie :as tpt]
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
