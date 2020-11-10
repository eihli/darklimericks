(ns com.darklimericks.server.db
  (:require [clojure.tools.namespace.repl :as c.t.n.r]))

(c.t.n.r/disable-reload!)

(defonce db (atom {}))

(defonce worker (atom nil))

(comment
  (reset! worker nil))
