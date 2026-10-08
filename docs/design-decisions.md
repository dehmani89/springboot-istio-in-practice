# Design Decisions

Lightweight decision records for the choices that shape this workspace.

---

## DD-01 — Istio ambient mode as the primary data plane

**Status:** Accepted

**Context.** Istio offers two data planes. Sidecar mode injects an Envoy proxy into every pod. Ambient mode uses a per-node L4 proxy (ztunnel) plus optional per-namespace or per-service L7 proxies (waypoints). Ambient reached general availability in Istio 1.24, and subsequent releases have focused on sidecar-to-ambient migration.

**Decision.** Use ambient as the primary learning track, with one sidecar lab (lab 09) for comparison.

**Consequences.**
- (+) Zero-restart onboarding and lower resource overhead on a laptop cluster.
- (+) Clear separation between L4 (identity, mTLS) and L7 (routing, resilience) concepts.
- (+) Aligns learning with the project's strategic direction.
- (−) Fewer community examples than sidecar mode. Some older Istio APIs and features behave differently or require a waypoint.

---

## DD-02 — Kubernetes Gateway API for ingress and mesh routing

**Status:** Accepted

**Context.** Istio supports its own `Gateway`/`VirtualService` APIs and the Kubernetes Gateway API. Waypoints are defined natively as Gateway API resources.

**Decision.** Use `Gateway` and `HTTPRoute` for ingress and east–west routing. Use Istio APIs only where Gateway API has no equivalent (`PeerAuthentication`, `AuthorizationPolicy`, `DestinationRule`, `Telemetry`).

**Consequences.** (+) Portable, vendor-neutral skills. (−) Some resilience features, such as fine-grained retries, are still in the Gateway API experimental channel.

---

## DD-03 — No client-side resilience or discovery libraries

**Status:** Accepted

**Context.** Spring Cloud traditionally supplies discovery (Eureka), client-side load balancing and circuit breakers (Resilience4j). A mesh provides these at the platform layer.

**Decision.** Services use plain `RestClient` with Kubernetes Service names, plus coarse connect/read timeouts as a safety net. Retries, timeouts and circuit breaking are mesh configuration.

**Consequences.**
- (+) Policy changes without redeploys; consistent behaviour across languages.
- (−) Local runs without the mesh have no retries or circuit breaking. This is acceptable for development.
- (!) Mesh retries make **idempotency** an application requirement. The inventory reservation endpoint is idempotent on `orderRef`.

---

## DD-04 — Application-level header propagation

**Status:** Accepted

**Context.** The mesh can route and trace only on headers present at each hop. A service that does not forward inbound headers breaks header-based routing and trace continuity downstream.

**Decision.** Micrometer Tracing propagates `traceparent` and `b3`. A `HeaderPropagationInterceptor` in order-service forwards a configurable allow-list (`x-request-id`, `x-pricing-version`, `x-customer-tier`).

**Consequences.** (+) End-to-end canary routing and traces. (−) Every calling service must adopt the convention. It is a candidate for a shared starter library in a larger codebase.

---

## DD-05 — PostgreSQL, database-per-service on a shared instance

**Status:** Accepted

**Context.** Realistic persistence is needed to exercise TCP mesh traffic, L4 policies on non-HTTP ports and data ownership, while keeping laptop resource usage reasonable.

**Decision.** One Postgres 17 instance with four databases and four login roles. Each service owns its database exclusively and manages its schema through Flyway. Bootstrap SQL is a single file shared by docker compose and Kubernetes.

**Consequences.** (+) Strong logical isolation, realistic migrations and seed data, and identity-based DB access control in the mesh. (−) Shared failure domain and resource pool, which is not representative of production deployment.

---

## DD-06 — Version as a deployment attribute, not a code branch

**Status:** Accepted

**Context.** Canary labs need two observably different versions of a service.

**Decision.** pricing-service ships one image. `PRICING_ENGINE_VERSION` selects behaviour, and two Deployments plus version-specific Services (`pricing-service-v1`, `-v2`) serve as `HTTPRoute` backends.

**Consequences.** (+) A single build, and the version is visible in every response. (−) Real canaries usually differ by image tag. The routing mechanics are identical.
