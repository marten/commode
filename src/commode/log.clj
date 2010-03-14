(ns commode.log
  (:require clj-record.boot)
  (:use [commode.config :only (db)]))

(defn add-created-and-updated-timestamps [hash]
  (let [t (java.util.Date.)]
    (assoc hash :created_at t :updated_at t)))

(clj-record.core/init-model
 (:callbacks
  (:before-save add-created-and-updated-timestamps)))

;; (defn insert-log-entry [{channel :channel 
;;                          nick :sender
;;                          login :login
;;                          hostname :hostname
;;                          message :message}]
;;   (println "  Inserting log entry in db")
;;   (let [now (Date.)]
;;     (sql/with-connection db
;;       (sql/transaction
;;        (sql/insert-records :logs
;;                            {:channel channel :nick nick :login login :hostname hostname
;;                             :created_at now :updated_at now
;;                             :message message}
;;                            )))))
