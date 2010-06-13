(ns commode.modules.logger
  (:require [commode.irc-util :as irc]
            [commode.records.log :as log])
  (:use [commode.core]
        [commode.message]))

(defresponder ::log -10
              (dfn true)
  (println (meta message))
  ;; first create a log entry
  (log/create {:channel channel
               :nick (:sender message)
               :message body})
  ;; then re-inject message into queue
  #(responder bot channel message))