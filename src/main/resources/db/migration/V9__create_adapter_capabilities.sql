-- V9__create_adapter_capabilities.sql
-- Table for explicit state capability declarations

CREATE TABLE adapter_capabilities (
    id UUID PRIMARY KEY,
    state_code VARCHAR(2) REFERENCES state_adapters(state_code),
    capability VARCHAR(50) NOT NULL,
    is_enabled BOOLEAN NOT NULL DEFAULT false,
    UNIQUE(state_code, capability)
);

INSERT INTO adapter_capabilities (id, state_code, capability, is_enabled) VALUES
(gen_random_uuid(), 'KL', 'PARCEL_GEOMETRY', true),
(gen_random_uuid(), 'KL', 'RECORD_OF_RIGHTS', true),
(gen_random_uuid(), 'KL', 'REGISTRATION', false),
(gen_random_uuid(), 'KL', 'PLANNING', false)
ON CONFLICT DO NOTHING;
