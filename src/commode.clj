(ns commode
  (:require [commode.bot]
            (commode.modules help shutup latex dice factoids)))

(defonce *bot* (ref {}))

(defn- wall-hack-method [class-name name- params obj & args]
  (-> class-name
      (.getDeclaredMethod (name name-) (into-array Class params))
      (doto (.setAccessible true))
      (.invoke obj (into-array Object args))))

(defn start-bot 
  ([opts additional-setup]
     (let [bot (commode.bot/pircbot opts)]
       (dosync (ref-set *bot* bot))
       (doto (:this bot)
         (.setVerbose true)
         (.connect (:server bot))
         (.changeNick (:nick bot)))
       (doseq [channel (:channels bot)] (.joinChannel (:this bot) channel))
       (additional-setup bot)
       bot))
  ([opts] (start-bot opts (fn [_]))))