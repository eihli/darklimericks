(ns user
  (:require [clojure.string :as string]
            [clojure.tools.deps.alpha.repl :refer [add-lib]]
            [clojure.tools.namespace.repl :refer [set-refresh-dirs]]
            [integrant.repl :as repl]
            [taoensso.carmine :as car]
            [taoensso.carmine.message-queue :as car-mq]
            [integrant.repl.state :as state]
            [com.darklimericks.server.handlers :as handlers]
            [integrant.core :as ig]
            [hawk.core :as hawk]
            [clojure.java.io :as io]
            [com.darklimericks.util.identicon :as identicon]
            [com.darklimericks.server.limericks :as limericks]
            [com.darklimericks.db.albums :as db.albums]
            [com.darklimericks.db.limericks :as db.limericks]
            [com.darklimericks.server.util :as util]
            [com.darklimericks.server.system]
            [reitit.core :as reitit]))

(comment
  (-> state/system
      :app/router
      (reitit/routes))
 )

(set-refresh-dirs "src" "resources")

(defn add-project-dep
  ([lib-name lib-version]
   (let [dep-name (symbol lib-name)
         dep-version (name lib-version)]
     (add-lib dep-name {:mvn/version dep-version}))))

(defn- clojure-file? [_ {:keys [file]}]
  (re-matches #"[^.].*(\.clj|\.edn)$" (.getName file)))

(defn- auto-reset-handler [ctx event]
  (binding [*ns* *ns*]
    (integrant.repl/reset)     
    ctx))

(defn auto-reset []
  (hawk/watch! [{:paths ["src/" "resources/" "dev/"]
                 :filter clojure-file?
                 :handler auto-reset-handler}]))

(defn init []
  (let [config (-> "config.edn"
                   io/resource
                   slurp
                   ig/read-string)]
    (ig/load-namespaces config)
    (-> config
        ig/prep
        constantly
        repl/set-prep!)
    (repl/go)))

(defn reset []
  (repl/halt))

(comment
  (let [db (-> state/system :com.darklimericks.db.core/connection)
        session (java.util.UUID/fromString "47e25213-6cd7-493d-a92a-b5bae635c8f4")]
    (db.limericks/limericks-by-session db session))

  (require '[taoensso.carmine.ring :as cr])
  (let [kv (-> state/system :com.darklimericks.kv.core/connection)]
    (cr/carmine-store kv))

  (require '[com.darklimericks.db.limericks :as db.limericks])
  (let [session
        (java.util.UUID/fromString "47e25213-6cd7-493d-a92a-b5bae635c8f4")]
    (db.limericks/limericks-by-session
     (-> state/system :com.darklimericks.db.core/connection)
     session))
  (init)
  (let [db (-> state/system :database.sql/connection)
        albums (db.albums/most-recent-albums db)]
    (->> albums
         (map :album/name)
         (map #(string/replace % #" " "-"))
         (map string/lower-case)
         (map #(identicon/generate % 128))))

  (repeatedly
   5
   (fn []
     (car/wcar
      (-> state/system :database.kv/connection)
      (car-mq/enqueue "limericks" '((A 8) (A 8) (B 4) (B 4) (A 8))))))

  (car/wcar
   (-> state/system :database.kv/connection)
   (car/ping)
   (car/set "baz" "buzz")
   (car/get "baz"))
  (limericks/get-artist-and-album-for-new-limerick (-> state/system :database.sql/connection))

  (repeatedly
   1
   (fn []
     (let [handler (handlers/limerick-generation-post-handler
                    (-> state/system :database.sql/connection)
                    (-> state/system :app/cache))]
       (handler {:params {:scheme "A9 A9 B5 B5 A9" #_'((A 9) (A 9) (B 5) (B 5) (A 9))}}))))
 
  (db.albums/num-albums
   (-> state/system :database.sql/connection))

  (limericks/get-artist-and-album-for-new-limerick
   (-> state/system :database.sql/connection))

  (reitit/match-by-path
   (-> state/system :app/router)
   "/limericks/1/1")

  (let [router (-> state/system :app/router)]
    (util/route-name->path {::reitit/router router}
                           :com.darklimericks.server.system/artist
                           {:artist-id 1}))

  (let [router (-> state/system :app/router)]
    (util/route-name->path
     {::reitit/router router}
     :com.darklimericks.server.system/artist
     {:artist-id 1}))

  (let [router (-> state/system :app/router)]
    (->> :com.darklimericks.server.system/artist
         (#(reitit/match-by-name (::reitit/router {::reitit/router router})
                                 %
                                 {:artist-id 1}))
         (reitit/match->path)))
  )
