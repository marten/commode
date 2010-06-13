(ns commode.records.log
  (:require [clojure.contrib.sql :as sql])
  (:use [commode.config :only (db)]))

(defn create [attrs]
  (let [now (java.util.Date.)
        attrs (merge attrs {:created_at now :updated_at now})]
    (sql/with-connection @db
      (sql/transaction 
       (sql/insert-records :logs attrs)
       (let [id (sql/with-query-results res 
                  ["SELECT LAST_INSERT_ID()"] 
                  (val (first (first res))))]
         (sql/with-query-results res
           ["SELECT * FROM logs WHERE id = ?" id]))))))