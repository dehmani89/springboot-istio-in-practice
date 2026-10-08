#!/usr/bin/env bash
# Deploy Postgres + all services + curl test client. Idempotent.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

echo "==> namespaces"
kubectl apply -f k8s/namespaces.yaml

echo "==> postgres-init ConfigMap (from infra/postgres/init)"
kubectl create configmap postgres-init -n dental-data \
  --from-file=infra/postgres/init --dry-run=client -o yaml | kubectl apply -f -

echo "==> postgres"
kubectl apply -f k8s/data/
kubectl rollout status statefulset/postgres -n dental-data --timeout=180s

echo "==> services + tools"
kubectl apply -f k8s/apps/ -f k8s/tools/
for d in catalog-service inventory-service pricing-service-v1 pricing-service-v2 order-service curl-client; do
  kubectl rollout status "deploy/$d" -n dental --timeout=300s
done

kubectl get pods -n dental
echo "==> deployed. Next: istio/labs/01-baseline-no-mesh/README.md"
