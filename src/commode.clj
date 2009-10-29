(ns main
  (:use [clojure.contrib.sql :as sql :only ()])
  (:import (org.jibble.pircbot PircBot)
           (java.util.regex Pattern)))

(require '[clojure.contrib.str-utils2 :as s])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; configuration ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; some global constants and vars
(def bot)                                       ; will hold pircbot object
(def channels (ref {}))                         ; will hold :channame => Agent(chanstate
(def memory-sleep-ms 5000)                      ; amount of time between reloading memory from database
(def running true)                              ; yes, we want our monitor agents to run continuously

;; irc settings
(def irc-server "irc.mhil.net")                 ; connect to
(def irc-nick   "ijbema")                       ; my nick
(def irc-channels ["#brak" "#perio" "#ijbema"]) ; channels to join on startup

;; database settings
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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; util ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn rand-elm "returns a random element from the sequence, or nil for an empty sequence"
  [seq] 
  (let [size (count seq)]
    (if (> size 0)
      (nth seq (rand-int (count seq))))))

(defn rand-elm-weighted "takes a [{:weight x, ...} ...] sequence, and returns a weighted-random element"
  [seq]
  nil) ;; TODO

(defn starts-with [prefix string]
  (.startsWith string prefix))

(defmacro async "just do this, I don't care" [& x]
  `(send-off (agent nil) (fn [& _#] ~@x )))

; (maybe 0.1 (foo) (bar))
(defmacro maybe [chance & fns]
  `(when (< (rand) ~chance)
     ~@fns))
    
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; irc helpers ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn say [destination & messages]
  (dorun (map (fn [message]
                (.sendMessage bot destination message)
                message)
              messages))))

(defn nicks [channel]
  (map #(.getNick %) (.getUsers bot channel)))

(defn join [channel]
  (.joinChannel bot channel))

(defn nick 
  ([] (.getNick bot))
  ([s] (.changeNick bot s)))

(defn op 
  ([channel nick] (.op bot channel nick)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; database ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn read-factoids
  "Read all factoids with triggers and responses"
  []
  (sql/with-connection db
    (sql/with-query-results res
      [(str "SELECT factoids.id     AS `factoid_id`, "
            "       triggers.id     AS `trigger_id`, "
            "       triggers.value  AS `trigger`, "
            "       responses.id    AS `response_id`, "
            "       responses.value AS `response`, "
            "       responses.karma AS `karma` "
            "  FROM factoids "
            "LEFT OUTER JOIN responses ON responses.factoid_id = factoids.id "
            "LEFT OUTER JOIN triggers  ON triggers.factoid_id  = factoids.id "
            "ORDER BY `factoid_id`")]
      (into [] res))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; memory ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def memory (agent nil))

(defn memory-reloader [x]
   (when running
     (send-off *agent* #'memory-reloader))
   (. Thread (sleep memory-sleep-ms))
   (read-factoids))

(defn regex-trigger? 
  "check if the trigger is a regex, returns trigger without surrounding slashes"
  [trigger]
  (if (and (= (first trigger) \/)
           (= (last  trigger) \/))
    (s/drop (s/butlast trigger 1) 1)
    nil))

(defn trigger-matches? [trigger message]
  (let [re-trigger (regex-trigger? trigger)]
    (if re-trigger
      (re-find (re-pattern re-trigger) message)
      (= trigger message))))

(defn responses [message]
  (map :response (filter #(trigger-matches? (:trigger %) message)
                         @memory)))

(defn lookup [message]
  (rand-elm (responses message)))

(defn transform [msg response]
  (let [channel (:channel msg)
        sender  (:sender  msg)]
    (loop [m (.replace response \newline \ )]
      (cond
        ;; replace all $who with sender
        (re-find #"\$who" m) 
          (recur (.replace m "$who" sender))
        ;; replace all $someone with a random nick
        (re-find #"\$someone" m) 
          (recur (.replace m "$someone" 
                             (rand-elm (nicks channel))))
        ;; replace all $whatever with a random element defining $whatever
        (re-find #"\$\w+" m)
          (recur (.replace m (re-find #"\$\w+" m)
                             (lookup (re-find #"\$\w+" m))))
        :else 
          m))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; messages ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defstruct msg :this :channel :sender :login :hostname :message)

(defn nick-prefix-pattern []
  (re-pattern (str "^" (nick) "[:,]\\s")))

(defn addressed? [msg]
  (when (or (re-find (nick-prefix-pattern) (:message msg))
            (nil? (:channel msg)))
    msg))

(defn strip-nick-prefix [s]
  (let [match (re-find (nick-prefix-pattern) s)]
    (.replaceFirst s match "")))

(defn strip-? [s]
  (if (= \? (last s))
    (subs s 0 (dec (count s)))
    s))

(defn origin [msg]
  (if (:channel msg)
    (:channel msg)
    (:sender  msg)))

(defn dispatch [msg]
  (let [normalized-message (strip-nick-prefix (strip-? (:message msg)))]
    (cond
      (= normalized-message "!help") :help
      (addressed? msg) :lookup)))

(defmulti responder dispatch)

(defmethod responder :help [msg]
  (say (origin msg)
       "ja hallo, nou moet ik zeker al m'n geheime truukjes op tafel leggen"))

(defmethod responder :lookup [msg]
  (let [response (lookup (:message msg))]
    (if response
      (action (origin msg)
              (transform msg (lookup (:message msg)))))))

(defmethod responder :default [msg]
  (let [response (lookup (:message msg))]
    (if response
      (maybe 0.2 (action (origin msg)
                         (transform msg response))))))

(defn action [destination response]
  (say destination 
       (.replaceFirst response "<reply>" "")))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; irc ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn handleMessage [this channel sender login hostname message]
  (responder (struct msg
                     this channel sender login hostname message)))

(defn handlePrivateMessage [this sender login hostname message]q
  (handleMessage this nil sender login hostname message))

(defn pircbot []
  (proxy [PircBot] []
    (onMessage [channel sender login hostname message]
               (handleMessage this channel sender login hostname message))
    (onPrivateMessage [sender login hostname message]
                      (handlePrivateMessage this sender login hostname message))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; GO GO GO GO ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def bot (pircbot))
(.setVerbose bot true)
(.connect bot irc-server)
(.changeNick bot irc-nick)
(map join irc-channels)

(send-off memory memory-reloader)   ; periodically reload memory

; eof