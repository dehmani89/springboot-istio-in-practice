# infra — Local Infrastructure

Supporting infrastructure for running the services **outside** Kubernetes, plus the Postgres bootstrap SQL that is **shared** with the Kubernetes deployment.

## Contents

| Path | Purpose |
|---|---|
| `docker-compose.yml` | Single Postgres 17 instance for local development |
| `postgres/init/01-create-databases.sql` | Creates one database + one login role per service. Mounted into `docker-entrypoint-initdb.d` locally and turned into a ConfigMap in Kubernetes by `scripts/deploy.sh` |
| `postgres/reset-local.sql` | Drops and recreates the four databases without deleting the volume |

## Data ownership model

Database-per-service on a shared server. A service can only connect to its own database; `PUBLIC` is revoked.

| Service | Database | Role / password (dev only) | Schema + seed owner |
|---|---|---|---|
| catalog-service | `catalog_db` | `catalog_svc` / `catalog_svc_pw` | Flyway in catalog-service |
| inventory-service | `inventory_db` | `inventory_svc` / `inventory_svc_pw` | Flyway in inventory-service |
| pricing-service | `pricing_db` | `pricing_svc` / `pricing_svc_pw` | Flyway in pricing-service |
| order-service | `order_db` | `order_svc` / `order_svc_pw` | Flyway in order-service |

Superuser: `postgres` / `postgres-admin-pw`.

Bootstrap SQL creates databases and roles only. Tables and seed data live in each service under `src/main/resources/db/migration` (`V1__*` schema, `V2__*` seed) and are applied automatically on service startup.

## Usage

```bash
# start
docker compose -f infra/docker-compose.yml up -d

# connect
docker exec -it oms-postgres psql -U postgres
docker exec -it oms-postgres psql -U catalog_svc -d catalog_db

# reset data (keeps roles)
docker exec -i oms-postgres psql -U postgres < infra/postgres/reset-local.sql

# full wipe (re-runs init SQL on next start)
docker compose -f infra/docker-compose.yml down -v
```

## Gotchas

- Init SQL runs **only when the data directory is empty**. Changing `01-create-databases.sql` requires `down -v` locally, or deleting the PVC in Kubernetes.
- Port 5432 conflict with a host Postgres: set `OMS_PG_PORT` and pass `DB_PORT` to each service.
