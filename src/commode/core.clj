(ns commode.core
  (:require [pqueue :as pq]
            [commode.message :as message]))

;;;; Preprocessors

(defn preprocess-message [bot channel message]
  (vary-meta message assoc :extracted (message/extract-message bot message)))

;;;; Responder

(defonce *dispatchers* (ref pq/empty))

(defmacro dfn 
  "Creates a dispatch fn with 'bot bound to the bot object
   and 'msg bound to a struct representing the message"
  [& body]
  `(fn [~'bot ~'channel ~'message]
     (and ~@body)))

(defn dispatch [bot channel message]
  (println "  Dispatching!")
  (loop [dispatchers (pq/seq @*dispatchers*)]
    (when dispatchers
      (let [[check key] (first dispatchers)]
        (if (check bot channel message)
          (do (println " " key "I choose you!")
              key)
          (recur (seq (rest dispatchers))))))))

(defn remove-dispatch-hook [dispatch-value]
  (dosync
    (alter
      *dispatchers*
      (comp (partial into pq/empty)
            (partial filter #(not= dispatch-value (last (last %))))))))

(defn add-dispatch-hook
  "Allows you to add your own hook to the message responder
   You *must* define a 'responder multimethod corresponding to the
   dispatch-value"
      ([dispatch-value dispatch-check]
         (add-dispatch-hook 0 dispatch-check dispatch-value))
      ([dispatch-value dispatch-priority dispatch-check]
         (remove-dispatch-hook dispatch-value)
         (dosync (commute *dispatchers* pq/conj dispatch-priority [dispatch-check dispatch-value]))))

(defmulti responder dispatch)

(defmethod responder nil [bot channel message]
  (println "  Dispatched to /dev/null"))

(defmacro defresponder [key priority check-fn & body]
  `(do
     (defmethod responder ~key [~'bot ~'channel ~'message]
       (let [~'mesage (vary-meta ~'message assoc ~key true)
             ~'body   (:body ~'message)
             ~'m      (:extracted (meta ~'message))]  ; note that we've seen this message
         ~@body))
     (add-dispatch-hook ~key
                        ~priority
                        (fn [~'bot ~'channel ~'message] 
                          (when (not (~key (meta ~'message))) ; skip if we've seen this message before
                            (let [~'body (:body ~'message)
                                  ~'m    (:extracted (meta ~'message))]
                              (~check-fn ~'bot ~'channel ~'message)))))))
