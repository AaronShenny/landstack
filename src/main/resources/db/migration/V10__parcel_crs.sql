-- V10__parcel_crs.sql
-- Add original coordinate system and native geometry retention

ALTER TABLE parcels ADD COLUMN source_crs VARCHAR(50);
ALTER TABLE parcels ADD COLUMN source_geom geometry(Polygon);
