(ns commode.modules.dice
  (:require [commode.irc-util :as irc])
  (:use [commode.core]
        [commode.message]
        [clojure.contrib.str-utils2 :only (join)]))

(defn roll-die [sides]
  (nth (range 1 (inc sides)) (rand-int sides)))

(defn format-total [modifier total]
  (if (= 0 modifier)
    (str total)
    (str total
         (if (< 0 modifier) 
           (str "+" modifier)
           modifier)
         "="
         (+ modifier total))))

(defresponder ::roll-dice 0
              (dfn (and (or (addressed? bot message)
                            (and (= (:type message) :action)
                                 (re-find #"(gooit|rolt)" (:body message))))
                        (re-find #"d[0-9]+" (extract-message bot message))))
  (let [body (:body message)
        dice-count (try (-> (re-find #"[0-9]+d" body)
                            (.replaceAll "[a-zA-Z]" "")
                            (Integer/parseInt))
                        (catch Exception e 1))
        dice-type  (-> (re-find #"d[0-9]+" body)
                       (.replaceAll "[a-zA-Z]" "")
                       (Integer/parseInt))
        modifier   (try (-> (re-find #"[+-][0-9]+" body)
                            (.replaceAll "[+]" "")
                            (Integer/parseInt))
                        (catch Exception e 0))]
    (let [dice   (map (fn [_] (roll-die dice-type)) (repeat dice-count nil))
          total  (reduce + dice)
          result (+ modifier total)]
      (irc/say bot channel (cond 
                             (>= 0 dice-count) (str (:sender message) 
                                                    (if (= 0 modifier) 
                                                      " gooit... nouja... geen dobbelstenen. wat een mafkees."
                                                      (str " gooit geen dobbelstenen voor een totaal van " modifier)))
                             (=  1 dice-count) (str (:sender message) " rolt: " 
                                                    (format-total modifier total))
                             (<  1 dice-count) (str (:sender message) " rolt: " 
                                                    (join ", " dice) 
                                                    " voor een totaal van " 
                                                    (format-total modifier total)))))))