#!/usr/bin/env bash
# Product Catalog API — authenticated Week 7 sample commands
BASE_URL="${BASE_URL:-http://localhost:8080}"
ADMIN_USERNAME="${CATALOG_ADMIN_USERNAME:-admin}"
: "${CATALOG_ADMIN_PASSWORD:?Set CATALOG_ADMIN_PASSWORD in your environment}"
ADMIN_AUTH=(-u "${ADMIN_USERNAME}:${CATALOG_ADMIN_PASSWORD}")
TRACE_ID="${TRACE_ID:-7046bd93-568f-49ae-ac1d-9d5be793f720}"

echo "=== Info ==="
curl -s "${BASE_URL}/api/info" | jq .

echo "=== Create product ==="
CREATE=$(curl -s -X POST "${BASE_URL}/api/products" \
  "${ADMIN_AUTH[@]}" \
  -H "X-Trace-Id: ${TRACE_ID}" \
  -H "Content-Type: application/json" \
  -d '{"sku":"SKU-001","name":"Sample","category":"General","price":19.99,"stockQuantity":10,"active":true}')
echo "$CREATE" | jq .
PRODUCT_ID=$(echo "$CREATE" | jq -r '.id')

echo "=== List products (paginated, default page size) ==="
curl -s "${ADMIN_AUTH[@]}" "${BASE_URL}/api/products" | jq .

echo "=== List products (page, size, sort) ==="
curl -s "${ADMIN_AUTH[@]}" "${BASE_URL}/api/products?page=0&size=5&sort=name,asc" | jq .

echo "=== Filter by category and active ==="
curl -s "${ADMIN_AUTH[@]}" "${BASE_URL}/api/products?category=electronics&active=true&page=0&size=10" | jq .

echo "=== Get product by id ==="
curl -s "${ADMIN_AUTH[@]}" "${BASE_URL}/api/products/${PRODUCT_ID}" | jq .

echo "=== Adjust stock (+5) ==="
curl -s -X PATCH "${BASE_URL}/api/products/${PRODUCT_ID}/stock" \
  "${ADMIN_AUTH[@]}" \
  -H "Content-Type: application/json" \
  -d '{"adjustment":5}' | jq .

echo "=== Adjust stock (-3) ==="
curl -s -X PATCH "${BASE_URL}/api/products/${PRODUCT_ID}/stock" \
  "${ADMIN_AUTH[@]}" \
  -H "Content-Type: application/json" \
  -d '{"adjustment":-3}' | jq .

echo "=== Low stock (dev profile: threshold 10) ==="
curl -s "${ADMIN_AUTH[@]}" "${BASE_URL}/api/products/low-stock" | jq .

echo "=== Actuator health (includes db) ==="
curl -s "${BASE_URL}/actuator/health" | jq .

echo "=== Actuator info ==="
curl -s "${ADMIN_AUTH[@]}" "${BASE_URL}/actuator/info" | jq .

echo "=== Validation error (400) ==="
curl -s -X POST "${BASE_URL}/api/products" \
  "${ADMIN_AUTH[@]}" \
  -H "Content-Type: application/json" \
  -d '{"sku":"","name":"X","category":"General","price":-1,"stockQuantity":0}' | jq .

echo "=== Insufficient stock (400, quantity unchanged) ==="
curl -s -X PATCH "${BASE_URL}/api/products/${PRODUCT_ID}/stock" \
  "${ADMIN_AUTH[@]}" \
  -H "Content-Type: application/json" \
  -d '{"adjustment":-999}' | jq .

echo "=== Missing authentication (401) ==="
curl -s "${BASE_URL}/api/products" | jq .
