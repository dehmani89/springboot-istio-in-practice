# Lab 07 — Resilience: Timeouts, Retries, Outlier Detection

## Goal
Use pricing-service's built-in chaos switches to observe mesh resilience features, and understand why **idempotency** is mandatory once a mesh can retry.

## Chaos switches (pricing-service)

| Env var | Effect |
|---|---|
| `CHAOS_LATENCY_MS` | Fixed delay on every `/api/**` request |
| `CHAOS_FAILURE_RATE` | 0.0–1.0 probability of HTTP 503 |

Changing env vars triggers a rollout. Wait for it to finish with `kubectl rollout status deploy/pricing-service-v2 -n dental`.

## A. Mesh timeout vs application timeout

```bash
kubectl set env deploy/pricing-service-v2 -n dental CHAOS_LATENCY_MS=3000
kubectl apply -f istio/labs/07-resilience/01-pricing-route-timeout.yaml   # 100% v2, 1s timeout
time curl -s -X POST localhost/api/orders -H 'Content-Type: application/json' \
  -d '{"customerId":"PRACTICE-3001","items":[{"sku":"DEN-GLV-001","quantity":1}]}'
```

**Expected:** about 1s, then a `503` problem response from order-service with `downstreamStatus: 504`. The waypoint gave up at 1s, long before order-service's 5s read timeout. Delete the route and repeat: the call takes about 3s and succeeds.

**Takeaway:** mesh timeouts are tunable per route without a redeploy. The app timeout remains a coarse safety net.

## B. Retries

```bash
kubectl set env deploy/pricing-service-v2 -n dental CHAOS_LATENCY_MS=0 CHAOS_FAILURE_RATE=0.5
kubectl apply -f istio/labs/06-traffic-splitting/pricing-route-v2-promoted.yaml
./scripts/generate-traffic.sh 40
kubectl logs -n dental deploy/waypoint --tail=100 | grep -E ' 503 |URX' | head
```

**Observe:** the success rate is noticeably higher than 50%. Istio applies a default retry policy at the waypoint. Compare pricing-service-v2's own logs (`Chaos: injecting 503`) with the order outcome. Some orders succeed although v2 returned 503 on the first attempt.

**Why idempotency matters:** pricing quotes are side-effect free, so retries are safe. Inventory reservations are not, which is why `ReservationService.reserve` is idempotent on `orderRef`. A retried reservation returns `replayed: true` instead of double-reserving stock. Any endpoint behind a retrying mesh must tolerate duplicates.

> Fine-grained retry configuration on `HTTPRoute` (`retry` field) is part of the Gateway API **experimental** channel. Check your Istio and Gateway API versions before relying on it. Default retries are sufficient for this lab.

## C. Outlier detection (passive health checking)

```bash
kubectl delete httproute pricing-service -n dental            # endpoints = v1 pod + v2 pod
kubectl set env deploy/pricing-service-v2 -n dental CHAOS_FAILURE_RATE=1.0
kubectl apply -f istio/labs/07-resilience/02-pricing-outlier-detection.yaml
./scripts/generate-traffic.sh 40
```

**Expected:** a handful of early failures or retries, then every order shows `pricingEngineVersion: v1`. The waypoint ejected the failing v2 endpoint for 30s.

**Takeaway:** the mesh provides circuit breaking. There is no Resilience4j in the codebase, and the policy is operational config.

## Cleanup

```bash
kubectl set env deploy/pricing-service-v2 -n dental CHAOS_LATENCY_MS=0 CHAOS_FAILURE_RATE=0.0
kubectl delete destinationrule pricing-service -n dental --ignore-not-found
kubectl delete httproute pricing-service -n dental --ignore-not-found
```
