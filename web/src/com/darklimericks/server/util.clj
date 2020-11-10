(ns com.darklimericks.server.util
  (:require [reitit.core :as reitit]
            [clojure.string :as string]))

(defn route-name->path
  ([request name]
   (route-name->path request name {}))
  ([request name params]
   (->> name
        (#(reitit/match-by-name (::reitit/router request) % params))
        reitit/match->path)))

(defn slug [s]
  (-> s
      (string/replace #"\s+" "_")
      string/lower-case))
