(ns rhymestorm.app
  (:require ["chart.js/auto" :as chart]
            [oz.core :as oz]
            [reagent.dom :as rdom]
            [reagent.core :as r]))


(defn play-data [& names]
  (for [n names
        i (range 20)]
    {:time i :item n :quantity (+ (Math/pow (* i (count n)) 0.8) (rand-int (count n)))}))

(def line-plot
  {:data {:values (play-data "monkey" "slipper" "broom")}
   :encoding {:x {:field "time" :type "quantitative"}
              :y {:field "quantity" :type "quantitative"}
              :color {:field "item" :type "nominal"}}
   :mark {:type "line" :tooltip true}})

(def stacked-bar
  {:data {:values (play-data "munchkin" "witch" "dog" "lion" "tiger" "bear")}
   :mark "bar"
   :encoding {:x {:field "time"
                  :type "ordinal"}
              :y {:aggregate "sum"
                  :field "quantity"
                  :type "quantitative"}
              :color {:field "item"
                      :type "nominal"}}})

(defn init-chart []
  (let [ctx (. js/document getElementById "myChart")]
    (rdom/render [:div [oz/vega-lite line-plot]] ctx)))


(defn init []
  (println "Hello world")
  (.addEventListener js/window "DOMContentLoaded" init-chart))

(init)
