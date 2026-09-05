-- V6__add_transform_hint_to_mappings.sql
-- Add transform_hint to adapter_field_mappings for advanced data conversion

ALTER TABLE adapter_field_mappings
ADD COLUMN transform_hint VARCHAR(50);
