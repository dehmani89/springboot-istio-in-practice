# Lab 04 — L4 Authorization (Zero Trust)

## Goal
Move from "anything in the cluster can call anything" to explicit identity-based allow-lists, enforced by **ztunnel** at L4 (no waypoint required).

## Intended call graph

```
dental-gateway-istio ──► order-service ──► catalog-service
        │                     ├──────────► pricing-service (v1, v2)
        └──► catalog-service  └──────────► inventory-service
curl-client ──► order-service, catalog-service      (test identity)
catalog / inventory / pricing / order ──► postgres:5432
```

## Steps

1. Apply deny-all plus allow-lists together, to avoid a window of total denial:
   ```bash
   kubectl apply -f istio/labs/04-l4-authorization/
   ```
2. Allowed paths:
   ```bash
   ./scripts/smoke-test.sh                                     # via gateway: OK
   curlc http://catalog-service/api/products/DEN-GLV-001; echo  # curl-client → catalog: OK
   ```
3. Denied paths:
   ```bash
   curlc -m 3 http://inventory-service/api/inventory/DEN-GLV-001 || echo "denied (expected)"
   curlc -m 3 http://pricing-service/api/pricing/DEN-GLV-001     || echo "denied (expected)"
   ```
4. Find the rejection in ztunnel logs:
   ```bash
   kubectl logs -n istio-system ds/ztunnel --since=2m | grep -i -E 'rbac|policy|denied' | tail -5
   ```

## Expected results
- Denied calls fail at the **connection** level (`curl: (56) Recv failure: Connection reset by peer`), not with HTTP 403. ztunnel operates at L4 and cannot speak HTTP.

## Takeaways
- `selector`-based policies are enforced by ztunnel on the destination node and can match identity, namespace, IP and port only.
- An empty `spec: {}` ALLOW policy is the idiomatic deny-all.
- The database is protected by identity too. A compromised pod with stolen DB credentials still cannot connect unless it runs as an allowed ServiceAccount.

## Cleanup
Keep the policies. Lab 05 amends two of them.
