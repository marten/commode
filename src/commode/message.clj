(ns commode.message
  (:gen-class))

;; (defn nick-prefix-pattern []
;;   (re-pattern (str "^" (nick) "[:,]\\s")))

;; (defn addressed? [msg]
;;   (when (or (re-find (nick-prefix-pattern) (:message msg))
;;             (nil? (:channel msg)))
;;     msg))

;; (defn strip-nick-prefix [s]
;;   (let [match (re-find (nick-prefix-pattern) s)]
;;     (if match (.replaceFirst s match "") s)))

;; (defn strip-? [s]
;;   (if (= \? (last s))
;;     (subs s 0 (dec (count s)))
;;     s))

;; (defn is-describe-message [s]
;;   (re-find #"^herhaal " s))

;; (defn is-assign-message [s]
;;   (re-find #"(.*) = (.*)" s))
