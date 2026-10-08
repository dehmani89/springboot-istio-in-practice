INSERT INTO price_list_entry (sku, list_price, currency) VALUES
('DEN-GLV-001',  14.99, 'USD'),
('DEN-GLV-002',  14.99, 'USD'),
('DEN-MSK-001',   9.49, 'USD'),
('DEN-STR-001',  11.25, 'USD'),
('DEN-CMP-001',  42.00, 'USD'),
('DEN-BND-001',  68.50, 'USD'),
('DEN-IMP-001',  18.75, 'USD'),
('DEN-IMP-002',  54.90, 'USD'),
('DEN-PRV-001',  27.80, 'USD'),
('DEN-PRV-002',  39.95, 'USD'),
('DEN-ANS-001',  12.60, 'USD'),
('DEN-BUR-001',  21.40, 'USD'),
('DEN-BUR-002',  24.10, 'USD'),
('DEN-DSP-001',   6.35, 'USD'),
('DEN-DSP-002',  33.00, 'USD'),
('DEN-END-001',  49.00, 'USD');

INSERT INTO volume_discount_tier (min_quantity, discount_percent) VALUES
(10,  5.00),
(25, 10.00),
(50, 15.00);
