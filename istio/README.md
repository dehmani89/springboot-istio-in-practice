# Istio Setup — Ambient Mode on Docker Desktop Kubernetes

This folder holds everything mesh-related: the control-plane install profile and a sequence of hands-on labs. Workload manifests live separately in `../k8s` so the delta the mesh introduces stays visible.

## Target configuration

| Item | Choice | Rationale |
|---|---|---|
| Cluster | Docker Desktop built-in Kubernetes | Already installed; LoadBalancer Services map to `localhost` |
| Data plane | **Ambient** (ztunnel + waypoints) | GA since Istio 1.24; the project's strategic direction; lower per-pod overhead than sidecars |
| Ingress / L7 routing API | **Kubernetes Gateway API** (`Gateway`, `HTTPRoute`) | Vendor-neutral standard; native API for ambient waypoints |
| Security / resilience API | Istio `PeerAuthentication`, `AuthorizationPolicy`, `DestinationRule` | No Gateway API equivalent yet |
| Observability | Kiali, Prometheus, Grafana, Jaeger add-ons | Standard Istio sample add-ons |
| Comparison | Lab 09 runs one service in **sidecar** mode | Most existing docs assume sidecars |

## Ambient in one diagram

```
  Pod A ──► ztunnel (node) ══ HBONE / mTLS ══► [waypoint] ──► ztunnel (node) ──► Pod B
            L4: identity, mTLS,                 L7 (optional, per Service):
            L4 authz, TCP telemetry             HTTP routing, retries, timeouts,
                                                L7 authz, HTTP metrics, tracing
```

- **ztunnel**: one per node (DaemonSet). Every enrolled pod gets mTLS and a SPIFFE identity with **no restart and no sidecar**.
- **waypoint**: an Envoy deployment you add only for Services that need L7 features. It is opt-in, per namespace or per Service.

## Prerequisites

| Requirement | Check |
|---|---|
| Docker Desktop with Kubernetes enabled | `kubectl config current-context` → `docker-desktop` |
| Docker Desktop resources ≥ 4 CPU / 8 GB RAM | Settings → Resources |
| `istioctl` (latest stable) | `brew install istioctl` then `istioctl version --remote=false` |
| `kubectl` | bundled with Docker Desktop |

Run the pre-flight check:

```bash
istioctl x precheck
```

## Install (scripted)

```bash
./scripts/istio-install.sh
```

The script performs the steps below. Run them manually the first time if you want to understand each one.

### 1. Gateway API CRDs

Docker Desktop does not ship them.

```bash
kubectl get crd gateways.gateway.networking.k8s.io &>/dev/null || \
  kubectl apply --server-side -f \
  https://github.com/kubernetes-sigs/gateway-api/releases/download/v1.3.0/standard-install.yaml
```

> Check the Istio release notes for the Gateway API version paired with your `istioctl` version and override with `GATEWAY_API_VERSION=vX.Y.Z ./scripts/istio-install.sh`.

### 2. Control plane and ambient data plane

```bash
istioctl install -f istio/install/istio-ambient.yaml -y
```

This installs `istiod`, `istio-cni` (traffic redirection) and `ztunnel` into `istio-system`. See `install/istio-ambient.yaml` for mesh config: access logs and the Jaeger/Zipkin tracing provider.

### 3. Observability add-ons and tracing

```bash
ISTIO_MINOR=$(istioctl version --remote=false | grep -oE '[0-9]+\.[0-9]+' | head -1)
for a in prometheus grafana kiali jaeger; do
  kubectl apply -f https://raw.githubusercontent.com/istio/istio/release-${ISTIO_MINOR}/samples/addons/${a}.yaml
done
kubectl apply -f istio/install/telemetry.yaml
```

### 4. Verify

```bash
kubectl get pods -n istio-system
# expect: istiod, istio-cni-node-*, ztunnel-*, prometheus, grafana, kiali, jaeger — all Running

istioctl verify-install -f istio/install/istio-ambient.yaml
kubectl get gatewayclass         # expect: istio, istio-waypoint (and istio-remote)
```

