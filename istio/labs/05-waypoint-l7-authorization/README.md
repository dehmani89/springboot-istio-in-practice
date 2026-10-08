# Lab 05 — Waypoint Proxy and L7 Authorization

## Goal
Add an L7 proxy (waypoint) **only** for the Services that need HTTP-aware features (inventory and pricing), then write method/path-level policy.

## Steps

1. Deploy the waypoint:
   ```bash
   kubectl apply -f istio/labs/05-waypoint-l7-authorization/waypoint.yaml
   kubectl get gateway waypoint -n dental     # PROGRAMMED=True
   kubectl get pods -n dental -l gateway.networking.k8s.io/gateway-name=waypoint
   ```
2. Update workload policies to admit the waypoint identity **before** routing through it:
   ```bash
   kubectl apply -f istio/labs/05-waypoint-l7-authorization/01-l4-allow-waypoint.yaml
   ```
3. Opt the two Services into the waypoint:
   ```bash
   kubectl label service inventory-service pricing-service -n dental istio.io/use-waypoint=waypoint
   istioctl ztunnel-config services | grep -E 'inventory|pricing'   # WAYPOINT column = waypoint
   ```
4. Apply L7 policies (enforced by the waypoint):
   ```bash
   kubectl apply -f istio/labs/05-waypoint-l7-authorization/02-inventory-l7-policy.yaml
   kubectl apply -f istio/labs/05-waypoint-l7-authorization/03-pricing-l7-policy.yaml
   ```
5. Test:
   ```bash
   curlc http://inventory-service/api/inventory/DEN-GLV-001; echo        # 200 — curl-client may GET
   curlc -X POST http://inventory-service/api/inventory/reservations \
     -H 'Content-Type: application/json' \
     -d '{"orderRef":"rogue-2","items":[{"sku":"DEN-GLV-001","quantity":1}]}'; echo
   # → RBAC: access denied  (HTTP 403)
   ./scripts/smoke-test.sh                                                # orders still work
   kubectl logs -n dental deploy/waypoint --tail=20                       # L7 access log lines
   ```

## Expected results
- curl-client can read stock but receives **HTTP 403** on writes. That is a proper L7 response, compared with lab 04's connection reset.
- order-service flows are unaffected.

## Takeaways
- **Pay for L7 only where needed.** catalog-service and order-service have no waypoint and incur no Envoy hop.
- Policy placement changes: `selector` targets ztunnel (L4) while `targetRefs: Service` targets the waypoint (L7).
- Once a waypoint fronts a Service, the destination pod sees the **waypoint's** identity. Caller-specific rules must live on the waypoint. This is the most common ambient migration pitfall.

## Cleanup
Keep the waypoint. Labs 06 and 07 need it for pricing-service.
