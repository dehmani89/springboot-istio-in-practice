# Lab 09 — Sidecar Mode Comparison

## Goal
Run the same catalog-service image in classic **sidecar** mode alongside ambient. Compare footprint and operations, and confirm the two modes interoperate.

## Steps

```bash
kubectl apply -f istio/labs/09-sidecar-comparison/sidecar-namespace.yaml
kubectl apply -f istio/labs/09-sidecar-comparison/postgres-allow-sidecar.yaml    # needed if lab 04 applied
kubectl apply -f istio/labs/09-sidecar-comparison/catalog-service-sidecar.yaml
kubectl get pods -n dental-sidecar          # READY 2/2  ← app + istio-proxy
kubectl get pods -n dental                  # READY 1/1  ← ambient
```

Compare:

```bash
kubectl top pods -n dental-sidecar --containers     # istio-proxy CPU/memory per pod
kubectl top pods -n istio-system -l app=ztunnel     # shared per-node cost in ambient
istioctl proxy-status                               # sidecars and waypoints/gateways listed
kubectl get pod -n dental-sidecar -l app=catalog-service -o jsonpath='{.items[0].spec.initContainers[*].name}{"\n"}{.items[0].spec.containers[*].name}{"\n"}'
```

Interop: the ambient curl-client calls the sidecar workload over mTLS.

```bash
curlc http://catalog-service.dental-sidecar/api/products/DEN-GLV-001; echo
```

If lab 04 is applied, this call is allowed because `dental-sidecar` has no deny-all.

## Comparison

| Dimension | Sidecar | Ambient |
|---|---|---|
| Onboarding | Restart pods (injection at admission) | Namespace label, no restart |
| Per-pod overhead | One Envoy per pod | None; ztunnel per node, waypoint per namespace or Service as needed |
| L7 features | Always on, every hop | Opt-in via waypoint |
| Upgrades | Restart every workload to pick up a new proxy | Upgrade ztunnel/waypoints independently of apps |
| Blast radius | Proxy fault affects one pod | ztunnel fault affects one node's workloads |
| Ecosystem / docs | Largest body of examples | Growing; Istio's strategic direction |

## Cleanup

```bash
kubectl delete namespace dental-sidecar
kubectl delete authorizationpolicy postgres-allow-sidecar-catalog -n dental-data --ignore-not-found
```
