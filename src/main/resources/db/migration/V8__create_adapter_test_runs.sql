-- V8__create_adapter_test_runs.sql
-- Table for storing adapter validation test results

CREATE TABLE adapter_test_runs (
    id UUID PRIMARY KEY,
    state_code VARCHAR(2) REFERENCES state_adapters(state_code),
    test_timestamp TIMESTAMP NOT NULL,
    is_successful BOOLEAN NOT NULL,
    detailed_results TEXT
);
