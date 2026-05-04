#!/bin/bash
set -e

echo "Building Docker image"
docker build -t rinha .

echo "Starting Docker containers"
docker compose up -d

echo "Waiting for services to be ready"
until curl -s http://localhost:9999/ready | grep -q "ok"; do
  sleep 1
done

echo "Running k6 tests"
k6 run test/test.js

echo "Tests completed. Shutting down Docker containers"
docker compose down