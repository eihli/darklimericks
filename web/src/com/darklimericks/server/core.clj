(ns com.darklimericks.server.core
  (:gen-class)
  (:require [integrant.core :as ig]
            [com.darklimericks.server.system :as system]
            [reitit.coercion]
            [reitit.coercion.spec]
            [taoensso.timbre :as timbre]
            [clojure.java.io :as io]))


(def a (atom {}))

(defn -main []
  (try
    (let [system (->> "server/config.edn"
                      io/resource
                      slurp
                      ig/read-string
                      ig/prep
                      ig/init)]
      (timbre/info "Running with config: server/config.edn" )
      system)
    (catch Throwable e
      (.printStackTrace e)
      (System/exit 1))))

