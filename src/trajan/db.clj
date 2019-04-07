(ns trajan.db
  (:require [datomic.api :as d]))

(def schema
  [{:db/ident :ticker
    :db/cardinality :db.cardinality/one
    :db/unique :db.unique/identity
    :db/valueType :db.type/string
    :db/index true
    :db/doc "A ticker symbol - represents a stock"}
   {:db/ident :ticker/record
    :db/cardinality :db.cardinality/many
    :db/valueType :db.type/ref
    :db/isComponent true
    :db/doc "A price record associated with a stock"}
   {:db/ident :open
    :db/cardinality :db.cardinality/one
    :db/valueType :db.type/float
    :db/doc "The price at the start of the day of a stock"}
   {:db/ident :high
    :db/cardinality :db.cardinality/one
    :db/valueType :db.type/float
    :db/doc "The highest price for the day"}
   {:db/ident :low
    :db/cardinality :db.cardinality/one
    :db/valueType :db.type/float
    :db/doc "The lowest price for the day"}
   {:db/ident :close
    :db/cardinality :db.cardinality/one
    :db/valueType :db.type/float
    :db/doc "The price at the close of the day"}
   {:db/ident :volume
    :db/cardinality :db.cardinality/one
    :db/valueType :db.type/long
    :db/doc "The volume of stock traded"}
   {:db/ident :day
    :db/cardinality :db.cardinality/one
    :db/valueType :db.type/instant
    :db/index true
    :db/doc "The day to which the record refers"}])

(defn initiate-database [uri]
  (d/create-database uri)
  (let [conn (d/connect uri)]
    @(d/transact conn schema)
    conn))

(def test-data
  [{:ticker "AUD"
    :ticker/record
    {:open 0.25 :high 0.28 :low 0.24 :close 0.26 :volume 100 :day #inst "2001-06-28"}}
   {:ticker "AUD"
    :ticker/record
    {:open 1.25 :high 1.28 :low 1.24 :close 1.26 :volume 200 :day #inst "2001-06-29"}}
   {:ticker "AUD"
    :ticker/record
    {:open 2.25 :high 2.28 :low 2.24 :close 2.26 :volume 300 :day #inst "2001-06-30"}}
   {:ticker "BUD"
    :ticker/record
    {:open 10.25 :high 10.28 :low 10.24 :close 10.26 :volume 1100 :day #inst "2001-06-28"}}
   {:ticker "BUD"
    :ticker/record
    {:open 11.25 :high 11.28 :low 11.24 :close 11.26 :volume 1200 :day #inst "2001-06-29"}}
   {:ticker "BUD"
    :ticker/record
    {:open 12.25 :high 12.28 :low 12.24 :close 12.26 :volume 1300 :day #inst "2001-06-30"}}])

(defn save [conn data]
  (println "Saving " (count data) " records.")
  @(d/transact conn data))

(defn populate-with-test-data [conn]
  (save conn test-data))

(defn as-db [conn]
  (d/db conn))

(defn find-by-ticker-and-day [db t d]
  (->>
   (d/q
   '[:find (pull ?r [:open :high :low :close :volume :day])
    :in $ ?t ?w
    :where
    [?e :ticker ?t]
    [?e :ticker/record ?r]
    [?r :day ?w]]
   db t d)
   (map first)))

(defn find-by-ticker-and-range [db t a b]
  (->>
   (d/q
    '[:find (pull ?r [:open :high :low :close :volume :day])
      :in $ ?t ?after ?before
      :where
      [?e :ticker ?t]
      [?e :ticker/record ?r]
      [?r :day ?day]
      [(<= ?day ?before)]
      [(>= ?day ?after)]]
    db t a b)
   (map first)))
