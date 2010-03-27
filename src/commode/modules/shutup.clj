(ns commode.modules.shutup
  (:require [commode.irc-util :as irc])
  (:use [commode.core]
        [commode.message]))

(defonce muted-channels (ref {}))

(defn mute [channel]
  (dosync (alter muted-channels assoc channel (java.util.Date.))))

(defn unmute [channel]
  (dosync (alter muted-channels dissoc channel)))

(defn muted? [channel]
  (@muted-channels channel))

;;;; Responders

(defresponder ::mute-in 0
              (dfn (addressed? bot message)
                   (re-find #"^!mute" m))
  (mute channel)
  (irc/say bot channel (reply message "okay, shutting up now")))

(defresponder ::unmute-in 0
              (dfn (addressed? bot message)
                   (re-find #"^!unmute" m))
  (unmute channel)
  (irc/say bot channel (reply message "bedankt, ik kon me al haast niet meer inhouden")))

(defresponder ::muted? 19
              (dfn (not (addressed? bot message))
                   (muted? channel))
  nil)