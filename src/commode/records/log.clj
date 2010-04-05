(ns commode.records.log
  (:require [clojure.contrib.sql :as sql])
  (:use [commode.config :only (db)]))

(defn create []
  (let [now (java.util.Date.)]
    (sql/with-connection @db
      (sql/transaction 
       (sql/insert-values :logs
                          [:created_at :updated_at] 
                          [now now])
       (let [id (sql/with-query-results res 
                  ["SELECT LAST_INSERT_ID()"] 
                  (val (first (first res))))]
         (sql/with-query-results res
           ["SELECT * FROM logs WHERE id = ?" id]))))))