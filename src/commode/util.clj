;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; util ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn rand-elm "returns a random element from the sequence, or nil for an empty sequence"
  [seq] 
  (let [size (count seq)]
    (if (> size 0)
      (nth seq (rand-int size)))))

(defn rand-elm-weighted "takes a [{:weight x, ...} ...] sequence, and returns a weighted-random element"
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

; (maybe 0.1 (foo) (bar))
(defmacro maybe [chance & fns]
  `(when (< (rand) ~chance)
     ~@fns))