(defproject trajan "0.1.0-SNAPSHOT"
  :description "A product providing search capabilities over historical stock market data"
  :url "https://bitbucket.org/account/user/cbsb/projects/TRAJ"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [com.datomic/datomic-free "0.9.5703"]
                 [org.clojure/data.csv "0.1.4"]
                 [clojure.java-time "0.3.2"]
                 [ring "1.7.1"]
                 [compojure "1.6.1"]
                 [metosin/muuntaja "0.6.3"]
                 [ring-logger "1.0.1"]
                 [org.clojure/tools.cli "0.4.2"]]
  :main ^:skip-aot trajan.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
