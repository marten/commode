(ns commode.util
  (:require [clojure.contrib.str-utils2 :as s])
  (:import (java.net URL URLEncoder)
           (java.io BufferedReader InputStreamReader OutputStreamWriter)
           (java.text SimpleDateFormat ParsePosition)
           (sun.misc BASE64Encoder)))

(defn random-element 
  "returns a random element from the sequence, or nil for an empty sequence"
  [seq] 
  (let [size (count seq)]
    (if (> size 0)
      (nth seq (rand-int size)))))

(defn random-element-weighted 
  "takes a [{:weight x, ...} ...] sequence, and returns a weighted-random element"
  [seq]
  (let [weights (map :weight seq)
        minim   (reduce min weights)
        change  (+ 1 (* -1 minim)) ;; eg. [-2 -1 0 -2 +3] => [+1 +2 +3 +1 +4]
        normw   (map (partial + change) weights)
        total   (reduce + weights)
        target  (rand-int total)]
    (loop [seqn seq
           upto (:weight (first seq))]
      (if (>= upto target)
        (first seqn)
        (recur (rest seqn) (+ upto (:weight (first seqn))))))))

(defn starts-with? [prefix string]
  (.startsWith string prefix))

(defmacro async "just do this, I don't care" [& x]
  `(send-off (agent nil) (fn [& _#] ~@x )))

(defmacro maybe "with chance x do this" [chance & fns]
  `(when (< (rand) ~chance)
     ~@fns))

(defn get-url [x]
  (with-open [a (-> (doto (-> x URL. .openConnection)
                      (.setRequestProperty "User-Agent" "clojurebot")
                      (.setRequestProperty "Accept" "application/xml"))
                    .getInputStream InputStreamReader. BufferedReader.)]
    (loop [buf (StringBuilder.) line (.readLine a)]
      (if line
        (recur (doto buf (.append line)) (.readLine a))
        (.toString buf)))))

(defn tinyurl [url]
  (try 
   (->> (get-url (str "http://shadyurl.com/create.php?myUrl=" (URLEncoder/encode url)))
        (re-find #"(http://5z8.info/[^']+)'")
        (second))
   (catch Exception e
     (-> "http://is.gd/api.php?longurl=%s" (format (URLEncoder/encode url))
         get-url))))
(def tinyurl (memoize tinyurl))
