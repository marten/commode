(ns commode.records.response
  (:require [clojure.contrib.sql :as sql])
  (:use [commode.config :only (db)]))

;; (defn responses-for-trigger [trigger]
;;   (let [fid (:factoid_id trigger)]
;;     (cql/run [@db results]
;;              (cql/query responses * (= factoid_id ~fid))
;;              (doall results))))

(defn create [attrs]
  (let [now (java.util.Date.)
        attrs (merge attrs {:created_at now :updated_at now})]
    (sql/with-connection @db
      (sql/transaction
       (sql/insert-records :responses attrs)
       (let [id (sql/with-query-results res
                  ["SELECT LAST_INSERT_ID()"]
                  (val (first (first res))))]
         (sql/with-query-results res
           ["SELECT * FROM responses WHERE id = ?" id]
           (first res)))))))

(defn find-all-by-factoid-id [factoid-id]
  (sql/with-connection @db 
    (sql/with-query-results rows ["SELECT * FROM responses WHERE factoid_id = ?" factoid-id]
      (into [] rows))))