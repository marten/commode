(defproject commode "0.1"
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [org.jibble/pircbot "1.5.0"]
                 [mysql/mysql-connector-java "5.1.6"]]
  :main ^:skip-aot commode
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
