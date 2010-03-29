(ns ijbotma
  (:require [commode]
            [swank.swank]))

(defn -main [& args]
  ;; start up a swank server
  (swank.swank/start-repl 4005 :host "127.0.0.1" :port 4005 :dont-close true)
  
  ;; start up a bot
  (commode/start-bot {:server "THE SERVER"
                      :nick "THE NICK"
                      :channels ["#ACHANNEL" "#ANOTHER CHANNEL"]
                      :db ["MYSQL_HOSTNAME" "MYSQL_USER" "MYSQL_PASSWORD" "MYSQL_DATABASE_NAME"]}))

(-main *command-line-args*)
