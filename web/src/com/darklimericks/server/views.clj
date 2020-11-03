(ns com.darklimericks.server.views
  (:require [hiccup.form :as form]
            [hiccup.page :as page]
            [clojure.string :as string]
            [com.darklimericks.db.albums :as db.albums]
            [com.darklimericks.db.artists :as db.artists]
            [com.darklimericks.server.util :as util]))

(defn wrapper
  ([db opts & body]
   (let [default-opts {:title "Dark Limericks"}
         opts (merge default-opts opts)
         title (:title opts)
         num-albums (db.albums/num-albums db)
         num-artists (db.artists/num-artists db)]
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
         [:a.washed-yellow.pl1 {:href "#"} "SUBMIT LIMERICKS"]
         [:span.dark-yellow
          (format
           "METAL LIMERICKS - CURRENTLY %d ALBUMS FROM %d+ BANDS"
           num-albums
           num-artists)]
         [:a.washed-yellow.pr1 {:href "#"} "LINKS"]]
        [:div.flex.items-stretch.bg-near-black.flex-wrap.flex-nowrap-l.f6
         (let [letters (map (comp str char) (range 97 123))]
           (for [letter letters]
             [:a.link.washed-yellow.bg-mid-gray.pv2.w1.w-100-ns.flex-auto
              {:href (format "/%s.html" letter)
               :style "margin: 1px;"}
              [:strong (string/upper-case letter)]]))]
        [:div.flex.items-center.justify-center.pv2
         [:span.f6.ph2 "Search the darkness for limericks most heartless"]
         [:form.ph2
          {:method "GET" :action "#"}
          [:input.bg-white.w4.w5-ns
           {:type "text"
            :name "search"
            :id "search"
            :value ""}]]]
        [:div.bg-near-black.br4.pa2
         body]

        [:div.flex.items-center.justify-center.pv2
         [:span.f6.ph2 "Search the darkness for limericks most heartless"]
         [:form.ph2
          {:method "GET" :action "#"}
          [:input.bg-white.w4.w5-ns
           {:type "text"
            :name "search"
            :id "search"
            :value ""}]]]
        [:div.flex.items-stretch.bg-near-black.flex-wrap.flex-nowrap-l.f6
         (let [letters (map (comp str char) (range 97 123))]
           (for [letter letters]
             [:a.link.washed-yellow.bg-mid-gray.pv2.w1.w-100-ns.flex-auto
              {:href (format "/%s.html" letter)
               :style "margin: 1px;"}
              [:strong (string/upper-case letter)]]))]

        [:div.f6.lh-copy.flex.justify-between
         [:a.washed-yellow.pr1 {:href "#"} "SUBMIT LIMERICKS"]
         [:span.dark-yellow "METAL LIMERICKS - CURRENTLY 0 ALBUMS FROM 0+ BANDS"]
         [:a.washed-yellow.pl1 {:href "#"} "LINKS"]]]]))))

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
      [:a.washed-yellow.pl1 {:href "#"} "SUBMIT LIMERICKS"]
      [:span.dark-yellow "METAL LIMERICKS - CURRENTLY 0 ALBUMS FROM 0+ BANDS"]
      [:a.washed-yellow.pr1 {:href "#"} "LINKS"]]
     [:div.flex.items-stretch.bg-near-black.flex-wrap.flex-nowrap-l.f6
      (let [letters (map (comp str char) (range 97 123))]
        (for [letter letters]
          [:a.link.washed-yellow.bg-mid-gray.pv2.w1.w-100-ns.flex-auto
           {:href (format "/%s.html" letter)
            :style "margin: 1px;"}
           [:strong (string/upper-case letter)]]))]
     [:div.flex.items-center.justify-center.pv2
      [:span.f6.ph2 "Search the darkness for limericks most heartless"]
      [:form.ph2
       {:method "GET" :action "#"}
       [:input.bg-white.w4.w5-ns
        {:type "text"
         :name "search"
         :id "search"
         :value ""}]]]
     [:div.bg-near-black.br4.pa2
      body]

     [:div.flex.items-center.justify-center.pv2
      [:span.f6.ph2 "Search the darkness for limericks most heartless"]
      [:form.ph2
       {:method "GET" :action "#"}
       [:input.bg-white.w4.w5-ns
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
      [:a.washed-yellow.pl1 {:href "#"} "SUBMIT LIMERICKS"]
      [:span.dark-yellow "METAL LIMERICKS - CURRENTLY 0 ALBUMS FROM 0+ BANDS"]
      [:a.washed-yellow.pr1 {:href "#"} "LINKS"]]]]))

(defn home [request recent-albums artists-by-album]
  (page
   "Dark Limericks"
   [:div.f3.light-yellow.pb3
    "Welcome to DARK LIMERICKS !"]
   [:div.f6.washed-yellow.pb3
    "Dark Limericks is the largest metal limericks archive on the Web."]
   [:div.f6.washed-yellow.pb3
    "( if you're looking for Dark Lyrics, go "
    [:a.light-yellow {:href "http://darklyrics.com"} "here"]
    " )"]
   [:div.f4.light-yellow.pb3
    "NEW ALBUMS"]
   [:div.flex.flex-wrap.space-between
    (for [album recent-albums]
      [:div.pb4
       {:style "flex: 1 24%"}
       [:div.p1
        [:img {:src (format
                     "/assets/images/%s-128.png"
                     (-> album
                         :album/name
                         string/lower-case
                         (string/replace #" " "-")))}]]
       [:div.p2
        [:a.light-yellow.f5
         {:href (util/route-name->path
                 request
                 :com.darklimericks.server.system/artist
                 {:artist-id (:album/artist_id album)})}
         (-> album :album/id (artists-by-album) :artist/name)]]
       [:div.p2
        [:a.link.washed-yellow.f6
         {:href (util/route-name->path
                 request
                 :com.darklimericks.server.system/album
                 {:artist-id (:album/artist_id album)
                  :album-id (:album/id album)})}
         (format "\"%s\"" (:album/name album))]]])]
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