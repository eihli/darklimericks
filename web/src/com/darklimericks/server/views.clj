(ns com.darklimericks.server.views
  (:require [hiccup.form :as form]
            [hiccup.page :as page]
            [clojure.string :as string]
            [com.darklimericks.server.util :as util]))

(defn page [title & body]
  (page/html5
   [:head
    [:meta {:charset "utf-8"}]
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
    (page/include-css "/assets/tachyons.css")
    [:title title]
    [:link {:rel "shortcut icon" :href "/assets/favicon.ico"}]]
   [:body.tc.washed-yellow.bg-near-black.avenir
    [:h1
     [:a.link.dim.washed-yellow {:href "/"} "DarkLimericks.com"]]
    [:div.w-50-ns.w-90.center.bg-dark-gray.pa2
     [:div.f6.lh-copy.flex.justify-between
      [:a.washed-yellow {:href "#"} "SUBMIT LIMERICKS"]
      [:span.dark-yellow "METAL LIMERICKS - CURRENTLY 0 ALBUMS FROM 0+ BANDS"]
      [:a.washed-yellow {:href "#"} "LINKS"]]
     [:div.flex.items-stretch.w-100.bg-near-black
      (let [letters (map (comp str char) (range 97 123))]
        (for [letter letters]
          [:a.washed-yellow.bg-mid-gray.w-100.pv2.flex-grow-1
           {:href (format "/%s.html" letter)
            :style "margin: 1px;"}
           (string/upper-case letter)]))]
     [:div.flex.items-center.justify-center.pv2
      [:span.f6.ph2 "Search the darkness for limericks heartless"]
      [:form.ph2
       {:method "GET" :action "#"}
       [:input.bg-white
        {:type "text"
         :name "search"
         :id "search"
         :value ""}]]]
     [:div.bg-near-black.br4.pa2
      body]

     [:div.flex.items-center.justify-center.pv2
      [:span.f6.ph2 "Search the darkness for limericks heartless"]
      [:form.ph2
       {:method "GET" :action "#"}
       [:input.bg-white
        {:type "text"
         :name "search"
         :id "search"
         :value ""}]]]

     [:div.flex.items-stretch.w-100.bg-near-black
      (let [letters (map (comp str char) (range 97 123))]
        (for [letter letters]
          [:a.washed-yellow.bg-mid-gray.w-100.pv2.flex-grow-1
           {:href (format "/%s.html" letter)
            :style "margin: 1px;"}
           (string/upper-case letter)]))]

     [:div.f6.lh-copy.flex.justify-between
      [:a.washed-yellow {:href "#"} "SUBMIT LIMERICKS"]
      [:span.dark-yellow "METAL LIMERICKS - CURRENTLY 0 ALBUMS FROM 0+ BANDS"]
      [:a.washed-yellow {:href "#"} "LINKS"]]]]))

(defn home [request]
  (page "Dark Limericks"
   (form/form-to
    [:post (util/route-name->path request :com.darklimericks.server.system/limerick-generation-task)]
    (form/text-field "scheme")
    (form/submit-button "Generate dark limerick"))))

(defn limerick-tasks [tasks]
  [:ul
   (for [[task-id task] tasks]
     [:li (format
           "%s - %s"
           task-id
           (if (:rhyme task)
             (string/join " / " (:rhyme task))
             (:status task)))])])

(defn limerick [i lim]
  (let [lines (string/split (:limerick/text lim) #"\n")
        name (:limerick/name lim)]
    [:div.tc
     [:a {:name (inc i)}
      [:h3.f3.washed-yellow (format "%s. %s" (inc i) name)]]
     (for [line lines]
       [:div line])]))
