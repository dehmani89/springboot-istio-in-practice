#!/usr/bin/env bash
# Install Gateway API CRDs, Istio (ambient profile), observability add-ons and tracing config.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
GATEWAY_API_VERSION="${GATEWAY_API_VERSION:-v1.3.0}"

ctx="$(kubectl config current-context)"
if [[ "$ctx" != "docker-desktop" ]]; then
  echo "WARNING: current kube context is '$ctx' (expected docker-desktop). Ctrl-C within 5s to abort."
  sleep 5
fi

command -v istioctl >/dev/null || { echo "istioctl not found — brew install istioctl"; exit 1; }
ISTIO_MINOR="$(istioctl version --remote=false 2>/dev/null | grep -oE '[0-9]+\.[0-9]+' | head -1)"
echo "==> istioctl ${ISTIO_MINOR}.x"

echo "==> precheck"
istioctl x precheck

echo "==> Gateway API CRDs (${GATEWAY_API_VERSION})"
if kubectl get crd gateways.gateway.networking.k8s.io >/dev/null 2>&1; then
  echo "    already installed"
else
  kubectl apply --server-side -f \
    "https://github.com/kubernetes-sigs/gateway-api/releases/download/${GATEWAY_API_VERSION}/standard-install.yaml"
fi

echo "==> istioctl install (ambient)"
istioctl install -f "$ROOT/istio/install/istio-ambient.yaml" -y

echo "==> add-ons"
for addon in prometheus grafana kiali jaeger; do
  url="https://raw.githubusercontent.com/istio/istio/release-${ISTIO_MINOR}/samples/addons/${addon}.yaml"
  kubectl apply -f "$url" >/dev/null || { echo "    retrying $addon"; sleep 5; kubectl apply -f "$url" >/dev/null; }
  echo "    $addon"
done

echo "==> mesh tracing (Telemetry)"
kubectl apply -f "$ROOT/istio/install/telemetry.yaml"

echo "==> waiting for istio-system"
kubectl rollout status deploy/istiod -n istio-system --timeout=180s
kubectl rollout status ds/ztunnel -n istio-system --timeout=180s
kubectl get pods -n istio-system
kubectl get gatewayclass
