# inventory-service

Stock levels per SKU and all-or-nothing reservations for orders.

| | |
|---|---|
| Local port | `8082` |
| Database | `inventory_db` (role `inventory_svc`) |
| Callers | order-service only (enforced in labs 04–05) |
| Calls | — |

## API

| Method | Path | Notes |
|---|---|---|
| GET | `/api/inventory` | All stock items |
| GET | `/api/inventory/{sku}` | `onHand, reserved, available, belowReorderLevel` |
| POST | `/api/inventory/reservations` | `{orderRef, items[{sku, quantity}]}`. `201` new, `200` replay, `409` with `shortages[]` |
| DELETE | `/api/inventory/reservations/{orderRef}` | Release (compensation). Idempotent, `204` |

## Design notes
- **Idempotent on `orderRef`.** A repeated POST returns the original reservation with `replayed: true`. This is required because the mesh may retry (lab 07).
- **Pessimistic row locks** in sorted SKU order prevent overselling and deadlocks under concurrent orders.
- DB constraint `reserved <= on_hand` is the last line of defence.

## Data

| Migration | Content |
|---|---|
| `V1__create_inventory_tables.sql` | `stock_item`, `reservation` |
| `V2__seed_stock.sql` | Stock for all 16 SKUs. `DEN-IMP-002` = 0 (out of stock), `DEN-BUR-002` = 3 (low) |

## Configuration
`SERVER_PORT` (8082), `DB_HOST`, `DB_PORT`, `DB_NAME` (`inventory_db`), `DB_USER` (`inventory_svc`), `DB_PASSWORD` (`inventory_svc_pw`).

## Run locally

```bash
mvn -pl services/inventory-service spring-boot:run
```

## Role in the Istio labs
- **Lab 04:** L4 allow-list limited to order-service; curl-client is reset at the connection level.
- **Lab 05:** fronted by the waypoint; L7 policy permits curl-client `GET` but not `POST`/`DELETE` (HTTP 403).
- **Lab 07:** the idempotency design is the reason mesh retries are safe here.
