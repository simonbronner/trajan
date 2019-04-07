(ns trajan.dloader
  (:require [clojure.data.csv :refer [read-csv]]
            [clojure.java.io :as io]
            [trajan.db :as d]
            [java-time :as t]))

(defn save-data [conn records]
  (let [entities (map #(zipmap
                        [:ticker :ticker/record]
                        [(-> % first key) (-> % first val)]) records)]
    (d/save conn (vec entities))))

(defn -coerce-to-float [v]
  (.floatValue (bigdec v)))

(defn -coerce-to-long [v]
  (Long/parseLong v))

(defn -join [v]
  (->> v
       (clojure.string/join "")
       (Integer/parseInt)))

(defn extract-field [v start end]
  (-join (subvec v start end)))

(defn produce-date-field [v]
  (let [raw (mapv #(Character/getNumericValue %)
                  (vec (.toCharArray v)))]
    (let [year (extract-field raw 0 4)
          month (extract-field raw 4 6)
          day (extract-field raw 6 8)]
      (-> (t/local-date year month day)
          (.atStartOfDay (java.time.ZoneId/of "Z"))
          (t/java-date)))))

(defn generate-record [row]
  (-> []
      (conj [:day (produce-date-field (row 1))])
      (into
       [[:open (-coerce-to-float (row 2))]
        [:high (-coerce-to-float (row 3))]
        [:low (-coerce-to-float (row 4))]
        [:close (-coerce-to-float (row 5))]
        [:volume (-coerce-to-long (row 6))]])))

(defn load-db [from-csv-file conn]
  (println "Loading database from: " (. (io/file from-csv-file) getAbsolutePath))
  (with-open [reader (io/reader from-csv-file)]
    (let [rows (read-csv reader)]
      (->>
       (for [row rows]
         (let [ticker-id (first row)
               raw-record (generate-record row)
               record (into {} raw-record)]
           (assoc {} ticker-id record)))
       (save-data conn))))
  (println "Done loading database from: " from-csv-file))

(defn create-db [from-csv-file ]
  (let [conn (d/initiate-database)]
    (load-db from-csv-file conn)))

(defn get-file-list [directory]
  (let [source-files (vec (.listFiles (io/file directory)))]
    (map
     #(.getPath %)
     (filter #(-> % (.getName) (.toLowerCase) (.endsWith ".txt")) source-files))))

(defn mark-as-done [file-name]
  (let [file (io/file file-name)
        new-file-name (str file-name ".done")]
    (.renameTo file (io/file new-file-name))))

(defn create-db-from-directory [directory database-uri]
  (let [source-files (get-file-list directory)]
    (do
      (let [conn (d/initiate-database database-uri)]
        (doseq [source-file source-files]
          (load-db source-file conn)
          (mark-as-done source-file))))))
