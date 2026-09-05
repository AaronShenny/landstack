-- V4__roles_seed.sql
-- Seed initial roles, resources, and canonical fields

INSERT INTO roles (role_id, description) VALUES
('CITIZEN', 'General public with basic read access'),
('REVENUE_OFFICER', 'State revenue officer with PII and update access'),
('ADMIN', 'Platform administrator'),
('SUPERADMIN', 'Super administrator')
ON CONFLICT DO NOTHING;

INSERT INTO resources (resource_name, description) VALUES
('PARCEL', 'Canonical parcel entity'),
('RECORD_OF_RIGHTS', 'Federated Record of Rights data')
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (id, role_id, resource_name, permlevel, can_create, can_read, can_update, can_delete) VALUES
(gen_random_uuid(), 'CITIZEN', 'RECORD_OF_RIGHTS', 0, false, true, false, false),
(gen_random_uuid(), 'REVENUE_OFFICER', 'RECORD_OF_RIGHTS', 1, false, true, true, false),
(gen_random_uuid(), 'ADMIN', 'RECORD_OF_RIGHTS', 2, false, true, false, false)
ON CONFLICT DO NOTHING;

INSERT INTO canonical_fields (field_id, resource_name, data_type, permlevel) VALUES
('ownerName', 'RECORD_OF_RIGHTS', 'STRING', 1),
('area', 'RECORD_OF_RIGHTS', 'NUMERIC', 0),
('landUseType', 'RECORD_OF_RIGHTS', 'STRING', 0),
('taxStatus', 'RECORD_OF_RIGHTS', 'STRING', 1),
('encumbrances', 'RECORD_OF_RIGHTS', 'STRING', 0)
ON CONFLICT DO NOTHING;
