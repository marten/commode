;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ;; memory ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; (def memory (agent nil))

;; (defn memory-reloader [x]
;;    (when running
;;      (send-off *agent* #'memory-reloader))
;;    (. Thread (sleep memory-sleep-ms))
;;    (read-factoids))

;; (defn regex-trigger? 
;;   "check if the trigger is a regex, returns trigger without surrounding slashes"
;;   [trigger]
;;   (if (and (= (first trigger) \/)
;;            (= (last  trigger) \/))
;;     (s/drop (s/butlast trigger 1) 1)
;;     nil))

;; (defn trigger-matches? [trigger message]
;;   (let [re-trigger (regex-trigger? trigger)]
;;     (if re-trigger
;;       (re-find (re-pattern re-trigger) message)
;;       (= trigger message))))

;; (defn responses [message]
;;   (map :response (filter #(trigger-matches? (:trigger %) message)
;;                          @memory)))

;; (defn lookup [message]
;;   (let [resp (responses message)]
;;     (println "  Found" (count resp) "responses.")
;;     (rand-elm resp)))

;; (defn transform [msg response]
;;   (let [channel (:channel msg)
;;         sender  (:sender  msg)]
;;     (loop [m (.replace response \newline \ )]
;;       (cond
;;         ;; replace <reply> with nil
;;         (re-find #"<reply>" m)
;;           (recur (.replace m "<reply>" ""))
;;         ;; replace all $who with sender
;;         (re-find #"\$who" m) 
;;           (recur (.replace m "$who" sender))
;;         ;; replace all $someone with a random nick
;;         (re-find #"\$someone" m) 
;;           (recur (.replace m "$someone" 
;;                              (rand-elm (nicks channel))))
;;         ;; replace all $whatever with a random element defining $whatever
;;         (re-find #"\$\w+" m)
;;           (recur (.replace m (re-find #"\$\w+" m)
;;                              (lookup (re-find #"\$\w+" m))))
;;         (re-find #"\n" m)
;;           (recur (.replace m "\n" ". "))
;;         :else 
;;           m))))

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ;; messages ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


;; (defn dispatch [msg]
;;   (let [message (:message msg)
;;         normalized-message (strip-nick-prefix (strip-? message))
;;         destination (cond
;;                       (= normalized-message "stats")           :stats
;;                       (= normalized-message "karma++")         :add-karma
;;                       (= normalized-message "karma--")         :rem-karma
;;                       (is-describe-message normalized-message) :describe
;;                       (is-assign-message normalized-message)   :assign
;;                       (addressed? msg)                         :lookup
;;                       :else                                    :default)]
;;     (println "  Message normalized to:" normalized-message)
;;     (println "  Dispatch table says go" destination)
;;     destination))

;; (defmulti responder dispatch)

;; (defmethod responder :stats [msg]
;;   (let [factoids  (count (set (map :factoid_id  @memory)))
;;         triggers  (count (set (map :trigger_id  @memory)))
;;         responses (count (set (map :response_id @memory)))]
;;     (println "  Stats; factoids:" factoids "triggers:" triggers "responses:" responses)
;;     (say (:channel msg)
;;          (str "Ik ken " factoids " factoids (" triggers " triggers en " responses " antwoorden daarop)."))))

;; (defmethod responder :add-karma [msg]
;;   (say (:channel msg) "maar dat kan ik, helemaal niet"))

;; (defmethod responder :rem-karma [msg]
;;   (say (:channel msg) "maar dat kan ik, helemaal niet"))

;; (defmethod responder :describe [msg]
;;   (doseq [r (responses (:message msg))]
;;     (println "  Describing" r)
;;     (say (:channel msg) r)))

;; (defmethod responder :assign [msg]
;;   (let [[message trigger response] (is-assign-message (:message msg))]
;;     (println "  Assigning" trigger "to" response))
;;   (say (:channel msg) "marten: fix dat"))

;; (defmethod responder :lookup [msg]
;;   (let [response (lookup (:message msg))]
;;     (if response
;;       (say (:channel msg) (transform msg response))
;;       (say (:channel msg) "$404"))))

;; (defmethod responder :default [msg]
;;   (when-let [response (lookup (:message msg))]
;;     (println "  Response is:" (transform msg response))
;;     (maybe 0.2 (say (:channel msg) (transform msg response)))))

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ;; irc ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
