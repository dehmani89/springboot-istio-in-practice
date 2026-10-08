-- =====================================================================================
-- Runs once, on first start of an EMPTY Postgres data directory
-- (docker-entrypoint-initdb.d). Used by BOTH docker-compose (local) and Kubernetes.
--
-- Database-per-service on a shared server:
--   * each service gets its own database and its own login role
--   * each role owns only its database; PUBLIC is revoked so services cannot read each other
--   * tables + seed data are created by each service's Flyway migrations, not here
--
-- DEV CREDENTIALS ONLY. Kubernetes manifests mirror these in Secrets.
-- =====================================================================================

CREATE ROLE catalog_svc   LOGIN PASSWORD 'catalog_svc_pw';
CREATE ROLE inventory_svc LOGIN PASSWORD 'inventory_svc_pw';
CREATE ROLE pricing_svc   LOGIN PASSWORD 'pricing_svc_pw';
CREATE ROLE order_svc     LOGIN PASSWORD 'order_svc_pw';

CREATE DATABASE catalog_db   OWNER catalog_svc;
CREATE DATABASE inventory_db OWNER inventory_svc;
CREATE DATABASE pricing_db   OWNER pricing_svc;
CREATE DATABASE order_db     OWNER order_svc;

REVOKE ALL ON DATABASE catalog_db   FROM PUBLIC;
REVOKE ALL ON DATABASE inventory_db FROM PUBLIC;
REVOKE ALL ON DATABASE pricing_db   FROM PUBLIC;
REVOKE ALL ON DATABASE order_db     FROM PUBLIC;
