(ns commode.irc-util
  (:import (org.jibble.pircbot PircBot))
  (:use [commode.irc :only (bot)])
  (:gen-class))

;;;; Helper functions.

(defn connect "Connect to a server."
  ([server]          (.connect bot server))
  ([server port]     (.connect bot server port)))

(defn disconnect "Disconnect from the server."
  []                 (.disconnect bot))

(defn nick "Get the current nick, or set it."
  ([]                (.getNick bot))
  ([nick]            (.changeNick bot nick)))

(defn join "Join a channel."
  [& channels]       (doseq [channel channels] (.joinChannel bot channel)))

(defn part "Leave a channel."
  [& channels]       (doseq [channel channels] (.leaveChannel bot channel)))

(defn voice "Grants voice privileges to a user on a channel."
  [channel & nicks]  (doseq [nick nicks] (.voice bot channel nick)))

(defn devoice "Removes voice privileges from a user on a channel."
  [channel & nicks]  (doseq [nick nicks] (.deVoice bot channel nick)))

(defn op "Grants operator privileges to a user on a channel."
  [channel & nicks]  (doseq [nick nicks] (.op bot channel nick)))

(defn deop "Removes operator privileges from a user on a channel."
  [channel & nicks]  (doseq [nick nicks] (.deOp bot channel nick)))

(defn ban "Bans a user from a channel."
  [channel & nicks]  (doseq [nick nicks] (.ban bot channel nick)))

(defn unban "Unbans a user from a channel."
  [channel & nicks]  (doseq [nick nicks] (.unBan bot channel nick)))

(defn topic "Get the topic for a channel, or set it."
  ([channel]         (.getTopic bot channel))
  ([channel topic]   (.setTopic bot channel topic)))

(defn users "Get the users for ``channel''."
  [channel]          (.getUsers bot channel))

(defn nicks "Get the nicks of users in ``channel''."
  [channel]          (map #(.getNick %) (users channel)))

(defn say "Send a message to a channel."
  [channel & messages] (doseq [message messages] (.sendMessage bot channel message)))