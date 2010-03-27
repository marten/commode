;; (ns commode.config
;;   (:gen-class))

;; ;;;; Environment. To support development vs. unit/function tests vs. production

;; (def env (or (. (. System getenv) get "COMMODE_ENV") "development"))

;; (defn if-dev  [] (= env "development"))
;; (defn if-test [] (= env "test"))
;; (defn if-prod [] (= env "production"))

;; (defn dev-test-prod [when-dev when-test when-prod]
;;   (cond
;;     (if-dev)  when-dev
;;     (if-test) when-test
;;     (if-prod) when-prod
;;     true      when-dev))

;; (def verbose (dev-test-prod true true false))

;; ;;;; Database.

;; (let [db-host (dev-test-prod "localhost" "localhost" "10.177.154.146")
;;       db-port 3306
;;       db-name "ijbel"
;;       db-user (dev-test-prod "root" "root" "ijbel")
;;       db-pass (dev-test-prod "" "" (. (. System getenv) get "COMMODE_DB_PASSWORD"))]
;;   (def db {:classname "com.mysql.jdbc.Driver" ; must be in classpath
;;            :subprotocol "mysql"
;;            :subname (str "//" db-host ":" db-port "/" db-name)
;;            ; Any additional keys are passed to the driver
;;            ; as driver-specific properties.
;;            :user db-user
;;            :password db-pass}))


;; ;;;; IRC Settings.

;; (def irc-server "astyanassa.mhil.net")

;; (def irc-nick   (dev-test-prod "ijbetest" 
;;                                "ijbetest" 
;;                                "ijbema"))

;; (def irc-channels (dev-test-prod ["#ijbema"]
;;                                  []
;;                                  ["#brak" "#perio" "#ijbema"]))