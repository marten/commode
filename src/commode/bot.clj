(ns commode.bot
  (:require [commode.irc-util :as irc-util]
            [commode.core :as core])
  (:import [org.jibble.pircbot PircBot]))

;;;; Event handlers.

(defn handleMessage [bot channel sender login hostname body]
  (let [bot bot
        message (core/preprocess-message bot channel 
                                         {:type :message
                                          :sender sender
                                          :body body})]
    (println "Processing in" channel "from" sender "saying" message)
    (core/responder bot channel message)))

(defn handlePrivateMessage [bot sender login hostname message]
  (handleMessage bot sender sender login hostname message))

(defn handleAction [bot channel sender login hostname body]
  (let [bot bot
        message {:type :action
                 :sender sender
                 :body body}]
    (println "Processing in" channel "from" sender "doing" message)
    (core/responder bot channel message)))

(defn handleInvite [bot sender login hostname channel]
  (.joinChannel bot channel))

(defn handleJoin [bot channel sender login hostname]
  (let [bot bot
        channel channel
        message (vary-meta "" assoc :type :join :sender sender)]
    (if (= (.getNick bot) sender)
      (do (println "Processing join of" sender "(that's us) to channel" channel)
          nil)
      (do (println "Processing join of" sender "to channel" channel)
          nil))))

(defn handlePart [bot channel sender login hostname]
  (let [bot bot
        channel channel
        message (vary-meta "" assoc :type :part :sender sender)]
    (if (= (.getNick bot) sender)
      (do (println "Processing part of" sender "(that's us) from channel" channel)
          nil)
      (do (println "Processing part of" sender "from channel" channel)
          nil))))

;; These are predefined for future use, adding handlers to a running proxy isn't possible.

(defn handleKick [bot channel kickernick kickerlogin kickerhostname recipientnick reason] nil)
(defn handleTopic [bot channel topic setby date changed] nil)
(defn handleNickChange [bot oldnick login hostname newnick] nil)
(defn handleOp [bot channel sourcenick sourcelogin sourcehostname recipient] nil)
(defn handleDeop [bot channel sourcenick sourcelogin sourcehostname recipient] nil)

;;;; Set up a bot

(defn pircbot [bot-config]
  (let [bot-obj (proxy [PircBot] []
                 ;; (onInvite [us sender login hostname channel]
                 ;;           (handleInvite this sender login hostname channel))
                 (onJoin [channel sender login hostname]
                         (handleJoin this channel sender login hostname))
                 (onPart [channel sender login hostname]
                         (handlePart this channel sender login hostname))
                 (onKick [channel kickernick kickerlogin kickerhostname recipientnick reason]
                         (handleKick this channel kickernick kickerlogin kickerhostname recipientnick reason))
                 (onNickChange [oldnick login hostname newnick]
                               (handleNickChange this oldnick login hostname newnick))
                 (onOp [channel sourcenick sourcelogin sourcehostname recipient]
                       (handleOp this channel sourcenick sourcelogin sourcehostname recipient))
                 (onDeop [channel sourcenick sourcelogin sourcehostname recipient]
                         (handleDeop this channel sourcenick sourcelogin sourcehostname recipient))
                 (onMessage [channel sender login hostname message]
                            (handleMessage this channel sender login hostname message))
                 (onAction [sender login hostname channel action]
                           (handleAction this channel sender login hostname action))
                 (onPrivateMessage [sender login hostname message]
                                   (handlePrivateMessage this sender login hostname message)))]
    (merge bot-config {:this bot-obj})))