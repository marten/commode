;; configuration ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def running true)

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

;; amount of time between reloading memory from database
(def memory-sleep-ms 5000)


;; database ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

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
            "LEFT OUTER JOIN triggers  ON triggers.factoid_id  = factoids.id "
            "ORDER BY `factoid_id`")]
      (into [] res))))

;; memory ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def memory (agent nil))

(defn memory-reloader [x]
   (when running
     (send-off *agent* #'memory-reloader))
   (. Thread (sleep memory-sleep-ms))
   (read-factoids))

;; messages ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defstruct message :time :channel :sender :body)

(defn responses [body]
  (filter #(= (:trigger %) body)
	  @memory))

(defn process [message]
  (let [body (:body message)
	responses (responses body)]
    (nth responses (rand-int (count responses)))))

;; GO GO GO GO ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(send-off memory memory-reloader)   ; periodically reload memory