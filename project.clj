(defproject hermione "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :source-paths ["src"]
  :java-source-paths ["java"]
  :prep-tasks ["javac" "compile"]
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [compojure "1.3.1"]
                 [ring/ring-defaults "0.1.2"]
                 [clj-http "2.0.0"]
                 [ring/ring-json "0.4.0"]
                 [mimina "0.1.0"]
                 [com.qiniu/qiniu-java-sdk "7.0.5"]
                 [commons-codec/commons-codec "1.10"]
                 [com.typesafe/config "1.2.1"]]
  :plugins [[lein-ring "0.8.13"]]
  :ring {:handler hermione.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring-mock "0.1.5"]]}})