## Labs

Work through them in order. Each lab has its own README with goal, steps, expected results and cleanup.

| # | Lab | Mesh concept | Key resources |
|---|---|---|---|
| 01 | [Baseline, no mesh](labs/01-baseline-no-mesh/README.md) | Reference behaviour; plaintext traffic | — |
| 02 | [Ambient enrollment and mTLS](labs/02-ambient-enrollment-mtls/README.md) | Zero-restart onboarding, SPIFFE identity, STRICT mTLS | namespace label, `PeerAuthentication` |
| 03 | [Ingress gateway](labs/03-ingress-gateway/README.md) | North–south traffic | `Gateway`, `HTTPRoute` |
| 04 | [L4 authorization](labs/04-l4-authorization/README.md) | Zero-trust, identity-based allow-lists | `AuthorizationPolicy` (selector) |
| 05 | [Waypoint and L7 authorization](labs/05-waypoint-l7-authorization/README.md) | L7 proxy opt-in, method/path policy | waypoint `Gateway`, `AuthorizationPolicy` (targetRefs) |
| 06 | [Traffic splitting](labs/06-traffic-splitting/README.md) | Canary, header routing, header propagation | `HTTPRoute` (GAMMA) |
| 07 | [Resilience](labs/07-resilience/README.md) | Timeouts, retries, outlier detection, idempotency | `HTTPRoute.timeouts`, `DestinationRule` |
| 08 | [Observability](labs/08-observability/README.md) | Topology, golden signals, distributed tracing | Kiali, Prometheus, Grafana, Jaeger |
| 09 | [Sidecar comparison](labs/09-sidecar-comparison/README.md) | Sidecar vs ambient trade-offs, interop | `istio-injection` label |

## Useful commands

```bash
istioctl ztunnel-config workloads              # which pods ztunnel manages, and their protocol (HBONE = in mesh)
istioctl ztunnel-config certificates           # workload certificates / identities
istioctl waypoint list -A
istioctl analyze -A                             # config validation
kubectl logs -n istio-system ds/ztunnel -f      # L4 connection logs (src/dst identity, allow/deny)
kubectl logs -n dental deploy/waypoint -f       # L7 access logs (after lab 05)
istioctl dashboard kiali                        # also: grafana, prometheus, jaeger
```

## Uninstall

```bash
./scripts/teardown.sh --istio
```

Manual:

```bash
kubectl delete -f istio/install/telemetry.yaml --ignore-not-found
istioctl uninstall --purge -y
kubectl delete namespace istio-system
# optional: kubectl delete -f <gateway-api standard-install.yaml URL>
```

## Troubleshooting

| Symptom | Likely cause / action |
|---|---|
| `ztunnel` or `istio-cni-node` CrashLoopBackOff | Insufficient Docker Desktop resources, or an unsupported CNI setup. Run `istioctl x precheck` and check pod logs |
| Kiali add-on apply fails first time | CRD ordering race; re-run the `kubectl apply` |
| `localhost:80` not responding after lab 03 | `kubectl get svc -n dental-ingress` must show `EXTERNAL-IP localhost`. Port 80 may be taken by another local process |
| Requests reset after applying lab 04 | Expected for denied identities. Inspect `kubectl logs -n istio-system ds/ztunnel` for `policy rejection` entries |
| L7 policy or route has no effect | The Service is not using a waypoint. Check `istioctl ztunnel-config services` / the `istio.io/use-waypoint` label |
| Waypoint-fronted calls fail after lab 04 + 05 | Workload policies must admit the waypoint identity (`05/01-l4-allow-waypoint.yaml`). Check ztunnel logs for the rejected principal |
| Prometheus `/actuator/prometheus` targets DOWN after STRICT | Expected: Prometheus is outside the mesh and scrapes in plaintext. Mesh metrics (ztunnel/waypoint) are unaffected. See lab 08 |
