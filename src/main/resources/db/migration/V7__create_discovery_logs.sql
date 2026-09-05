-- V7__create_discovery_logs.sql
-- Table for auditing API discovery attempts

CREATE TABLE discovery_logs (
    id UUID PRIMARY KEY,
    state_code VARCHAR(2) REFERENCES state_adapters(state_code),
    endpoint_url TEXT NOT NULL,
    sample_response_hash VARCHAR(255) NOT NULL,
    discovered_fields TEXT,
    attempt_timestamp TIMESTAMP NOT NULL
);
