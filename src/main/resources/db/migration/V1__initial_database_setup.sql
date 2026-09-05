-- V1__initial_database_setup.sql

-- Enable PostGIS extension
CREATE EXTENSION IF NOT EXISTS postgis;

-- Simple table to verify Flyway is working
CREATE TABLE IF NOT EXISTS flyway_test_setup (
    id SERIAL PRIMARY KEY,
    setup_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
