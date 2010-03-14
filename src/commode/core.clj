(ns commode.core
  (:require [commode.config :as config]
            [commode.factoid :as factoid]
            [commode.trigger :as trigger]
            [commode.response :as response]
            [commode.irc :as irc]
            [commode.irc-util :as irc-util]
            [commode.channel :as channel]
            [swank.swank :as swank])
                                        ;[clojure.contrib.str-utils2 :as s]
                                        ;  (:import (org.jibble.pircbot PircBot)
                                        ;           (java.util.regex Pattern)
                                        ;           (java.util Date))
  (:use commode.boot)
  (:gen-class))

(defn -main [& args]
  (.setVerbose irc/bot config/verbose)
  (irc-util/connect config/irc-server)
  (irc-util/nick config/irc-nick)
  (doseq [channel config/irc-channels] (irc-util/join channel))
  (swank/start-repl)                ; get me a nice repl
)
