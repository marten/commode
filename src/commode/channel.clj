(ns commode.channel
  (:require [commode.irc-util :as irc-util])
  (:gen-class))

;;;; The Variable

;;; This contains a reference to a hash. The hash is a mapping from channelname
;;; to an Agent maintaining the state of the channel.

(def channels (ref {}))


;;;; Helper functions for using The Variable.

(defn new [channel]
  (dosync
   (alter channels assoc channel (agent {:channel channel}))))

(defn delete [channel]
  (dosync
   (alter channels dissoc channel)))

(defn find-channel [channel]
  (@channels channel))

;;;; These functions are ready to be sent to a channel

(defmacro try-or [& forms]
  `(or ~@(map (fn [x] (list 'try x '(catch Throwable e nil)))
              forms)))

(defn lookup [chanstate msg]
  {:pre [false]}
  (let [response (lookup (:message msg))]
    (if response
      (say (:channel msg) '(transform msg response))
      (say (:channel msg) "$404"))))

(defn respond [{channel :channel :as chanstate}
               {nick :nick message :message}]
  (try-or
   (lookup chanstate message)
   chanstate))

(defn say [{channel :channel :as chanstate} & messages]
  (irc-util/say channel messages)
  chanstate)

(defn part [{channel :channel :as chanstate}]
  (irc-util/part channel)
  chanstate)