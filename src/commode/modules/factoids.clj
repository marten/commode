(ns commode.modules.factoids
  (:require [commode.irc-util :as irc]
            [commode.factoid :as factoid])
  (:use [commode.core]
        [commode.message]
        [commode.util :only (random-element)]))

(defonce probability (ref 0.2))

;;;; Random responding to channel messages
(defresponder ::random-factoid-response 20
              (dfn (and (not (addressed-to-anyone? message))
                        (or (= channel "#ijbema") ; in #ijbema, the bot always listens
                            (< (rand) @probability))))
  (when-let [trigger (factoid/trigger-exists? (extract-message bot message))]
    (let    [responses (factoid/responses-for-trigger trigger)
             response  (random-element responses)]
      (println trigger)
      (println responses)
      (irc/say bot channel (:value response)))))


;;;; Display list of all known responses for trigger
(defresponder ::repeat-responses 0
              (dfn (and (addressed? bot message)
                        (re-find #"^herhaal " (extract-message bot message))))
  (let [trigger (factoid/trigger-exists? (extract-message bot message))]
    (if (not trigger)
      (irc/say bot channel (str (:sender message) ": sorry, niks gevonden"))
      (let    [responses (factoid/responses-for-trigger trigger)]
        (doseq [response responses]
          (irc/say bot channel (str "[" (:id response) "] " (:value response))))))))


;;;; Change the probability of chatter
(defresponder ::set-probability 0
              (dfn (and (addressed? bot message)
                        (re-find #"^!set probability [01]\.[0-9]+" (extract-message bot message))))
  (when-let [prob (-> (re-find #"[01]\.[0-9]+" (extract-message bot message))
                      (Float/parseFloat))]
    (dosync (ref-set probability prob))
    (irc/say bot channel (str (:sender message) ": okay, probability is now " @probability))))