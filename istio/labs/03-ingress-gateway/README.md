# Lab 03 — Ingress Gateway (Gateway API)

## Goal
Expose `order-service` and `catalog-service` to your laptop through an Istio-managed gateway. Keep `inventory-service` and `pricing-service` internal.

## Steps

```bash
kubectl apply -f istio/labs/03-ingress-gateway/gateway.yaml
kubectl apply -f istio/labs/03-ingress-gateway/ingress-routes.yaml

kubectl get gateway -n dental-ingress              # PROGRAMMED=True
kubectl get svc -n dental-ingress                  # dental-gateway-istio  LoadBalancer  EXTERNAL-IP localhost
kubectl get httproute -n dental                    # dental-ingress
```

Test from your Mac:

```bash
curl -s localhost/api/products?category=Preventive
curl -s -X POST localhost/api/orders -H 'Content-Type: application/json' \
  -d '{"customerId":"PRACTICE-2001","items":[{"sku":"DEN-CMP-001","quantity":2}]}'
curl -s -o /dev/null -w '%{http_code}\n' localhost/api/inventory     # 404 — no route
./scripts/smoke-test.sh
```

Or use IntelliJ's HTTP client with environment `mesh` (`http/*.http`).

## Expected results
- `/api/orders` and `/api/products` are reachable on `localhost:80`.
- Internal services return 404 from the gateway because no route matches.

## Takeaways
- Gateway API `Gateway` describes the listener, and Istio provisions the Envoy deployment automatically, named `<gateway>-istio`.
- `HTTPRoute` attaches to the Gateway via `parentRefs`. Routes are owned by the app namespace and the gateway by the platform namespace. This is the intended role split.
- The gateway has its own identity (`dental-ingress/sa/dental-gateway-istio`), which lab 04 relies on.

## Cleanup
Keep for subsequent labs.
