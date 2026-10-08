# springboot-istio-in-practice project

A hands-on workspace for learning **Istio service mesh (ambient mode)** with **Spring Boot** microservices, built around a realistic domain: an **Order Management System for dental products**.

## Purpose

- Build practical Istio fluency through incremental, runnable labs rather than reading.
- Demonstrate which cross-cutting concerns move **out of application code** and into the mesh: mTLS, identity-based authorization, traffic shifting, retries, timeouts, circuit breaking and telemetry.
- Give the team a reference implementation for how Spring Boot services should be written to cooperate with a mesh: header propagation, idempotency, health probes and per-service identity.

## System at a glance

```
                      ┌───────────────── Istio ambient mesh (mTLS everywhere) ─────────────────┐
  laptop ──► Gateway  │                                                                        │
  :80        (Gateway │   order-service ──► catalog-service                                    │
             API)  ───┼─►   :8080      ──► [waypoint] ──► pricing-service v1 / v2  (canary)    │
                      │                ──► [waypoint] ──► inventory-service                    │
                      │        └─────────────┴──────────────┴─────────────► Postgres           │
                      │                                         (one database per service)     │
                      └────────────────────────────────────────────────────────────────────────┘
```

Details are in [docs/architecture.md](docs/architecture.md). Rationale for key choices is in [docs/design-decisions.md](docs/design-decisions.md).

## Projects in this workspace

| Module | Port | Responsibility | README |
|---|---|---|---|
| `services/order-service` | 8080 | Public entry point; orchestrates order placement | [README](services/order-service/README.md) |
| `services/catalog-service` | 8081 | Dental product master data | [README](services/catalog-service/README.md) |
| `services/inventory-service` | 8082 | Stock levels and idempotent reservations | [README](services/inventory-service/README.md) |
| `services/pricing-service` | 8083 | Quotes; v1 list price, v2 volume discounts, with chaos switches | [README](services/pricing-service/README.md) |

Supporting folders:

| Folder | Contents | README |
|---|---|---|
| `infra/` | Local Postgres (docker compose) and shared DB bootstrap SQL | [README](infra/README.md) |
| `k8s/` | Plain Kubernetes manifests, with no Istio resources | [README](k8s/README.md) |
| `istio/` | Istio install profile and nine sequential labs | [README](istio/README.md) |
| `scripts/` | Build, install, deploy, smoke-test, traffic and teardown helpers | — |
| `http/` | IntelliJ HTTP Client requests (`local` and `mesh` environments) | [README](http/README.md) |
| `docs/` | Architecture and design decisions | — |

## Technology baseline

| Area | Version / choice |
|---|---|
| Language / framework | Java 21, Spring Boot 3.5.x |
| Build | Maven multi-module (parent POM + one module per service) |
| Persistence | PostgreSQL 17, one database and role per service, Flyway migrations and seed data |
| Runtime | Docker Desktop with built-in Kubernetes |
| Mesh | Istio, **ambient** data plane (ztunnel + waypoints) |
| Traffic APIs | Kubernetes Gateway API (`Gateway`, `HTTPRoute`) |
| Observability | Micrometer (Prometheus metrics, W3C/B3 trace propagation), Kiali, Grafana, Jaeger |

## Prerequisites

- JDK 21, Maven 3.9+
- Docker Desktop with Kubernetes enabled and ≥ 4 CPU / 8 GB RAM allocated
- `istioctl` (`brew install istioctl`) for the mesh path
- IntelliJ IDEA: open the **root folder** and IntelliJ imports all modules from the parent `pom.xml`

## Quick start A — local, no Kubernetes (about 5 minutes)

```bash
make local-db                                    # Postgres on localhost:5432
mvn -DskipTests install
mvn -pl services/catalog-service   spring-boot:run   # each in its own terminal,
mvn -pl services/inventory-service spring-boot:run   # or run the *Application classes
mvn -pl services/pricing-service   spring-boot:run   # from IntelliJ
mvn -pl services/order-service     spring-boot:run
./scripts/smoke-test.sh local
```

Flyway creates each schema and seeds data on first start.

## Quick start B — Kubernetes and Istio

```bash
make images          # jars + local Docker images (no registry needed with Docker Desktop)
make istio           # Gateway API CRDs, Istio ambient, Kiali/Prometheus/Grafana/Jaeger
make deploy          # Postgres + services, initially OUTSIDE the mesh
```

Then follow the labs in order, starting with [Lab 01](istio/labs/01-baseline-no-mesh/README.md).

## Learning path

| # | Lab | You will learn |
|---|---|---|
| 01 | Baseline, no mesh | What plain Kubernetes does and does not give you |
| 02 | Ambient enrollment and mTLS | Zero-restart onboarding, SPIFFE identities, STRICT mTLS |
| 03 | Ingress gateway | Gateway API north–south routing |
| 04 | L4 authorization | Zero trust with identity allow-lists, including the database |
| 05 | Waypoint and L7 authorization | Opt-in L7 proxying and method/path policies |
| 06 | Traffic splitting | Canary, header routing, and why apps must propagate headers |
| 07 | Resilience | Timeouts, retries, outlier detection, and why idempotency matters |
| 08 | Observability | Topology, golden signals, distributed tracing |
| 09 | Sidecar comparison | Sidecar vs ambient trade-offs and interop |

## Seed data cheat sheet

| SKU | Notes |
|---|---|
| `DEN-GLV-001` | Nitrile gloves (M), plentiful stock; default happy-path item |
| `DEN-DSP-001` | Saliva ejectors; used by the traffic generator. Quantities ≥10 show v2 discounts |
| `DEN-IMP-002` | **Out of stock**, returns `409` |
| `DEN-BUR-002` | Low stock (3) |
| `DEN-END-001` | **Inactive product**, returns `422` |
| `PRACTICE-1001`, `PRACTICE-1002` | Customers with seeded order history |

## Conventions

- Service code contains **no mesh-specific libraries**. Anything Istio does is configured in `istio/`.
- Configuration is via environment variables with local defaults. No Spring profiles are required.
- Each service owns its schema through Flyway: `V1__*` for schema, `V2__*` for seed data.
- All credentials in this repository are **development-only**.

## Housekeeping

```bash
make teardown        # remove app namespaces
make teardown-all    # also uninstall Istio
make local-db-down   # remove local Postgres and its volume
```
