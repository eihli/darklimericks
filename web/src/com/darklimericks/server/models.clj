(ns com.darklimericks.server.models
  (:require [taoensso.nippy :as nippy]
            [com.owoga.trie :as trie]
            [com.owoga.tightly-packed-trie :as tpt]
            [clojure.java.io :as io]
            [com.owoga.corpus.markov :as markov]))

(def database (nippy/thaw-from-resource "models/database.bin"))
(def rhyme-trie (into (trie/make-trie) (nippy/thaw-from-resource "models/rhyme-trie.bin")))
(def markov-trie (tpt/load-tightly-packed-trie-from-file
                  (io/resource "models/tpt.bin")
                  (markov/decode-fn database)))
