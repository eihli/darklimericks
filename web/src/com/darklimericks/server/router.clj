(ns com.darklimericks.server.router
  (:require [integrant.core :as ig]
            [taoensso.timbre :as timbre]
            [reitit.http :as http]
            [reitit.coercion.spec]
            [reitit.http.interceptors.parameters :refer [parameters-interceptor]]
            [com.darklimericks.server.handlers :as handlers]
            [com.darklimericks.server.interceptors :as interceptors]
            [reitit.ring :as ring]))

(defmethod ig/init-key ::router [_ {:keys [db cache]}]
  (let [routes [["/" {:name ::home
                      :get {:handler (handlers/home-handler db)}}]
                ["/{letter}.html"
                 {:name ::artists-by-letter
                  :handler (handlers/artists-by-letter db)}]
                ["/limerick-generation-task"
                 {:name ::limerick-generation-task
                  :post {:handler (handlers/limerick-generation-post-handler db cache)}
                  :get {:handler (handlers/submit-limericks-get-handler db)}}]
                ["/submit"
                 {:name ::submit
                  :get {:handler (handlers/submit-limericks-get-handler db)}}]
                ["/limericks"
                 ["/{artist-name}-{artist-id}/{album-name}-{album-id}"
                  {:name ::album
                   :coercion reitit.coercion.spec/coercion
                   :parameters {:path {:artist-id int?
                                       :album-id int?}}
                   :get {:handler (handlers/limericks-get-handler db cache)}}]
                 ["/{artist-name}-{artist-id}"
                  {:name ::artist
                   :coercion reitit.coercion.spec/coercion
                   :parameters {:path {:artist-id int?}}
                   :get {:handler (handlers/artist-get-handler db)}}]]
                ["/assets/*" handlers/file-handler]
                ["/rhymestorm"
                 [""
                  {:name ::rhymestorm
                   :get {:handler (handlers/rhymestorm db cache)}
                   :post {:handler (handlers/rhyming-lyric db cache)}}]
                 ["/rhyme"
                  {:name ::rhyme
                   :get {:handler (handlers/rhymes-with-quality-and-frequency db cache)}}]
                 ["/rhyming-lyric"
                  {:name ::rhyming-lyric
                   :get {:handler (handlers/rhyming-lyric db cache)}}]
                 ["/lyric-from-seed"
                  {:name ::lyric-from-seed
                   :get {:handler (handlers/lyrics-from-seed db cache)}}]]
                ["/.well-known/*" (ring/create-file-handler
                                   {:root "resources/public/.well-known"})]]]
    (timbre/info "Starting router.")
    (http/router
     routes
     {:data {:interceptors [(interceptors/session-interceptor cache)
                            interceptors/coerce-request-interceptor
                            interceptors/logging-interceptor
                            interceptors/format-interceptor
                            (parameters-interceptor)
                            interceptors/keywordize-params-interceptor]}})))
