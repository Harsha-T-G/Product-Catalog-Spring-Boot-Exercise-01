#!/usr/bin/env bash
# Product Catalog API — sample curl commands
BASE_URL="${BASE_URL:-http://localhost:8080}"

echo "=== Info ==="
curl -s "${BASE_URL}/api/info" | jq .

echo "=== Create product ==="
curl -s -X POST "${BASE_URL}/api/products" \
  -H "Content-Type: application/json" \
  -d '{"sku":"SKU-001","name":"Sample","category":"General","price":19.99,"stockQuantity":10,"active":true}' | jq .

echo "=== List products ==="
curl -s "${BASE_URL}/api/products" | jq .

echo "=== Low stock (dev profile: threshold 10) ==="
curl -s "${BASE_URL}/api/products/low-stock" | jq .

echo "=== Actuator health ==="
curl -s "${BASE_URL}/actuator/health" | jq .

echo "=== Actuator info ==="
curl -s "${BASE_URL}/actuator/info" | jq .

echo "=== Validation error (400) ==="
curl -s -X POST "${BASE_URL}/api/products" \
  -H "Content-Type: application/json" \
  -d '{"sku":"","name":"X","category":"General","price":-1,"stockQuantity":0}' | jq .
