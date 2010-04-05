(ns commode.records.factoid
  (:require [clojure.contrib.sql :as sql]
            [commode.records.trigger :as trigger]
            [commode.records.response :as response])
  (:use [commode.config :only (db)]))

(defn create []
  (let [now (java.util.Date.)]
    (sql/with-connection @db
      (sql/transaction 
       (sql/insert-values :factoids 
                          [:created_at :updated_at] 
                          [now now])
       (let [id (sql/with-query-results res 
                  ["SELECT LAST_INSERT_ID()"] 
                  (val (first (first res))))]
         (sql/with-query-results res
           ["SELECT * FROM factoids WHERE id = ?" id]
           (first res)))))))

(defn create-or-update-pair [trigger-value response-value]
  (let [trigger (trigger/find-by-value trigger-value)]
    (if trigger
      [trigger
       (response/create {:factoid_id (:factoid_id trigger)
                         :value      response-value})]
      (let [factoid (create)
            trigger (trigger/create {:factoid_id (:id factoid)
                                     :value      trigger-value})
            response (response/create {:factoid_id (:id factoid)
                                       :value      response-value})]
        [trigger response]))))