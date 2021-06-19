(ns com.darklimericks.server.handlers
  (:require [taoensso.timbre :as timbre]
            [hiccup.core :as hiccup]
            [next.jdbc :as jdbc]
            [honeysql.core]
            [honeysql.helpers]
            [reitit.ring :as ring]
            [reitit.http :as http]
            [reitit.interceptor.sieppari :as sieppari]
            [integrant.core :as ig]
            [clojure.string :as string]
            [taoensso.carmine :as car]
            [taoensso.carmine.message-queue :as car-mq]
            [com.darklimericks.server.util :as util]
            [com.darklimericks.db.albums :as db.albums]
            [com.darklimericks.db.limericks :as db.limericks]
            [com.darklimericks.db.artists :as db.artists]
            [com.darklimericks.server.views :as views]
            [com.darklimericks.server.limericks :as limericks]))

(defmethod ig/init-key ::handler [_ {:keys [router]}]
  (http/ring-handler
   router
   (ring/create-default-handler)
   {:executor sieppari/executor}))

(defn home-handler
  [db]
  (fn [request]
    (let [recent-albums (db.albums/most-recent-albums db)
          artists-by-album (into
                            {}
                            (map
                             (fn [{:album/keys [id artist_id]}]
                               (vector id (db.artists/artist db artist_id)))
                             recent-albums))]
      (timbre/info "home-handler")
      {:status 200
       :headers {"Content-Type" "text/html; charset=utf-8"}
       :body (hiccup/html (views/home
                           db
                           request
                           recent-albums
                           artists-by-album))})))

(def resource-handler (ring/create-resource-handler {:allow-symlinks? true}))

