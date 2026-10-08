#!/usr/bin/env bash
# End-to-end check.
#   ./scripts/smoke-test.sh               → via ingress gateway (http://localhost), lab 03+
#   ./scripts/smoke-test.sh in-cluster    → from curl-client pod inside the cluster
#   ./scripts/smoke-test.sh local         → services running locally (ports 8080/8081)
set -euo pipefail
MODE="${1:-gateway}"

case "$MODE" in
  gateway)    ORDER=http://localhost;            CATALOG=http://localhost ;;
  local)      ORDER=http://localhost:8080;       CATALOG=http://localhost:8081 ;;
  in-cluster) ORDER=http://order-service;        CATALOG=http://catalog-service ;;
  *) echo "usage: $0 [gateway|local|in-cluster]"; exit 1 ;;
esac

call() {
  if [[ "$MODE" == "in-cluster" ]]; then
    kubectl exec -n dental deploy/curl-client -- curl -s -w '\n%{http_code}' "$@"
  else
    curl -s -w '\n%{http_code}' "$@"
  fi
}

check() {   # name, expected-code, curl args...
  local name="$1" expected="$2"; shift 2
  local out code
  out="$(call "$@" || true)"; code="$(tail -n1 <<<"$out")"
  if [[ "$code" == "$expected" ]]; then echo "PASS  $name ($code)"; else echo "FAIL  $name (got $code, want $expected)"; echo "$out" | sed '$d'; FAILED=1; fi
}

FAILED=0
check "list products"            200 "$CATALOG/api/products"
check "get product"              200 "$CATALOG/api/products/DEN-GLV-001"
check "seeded order history"     200 "$ORDER/api/orders?customerId=PRACTICE-1001"
check "place order"              201 -X POST "$ORDER/api/orders" -H 'Content-Type: application/json' \
      -d '{"customerId":"PRACTICE-9001","items":[{"sku":"DEN-GLV-001","quantity":12},{"sku":"DEN-PRV-001","quantity":2}]}'
check "out-of-stock rejected"    409 -X POST "$ORDER/api/orders" -H 'Content-Type: application/json' \
      -d '{"customerId":"PRACTICE-9001","items":[{"sku":"DEN-IMP-002","quantity":1}]}'
check "inactive product rejected" 422 -X POST "$ORDER/api/orders" -H 'Content-Type: application/json' \
      -d '{"customerId":"PRACTICE-9001","items":[{"sku":"DEN-END-001","quantity":1}]}'

exit "$FAILED"
