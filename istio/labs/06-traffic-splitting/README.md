# Lab 06 — Traffic Splitting and Header-Based Routing

## Goal
Canary `pricing-service` v2 (volume discounts) using weighted and header-based routes. Observe why **header propagation in the application** is a mesh prerequisite.

## Background
| Version | Behaviour |
|---|---|
| v1 | List price only |
| v2 | Volume tiers: ≥10 → 5%, ≥25 → 10%, ≥50 → 15% |

Every order response includes `pricingEngineVersion`, so routing decisions are visible end to end.

## Steps

1. Baseline without a route: about a 50/50 split (round-robin over both Deployments).
   ```bash
   ./scripts/generate-traffic.sh 40
   ```
2. Weighted canary at 90/10:
   ```bash
   kubectl apply -f istio/labs/06-traffic-splitting/pricing-route-weighted.yaml
   ./scripts/generate-traffic.sh 100        # ≈ 90 v1 / 10 v2
   ```
3. Header-based routing for internal testers:
   ```bash
   kubectl apply -f istio/labs/06-traffic-splitting/pricing-route-header.yaml
   ./scripts/generate-traffic.sh 20                     # all v1
   PRICING_VERSION=v2 ./scripts/generate-traffic.sh 20  # all v2
   ```
   The header enters at the gateway and is addressed to **order-service**, yet it affects routing to **pricing-service** two hops away. That works only because `HeaderPropagationInterceptor` in order-service copies `x-pricing-version` onto outbound calls. Remove the header from `oms.downstream.propagate-headers`, rebuild, and watch it stop working.
4. Promote:
   ```bash
   kubectl apply -f istio/labs/06-traffic-splitting/pricing-route-v2-promoted.yaml
   ./scripts/generate-traffic.sh 20         # all v2
   ```
5. Roll back instantly by re-applying the weighted or header route. No redeploy is needed.

## Expected results
Distributions match the route definitions within normal sampling variance.

## Takeaways
- Gateway API splits across **Services** (`pricing-service-v1` / `-v2`), unlike Istio's legacy `DestinationRule` subsets.
- `parentRefs: kind: Service` (the GAMMA pattern) routes **east–west** mesh traffic, and the waypoint executes it.
- The mesh can route on headers only if every hop forwards them. Header and trace propagation is an application responsibility.

## Cleanup
Leave a route in place or delete it: `kubectl delete httproute pricing-service -n dental`
