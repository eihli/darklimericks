(ns com.darklimericks.server.db
  (:require [clojure.tools.namespace.repl :as c.t.n.r]))

(c.t.n.r/disable-unload!)

(defonce db (atom {}))
