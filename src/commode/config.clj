(ns commode.config
  (:require [dk.bestinclass.clojureql :as cql]
            [dk.bestinclass.clojureql.backend.mysql :as cql-mysql]))

;;;; Slots

(def bot (ref {}))
(def db (ref nil))

(defn init [botobj]
   (let [[host user pass dbname] (:db botobj)]
     (dosync
      (ref-set bot botobj)
      (ref-set db (cql/make-connection-info "mysql" (str "//" host "/" dbname) user pass)))))