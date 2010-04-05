(ns commode.modules.joinpart
  (:require [commode.irc-util :as irc])
  (:use [commode.core]
        [commode.message]
        [commode.util :only (random-element)]))

(defresponder ::join-channel 0
              (dfn (addressed? bot message)
                   (re-find #"^!join" m))
  (let [chan (second (re-find #"^!join (#\S*)" m))]
    (irc/join bot chan)))

(defresponder ::part-channel 0
              (dfn (addressed? bot message)
                   (re-find #"^!part$" m))
  (irc/part bot channel))