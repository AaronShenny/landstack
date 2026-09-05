-- V3__seed_data.sql
-- Seeds the database with real configuration for the mock Flask server and sample parcels

INSERT INTO states (state_code, name) VALUES ('KL', 'Kerala') ON CONFLICT DO NOTHING;

INSERT INTO state_adapters (state_code, base_url, auth_type, status, sync_cron_expression) 
VALUES ('KL', 'http://localhost:5000/v1', 'NONE', 'ACTIVE', '0 0 0 * * *') ON CONFLICT DO NOTHING;

INSERT INTO adapter_endpoints (id, state_code, capability, path, method)
VALUES 
(gen_random_uuid(), 'KL', 'RECORD_OF_RIGHTS', '/land/{ulpin}/ror', 'GET'),
(gen_random_uuid(), 'KL', 'PARCEL_GEOMETRY', '/spatial/parcels?bbox={bbox}', 'GET')
ON CONFLICT DO NOTHING;

INSERT INTO adapter_field_mappings (id, state_code, capability, canonical_field, source_field)
VALUES 
(gen_random_uuid(), 'KL', 'RECORD_OF_RIGHTS', 'ownerName', '$.owner_name'),
(gen_random_uuid(), 'KL', 'RECORD_OF_RIGHTS', 'area', '$.land_area_sqm'),
(gen_random_uuid(), 'KL', 'RECORD_OF_RIGHTS', 'landUseType', '$.usage_category'),
(gen_random_uuid(), 'KL', 'RECORD_OF_RIGHTS', 'taxStatus', '$.tax_status'),
(gen_random_uuid(), 'KL', 'RECORD_OF_RIGHTS', 'encumbrances', '$.encumbrances')
ON CONFLICT DO NOTHING;

-- Insert 5 mock parcels around Kochi (WGS84 76.2673, 9.9312) using PostGIS 
INSERT INTO parcels (ulpin, state_code, local_parcel_id, geom, created_at, updated_at) VALUES 
('KL-001', 'KL', 'LOCAL-101', ST_SetSRID(ST_GeomFromText('POLYGON((76.2673 9.9312, 76.2674 9.9312, 76.2674 9.9313, 76.2673 9.9313, 76.2673 9.9312))'), 4326), NOW(), NOW()),
('KL-002', 'KL', 'LOCAL-102', ST_SetSRID(ST_GeomFromText('POLYGON((76.2675 9.9312, 76.2676 9.9312, 76.2676 9.9313, 76.2675 9.9313, 76.2675 9.9312))'), 4326), NOW(), NOW()),
('KL-003', 'KL', 'LOCAL-103', ST_SetSRID(ST_GeomFromText('POLYGON((76.2677 9.9312, 76.2678 9.9312, 76.2678 9.9313, 76.2677 9.9313, 76.2677 9.9312))'), 4326), NOW(), NOW()),
('KL-004', 'KL', 'LOCAL-104', ST_SetSRID(ST_GeomFromText('POLYGON((76.2673 9.9314, 76.2674 9.9314, 76.2674 9.9315, 76.2673 9.9315, 76.2673 9.9314))'), 4326), NOW(), NOW()),
('KL-005', 'KL', 'LOCAL-105', ST_SetSRID(ST_GeomFromText('POLYGON((76.2675 9.9314, 76.2676 9.9314, 76.2676 9.9315, 76.2675 9.9315, 76.2675 9.9314))'), 4326), NOW(), NOW())
ON CONFLICT DO NOTHING;
