#!/usr/bin/env bash
# Build all service jars, then local Docker images (shared with Docker Desktop Kubernetes).
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
TAG="${TAG:-0.1.0}"
SERVICES=(catalog-service inventory-service pricing-service order-service)

cd "$ROOT"
echo "==> mvn package"
mvn -q -DskipTests package

for svc in "${SERVICES[@]}"; do
  echo "==> docker build dental-oms/${svc}:${TAG}"
  docker build -q -t "dental-oms/${svc}:${TAG}" "services/${svc}"
done

echo "==> done"
docker images | grep dental-oms
