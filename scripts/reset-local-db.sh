#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

ENV_FILE="${1:-.env}"

if [[ ! -f "$ENV_FILE" ]]; then
  echo "Copy .env.example to .env first: cp .env.example .env" >&2
  exit 1
fi

echo "Stopping PostgreSQL and removing local data volume..."
docker compose --env-file "$ENV_FILE" down -v

echo "Starting PostgreSQL with credentials from $ENV_FILE ..."
docker compose --env-file "$ENV_FILE" up -d

echo "Waiting for PostgreSQL..."
until docker compose --env-file "$ENV_FILE" exec -T postgres pg_isready -U root -d product_catalog >/dev/null 2>&1; do
  sleep 1
done

echo "Done. Database user 'root' initialized from POSTGRES_PASSWORD in .env."
echo "Run: ./scripts/run-dev.sh   or   set -a && source .env && set +a && ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev"
