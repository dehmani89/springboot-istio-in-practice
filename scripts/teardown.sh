#!/usr/bin/env bash
# Remove the Dental OMS from the cluster. Add --istio to also uninstall Istio.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"

kubectl delete namespace dental dental-data dental-ingress dental-sidecar --ignore-not-found

if [[ "${1:-}" == "--istio" ]]; then
  kubectl delete -f "$ROOT/istio/install/telemetry.yaml" --ignore-not-found
  istioctl uninstall --purge -y
  kubectl delete namespace istio-system --ignore-not-found
  echo "Istio removed. Gateway API CRDs left in place (remove manually if desired)."
fi
