(ns commode
  (:gen-class)
  (:require [commode.bot]
            [commode.config]
            (commode.modules help shutup latex dice factoids joinpart)))

(defn- wall-hack-method [class-name name- params obj & args]
  (-> class-name
      (.getDeclaredMethod (name name-) (into-array Class params))
      (doto (.setAccessible true))
      (.invoke obj (into-array Object args))))

(defn start-bot
  ([opts additional-setup]
     (let [bot (commode.bot/pircbot opts)]
       (commode.config/init bot)
       (doto (:this bot)
         (.setVerbose true)
         (.connect (:server bot))
         (.changeNick (:nick bot)))
       (doseq [channel (:channels bot)] (.joinChannel (:this bot) channel))
       (additional-setup bot)
       bot))
  ([opts] (start-bot opts (fn [_]))))

(defn -main [& args]
  ;; start up a swank server
  ; (swank.swank/start-repl 4005 :host "127.0.0.1" :port 4005 :dont-close true)

  ;; start up a bot
  (start-bot {:server "irc.frozenfractal.com"
              :nick "ijbotma"
              :channels ["#bots"]
              :db ["localhost" "root" "" "ijbel"]}))
