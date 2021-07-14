(ns com.darklimericks.server.views
  (:require [hiccup.form :as form]
            [hiccup.page :as page]
            [clojure.string :as string]
            [com.darklimericks.db.albums :as db.albums]
            [com.darklimericks.db.artists :as db.artists]
            [com.darklimericks.server.util :as util]))

(defn wrapper
  ([db request opts & body]
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
         [:a.washed-yellow.pl1
          {:href (util/route-name->path
                  request
                  :com.darklimericks.server.router/submit)}
          "SUBMIT LIMERICKS"]
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
         [:a.washed-yellow.pr1
          {:href (util/route-name->path
                  request
                  :com.darklimericks.server.router/submit)}
          "SUBMIT LIMERICKS"]
         [:span.dark-yellow
          (format
           "METAL LIMERICKS - CURRENTLY %d ALBUMS FROM %d+ BANDS"
           num-albums
           num-artists)]
         [:a.washed-yellow.pl1 {:href "#"} "LINKS"]]]]))))

(defn wrap-with-js
  ([{db :db
     request :request
     {title :title
      js :js
      css :css
      :or {title "DarkLimericks"
           css ["/assets/tachyons.css"]
           js ["/assets/wgu/main.js"]}
      :as opts} :opts}
    & body]
   (println (keys request))
   (let [num-albums (db.albums/num-albums db)
         num-artists (db.artists/num-artists db)]
     (page/html5
      [:head
       [:meta {:charset "utf-8"}]
       [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
       (apply page/include-css css)
       (apply page/include-js js)
       [:title title]
       [:link {:rel "shortcut icon" :href "/assets/favicon.ico"}]]
      [:body.tc.washed-yellow.bg-near-black.avenir
       [:h1
        [:a.link.dim.washed-yellow {:href "/"} "DarkLimericks.com"]]
       [:div.w-50-ns.w-90.center.bg-dark-gray.pa2
        [:div.f6.lh-copy.flex.justify-between
         [:a.washed-yellow.pl1
          {:href (util/route-name->path
                  request
                  :com.darklimericks.server.router/submit)}
          "SUBMIT LIMERICKS"]
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
         [:a.washed-yellow.pr1
          {:href (util/route-name->path
                  request
                  :com.darklimericks.server.router/submit)}
          "SUBMIT LIMERICKS"]
         [:span.dark-yellow
          (format
           "METAL LIMERICKS - CURRENTLY %d ALBUMS FROM %d+ BANDS"
           num-albums
           num-artists)]
         [:a.washed-yellow.pl1 {:href "#"} "LINKS"]]]]))))



(defn home [db request recent-albums artists-by-album]
  (wrapper
   db
   request
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
                 :com.darklimericks.server.router/artist
                 {:artist-id (:album/artist_id album)
                  :artist-name (-> album
                                   :album/id
                                   artists-by-album
                                   :artist/name
                                   util/slug)})}
         (-> album :album/id (artists-by-album) :artist/name)]]
       [:div.p2
        [:a.link.washed-yellow.f6
         {:href (util/route-name->path
                 request
                 :com.darklimericks.server.router/album
                 {:artist-id (:album/artist_id album)
                  :artist-name (-> album
                                   :album/id
                                   artists-by-album
                                   :artist/name
                                   util/slug)
                  :album-id (:album/id album)
                  :album-name (util/slug (:album/name album))})}
         (format "\"%s\"" (:album/name album))]]])]))

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

(defn submit-limericks
  [request limericks]
  [:div
   [:h1 "Generate Limerick"]
   (form/form-to
    [:post (util/route-name->path
            request
            :com.darklimericks.server.router/limerick-generation-task)]
    (form/text-field
     {:placeholder "A10 A10 B6 B6 A10"}
     "scheme")
    (form/submit-button
     {:class "ml2"}
     "Generate dark limerick"))
   (when (:session/key request)
     [:p "Session " (-> (:session/key request)
                        (string/split #":")
                        (nth 2))])
   [:h2 "Generated Limericks"]
   (if (empty? limericks)
     [:p "None, yet..."]
     (for [[i limerick] (map vector (range 1 (inc (count limericks))) limericks)]
       [:div
        [:h3 (format "%s: %s" i (:limerick/name limerick))]
        [:div
         [:div (format "artist: %s" (:artist/name limerick))]
         [:div (format "album: %s" (:album/name limerick))]]
        [:p
         (for [line (string/split (:limerick/text limerick) #"\n")]
           [:div line])]]))])

(defn wgu
  [request]
  [:div
   [:h1 "WGU Capstone"]
   (form/form-to
    [:post (util/route-name->path
            request
            :com.darklimericks.server.router/wgu)]
    (form/label
     "rhyme-target"
     "Target word or phrase for which to find rhyme suggestions")
    " "
    (form/text-field
     {:placeholder "instead of war on poverty"}
     "rhyme-target")
    (form/submit-button
     {:class "ml2"}
     "Show rhyme suggestions"))
   #_[:div
    [:canvas#myChart {:width 400 :height 400}]]
   [:iframe {:src "/assets/README_WGU.htm"
             :style "background-color: white; width: 100%; height: 760px;"}]])

(defn show-rhyme-suggestion
  [request suggestions]
  [:div
   (wgu request)
   (for [[suggestion p1 freq _ p2 quality] suggestions]
     [:div (string/join " - " [suggestion freq p1 p2])])])
