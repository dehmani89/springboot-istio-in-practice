-- Seed data. SKUs are shared across catalog, pricing and inventory seeds — keep them in sync.
-- DEN-END-001 is intentionally inactive to exercise order validation.
INSERT INTO product (sku, name, category, manufacturer, description, unit_of_measure, active) VALUES
('DEN-GLV-001', 'Nitrile Exam Gloves, Medium',              'Infection Control', 'ShieldPro Medical',  'Powder-free, textured fingertips, 200 per box',            'Box of 200', TRUE),
('DEN-GLV-002', 'Nitrile Exam Gloves, Large',               'Infection Control', 'ShieldPro Medical',  'Powder-free, textured fingertips, 200 per box',            'Box of 200', TRUE),
('DEN-MSK-001', 'ASTM Level 3 Procedure Masks',             'Infection Control', 'ShieldPro Medical',  'Ear-loop, fluid resistant, 50 per box',                    'Box of 50',  TRUE),
('DEN-STR-001', 'Self-Sealing Sterilization Pouches 3.5x10','Sterilization',     'SteriSafe Labs',     'Internal and external process indicators',                 'Box of 200', TRUE),
('DEN-CMP-001', 'Nano-Hybrid Universal Composite A2',       'Restorative',       'BrightSmile Dental', '4g syringe, universal shade A2',                           'Syringe',    TRUE),
('DEN-BND-001', 'Universal Bonding Agent',                  'Restorative',       'BrightSmile Dental', 'Single-component, 5ml bottle',                             'Bottle',     TRUE),
('DEN-IMP-001', 'Alginate Impression Material',             'Impression',        'OralForm Inc.',      'Fast set, mint flavored, 1lb bag',                         'Bag',        TRUE),
('DEN-IMP-002', 'VPS Impression Material Heavy Body',       'Impression',        'OralForm Inc.',      '2 x 50ml cartridges',                                      'Pack of 2',  TRUE),
('DEN-PRV-001', 'Prophy Paste Medium Grit, Mint',           'Preventive',        'FreshCoat',          'Single-use cups, 200 per box',                             'Box of 200', TRUE),
('DEN-PRV-002', '5% Sodium Fluoride Varnish',               'Preventive',        'FreshCoat',          '0.4ml unit doses, 50 per box',                             'Box of 50',  TRUE),
('DEN-ANS-001', 'Topical Anesthetic Gel 20%',               'Anesthetics',       'NumbWell Pharma',    'Cherry flavored, 1oz jar',                                 'Jar',        TRUE),
('DEN-BUR-001', 'Carbide Burs FG #330',                     'Rotary',            'PrecisionCut',       'Friction grip pear-shaped carbide, 10 per pack',           'Pack of 10', TRUE),
('DEN-BUR-002', 'Diamond Burs FG Round Medium',             'Rotary',            'PrecisionCut',       'Friction grip round diamond, medium grit, 10 per pack',    'Pack of 10', TRUE),
('DEN-DSP-001', 'Saliva Ejectors Clear/Blue',               'Disposables',       'ChairSide Supply',   'Flexible, 100 per bag',                                    'Bag of 100', TRUE),
('DEN-DSP-002', 'Patient Bibs 2-Ply, Blue',                 'Disposables',       'ChairSide Supply',   '13x18 in, 500 per case',                                   'Case of 500',TRUE),
('DEN-END-001', 'NiTi Rotary Files 25mm Assorted',          'Endodontics',       'RootLine',           'Discontinued — retained for order-validation testing',     'Pack of 6',  FALSE);
