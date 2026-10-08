# pricing-service

Price list and quote calculation. One image, two behaviours, which makes it the **canary target** for traffic-management labs.

| | |
|---|---|
| Local port | `8083` (run a second instance on `8093` for v2) |
| Database | `pricing_db` (role `pricing_svc`) |
| Callers | order-service only |
| Calls | — |

## Engine versions

| `PRICING_ENGINE_VERSION` | Behaviour |
|---|---|
| `v1` (default) | List price |
| `v2` | List price less the highest qualifying volume tier: ≥10 → 5%, ≥25 → 10%, ≥50 → 15% |

The serving version is returned in the body (`engineVersion`) and the `x-pricing-engine` response header.

## API

| Method | Path | Notes |
|---|---|---|
| POST | `/api/pricing/quotes` | `{items[{sku, quantity}]}` → lines with `unitPrice, discountPercent, lineTotal`, plus `total` |
| GET | `/api/pricing/{sku}` | List price lookup |

## Chaos switches (resilience labs)

| Env var | Effect |
|---|---|
| `CHAOS_LATENCY_MS` | Fixed delay added to every `/api/**` request |
| `CHAOS_FAILURE_RATE` | 0.0–1.0 probability of returning `503` |

Actuator endpoints are never affected, so probes stay healthy while the API degrades. This mirrors a realistic partial failure.

## Data

| Migration | Content |
|---|---|
| `V1__create_pricing_tables.sql` | `price_list_entry`, `volume_discount_tier` |
| `V2__seed_prices.sql` | Prices for all 16 SKUs, three discount tiers |

## Run locally

```bash
mvn -pl services/pricing-service spring-boot:run                                     # v1 on 8083
SERVER_PORT=8093 PRICING_ENGINE_VERSION=v2 mvn -pl services/pricing-service spring-boot:run   # v2 on 8093
```

## Tests
`DiscountCalculatorTest` covers the pricing math (pure Java, no database): `mvn -pl services/pricing-service test`.

## Role in the Istio labs
- **Lab 06:** weighted (90/10), header-based (`x-pricing-version`) and promoted (100% v2) routes.
- **Lab 07:** chaos-driven timeouts, default retries and outlier detection.
