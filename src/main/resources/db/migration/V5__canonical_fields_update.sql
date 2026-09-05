-- V5__canonical_fields_update.sql
-- Add missing canonical fields for PARCEL resource

INSERT INTO canonical_fields (field_id, resource_name, data_type, permlevel) VALUES
('geometry', 'PARCEL', 'GEOMETRY', 0),
('ulpin', 'PARCEL', 'STRING', 0),
('localParcelId', 'PARCEL', 'STRING', 0)
ON CONFLICT DO NOTHING;
