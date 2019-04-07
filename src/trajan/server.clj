(ns trajan.server
  (:require [ring.adapter.jetty :as jetty]
            [ring.middleware.reload :refer [wrap-reload]]
            [compojure.core :refer [GET defroutes]]
            [compojure.route :as route]
            [muuntaja.core :as m]
            [java-time :as t]
            [ring.logger :as logger]
            [trajan.db :as d]))

(defn parse-date [d]
  (-> (t/local-date d)
      (.atStartOfDay (java.time.ZoneId/of "Z"))
      (t/java-date)))

(defn gen-response [f & rest]
  (try
    (let [value (apply f rest)]
      (if (empty? value)
        {:status 404
        :headers {"Content-Type" "application/json"}
        :body (->> {:sucess false :message "Not found"} (m/encode "application/json"))}
        {:status 200
        :headers {"Content-Type" "application/json"}
        :body (->> value (sort-by :day) (m/encode "application/json"))}))
    (catch Exception e
      {:status 500
        :headers {"Content-Type" "application/json"}
        :body (->> {:sucess false :message (.getMessage e)} (m/encode "application/json"))})))

(defn retrieve-ticker-on [db ticker on-date]
  (d/find-by-ticker-and-day db ticker on-date))

(defn retrieve-ticker-range [db ticker from to]
  (d/find-by-ticker-and-range db ticker from to))

(defroutes app
  (GET "/" [] "pong")
  (GET "/stock/:ticker{[\\w]{3}}/:on-date{[0-9]{4}-[0-9]{2}-[0-9]{2}}"
       [ticker on-date :as request]
       (gen-response retrieve-ticker-on (:db request) ticker (parse-date on-date)))
  (GET "/stock/:ticker{[\\w]{3}}/:from{[0-9]{4}-[0-9]{2}-[0-9]{2}}/:to{[0-9]{4}-[0-9]{2}-[0-9]{2}}"
       [ticker from to :as request]
       (gen-response retrieve-ticker-range (:db request) ticker (parse-date from) (parse-date to)))
  (route/not-found
   (->> {:success false :message "endpoint not found"} (m/encode "application/json"))))

(defn wrap-exception-handling
  [handler]
  (fn [request]
    (try
      (handler request)
      (catch Exception e
        {:status 500 :body (.getMessage e)}))))

(defn wrap-db-handler [handler db]
  (fn [request] (handler (assoc-in request [:db] db))))

(defn wrap-with-logger-handler [handler]
  (logger/wrap-with-logger handler
                           {:log-fn
                            (fn [{:keys [level throwable message]}]
                              (println level throwable message))}))

(defn get-handlers [routes database devmode]
  (if (true? devmode)
    (-> routes (wrap-db-handler database) (wrap-reload) (wrap-with-logger-handler) (wrap-exception-handling))
    (-> routes (wrap-db-handler database) (wrap-with-logger-handler) (wrap-exception-handling))))

(defn get-db [database-uri]
  (println "Connecting to database")
  (-> (d/initiate-database database-uri) (d/as-db)))

(defonce server (atom nil))

(defn start-jetty [handler port devmode]
  (when-not @server
    (swap! server
           (constantly
            (jetty/run-jetty handler {:port port :join? false})))))

(defn stop-jetty []
  (println "Stopped Server!")
  (when @server
    (.stop @server)
    (swap! server (constantly nil))))

(defn start[{:keys [port database devmode]}]
  (println "Starting server on port " port)
  (try
    (let [db (get-db database)
          handlers (get-handlers #'app db devmode)]
      (start-jetty handlers port devmode)
      (println "Started server."))
    (catch Exception e
      (println (.getMessage e)))))
