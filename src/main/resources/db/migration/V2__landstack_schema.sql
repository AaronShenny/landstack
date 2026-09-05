-- Administrative Hierarchy
CREATE TABLE states (
    state_code VARCHAR(2) PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

CREATE TABLE districts (
    district_code VARCHAR(10) PRIMARY KEY,
    state_code VARCHAR(2) REFERENCES states(state_code),
    name VARCHAR(100) NOT NULL
);

CREATE TABLE sub_districts (
    sub_district_code VARCHAR(10) PRIMARY KEY,
    district_code VARCHAR(10) REFERENCES districts(district_code),
    name VARCHAR(100) NOT NULL
);

CREATE TABLE villages (
    village_code VARCHAR(10) PRIMARY KEY,
    sub_district_code VARCHAR(10) REFERENCES sub_districts(sub_district_code),
    name VARCHAR(100) NOT NULL
);

-- Canonical Parcel (Base Layer)
CREATE TABLE parcels (
    ulpin VARCHAR(14) PRIMARY KEY,
    state_code VARCHAR(2) REFERENCES states(state_code),
    district_code VARCHAR(10) REFERENCES districts(district_code),
    village_code VARCHAR(10) REFERENCES villages(village_code),
    local_parcel_id VARCHAR(100) NOT NULL,
    geom GEOMETRY(Polygon, 4326) NOT NULL,
    area_sqm NUMERIC(10, 2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_parcels_geom ON parcels USING GIST (geom);

-- State Adapter Configuration
CREATE TABLE state_adapters (
    state_code VARCHAR(2) PRIMARY KEY REFERENCES states(state_code),
    base_url VARCHAR(255) NOT NULL,
    auth_type VARCHAR(20),
    auth_credentials TEXT,
    status VARCHAR(20),
    sync_cron_expression VARCHAR(100)
);

CREATE TABLE adapter_endpoints (
    id UUID PRIMARY KEY,
    state_code VARCHAR(2) REFERENCES state_adapters(state_code),
    capability VARCHAR(50) NOT NULL,
    path VARCHAR(255) NOT NULL,
    method VARCHAR(10) DEFAULT 'GET'
);

CREATE TABLE adapter_field_mappings (
    id UUID PRIMARY KEY,
    state_code VARCHAR(2) REFERENCES state_adapters(state_code),
    capability VARCHAR(50) NOT NULL,
    source_field VARCHAR(100) NOT NULL,
    canonical_field VARCHAR(100) NOT NULL
);

-- Security & Permissions (Frappe-Style)
CREATE TABLE users (
    user_id UUID PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    is_superadmin BOOLEAN DEFAULT false
);

CREATE TABLE roles (
    role_id VARCHAR(50) PRIMARY KEY,
    description VARCHAR(255)
);

CREATE TABLE user_roles (
    id UUID PRIMARY KEY,
    user_id UUID REFERENCES users(user_id),
    role_id VARCHAR(50) REFERENCES roles(role_id)
);

CREATE TABLE user_permissions (
    id UUID PRIMARY KEY,
    user_id UUID REFERENCES users(user_id),
    allow_type VARCHAR(50) NOT NULL,
    for_value VARCHAR(50) NOT NULL
);

CREATE TABLE resources (
    resource_name VARCHAR(50) PRIMARY KEY,
    description VARCHAR(255)
);

CREATE TABLE role_permissions (
    id UUID PRIMARY KEY,
    role_id VARCHAR(50) REFERENCES roles(role_id),
    resource_name VARCHAR(50) REFERENCES resources(resource_name),
    permlevel INTEGER DEFAULT 0,
    can_create BOOLEAN DEFAULT false,
    can_read BOOLEAN DEFAULT false,
    can_update BOOLEAN DEFAULT false,
    can_delete BOOLEAN DEFAULT false
);

CREATE TABLE canonical_fields (
    field_id VARCHAR(100) PRIMARY KEY,
    resource_name VARCHAR(50) REFERENCES resources(resource_name),
    data_type VARCHAR(20),
    permlevel INTEGER DEFAULT 0
);

-- Synchronization Jobs
CREATE TABLE sync_jobs (
    job_id UUID PRIMARY KEY,
    state_code VARCHAR(2) REFERENCES state_adapters(state_code),
    started_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    status VARCHAR(20) NOT NULL,
    records_updated INTEGER,
    error_log TEXT
);
