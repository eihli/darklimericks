(ns com.darklimericks.server.views
  (:require [hiccup.form :as form]
            [hiccup.page :as page]
            [clojure.string :as string]
            [com.darklimericks.db.albums :as db.albums]
            [com.darklimericks.db.artists :as db.artists]
            [com.darklimericks.server.util :as util]
            [com.darklimericks.server.models :as models]
            [com.owoga.prhyme.core :as prhyme]
            [com.owoga.corpus.markov :as markov]
            [com.darklimericks.linguistics.core :as linguistics]))

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
  [request {:keys [rhymes rhyming-lyrics lyrics-from-seed]}]
  [:div
   [:h1 "WGU Capstone"]
   [:div

    [:h2 "Generate Rhyme"]
    [:div
     [:p.tl "Use the input field below to enter a word or phrase and view a list
of words that rhyme. Rhyming words will be sorted by a quality score that represents
how well the rhyme matches your target."]]
    (form/form-to
     [:get (util/route-name->path
            request
            :com.darklimericks.server.router/rhyme)]
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
    (when rhymes
      rhymes)]

   [:div
    [:h2 "Generate Rhyming Lyric"]
    [:p.tl "Use the input field below to enter a word or phrase and view a list
of phrases that rhyme. Phrases will be generated from a 4-gram Hidden Markov Model.
Not all of the phrases will make sense. But they will be useful for brainstorming and
they will all end with a word that rhymes with your target word or phrase."]
    [:p.tl "One way to use this input field is to find a rhyming phrase that you like and
then copy and paste that rhyming phrase into the Generate Lyric From Seed field
to generate many lines that all end with that specific phrase."]
    (form/form-to
     [:get (util/route-name->path
             request
             :com.darklimericks.server.router/lyric-from-seed)]
     (form/label
      "lyric-from-seed"
      "Target word or phrase for which to find a rhyming lyric")
     " "
     (form/text-field
      {:placeholder "instead of war on poverty"}
      "seed")
     (form/submit-button
      {:class "ml2"}
      "Generate lyric from seed word or phrase"))
    (when lyrics-from-seed
      lyrics-from-seed)]

   [:div
    [:h2 "Generate Lyric From Seed"]
    [:p.tl "Use the input field below to enter a word or phrase and view a list of
phrases that end with that word or phrase. Phrases will be generated from a
4-gram Hidden Mrakov Model. This option is useful if you know exactly what word
you want your lyric to end with but want help brainstorming the beginning of the
lyric."]
    [:p.tl "A good way to use this field is to first use the Generate Rhyming Lyric
field above to find a rhyming phrase that you like, then use this field to generate
prefixes to that rhyming phrase."]
    (form/form-to
     [:get (util/route-name->path
             request
             :com.darklimericks.server.router/rhyming-lyric)]
     (form/label
      "rhyming-lyric-target"
      "Target word or phrase for which to generate prefixes")
     " "
     (form/text-field
      {:placeholder "instead of war on poverty"}
      "rhyming-lyric-target")
     (form/submit-button
      {:class "ml2"}
      "Generate lyrics ending with seed word or phrase"))
    (when rhyming-lyrics
      rhyming-lyrics)]
   [:div#myChart]
   [:br]
   [:br]
   [:br]
   [:iframe {:src "/assets/README_WGU.htm"
             :style "background-color: white; width: 100%; height: 760px;"}]])

(defn lyric-suggestions
  [request suggestions]
  [:div
   (wgu
    request
    {:rhyming-lyrics
     [:table {:style "margin: auto;"}
      [:tr
       [:th "Lyric"]
       [:th "OpenNLP Perplexity"]
       [:th "Per-word OpenNLP Perplexity"]]
      (let [suggestions
            (for [suggestion suggestions]
              (cons suggestion (linguistics/open-nlp-perplexity suggestion)))]
        (for [[suggestion parse perplexity per-word-perplexity]
              (sort-by (comp - last) suggestions)]
          [:tr
           [:td suggestion]
           [:td perplexity]
           [:td per-word-perplexity]]))]})])

(defn show-rhyme-suggestion
  [request suggestions]
  [:div
   (wgu
    request
    {:rhymes
     (for [[suggestion p1 freq _ p2 quality] suggestions]
       [:div (string/join " - " [suggestion freq p1 p2])])})])

(defn rhymes-with-quality-and-frequency
  [request suggestions]
  (let [rhymes (->> (for [[pronunciation rhymes] suggestions]
                      (for [[phones rhyme] rhymes]
                        rhyme))
                    flatten
                    distinct)
        grouped-by-quality (group-by :rhyme-quality rhymes)
        top-20-by-quality (reduce
                           (fn [acc [_ rhymes]]
                             (into acc (take 20 (sort-by
                                                 (comp - :freq)
                                                 rhymes))))
                           []
                           grouped-by-quality)
        top-20-rhyme (take 60 (sort-by
                               (juxt (comp - :rhyme-quality)
                                     (comp - :freq))
                               top-20-by-quality))]
    (wgu
     request
     {:rhymes
      [:div
       [:table {:style "margin: auto;"}
        [:tr [:th "Rhyme"] [:th "Pronunciation"] [:th "Quality"] [:th "Frequency"]]
        (for [{:keys [word pronunciation rhyme-quality freq]} top-20-rhyme]
          [:tr [:td word] [:td (String/join "-" pronunciation)] [:td rhyme-quality] [:td freq]])]]})))

(defn lyrics-from-seed
  [request seed]
  (let [suggestions (linguistics/wgu-lyric-suggestions
                     (-> request :params :seed))]
    (wgu
     request
     {:lyrics-from-seed
      [:div
       [:table {:style "margin: autoh"}
        [:tr [:th "Rhyme"] [:th "Quality"] [:th "Lyric"] [:th "Perplexity"]]
        (for [[seed [line [parse perp per-word-perp]]] suggestions]
          [:tr
           [:td (:word seed)]
           [:td (:rhyme-quality seed)]
           [:td line]
           [:td per-word-perp]])]]})))
