#!/usr/bin/env bash
# Compatible with macOS bash 3.2.
# Place N orders through the gateway and tally which pricing engine served them.
#   ./scripts/generate-traffic.sh 100
#   PRICING_VERSION=v2 ./scripts/generate-traffic.sh 20     # adds x-pricing-version header
#   BASE_URL=http://localhost:8080 ./scripts/generate-traffic.sh 10
set -uo pipefail
N="${1:-50}"
BASE_URL="${BASE_URL:-http://localhost}"
HDR=()
[[ -n "${PRICING_VERSION:-}" ]] && HDR=(-H "x-pricing-version: ${PRICING_VERSION}")

v1=0; v2=0; fail=0
for ((i=1; i<=N; i++)); do
  qty=$(( (RANDOM % 30) + 1 ))
  body="{\"customerId\":\"PRACTICE-LOAD\",\"items\":[{\"sku\":\"DEN-DSP-001\",\"quantity\":${qty}}]}"
  resp="$(curl -s -m 10 -X POST "$BASE_URL/api/orders" -H 'Content-Type: application/json' ${HDR[@]+"${HDR[@]}"} -d "$body")"
  case "$resp" in
    *'"pricingEngineVersion":"v1"'*) ((v1++)); printf '1' ;;
    *'"pricingEngineVersion":"v2"'*) ((v2++)); printf '2' ;;
    *) ((fail++)); printf 'x' ;;
  esac
done
echo
echo "orders=$N  v1=$v1  v2=$v2  failed=$fail"
