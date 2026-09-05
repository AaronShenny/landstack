# Canonical Field Registry API

## Overview
The Canonical Field Registry API manages the global dictionary of fields for resources (like PARCEL) in the LandStack system. Each field is assigned a permlevel (permission level), dictating the minimum clearance required for a user or role to view that field. The JSON redaction filter uses this registry to dynamically strip fields from HTTP responses if the authenticated user's permission level is too low.

## API Endpoints

### 1. List Canonical Fields
**Endpoint:** GET /api/v1/admin/canonical-fields

**Description:** Retrieves a list of all canonical fields. Supports optional filtering by resource.

**Query Parameters:**
- esource (optional): Filter fields by the associated resource name (e.g., PARCEL).

**Response:**
`json
[
  {
    "fieldId": "ownerName",
    "resource": {
      "resourceName": "PARCEL",
      "description": "Land Parcel Data"
    },
    "dataType": "STRING",
    "permlevel": 50
  },
  ...
]
`

### 2. Register New Canonical Field
**Endpoint:** POST /api/v1/admin/canonical-fields

**Description:** Registers a new field for a resource and sets its base permission level.

**Request Body:**
`json
{
  "fieldId": "geometry",
  "resourceName": "PARCEL",
  "dataType": "GEOMETRY",
  "permlevel": 0
}
`

### 3. Update Field Permission Level
**Endpoint:** PATCH /api/v1/admin/canonical-fields/{fieldId}/permlevel

**Description:** Updates the permlevel for a specific field. Changes take effect immediately and are applied via the redaction filter on subsequent requests.

**Request Body:**
`json
{
  "permlevel": 100
}
`

## Seed Data
Initial field mappings are seeded via V4__roles_seed.sql and V5__canonical_fields_update.sql:
- ulpin, geometry, localParcelId, rea -> permlevel = 0 (Public)
- landUseType, 	axStatus -> permlevel = 50 (Revenue Officer / Surveyor)
- ownerName, encumbrances -> permlevel = 100 (Sub-Registrar / Higher)
