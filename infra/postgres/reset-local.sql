-- Manual reset for the LOCAL docker-compose database without deleting the volume.
-- Run as the postgres superuser:
--   docker exec -i oms-postgres psql -U postgres < infra/postgres/reset-local.sql
-- Services re-create schema + seed data via Flyway on next start.
DROP DATABASE IF EXISTS catalog_db   WITH (FORCE);
DROP DATABASE IF EXISTS inventory_db WITH (FORCE);
DROP DATABASE IF EXISTS pricing_db   WITH (FORCE);
DROP DATABASE IF EXISTS order_db     WITH (FORCE);

CREATE DATABASE catalog_db   OWNER catalog_svc;
CREATE DATABASE inventory_db OWNER inventory_svc;
CREATE DATABASE pricing_db   OWNER pricing_svc;
CREATE DATABASE order_db     OWNER order_svc;

REVOKE ALL ON DATABASE catalog_db   FROM PUBLIC;
REVOKE ALL ON DATABASE inventory_db FROM PUBLIC;
REVOKE ALL ON DATABASE pricing_db   FROM PUBLIC;
REVOKE ALL ON DATABASE order_db     FROM PUBLIC;
