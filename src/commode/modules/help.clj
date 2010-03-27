(ns commode.modules.help
  (:require [commode.irc-util :as irc])
  (:use [commode.core]
        [commode.message]))

(defmethod responder ::admin [bot channel message]
  (irc/say bot channel 
           (str (:sender message) ": zie http://ijbel.org user=#brak pass=afwas")))

(add-dispatch-hook ::admin 0
                   (dfn (and (addressed? bot message)
                             (re-find #"!admin" (:body message)))))