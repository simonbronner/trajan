#!/bin/bash

lein run --server --port 8002 --database datomic:free://localhost:4334/hello?password=simonsays1
