(ns commode.modules.factoids
  (:require [commode.irc-util :as irc]
            [commode.factoid :as factoid])
  (:use [commode.core]
        [commode.message]
        [commode.util :only (random-element)]))

;;;; Variables

(def probability (ref 0.2))
(def channels (ref {}))

;;;; Helpers

(defn prep-response [bot channel message response]
  (loop [r (-> response
               (.replaceAll "<reply>" "")
               (.replaceAll "\\\\n" "; "))]
    (cond
      (re-find #"\$who" r)     (recur (.replace r "$who" (:sender message)))
      (re-find #"\$someone" r) (recur (.replace r "$someone" (->> (irc/nicks bot channel)
                                                                  (filter #(not (= % (irc/nick bot))))
                                                                  random-element)))
      ;; TODO add recursive $thing lookups
      :else                    r
      )))

;;;; Random responding to channel messages
(defresponder ::random-factoid-response 20
              (dfn (and (not (addressed-to-anyone? message))
                        (or (= channel "#ijbema") ; in #ijbema, the bot always listens
                            (< (rand) @probability))))
  (when-let [trigger (factoid/trigger-exists? (extract-message bot message))]
    (let    [responses (factoid/responses-for-trigger trigger)
             response  (random-element responses)]
      (println "  Responding with:" (:value response))
      (dosync (alter channels assoc channel (assoc (@channels channel) :last-response {:trigger trigger 
                                                                                       :response (assoc response :when (java.util.Date.))})))
      (irc/say bot channel (prep-response bot channel message (:value response))))))

;;;; Why did we just respond?
(defresponder ::what-factoid-was-that 0
              (dfn (and (addressed? bot message)
                        (re-find #"^!wtf" m)))
  (irc/say bot channel (reply message "reactie op ``" (-> (@channels channel) :last-response :trigger :value) "''")))

;;;; Display list of all known responses for trigger
(defresponder ::repeat-responses 0
              (dfn (and (addressed? bot message)
                        (re-find #"^herhaal " m)))
  (let [trigger (factoid/trigger-exists? m)]
    (if (not trigger)
      (irc/say bot channel (reply message "dat ken ik niet"))
      (let    [responses (factoid/responses-for-trigger trigger)]
        (doseq [response responses]
          (irc/say bot channel (str "[" (:id response) "] " (:value response))))))))

;;;; Add a new factoid
(defresponder ::add-factoid 0
              (dfn (and (addressed? bot message)
                        (re-find #"^.* = .*$" m)))
  (let [trigger (second (re-find #"^(.*) =" m))
        response (second (re-find #"= (.*)$" m))]
    (factoid/create-or-update-pair trigger response)
    (irc/say bot channel (reply message "okay."))))

;;;; Change the probability of chatter
(defresponder ::set-probability 0
              (dfn (and (addressed? bot message)
                        (re-find #"^!set probability [01]\.[0-9]+" m)))
  (when-let [prob (-> (re-find #"[01]\.[0-9]+" m)
                      (Float/parseFloat))]
    (dosync (ref-set probability prob))
    (irc/say bot channel (reply message "mkay, probability is nu" @probability))))