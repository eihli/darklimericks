(ns user
  (:require [clojure.string :as string]
            [clojure.tools.deps.alpha.repl :refer [add-lib]]
            [oz.core :as oz]
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
  (require '[clojure.java.jdbc :as sql])

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

  (->> (car/wcar
        (-> state/system :database.kv/connection)
        #_#_(car/ping)
        (car/set "baz" "buzz")
        (car/keys "*"))
       (map
        #(car/wcar (-> state/system :database.kv/connection) (car/get %))))

  (car/wcar
   (-> state/system :database.kv/connection)
   (car/get "carmine:session:174a7525-04cb-4d73-a03c-1141bef6ad75"))

  (limericks/get-artist-and-album-for-new-limerick (-> state/system :database.sql/connection))

  (repeatedly
   1
   (fn []
     (let [handler (handlers/limerick-generation-post-handler
                    (-> state/system :com.darklimericks.db.core/connection)
                    (-> state/system :com.darklimericks.kv.core/connection))
           session-id "carmine:session:174a7525-04cb-4d73-a03c-1141bef6ad75"
           router (state/system :com.darklimericks.server.router/router)]
       (handler {:params {:scheme "A9 A9 B6 B6 A9" #_'((A 9) (A 9) (B 5) (B 5) (A 9))}
                 :session/key session-id
                 ::reitit/router router}))))

  ;; If the namespace gets dirty, this can clear it up.
  (run!
   #(ns-unalias (find-ns 'user) %)
   (keys (ns-aliases 'user)))

  ;; Making a request from the REPL
  (let [handler (handlers/show-rhyme-suggestion
                 (-> state/system :com.darklimericks.db.core/connection)
                 (-> state/system :com.darklimericks.kv.core/connection))
        router (state/system :com.darklimericks.server.router/router)]
    (handler {:params {:rhyme-target "foo"}
              ::reitit/router router}))

  (let [router (state/system :com.darklimericks.server.router/router)]
    (reitit/match-by-path router "/.well-known/foo.txt"))


  (db.albums/num-albums
   (-> state/system :database.sql/connection))

  (limericks/get-artist-and-album-for-new-limerick
   (-> state/system :database.sql/connection))

  (:template
   (reitit/match-by-path
    (-> state/system :com.darklimericks.server.router/router)
    "/rhymestorm/foo.html"))

  )

(comment
  (require '[oz.core :as oz])
  (oz/embed-for-html
   [:vega-lite
    {:data {:values [{:a 1 :b 2} {:a 3 :b 5} {:a 4 :b 2}]}
     :mark :point
     :encoding {:x {:field :a}
                :y {:field :b}}}])


  :done-comment)
