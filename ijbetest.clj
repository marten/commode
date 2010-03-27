(ns ijbotma
  (:require [commode]
            [swank.swank]))

(defn -main [& args]
  ;; start up a swank server
  (swank.swank/start-repl 4005 :host "127.0.0.1" :port 4005 :dont-close true)
  
  ;; start up a bot
  (commode/start-bot {:server "irc.mhil.net"
                      :nick "ijbetest"
                      :channels ["#ijbema"]
                      :db ["localhost" "root" ""]}))

(-main *command-line-args*)