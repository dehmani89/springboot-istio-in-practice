# catalog-service

Dental product master data: SKUs, names, categories, manufacturers. Read-only API.

| | |
|---|---|
| Local port | `8081` |
| Database | `catalog_db` (role `catalog_svc`) |
| Callers | order-service, ingress gateway (public), curl-client |
| Calls | — |

## API

| Method | Path | Notes |
|---|---|---|
| GET | `/api/products` | Optional `?category=` (case-insensitive) |
| GET | `/api/products/{sku}` | `404` problem detail if unknown |

Response fields: `sku, name, category, manufacturer, description, unitOfMeasure, active`.

## Data

| Migration | Content |
|---|---|
| `V1__create_product_table.sql` | `product` table, category index |
| `V2__seed_products.sql` | 16 products across 9 categories. `DEN-END-001` is **inactive** (order validation test) |

## Configuration

| Env var | Default |
|---|---|
| `SERVER_PORT` | `8081` |
| `DB_HOST` / `DB_PORT` / `DB_NAME` | `localhost` / `5432` / `catalog_db` |
| `DB_USER` / `DB_PASSWORD` | `catalog_svc` / `catalog_svc_pw` |

## Run locally

```bash
docker compose -f infra/docker-compose.yml up -d
mvn -pl services/catalog-service spring-boot:run
curl -s localhost:8081/api/products/DEN-GLV-001
```

## Role in the Istio labs
- **Lab 03:** exposed publicly through the ingress gateway (`/api/products`).
- **Lab 04:** allow-list includes the gateway identity. A public service still uses zero trust.
- **Lab 05:** deliberately has **no waypoint**. It shows that L4-only services pay no Envoy hop and emit only TCP telemetry.
- **Lab 09:** deployed a second time in sidecar mode for comparison.