(defn limerick-generation-post-handler
  [db cache]
  (fn [{{:keys [scheme]} :params
        session-id :session/key
        :as request}]
    (if-let [session-id (-> session-id
                            (string/split #":")
                            (nth 2)
                            java.util.UUID/fromString)]
      (let [scheme (limericks/parse-scheme scheme)
            mid (car/wcar db (car-mq/enqueue
                              "limericks"
                              {:scheme scheme
                               :session-id session-id}))]
        {:status 301
         :headers {"Content-Type" "text/html; charset=utf-8"}
         :body (views/wrapper
                db
                request
                {}
                [:h1 "Creating your limerick..."]
                [:div "Submission processing... " mid])})
      {:status 400
       :headers {"Content-Type" "text/html; charset=utf-8"}
       :body (views/wrapper
              db
              request
              {}
              [:div "Enable cookies to submit limericks"])})))

(defn limericks-get-handler [db cache]
  (fn [request]
    (let [artist-id (get-in request [:parameters :path :artist-id])
          artist (db.artists/artist db artist-id)
          album-id (get-in request [:parameters :path :album-id])
          album (db.albums/album db album-id)
          limericks (db.limericks/album-limericks db album-id)]
      {:status 200
       :headers {"Content-Type" "text/html; charset=utf-8"}
       :body (views/wrapper
              db
              request
              {}
              [:div.flex.items-center.flex-column
               [:div.f3.pt4.light-yellow
                (->> artist
                     :artist/name
                     (format "%s LYRICS")
                     (string/upper-case))]
               [:div.f4.pv3
                [:strong
                 (->> album
                      :album/name
                      (format "album: %s"))]]
               (map-indexed
                (fn [i limerick]
                  (let [i (inc i)
                        limerick-url
                        (format
                         "%s#%s"
                         (util/route-name->path
                          request
                          :com.darklimericks.server.router/album
                          {:artist-id (:artist/id artist)
                           :artist-name (util/slug (:artist/name artist))
                           :album-id (:album/id album)
                           :album-name (util/slug (:album/name album))})
                         i)]
                    [:a.db.light-yellow
                     {:href limerick-url}
                     (format
                      "%s. %s"
                      i
                      (:limerick/name limerick))]))
                limericks)
               (map-indexed
                (fn [i limerick]
                  (views/limerick i limerick))
                limericks)
               [:div.f6.light-yellow.pt4.w-60
                (string/join
                 " / "
                 ["Thanks be to Optimus Prhyme"
                  "by blood honor these words sublime"
                  "sacred heuristics"
                  "glorious limericks"
                  "singularity we climb"])]
               [:div.f6.washed-yellow.pt3.w-60
                (string/join
                 " / "
                 ["Submissions, comments, corrections"
                  "adorations, exultations, rejections"
                  "all receieved freely"
                  "@owoga.com, eihli"
                  "by Optimus Prhyme's directions"])]])})))

(defn limerick-generation-get-handler [db cache]
  (fn [request]
    (let [tasks (:tasks @cache)]
      {:status 200
       :headers {"Content-Type" "text/html; charset=utf-8"}
       :body (views/wrapper
              db
              request
              {}
              (views/limerick-tasks tasks))})))

(defn artists-by-letter [db]
  (fn [{{:keys [letter]} :path-params :as req}]
    (let [artists (db.artists/artists-by-letter db (string/upper-case letter))]
      {:status 200
       :headers {"Content-Type" "text/html; charset=utf-8"}
       :body (views/wrapper
              db
              req
              {}
              (when artists
                (let [[right-hand-side-artists
                       left-hand-side-artists]
                      ((juxt #(take (quot (inc (count artists)) 2) %)
                             #(drop (quot (inc (count artists)) 2) %))
                       artists)]
                  [:div.bg-near-black.br4.flex
                   [:div.fl.w-50.pa2
                    (for [artist right-hand-side-artists]
                      [:a.washed-yellow.link
                       {:href (util/route-name->path
                               req
                               :com.darklimericks.server.router/artist
                               {:artist-id (:artist/id artist)
                                :artist-name (:artist/name artist)})}
                       (:artist/name artist)])]
                   [:div.fl.w-50.pa2
                    (for [artist left-hand-side-artists]
                      [:a.washed-yellow
                       {:href "#"}
                       (:artist/name artist)])]])))})))

(defn artist-get-handler [db]
  (fn [request]
    (let [artist-id (get-in request [:parameters :path :artist-id])
          artist (db.artists/artist db artist-id)
          albums (db.albums/artist-albums db artist-id)
          limericks (into {} (map
                              #(vector
                                (:album/id %)
                                (db.limericks/album-limericks db (:album/id %)))
                              albums))]
      {:status 200
       :headers {"Content-Type" "text/html; charset=utf-8"}
       :body (views/wrapper
              db
              request
              {}
              [:div
               [:div.f3.pt3.light-yellow
                (->> artist
                     :artist/name
                     (format "%s Lyrics")
                     (string/upper-case))]
               (for [album albums]
                 [:div
                  [:div.f4.pt3.light-yellow.pb3
                   (->> album
                        :album/name
                        (format "album: \"%s\""))]
                  (let [album-url (util/route-name->path
                                   request
                                   :com.darklimericks.server.router/album
                                   {:artist-id (:artist/id artist)
                                    :artist-name (util/slug (:artist/name artist))
                                    :album-id (:album/id album)
                                    :album-name (util/slug (:album/name album))})]
                    (map-indexed
                     (fn [index limerick]
                       [:a.f5.washed-yellow.db
                        {:href (format "%s#%s" album-url (inc index))}
                        (:limerick/name limerick)])
                     (get limericks (:album/id album))))])])})))

(defn submit-limericks-get-handler [db]
  (fn [request]
    (timbre/info request)
    (if-let [session-key (:session/key request)]
      (let [session-key (-> session-key
                            (string/split #":")
                            (nth 2)
                            java.util.UUID/fromString)
            limericks (db.limericks/limericks-by-session db session-key)]
        {:status 200
         :headers {"Content-Type" "text/html; charset=utf-8"}
         :body (views/wrapper
                db
                request
                {}
                (views/submit-limericks request limericks))})
      {:status 200
       :session {}
       :headers {"Content-Type" "text/html; charset=uft-8"}
       :body (views/wrapper
              db
              request
              {}
              (views/submit-limericks request []))})))

(defn wgu [db cache]
  (fn [request]
    {:status 200
     :headers {"Content-Type" "text/html; charset=utf-8"}
     :body (views/wrapper
            db
            request
            {}
            (views/wgu request))}))

(defn show-rhyme-suggestion [db cache]
  (fn [request]
    {:status 201
     :headers {"Content-Type" "text/html; charset=utf-8"}
     :body (views/wrapper
            db
            request
            {}
            (views/show-rhyme-suggestion request))}))
