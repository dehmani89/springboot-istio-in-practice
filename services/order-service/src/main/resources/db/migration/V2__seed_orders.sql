-- Historical orders so GET endpoints return data immediately.
-- They represent already-shipped orders and are intentionally not reflected in inventory reservations.

INSERT INTO customer_order (order_number, customer_id, status, currency, total_amount, pricing_engine_version, created_at) VALUES
('7b1e9c2a-0d1f-4c55-9a51-2f0c6a1d0001', 'PRACTICE-1001', 'CONFIRMED', 'USD', 191.90, 'v1', now() - INTERVAL '14 days'),
('7b1e9c2a-0d1f-4c55-9a51-2f0c6a1d0002', 'PRACTICE-1002', 'CONFIRMED', 'USD', 626.88, 'v2', now() - INTERVAL '3 days');

-- Order 1, engine v1 (list price only):
--   10 x DEN-GLV-001 @ 14.99 = 149.90
--    1 x DEN-CMP-001 @ 42.00 =  42.00   -> 191.90
INSERT INTO order_line (order_id, sku, product_name, quantity, unit_price, discount_percent, line_total)
SELECT o.id, v.sku, v.product_name, v.quantity, v.unit_price, v.discount_percent, v.line_total
FROM customer_order o
JOIN (VALUES
    ('DEN-GLV-001', 'Nitrile Exam Gloves, Medium',         10, 14.99, 0.00, 149.90),
    ('DEN-CMP-001', 'Nano-Hybrid Universal Composite A2',   1, 42.00, 0.00,  42.00)
) AS v(sku, product_name, quantity, unit_price, discount_percent, line_total) ON TRUE
WHERE o.order_number = '7b1e9c2a-0d1f-4c55-9a51-2f0c6a1d0001';

-- Order 2, engine v2 (volume tiers):
--   30 x DEN-DSP-001 @  6.35, 10% = 171.45
--   12 x DEN-PRV-002 @ 39.95,  5% = 455.43   -> 626.88
INSERT INTO order_line (order_id, sku, product_name, quantity, unit_price, discount_percent, line_total)
SELECT o.id, v.sku, v.product_name, v.quantity, v.unit_price, v.discount_percent, v.line_total
FROM customer_order o
JOIN (VALUES
    ('DEN-DSP-001', 'Saliva Ejectors Clear/Blue',   30,  6.35, 10.00, 171.45),
    ('DEN-PRV-002', '5% Sodium Fluoride Varnish',   12, 39.95,  5.00, 455.43)
) AS v(sku, product_name, quantity, unit_price, discount_percent, line_total) ON TRUE
WHERE o.order_number = '7b1e9c2a-0d1f-4c55-9a51-2f0c6a1d0002';
