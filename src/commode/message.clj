(ns commode.message
  (:require [commode.irc-util :as irc])
  (:use [clojure.contrib.str-utils2 :only (join)]))

(defn addressed-to-anyone? [{body :body}]
  (re-find #"^.+: " body))

(defn nick-prefix-pattern [bot]
  (re-pattern (str "^" (irc/nick bot) "[:,]\\s")))

(defn addressed? [bot message]
  (or (re-find (nick-prefix-pattern bot) (:body message))
      false))

(defn strip-nick-prefix [bot body]
  (let [match (re-find (nick-prefix-pattern bot) body)]
    (if match (.replaceFirst body match "") body)))

(defn extract-message [bot {body :body}]
  (->> body
       (strip-nick-prefix bot)))

(defn reply [{body :body sender :sender} & strings]
  (str sender ": "
       (join " " strings)))

;; (defn strip-? [s]
;;   (if (= \? (last s))
;;     (subs s 0 (dec (count s)))
;;     s))

;; (defn is-describe-message [s]
;;   (re-find #"^herhaal " s))

;; (defn is-assign-message [s]
;;   (re-find #"(.*) = (.*)" s))
