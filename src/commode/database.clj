(ns commode.database
  (:use [clojure.contrib.sql :as sql :only ()]))

(let [db-host "localhost"
      db-port 3306
      db-name "ijbel"] 
  (def db {:classname "com.mysql.jdbc.Driver" ; must be in classpath
           :subprotocol "mysql"
           :subname (str "//" db-host ":" db-port "/" db-name)
           ; Any additional keys are passed to the driver
           ; as driver-specific properties.
           :user "root"
           :password ""}))

;;; SELECT factoids.id AS `factoid_id`, 
;;;        triggers.id AS trigger_id,   triggers.value AS `trigger_value`,
;;;        responses.id AS response_id, responses.value AS response_value 
;;;   FROM factoids
;;;   LEFT OUTER JOIN responses ON responses.factoid_id = factoids.id
;;;   LEFT OUTER JOIN triggers  ON triggers.factoid_id  = factoids.id;

(defn read-factoids
  "Read all factoids with triggers and responses"
  []
  (clojure.contrib.sql/with-connection db
    (clojure.contrib.sql/with-query-results res
      [(str "SELECT factoids.id AS `factoid_id`, "
            "triggers.id AS trigger_id,   triggers.value  AS `trigger`, "
            "responses.id AS response_id, responses.value AS `response` "
            "FROM factoids "
            "LEFT OUTER JOIN responses ON responses.factoid_id = factoids.id "
            "LEFT OUTER JOIN triggers  ON triggers.factoid_id  = factoids.id")]
      (into [] res))))