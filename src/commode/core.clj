(ns commode.core
  (:require [clojure.contrib.sql :as sql]
            [clojure.contrib.str-utils2 :as s]
            [swank.swank :as swank])
  (:import (org.jibble.pircbot PircBot)
           (java.util.regex Pattern))
  (:use (util))
  (:gen-class))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; configuration ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def env (or (. (. System getenv) get "COMMODE_ENV") "development"))

(defn dev-test-prod [ifdev iftest ifprod]
  (cond
    (= env "development") ifdev
    (= env "test") iftest
    (= env "production") ifprod
    true ifdev))

;; some global constants and vars
(def bot)                                       ; pircbot object
(def channels (ref {}))                         ; {"channame" => Agent(chanstate)}
(def memory-sleep-ms 5000)                      ; amount of time between reloading memory from database
(def running true)                              ; yes, we want our monitor agents to run continuously

;; irc settings
(def irc-server "irc.mhil.net")                 ; connect to
(def irc-nick   (dev-test-prod "ijbetest" 
                               "ijbetest" 
                               "ijbema"))       ; my nick
(def irc-channels (dev-test-prod ["#ijbema"]
                                 ["#ijbema"]
                                 ["#brak" "#perio" "#ijbema"])) ; channels to join on startup

;; database settings
(let [db-host (dev-test-prod "localhost" "localhost" "10.177.154.146")
      db-port 3306
      db-name "ijbel"
      db-user (dev-test-prod "root" "root" "ijbel")
      db-pass (dev-test-prod "" "" "Onefbuwag5")]
  (def db {:classname "com.mysql.jdbc.Driver" ; must be in classpath
           :subprotocol "mysql"
           :subname (str "//" db-host ":" db-port "/" db-name)
           ; Any additional keys are passed to the driver
           ; as driver-specific properties.
           :user db-user
           :password db-pass}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; structs ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defstruct msg  :this :channel :sender :login :hostname :message)
(defstruct chan :channel :nicks)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; util ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn rand-elm "returns a random element from the sequence, or nil for an empty sequence"
  [seq] 
  (let [size (count seq)]
    (if (> size 0)
      (nth seq (rand-int size)))))

(defn rand-elm-weighted "takes a [{:weight x, ...} ...] sequence, and returns a weighted-random element"
  [seq]
  (let [weights (map :weight seq)
        minim   (reduce min weights)
        change  (+ 1 (* -1 minim)) ;; eg. [-2 -1 0 -2 +3] => [+1 +2 +3 +1 +4]
        normw   (map (partial + change) weights)
        total   (reduce + weights)
        target  (rand-int total)]
    (loop [seqn seq
           upto (:weight (first seq))]
      (if (>= upto target)
        (first seqn)
        (recur (rest seqn) (+ upto (:weight (first seqn))))))))

(defn starts-with? [prefix string]
  (.startsWith string prefix))

(defmacro async "just do this, I don't care" [& x]
  `(send-off (agent nil) (fn [& _#] ~@x )))

; (maybe 0.1 (foo) (bar))
(defmacro maybe [chance & fns]
  `(when (< (rand) ~chance)
     ~@fns))
    
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; irc helpers ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmacro do-chan [{channel :channel} fun & args]
  `(send-off (@channels ~channel) 
             (fn [a# & _#] 
               (dorun (~fun a# ~@args))
               a#)))

(defmacro alter-chan [{channel :channel} fun & args]
  `(send-off (@channels ~channel) 
             ~fun ~@args))

(defn say [chan & messages]
  (do-chan chan
           (dorun (map (fn [message]
                         (.sendMessage bot chan message)
                         message)
                       messages))))

(defn nicks [channel]
  (map #(.getNick %) (.getUsers bot channel)))

(defn join [channel]
  (dosync
   (alter channels assoc channel (agent (struct chan channel nil))))
  (println "JOINING" channel)
  (.joinChannel bot channel))

(defn part [channel]
  (dosync
   (alter channels assoc channel nil))
  (.leaveChannel bot channel))

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
    (s/drop (s/butlast trigger 3) 3)
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

(defn nick-prefix-pattern []
  (re-pattern (str "^" (nick) "[:,]\\s")))

(defn addressed? [msg]
  (when (or (re-find (nick-prefix-pattern) (:message msg))
            (nil? (:channel msg)))
    msg))

(defn strip-nick-prefix [s]
  (let [match (re-find (nick-prefix-pattern) s)]
    (if match (.replaceFirst s match "") s)))

(defn strip-? [s]
  (if (= \? (last s))
    (subs s 0 (dec (count s)))
    s))

(defn dispatch [msg]
  (let [normalized-message (strip-nick-prefix (strip-? (:message msg)))]
    (cond
      (= normalized-message "karma++") :add-karma
      (= normalized-message "karma--") :rem-karma
      (addressed? msg)                 :lookup
      :else                            :default)))

(defmulti responder dispatch)

(defmethod responder :add-karma [msg]
  (say (:channel msg) "maar dat kan ik, helemaal niet"))

(defmethod responder :rem-karma [msg]
  (say (:channel msg) "maar dat kan ik, helemaal niet"))

(defmethod responder :lookup [msg]
  (when-let [response (lookup (:message msg))]
    (say (:channel msg) (transform msg response))))

(defmethod responder :default [msg]
  (when-let [response (lookup (:message msg))]
    (maybe 0.2 (say (:channel msg) (transform msg response)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; irc ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn handleMessage [this channel sender login hostname message]
  (responder (struct msg
                     this channel sender login hostname message)))

(defn handlePrivateMessage [this sender login hostname message]
  (handleMessage this nil sender login hostname message))

(defn pircbot []
  (proxy [PircBot] []
    (onServerResponse [code response] 
                      (if (= code 376)
                        (dorun (map join irc-channels))))
    (onMessage [channel sender login hostname message]
               (handleMessage this channel sender login hostname message))
    (onInvite [us sender login hostname channel]
              (join channel))
    (onPrivateMessage [sender login hostname message]
                      (handlePrivateMessage this sender login hostname message))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; GO GO GO GO ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn -main [& args]
  
  (def bot (pircbot))
  (.setVerbose bot true)
  (.connect bot irc-server)
  (.changeNick bot irc-nick)

  (send-off memory memory-reloader) ; periodically reload memory
  (swank/start-repl)                ; get me a nice repl
)

; eof