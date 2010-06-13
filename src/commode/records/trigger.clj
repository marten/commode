(ns commode.records.trigger
  (:require [clojure.contrib.sql :as sql])
  (:use [commode.config :only (db)]))

(defn create [attrs]
  (let [now (java.util.Date.)
        attrs (merge attrs {:created_at now :updated_at now})]
    (sql/with-connection @db
      (sql/transaction
       (sql/insert-records :triggers attrs)
       (let [id (sql/with-query-results res
                  ["SELECT LAST_INSERT_ID()"]
                  (val (first (first res))))]
         (sql/with-query-results res
           ["SELECT * FROM triggers WHERE id = ?" id]
           (first res)))))))

(defn find-by-value [value]
  (sql/with-connection @db
    (sql/with-query-results rows
      ["SELECT * FROM triggers WHERE value = ? LIMIT 1" value]
      (first rows))))

(defn find-all-by-message [body]
  (sql/with-connection @db
    (sql/with-query-results rows
      ["SELECT * FROM triggers WHERE ? RLIKE CONCAT('[[:<:]]', value, '[[:>:]]')" body]
      (into [] rows))))