#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

if ! docker info >/dev/null 2>&1; then
  echo "ERROR: Docker is not running. Start Docker Desktop and retry." >&2
  exit 1
fi

if ! docker image inspect postgres:16-alpine >/dev/null 2>&1; then
  echo "Pulling postgres:16-alpine for Testcontainers..."
  docker pull postgres:16-alpine
fi

exec ./mvnw clean verify "$@"
