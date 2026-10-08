-- Seed stock. DEN-IMP-002 is out of stock and DEN-BUR-002 is low, to exercise 409 paths.
INSERT INTO stock_item (sku, warehouse_code, on_hand, reserved, reorder_level) VALUES
('DEN-GLV-001', 'OH-DAY-01', 1200, 0, 200),
('DEN-GLV-002', 'OH-DAY-01',  950, 0, 200),
('DEN-MSK-001', 'OH-DAY-01',  600, 0, 100),
('DEN-STR-001', 'OH-DAY-01',  400, 0,  80),
('DEN-CMP-001', 'OH-DAY-01',  150, 0,  30),
('DEN-BND-001', 'OH-DAY-01',   90, 0,  20),
('DEN-IMP-001', 'OH-DAY-01',  210, 0,  40),
('DEN-IMP-002', 'OH-DAY-01',    0, 0,  25),
('DEN-PRV-001', 'OH-DAY-01',  320, 0,  60),
('DEN-PRV-002', 'OH-DAY-01',  180, 0,  40),
('DEN-ANS-001', 'OH-DAY-01',  140, 0,  30),
('DEN-BUR-001', 'OH-DAY-01',   75, 0,  15),
('DEN-BUR-002', 'OH-DAY-01',    3, 0,  15),
('DEN-DSP-001', 'OH-DAY-01',  500, 0, 100),
('DEN-DSP-002', 'OH-DAY-01',  260, 0,  50),
('DEN-END-001', 'OH-DAY-01',   12, 0,   0);
