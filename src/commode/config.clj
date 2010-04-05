(ns commode.config)

;;;; Slots

(def bot (ref {}))
(def db (ref {}))

(defn make-connection-info [host user pass dbname]
  {:classname "com.mysql.jdbc.Driver"
   :subprotocol "mysql"
   :subname (str "//" host ":" 3306 "/" dbname)
   :user user
   :password pass})

(defn init [botobj]
   (let [[host user pass dbname] (:db botobj)]
     (dosync 
      (ref-set bot botobj)
      (ref-set db (make-connection-info host user pass dbname)))))