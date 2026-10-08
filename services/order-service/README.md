# order-service

Entry point for placing orders. Orchestrates catalog → pricing → inventory, then persists the order with a price snapshot.

| | |
|---|---|
| Local port | `8080` |
| Database | `order_db` (role `order_svc`) |
| Callers | ingress gateway (public), curl-client |
| Calls | catalog-service, pricing-service, inventory-service |

## API

| Method | Path | Notes |
|---|---|---|
| POST | `/api/orders` | `{customerId, items[{sku, quantity}]}` → `201` with order, `422` invalid/inactive SKU, `409` out of stock, `503` downstream failure |
| GET | `/api/orders/{orderNumber}` | |
| GET | `/api/orders?customerId=` | Newest first |

## Order flow

```
validate SKUs (catalog) ─► quote (pricing) ─► reserve (inventory, idempotency key = orderNumber) ─► persist
                                                                          └─ on persist failure: release (compensate)
```

Remote calls run outside any database transaction.

## Mesh-relevant design choices

| Choice | Reason |
|---|---|
| Plain `RestClient` with Service-name base URLs (`http://pricing-service`) | Discovery and load balancing come from Kubernetes plus the mesh. No Eureka, no Spring Cloud LoadBalancer |
| **No Resilience4j** | Retries, timeouts and circuit breaking are mesh policy (lab 07). The app keeps only a coarse 2s connect / 5s read timeout as a safety net |
| `HeaderPropagationInterceptor` | Forwards `x-request-id`, `x-pricing-version` and `x-customer-tier` to downstream calls. Without it, header-based routing (lab 06) breaks after the first hop |
| Micrometer Tracing (`w3c,b3`) | Propagates `traceparent` / `b3` so Istio can stitch gateway and waypoint spans into one trace (lab 08) |
| Built from Boot's `RestClient.Builder` | Keeps observation (metrics and tracing) wired in |

## Configuration

| Env var | Default |
|---|---|
| `SERVER_PORT` | `8080` |
| `CATALOG_SERVICE_URL` | `http://localhost:8081` |
| `INVENTORY_SERVICE_URL` | `http://localhost:8082` |
| `PRICING_SERVICE_URL` | `http://localhost:8083` |
| `DB_HOST` / `DB_NAME` / `DB_USER` / `DB_PASSWORD` | `localhost` / `order_db` / `order_svc` / `order_svc_pw` |

Propagated headers are configurable via `oms.downstream.propagate-headers`.

## Data

| Migration | Content |
|---|---|
| `V1__create_order_tables.sql` | `customer_order`, `order_line` |
| `V2__seed_orders.sql` | Two historical orders (`PRACTICE-1001` on v1, `PRACTICE-1002` on v2) |

## Run locally
Start the other three services first, then:

```bash
mvn -pl services/order-service spring-boot:run
./scripts/smoke-test.sh local
```
