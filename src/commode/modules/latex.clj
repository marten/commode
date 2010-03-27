(ns commode.modules.latex
  (:require [commode.irc-util :as irc])
  (:use [commode.core]
        [commode.message]
        [commode.util :only (tinyurl)])
  (:import (java.net URLEncoder)))

(defn chart-url [latex]
  (format "http://chart.apis.google.com/chart?cht=tx&chf=bg,s,FFFFFFFF&chco=000000&chl=%s"
          (URLEncoder/encode latex)))

(defresponder ::latex 0
              (dfn (and (addressed? bot message)
                        (re-find #"^latex " (extract-message bot message))))
  (let [m (.replaceAll (extract-message bot message) "^latex " "")]
    (irc/say bot channel (str (:sender message) ": " (tinyurl (chart-url m))))))
