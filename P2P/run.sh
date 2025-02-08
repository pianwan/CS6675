#!/bin/bash

# build p2p
mvn clean package

# start containers
docker compose up --build -d

# attach peer1
docker attach peer1