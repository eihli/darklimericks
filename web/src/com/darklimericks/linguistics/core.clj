(ns com.darklimericks.linguistics.core
  (:require [com.owoga.prhyme.data.dictionary :as dict]
            [clojure.string :as string]))

(defn gen-artist []
  (->> [(rand-nth (seq dict/adverbs))
        (rand-nth (seq dict/nouns))]
       (map string/capitalize)
       (string/join " ")))

(defn gen-album []
  (->> [(rand-nth (seq dict/adjectives))
        (rand-nth (seq dict/nouns))]
       (map string/capitalize)
       (string/join " ")))
