-- Fix the schema length first
ALTER TABLE districts ALTER COLUMN district_code TYPE VARCHAR(20);
ALTER TABLE sub_districts ALTER COLUMN district_code TYPE VARCHAR(20);
ALTER TABLE sub_districts ALTER COLUMN sub_district_code TYPE VARCHAR(20);
ALTER TABLE villages ALTER COLUMN sub_district_code TYPE VARCHAR(20);
ALTER TABLE villages ALTER COLUMN village_code TYPE VARCHAR(20);
ALTER TABLE parcels ALTER COLUMN district_code TYPE VARCHAR(20);
ALTER TABLE parcels ALTER COLUMN village_code TYPE VARCHAR(20);
-- V11__admin_hierarchy_seed.sql

-- 1. States
INSERT INTO states (state_code, name) VALUES
('KL', 'Kerala'),
('MH', 'Maharashtra'),
('KA', 'Karnataka'),
('DL', 'Delhi'),
('UP', 'Uttar Pradesh')
ON CONFLICT (state_code) DO NOTHING;

-- 2. Districts (Sample for Kerala)
INSERT INTO districts (district_code, state_code, name) VALUES
('KL-TVM', 'KL', 'Thiruvananthapuram'),
('KL-KLM', 'KL', 'Kollam'),
('KL-PTA', 'KL', 'Pathanamthitta'),
('KL-ALP', 'KL', 'Alappuzha'),
('KL-KTM', 'KL', 'Kottayam'),
('KL-IDK', 'KL', 'Idukki'),
('KL-EKM', 'KL', 'Ernakulam'),
('KL-TCR', 'KL', 'Thrissur'),
('KL-PKD', 'KL', 'Palakkad'),
('KL-MLP', 'KL', 'Malappuram'),
('KL-KKD', 'KL', 'Kozhikode'),
('KL-WYD', 'KL', 'Wayanad'),
('KL-KNR', 'KL', 'Kannur'),
('KL-KGD', 'KL', 'Kasaragod')
ON CONFLICT (district_code) DO NOTHING;

-- 3. Sub-Districts (Sample for Thiruvananthapuram)
INSERT INTO sub_districts (sub_district_code, district_code, name) VALUES
('KL-TVM-TVM', 'KL-TVM', 'Thiruvananthapuram'),
('KL-TVM-NYA', 'KL-TVM', 'Neyyattinkara'),
('KL-TVM-NED', 'KL-TVM', 'Nedumangad'),
('KL-TVM-CHK', 'KL-TVM', 'Chirayinkeezhu')
ON CONFLICT (sub_district_code) DO NOTHING;

-- 4. Villages (Sample for Thiruvananthapuram Sub-District)
INSERT INTO villages (village_code, sub_district_code, name) VALUES
('KL-TVM-TVM-001', 'KL-TVM-TVM', 'Pattom'),
('KL-TVM-TVM-002', 'KL-TVM-TVM', 'Kowdiar'),
('KL-TVM-TVM-003', 'KL-TVM-TVM', 'Thycaud'),
('KL-TVM-TVM-004', 'KL-TVM-TVM', 'Vanchiyoor')
ON CONFLICT (village_code) DO NOTHING;

-- Add districtCode and villageCode to canonical_fields
INSERT INTO canonical_fields (field_id, resource_name, data_type, permlevel) VALUES
('districtCode', 'PARCEL', 'STRING', 0),
('villageCode', 'PARCEL', 'STRING', 0)
ON CONFLICT DO NOTHING;

-- Add bbox to villages
ALTER TABLE villages ADD COLUMN bbox geometry(Polygon);

-- Provide sample bbox for the Thiruvananthapuram villages
UPDATE villages SET bbox = ST_MakeEnvelope(76.90, 8.45, 77.05, 8.60, 4326) WHERE village_code LIKE 'KL-TVM-TVM-%';

