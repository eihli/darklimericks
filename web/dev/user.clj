(ns user
  (:require [clojure.tools.deps.alpha.repl :refer [add-lib]]
            [clojure.tools.namespace.repl :refer [set-refresh-dirs]]
            [integrant.repl :as repl]
            [integrant.repl.state :as state]
            [migratus.core :as migratus]
            [com.darklimericks.server.handlers :as handlers]
            [integrant.core :as ig]
            [hawk.core :as hawk]
            [clojure.java.io :as io]
            [com.darklimericks.server.limericks :as limericks]
            [com.darklimericks.server.util :as util]
            [com.owoga.prhyme.limerick :as limerick]
            [com.darklimericks.server.system]
            [reitit.core :as reitit]))

(comment
  (-> state/system
      :app/router
      (reitit/routes))
 )

(set-refresh-dirs "src" "dev" "resources")

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
  (repl/halt)
  (-> "server/config.edn"
      io/resource
      slurp
      ig/read-string
      ig/prep
      constantly
      repl/set-prep!)
  (repl/prep)
  (repl/init))

(defn reset []
  (repl/halt))

(comment
  (init)
  (auto-reset)
  (limericks/get-artist-and-album-for-new-limerick (-> state/system :database.sql/connection))
  (let [handler (handlers/limerick-generation-post-handler
                 (-> state/system :database.sql/connection)
                 (-> state/system :app/cache))]
    (handler {:params {:scheme "A9 A9 B5 B5 A9" #_'((A 9) (A 9) (B 5) (B 5) (A 9))}}))

  (reitit/match-by-path
   (-> state/system :app/router)
   "/limericks/1/1")

  (let [router (-> state/system :app/router)]
    (util/route-name->path {::reitit/router router}
                           :com.darklimericks.server.system/artist))
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
