#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

ENV_FILE="${1:-.env}"

if [[ ! -f "$ENV_FILE" ]]; then
  echo "ERROR: Missing $ENV_FILE — copy .env.example and set DB_PASSWORD / POSTGRES_PASSWORD." >&2
  exit 1
fi

if ! docker info >/dev/null 2>&1; then
  echo "ERROR: Docker is not running. Start Docker Desktop first." >&2
  exit 1
fi

docker compose --env-file "$ENV_FILE" up -d

echo "Waiting for PostgreSQL..."
until docker compose --env-file "$ENV_FILE" exec -T postgres pg_isready -U root -d product_catalog >/dev/null 2>&1; do
  sleep 1
done

set -a
# shellcheck disable=SC1090
source "$ENV_FILE"
set +a

if [[ -z "${DB_PASSWORD:-}" ]]; then
  echo "ERROR: DB_PASSWORD is empty in $ENV_FILE." >&2
  exit 1
fi

export DB_URL="${DB_URL:-jdbc:postgresql://localhost:5432/product_catalog}"
export DB_USERNAME="${DB_USERNAME:-root}"
export DB_PASSWORD="${DB_PASSWORD:-root@123}"

exec ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
