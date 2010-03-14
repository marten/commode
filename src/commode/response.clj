(ns commode.response
  (:require clj-record.boot)
  (:use [commode.config :only (db)]))

(clj-record.core/init-model
 (:associations
  (belongs-to factoid)))