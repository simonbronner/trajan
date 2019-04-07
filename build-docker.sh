#!/bin/bash

lein uberjar
docker build . -t sxbronner/trajan-server
docker push sxbronner/trajan-server
