#!/usr/bin/env bash
# Product Catalog API — sample curl commands (Week 6 / PostgreSQL)
BASE_URL="${BASE_URL:-http://localhost:8080}"

echo "=== Info ==="
curl -s "${BASE_URL}/api/info" | jq .

echo "=== Create product ==="
CREATE=$(curl -s -X POST "${BASE_URL}/api/products" \
  -H "Content-Type: application/json" \
  -d '{"sku":"SKU-001","name":"Sample","category":"General","price":19.99,"stockQuantity":10,"active":true}')
echo "$CREATE" | jq .
PRODUCT_ID=$(echo "$CREATE" | jq -r '.id')

echo "=== List products (paginated, default page size) ==="
curl -s "${BASE_URL}/api/products" | jq .

echo "=== List products (page, size, sort) ==="
curl -s "${BASE_URL}/api/products?page=0&size=5&sort=name,asc" | jq .

echo "=== Filter by category and active ==="
curl -s "${BASE_URL}/api/products?category=electronics&active=true&page=0&size=10" | jq .

echo "=== Get product by id ==="
curl -s "${BASE_URL}/api/products/${PRODUCT_ID}" | jq .

echo "=== Adjust stock (+5) ==="
curl -s -X PATCH "${BASE_URL}/api/products/${PRODUCT_ID}/stock" \
  -H "Content-Type: application/json" \
  -d '{"adjustment":5}' | jq .

echo "=== Adjust stock (-3) ==="
curl -s -X PATCH "${BASE_URL}/api/products/${PRODUCT_ID}/stock" \
  -H "Content-Type: application/json" \
  -d '{"adjustment":-3}' | jq .

echo "=== Low stock (dev profile: threshold 10) ==="
curl -s "${BASE_URL}/api/products/low-stock" | jq .

echo "=== Actuator health (includes db) ==="
curl -s "${BASE_URL}/actuator/health" | jq .

echo "=== Actuator info ==="
curl -s "${BASE_URL}/actuator/info" | jq .

echo "=== Validation error (400) ==="
curl -s -X POST "${BASE_URL}/api/products" \
  -H "Content-Type: application/json" \
  -d '{"sku":"","name":"X","category":"General","price":-1,"stockQuantity":0}' | jq .

echo "=== Insufficient stock (400, quantity unchanged) ==="
curl -s -X PATCH "${BASE_URL}/api/products/${PRODUCT_ID}/stock" \
  -H "Content-Type: application/json" \
  -d '{"adjustment":-999}' | jq .
