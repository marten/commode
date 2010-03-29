(ns commode.factoid
  (:require [clojure.contrib.sql :as sql]
            [dk.bestinclass.clojureql :as cql]
            [dk.bestinclass.clojureql.backend.mysql :as cql-mysql])
  (:use [commode.config :only (db)]))

(defn all-with-triggers-and-responses [] "Read all factoids with triggers and responses"
  (cql/run [@db results]
           (cql/raw (str "SELECT factoids.id     AS `factoid_id`, "
                         "       triggers.id     AS `trigger_id`, "
                         "       triggers.value  AS `trigger`, "
                         "       responses.id    AS `response_id`, "
                         "       responses.value AS `response`, "
                         "       responses.karma AS `karma` "
                         "  FROM factoids "
                         "LEFT OUTER JOIN responses ON responses.factoid_id = factoids.id "
                         "LEFT OUTER JOIN triggers  ON triggers.factoid_id  = factoids.id "
                         "ORDER BY `factoid_id`"))
           (doall results)))

(defn trigger-exists? [body]
  (cql/run [@db results]
           (cql/query triggers * (like ~body (concat "'%'" value "'%'")))
           (first results)))

(defn responses-for-trigger [trigger]
  (let [fid (:factoid_id trigger)]
    (cql/run [@db results]
             (cql/query responses * (= factoid_id ~fid))
             (doall results))))

(defn create-factoid []
  (let [now (java.util.Date.)]
    (cql/run [@db results]
             (cql/batch-statements
              (cql/insert-into factoids [created_at ~now
                                         updated_at ~now])
              (cql/raw "SELECT LAST_INSERT_ID()"))
             results)))

(defn create-trigger  [{factoid_id :factoid_id} value]
  (let [now (java.util.Date.)]
    (cql/run @db
             (cql/insert-into triggers [factoid_id ~factoid_id
                                        value      ~value
                                        created_at ~now
                                        updated_at ~now]))))

(defn create-response [{factoid_id :factoid_id} value]
  (let [now (java.util.Date.)]
    (cql/run @db
             (cql/insert-into triggers [factoid_id ~factoid_id
                                        value      ~value
                                        created_at ~now
                                        updated_at ~now]))))

(defn create-or-update-pair [trigger response]
  (let [trigger-record (trigger-exists? trigger)]
    (if trigger-record
      [trigger-record 
       (create-response {:factoid_id (:factoid_id trigger-record)} response)]
      (let [factoid-record  (create-factoid)
            trigger-record  (create-trigger factoid-record trigger)
            response-record (create-response factoid-record response)]
        [trigger-record 
         response-record]))))