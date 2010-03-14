(ns commode.factoid
  (:require [clojure.contrib.sql :as sql]
            clj-record.boot)
  (:use [commode.config :only (db)])
  (:gen-class))

(clj-record.core/init-model
 (:associations
  (has-many triggers)
  (has-many responses)))

(defn all-with-triggers-and-responses []
    "Read all factoids with triggers and responses"
  []
  (sql/with-connection db
    (sql/with-query-results res
      [(str "SELECT factoids.id     AS `factoid_id`, "
            "       triggers.id     AS `trigger_id`, "
            "       triggers.value  AS `trigger`, "
            "       responses.id    AS `response_id`, "
            "       responses.value AS `response`, "
            "       responses.karma AS `karma` "
            "  FROM factoids "
            "LEFT OUTER JOIN responses ON responses.factoid_id = factoids.id "
            "LEFT OUTER JOIN triggers  ON triggers.factoid_id  = factoids.id "
            "ORDER BY `factoid_id`")]
      (into [] res))))