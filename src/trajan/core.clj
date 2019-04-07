(ns trajan.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [trajan.server :as server]
            [trajan.dloader :as dbloader])
  (:gen-class))

(def cli-options
  ;; An option with a required argument
  [["-p" "--port PORT" "Port number on which to run the rest server"
    :default 3000
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 % 0x10000) "Must be a number between 0 and 65536"]]
   ["-i" "--import DIRECTORY" "File or directory to import into the database"
    :default nil]
   ["-d" "--database DATABASE" "Uri to the datomic database"
    :default "datomic:free://localhost:4334/hello"]
   [nil "--devmode" "Indicates that the server should scan for changes to source"]
   ["-s" "--server" "Indicates that the rest server should be started"]
   ["-h" "--help"]])

(defn -main [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    (cond
      (empty? args)
      (println summary)
      (not (empty? errors))
      (println errors)
      (:help options)
      (println summary)
      (:server options)
      (server/start options)
      (:import options)
      (let [{:keys [import database]} options]
        (println "going to load...")
        (dbloader/create-db-from-directory import database))
      :else (println options))))
