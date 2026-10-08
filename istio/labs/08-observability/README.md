# Lab 08 — Observability

## Goal
See topology, golden signals and distributed traces with zero instrumentation code beyond trace-header propagation.

## Steps

1. Generate steady traffic in a separate terminal:
   ```bash
   ./scripts/generate-traffic.sh 500
   ```
2. **Kiali**, for topology and health:
   ```bash
   istioctl dashboard kiali
   ```
   In the Graph view, select namespaces `dental`, `dental-ingress` and `dental-data` and display Security and Traffic Distribution. Observe mTLS padlocks on every edge, the waypoint node, and the v1/v2 split on pricing. Edges through ztunnel only show TCP metrics, while waypoint-fronted edges show HTTP rates and response codes.
3. **Grafana**, for golden signals:
   ```bash
   istioctl dashboard grafana
   ```
   Open the *Istio Service Dashboard* and *Istio Workload Dashboard* for `pricing-service`, and the *Istio Ztunnel Dashboard* where available.
4. **Jaeger**, for distributed tracing:
   ```bash
   istioctl dashboard jaeger
   ```
   Find traces for service `dental-gateway-istio.dental-ingress`. A trace spans gateway → order-service → waypoint → pricing/inventory.
5. **Prometheus**, for raw queries:
   ```bash
   istioctl dashboard prometheus
   ```
   ```promql
   sum by (destination_service_name, response_code) (rate(istio_requests_total{destination_service_namespace="dental"}[1m]))
   sum by (destination_workload) (rate(istio_tcp_sent_bytes_total{destination_workload_namespace="dental"}[1m]))
   ```

## Ambient-specific caveats
- **L7 metrics and spans come only from L7 proxies** (gateway and waypoint). Calls to catalog-service, which has no waypoint, appear as TCP-level telemetry from ztunnel.
- **Trace continuity requires header propagation.** Spring Boot's Micrometer Tracing forwards `traceparent` and `b3` (`management.tracing.propagation.type: w3c,b3`). Log lines include `[service,traceId,spanId]` for correlation.
- **App metrics vs STRICT mTLS.** The Prometheus add-on runs outside the mesh, so after lab 02's STRICT policy, plaintext scrapes of `/actuator/prometheus` are rejected and those targets show DOWN under Status → Targets. Mesh metrics are unaffected. Options: switch `dental` to PERMISSIVE for the lab, or enroll Prometheus in the mesh. Treat this as a lesson in how STRICT interacts with out-of-mesh tooling.

## Takeaways
The mesh delivers consistent, language-agnostic telemetry. The application's only obligation is to propagate context headers.
