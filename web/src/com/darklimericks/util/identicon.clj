(ns com.darklimericks.util.identicon
  (:require [digest]
            [clojure.string :as str])
  (:import java.io.File
           java.awt.Color
           java.awt.image.BufferedImage
           javax.imageio.ImageIO))

(def tiles-per-side 6)
(def total-tiles
  (* tiles-per-side tiles-per-side))

(defn- get-color
  [pos]
  (nth '(
    (3 7 30) ;; Darkness
    (55 6 23) ;; Deep Red
    (106 4 15) ;; Rosewood
    (157 2 8) ;; Dark Red
    (208 0 0) ;; Red
    (220 47 2) ;; Vermillion
    (232 93 4) ;; Persimmon
    (244 140 6) ;; Carrot
    (3 7 30) ;; Darkness
    (55 6 23) ;; Deep Red
    (106 4 15) ;; Rosewood
    (157 2 8) ;; Dark Red
    (208 0 0) ;; Red
    (220 47 2) ;; Vermillion
    (232 93 4) ;; Persimmon
    (244 140 6) ;; Carrot
  ) pos))

(defn- from-hex-to-dec
  "Convert a hex char to an int."
  [character]
  (cond
    (= "a" character) 10
    (= "b" character) 11
    (= "c" character) 12
    (= "d" character) 13
    (= "e" character) 14
    (= "f" character) 15
    :else (Integer/parseInt character)))

(defn- to-numbers
  "Convert a string of hex chars to a seq of ints."
  [num_string]
  (map from-hex-to-dec (rest (clojure.string/split num_string #""))))

(defn- to-bools
  "Formulaically convert a seq of ints to bools."
  [num_string]
  (take (/ total-tiles 2) (cycle
    (map #(> % 7) (to-numbers num_string)))))

(defn- in-row
  "Return the row for a particular position in the seq."
  [pos]
  (quot pos (/ tiles-per-side 2)))

(defn- in-col
  "Return the column for a particular position in the seq."
  [pos]
  (rem pos (/ tiles-per-side 2)))

(defn- draw-tile
  "Fill in a tile at a particular position starting from the left of the image."
  [draw tile-size pos]
  (.fillRect draw
    (* (in-col pos) tile-size)
    (* (in-row pos) tile-size)
    tile-size tile-size))

(defn- draw-mirror-tile
  "Fill in a tile at a particular position starting from the right of the image."
  [draw tile-size pos]
  (.fillRect draw
    (* (- tiles-per-side (in-col pos) 1) tile-size)
    (* (in-row pos) tile-size)
    tile-size tile-size))

(defn- draw-it
  "Draw tiles on the image based on a seq of booleans to determine whether the tile is filled in or not."
  [draw tile-size pos bools]
  (when (first bools)
    ;; left half
    (draw-tile draw tile-size pos)
    ;; right half
    (draw-mirror-tile draw tile-size pos))

  (when (> (count bools) 1)
    (draw-it draw tile-size (inc pos) (rest bools))))

(defn- file-name
  "Generate a file name."
  [name size]
  (format "resources/public/images/%s-%s.png" name size))

(defn- fill-background
  "Fill in the background with white."
  [draw size]
  (.setColor draw (Color/BLACK))
  (.fillRect draw 0 0 size size))

(defn generate
  "Make a new avatar."
  [identifier size]
  (let
    [tile-size (quot size tiles-per-side)
     md5       (digest/md5 identifier)
     icon      (BufferedImage. size size BufferedImage/TYPE_INT_RGB)
     [r g b]   (get-color (first (to-numbers md5)))
     color     (Color. r g b)
     draw      (.createGraphics icon)]
    (fill-background draw size)
    (.setColor draw color)
    (draw-it draw tile-size 0 (to-bools md5))
    (ImageIO/write icon "png" (File. (file-name identifier size)))
    file-name))

(comment
  (generate "foobar-bazz" 128))
