# Lab 01 — Baseline, No Mesh

## Goal
Establish reference behaviour before Istio touches the workloads. Istio is installed in the cluster but the `dental` namespaces are **not** enrolled.

## Steps

```bash
./scripts/build-images.sh
./scripts/deploy.sh
kubectl get pods -n dental         # all 1/1 READY — no sidecars
kubectl get pods -n dental-data
```

Call services from inside the cluster:

```bash
curlc http://catalog-service/api/products | head -c 400; echo
curlc http://inventory-service/api/inventory/DEN-GLV-001; echo
curlc -X POST http://order-service/api/orders -H 'Content-Type: application/json' \
  -d '{"customerId":"PRACTICE-2001","items":[{"sku":"DEN-GLV-001","quantity":12},{"sku":"DEN-PRV-001","quantity":3}]}'; echo
```

Prove the traffic is unauthenticated plaintext — anyone can call inventory directly:

```bash
curlc -X POST http://inventory-service/api/inventory/reservations -H 'Content-Type: application/json' \
  -d '{"orderRef":"rogue-1","items":[{"sku":"DEN-GLV-001","quantity":1}]}'; echo
```

Check the ztunnel view:

```bash
istioctl ztunnel-config workloads | grep dental     # PROTOCOL = TCP  → not in mesh
```

## Expected results
- Order placed successfully; `pricingEngineVersion` alternates between `v1` and `v2` (plain Kubernetes round-robin over both pricing Deployments).
- The rogue reservation succeeds. There is no identity and no policy.

## Takeaways
Kubernetes Services give you discovery and naive load balancing only. There is no encryption, no caller identity, no traffic control and no L7 telemetry.

## Cleanup
Release the rogue reservation: `curlc -X DELETE http://inventory-service/api/inventory/reservations/rogue-1`
