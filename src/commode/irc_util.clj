(ns commode.irc-util
  (:use clojure.template))

;;;; IRC Helper functions.

(defn nick "Get the current nick, or set it."
  ([bot]                 (.getNick bot))
  ([bot nick]            (.changeNick bot nick)))

(do-template [fn-name java-bot-method docstring]
             
             (defn fn-name docstring [bot & channels]
               (doseq [channel channels] (java-bot-method bot channel)))

             join .joinChannel "Join a channel"
             part .partChannel "Leave a channel")

(do-template [fn-name java-bot-method]
             
             (defn fn-name [bot channel & nicks]
               (doseq [nick nicks] (java-bot-method bot channel nick)))
             
             voice   .voice
             devoice .deVoice
             op      .op
             deop    .deOp
             ban     .ban
             unban   .unBan)

(defn topic "Get the topic for a channel, or set it."
  ([bot channel]         (.getTopic bot channel))
  ([bot channel topic]   (.setTopic bot channel topic)))

(defn users "Get the users for ``channel''."
  [bot channel]          (.getUsers bot channel))

(defn nicks "Get the nicks of users in ``channel''."
  [bot channel]          (map #(.getNick %) (users channel)))

(defn say "Send a message to a channel."
  [bot channel & messages] (doseq [message messages] (.sendMessage bot channel message)))

;;;; Additional helpers

(defn address [nick body]
  (str nick ": " body))