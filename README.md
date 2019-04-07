# trajan-server

A service providing search capabilities over historical stock market data.

## Data

Data soured from: https://www.asxhistoricaldata.com

## Pre-requisites

* [Java 1.8+](https://docs.aws.amazon.com/corretto/latest/corretto-8-ug/downloads-list.html)
* [Clojure](https://clojure.org) - 1.9.0
* [Datomic](http://datomic.com/) - this was specifically developed against [Datomic Free](https://my.datomic.com/downloads/free) - but the pro & cloud versions should also work
* [Leningen](https://leiningen.org) - clojure build tool

## How to Build

```
lein uberjar
```

## Project Structure

* core - entry point of application - provides command line interface
* db - provides db connection, query and persistence
* dloader - provides data load (import) functionality
* server - provides the rest endpoints and server lifecycle

## How To Run

The service requires a datomic instance - the example below assume you are running a datomic (free) instance on your localhost on port 4334.

To import Stock data residing in a directory called /stock-data into a datomic instance, calling the database trajan-db:

```
lein run --import /stock-data --database datomic:free://localhost:4334/trajan-db
```

To run a server serving this data on port 8000:

```
lein run --server --port 8000 --database datomic:free://localhost:4334/trajan-db
```

### or via Docker

```
docker run --rm -p 8000:8000 sxbronner/trajan-server --server --port 8000 --database datomic:free://localhost:4334/trajan-db
```

## How To Package

The following will produce a standalone jar file that can then be used via and 1.8+ jre via java -jar:

```
lein uberjar
```

Alternatively you can package the app in a docker image:

```
lein uberjar
docker build . -t sxbronner/trajan-server
docker push sxbronner/trajan-server
```

## License

Copyright © 2019 Simon Bronner
