# LandStack — Prototype → Production Gap Analysis & Roadmap

> **Project:** SIH26014 — Integrated GIS-based Digital Public Infrastructure for Land Governance  
> **Assessed:** 2026-09-05  
> **Codebase:** `e:/aaronshenny/PROJECTS/SIH/hack02`

---

## Executive Summary

The prototype demonstrates the **core architectural ideas** (federated state adapters, dynamic field mapping, JWT-based auth, PostGIS parcel storage, per-level data redaction) with **one mock state (Kerala)** and **five seed parcels**. The current implementation is roughly **~10–15% of a production system**. The remaining ~85–90% spans real GIS capabilities, a functioning admin UI, full state adapter lifecycle management, a complete security model, API gateway features, spatial analytics, satellite/watershed integration, multi-state onboarding, observability, and production deployment infrastructure.

---

## What Is Already Built (Prototype State)

| Layer | Implemented | Status |
|---|---|---|
| **Database schema** | `states`, `districts`, `sub_districts`, `villages`, `parcels`, `state_adapters`, `adapter_endpoints`, `adapter_field_mappings`, `users`, `roles`, `user_roles`, `user_permissions`, `resources`, `role_permissions`, `canonical_fields`, `sync_jobs` | ✅ Schema exists via Flyway migrations |
| **Seed data** | Kerala state, 1 active adapter, 2 endpoints (RoR, parcel geometry), 5 field mappings, 5 mock parcels near Kochi | ✅ V3 seed |
| **Backend — Parcel API** | `GET /api/v1/parcels?bbox=` (PostGIS spatial query), `GET /api/v1/parcels/{ulpin}/ror` (federated RoR fetch) | ✅ Working |
| **Backend — Admin API** | `GET /admin/adapters`, `GET /admin/users`, stub `POST /admin/users`, stub POST permissions, stub `GET /admin/roles` | ⚠️ Mostly stubs |
| **Interoperability** | `StateApiClient` (HTTP calls), `DynamicFieldMapper` (basic JSONPath field map), `CrsNormalizationService` (STUBBED — returns geometry as-is) | ⚠️ Prototype-quality |
| **Sync Scheduler** | Reads `sync_cron_expression` from DB, schedules per-adapter sync jobs, fetches raw data — **geometry parse/save is a TODO** | ⚠️ Incomplete |
| **Security** | JWT auth filter, BCrypt, Spring Security, `@PreAuthorize` role checks, `UserPermissionsAspect` (Hibernate `stateFilter`), `PermlevelRedactionFilter` (rewrites JSON but all 3 TODOs are unfilled) | ⚠️ Framework wired, logic stubs |
| **Frontend** | Login page (hardcoded mock user), Map Viewer (Leaflet, shows 5 parcels, click for RoR panel), Admin Dashboard (lists adapter + user table) | ⚠️ Prototype UI |
| **Mock State Node** | Flask server serving mock Kerala RoR records for 5 ULPINs | ✅ For demo only |
| **Infrastructure** | `docker-compose.yml` (Spring Boot + PostgreSQL/PostGIS) | ✅ Dev only |

---

## Phase 1 — Backend Foundation Completion
> *Complete the security, permission and admin CRUD layer that is currently wired but unimplemented. This unlocks all further real functionality.*

### Milestone 1.1 — Complete Auth & User Management APIs

**Problem:** `POST /api/v1/admin/users` returns `200 OK` immediately with no logic. Password hashing, user creation, validation and duplicate checks are absent.

**Tasks:**
- Implement `UserService` with `createUser(username, email, rawPassword)` — hash with BCrypt, persist, return created user DTO
- Implement `GET /api/v1/auth/login` endpoint (`AuthController`) that validates credentials and issues a signed JWT
- Implement `GET /api/v1/auth/me` returning the currently authenticated user's profile and roles
- Add `POST /api/v1/admin/users/{userId}/roles` to assign/remove roles (wire to `UserRole` table)
- Implement `POST /api/v1/admin/users/{userId}/permissions` (currently returns `200` with no body) — persist `UserPermission` row
- Add `DELETE` endpoints for users, roles, permissions
- Add validation (`@Valid`, `@NotBlank`, custom exception messages) to all request bodies
- Replace frontend hardcoded mock login with real JWT-issuing endpoint call

---

### Milestone 1.2 — Complete Role & Permission System

**Problem:** The `role_permissions` and `canonical_fields` tables exist but are never written to through any API and are not read by `PermlevelRedactionFilter`.

**Tasks:**
- Implement `POST /api/v1/admin/roles` — create a new role, persist to `roles`
- Implement `POST /api/v1/admin/roles/{roleId}/permissions` — grant resource + permlevel permissions
- Implement `GET /api/v1/admin/resources` — list all canonical resources
- Fill in the 3 TODOs in `PermlevelRedactionFilter`:
  1. Extract authenticated user's max permlevel from `SecurityContext` → `UserDetailsService`
  2. Load `CanonicalField` definitions from DB (cache with `@Cacheable`)
  3. Traverse response `JsonNode`, remove fields where `canonical_field.permlevel > user_max_permlevel`
- Seed initial roles (`CITIZEN`, `REVENUE_OFFICER`, `ADMIN`, `SUPERADMIN`) and resource definitions in `V4__roles_seed.sql`
- Write unit tests for redaction logic (assert PII fields are absent at permlevel 0)

---

### Milestone 1.3 — Complete State-Filter (Hibernate Row-Level Security)

**Problem:** `UserPermissionsAspect` enables a Hibernate `stateFilter` but the filter is never defined on any entity (no `@FilterDef` / `@Filter` annotations exist on `Parcel` or related entities).

**Tasks:**
- Add `@FilterDef(name = "stateFilter", parameters = @ParamDef(name = "stateCode", type = String.class))` to `Parcel` entity
- Add `@Filter(name = "stateFilter", condition = "state_code = :stateCode")` on `Parcel`
- Extend filter to `SyncJob`, `AdapterEndpoint`, `AdapterFieldMapping` where appropriate
- Test that a non-superadmin user scoped to `KL` cannot read parcels from other state codes
- Add integration tests covering access-control boundary

---

### Milestone 1.4 — Canonical Field Registry API

**Problem:** `canonical_fields` table and `resources` table exist but have no seed data and no API to manage them.

**Tasks:**
- Seed initial canonical fields (e.g., `ownerName`, `area`, `landUseType`, `encumbrances`, `taxStatus`, `geometry`, `ulpin`, `localParcelId`) with permlevels in `V4` migration
- Implement `GET /api/v1/admin/canonical-fields` (list all, filterable by resource)
- Implement `POST /api/v1/admin/canonical-fields` (register new field with permlevel)
- Implement `PATCH /api/v1/admin/canonical-fields/{fieldId}/permlevel` (update permlevel)
- Document canonical field registry in `docs/backend/canonical-fields.md`

---

## Phase 2 — State Adapter Lifecycle & Configuration Engine
> *Turn the adapter registry from seed-data-only into a fully UI-driven, API-backed configuration system. This is the core architectural piece.*

### Milestone 2.1 — State Registration API (Full CRUD)

**Problem:** State adapters exist in DB only via seed SQL. There are no `POST`, `PUT`, `DELETE` endpoints to register or update state configurations at runtime.

**Tasks:**
- `POST /api/v1/admin/adapters` — create state adapter (validate state code against ISO 3166-2:IN, set status to `DRAFT`)
- `PUT /api/v1/admin/adapters/{stateCode}` — update base URL, auth type, credentials, cron expression
- `DELETE /api/v1/admin/adapters/{stateCode}` — soft delete / deactivate
- `PATCH /api/v1/admin/adapters/{stateCode}/status` — lifecycle transitions (`DRAFT → CONFIGURING → MAPPING → TESTING → VALIDATED → ACTIVE → SUSPENDED`)
- Validate status transitions (e.g., cannot jump from `DRAFT` to `ACTIVE` without passing through `TESTING`)
- Add `StateAdapterService` layer (avoid direct repo access in controller)

---

### Milestone 2.2 — Endpoint Configuration API

**Problem:** Adapter endpoints (`adapter_endpoints` table) are seed-only. There is no runtime API to add/update/remove endpoints per adapter.

**Tasks:**
- `GET /api/v1/admin/adapters/{stateCode}/endpoints` — list all endpoints for a state
- `POST /api/v1/admin/adapters/{stateCode}/endpoints` — add new capability endpoint (path, method, capability enum)
- `PUT /api/v1/admin/adapters/{stateCode}/endpoints/{id}` — update endpoint path/method
- `DELETE /api/v1/admin/adapters/{stateCode}/endpoints/{id}` — remove endpoint
- Add `capability` as a proper enum/registry (`PARCEL_GEOMETRY`, `RECORD_OF_RIGHTS`, `REGISTRATION`, `PLANNING`, `RESTRICTIONS`, `BUILDING_PERMISSIONS`, `TAX`)
- Validate that path templates use consistent URI-variable notation

---

### Milestone 2.3 — Field Mapping Configuration API

**Problem:** `adapter_field_mappings` table is seed-only. Dynamic field mapping exists in `DynamicFieldMapper` but only handles the simplest JSONPath (`$.field`). Complex paths, nested objects, arrays are unsupported.

**Tasks:**
- `GET /api/v1/admin/adapters/{stateCode}/mappings` — list all mappings
- `POST /api/v1/admin/adapters/{stateCode}/mappings` — create mapping (source field, canonical field, capability, optional transform hint)
- `PUT /api/v1/admin/adapters/{stateCode}/mappings/{id}` — update mapping
- `DELETE /api/v1/admin/adapters/{stateCode}/mappings/{id}` — remove mapping
- Upgrade `DynamicFieldMapper` to support proper JSONPath library (e.g., Jayway `jsonpath`) for nested, array, and conditional field access
- Support unit conversion hints (e.g., `sqft_to_sqm`, `acres_to_sqm`) per mapping
- Support value transformation functions (e.g., date format normalization)

---

### Milestone 2.4 — API Discovery & Assisted Mapping (Admin Feature)

**Problem:** The `STATE_ADAPTER.md` doc describes an assisted mapping workflow where the admin provides an endpoint URL and the system suggests field mappings — this does not exist at all.

**Tasks:**
- `POST /api/v1/admin/adapters/{stateCode}/discover` — given a configured endpoint, call it, capture sample response, extract all top-level and nested field names
- Return discovered fields alongside existing canonical fields for comparison
- Implement suggestion logic: fuzzy-match state field names against canonical field names (e.g., `survey_no` → `local_parcel_id`, `area_hect` → `area`)
- Return ranked suggestions with confidence scores
- Admin reviews and accepts/rejects suggestions → persists confirmed mappings to DB
- Log discovery attempts with timestamp and sample response hash for auditing

---

### Milestone 2.5 — Adapter Testing & Validation Workflow

**Problem:** There is no way to test a configured adapter before activating it. Activating a broken adapter would silently fail.

**Tasks:**
- `POST /api/v1/admin/adapters/{stateCode}/test` — run connection test against each configured endpoint
- For each endpoint: send a probe request (with mock params), validate HTTP 200, validate response is parseable JSON
- Apply field mappings to sample response and return a "preview canonical output" to the admin
- Return pass/fail per endpoint, with error details on failure
- Block status transition to `VALIDATED` unless all required endpoint tests pass
- Store test results in a `adapter_test_runs` table (add migration)

---

### Milestone 2.6 — Capability Model

**Problem:** The architecture requires each adapter to declare which capabilities it supports (e.g., does Kerala expose planning data?). Currently there is no capability flag per adapter.

**Tasks:**
- Add `adapter_capabilities` table: `(state_code, capability, enabled BOOLEAN)`
- Seed Kerala capabilities (parcel geometry: true, RoR: true, registration: false, planning: false)
- Implement `CapabilityCheck` service: `boolean supportsCapability(stateCode, capability)`
- Before executing any adapter operation in `ParcelService` or future services, check capability first; return structured `NOT_SUPPORTED` response if missing
- Expose `GET /api/v1/adapters/{stateCode}/capabilities` as a public read endpoint

---

## Phase 3 — Real GIS & Spatial Data Pipeline
> *Replace the mocked parcel geometry with a real spatial data pipeline including actual CRS transformation.*

### Milestone 3.1 — Real CRS Transformation

**Problem:** `CrsNormalizationService.transformToWgs84()` is a documented stub that returns geometry as-is with a warning. This is not acceptable for production.

**Tasks:**
- Add GeoTools dependencies to `pom.xml` (resolve OSGeo Maven repository issue noted in source code comment)
- Implement proper `CRS.decode(sourceCrsEpsgCode)` + `MathTransform` + `JTS.transform()` pipeline
- Test transformations: `EPSG:32643` (UTM Zone 43N, used in parts of India) → `EPSG:4326`; `EPSG:32644` → `EPSG:4326`; validate round-trip accuracy
- Add `source_crs` column to `parcels` table (migration `V5__parcel_crs.sql`) to preserve original CRS
- Store both `source_geom` (in native CRS) and `geom` (WGS84) — or document the chosen approach
- Handle unknown CRS gracefully (log + mark parcel as `CRS_UNKNOWN`, do not fail the sync)
- Write unit tests with real coordinate transformation assertions

---

### Milestone 3.2 — Complete Parcel Sync Pipeline

**Problem:** The `DynamicSyncScheduler.executeSyncJob()` contains a TODO comment: `"Geometry parsing and saving implemented in next iteration"` — the geometry is fetched but never persisted.

**Tasks:**
- Parse GeoJSON `FeatureCollection` from state geometry endpoint response
- For each feature: extract geometry, transform CRS if needed, extract `local_parcel_id` and admin context via field mappings
- Generate ULPIN from state code + administrative context + local parcel ID (define ULPIN generation algorithm consistent with DILRMP standards)
- Upsert parcel into `parcels` table using `INSERT ... ON CONFLICT (ulpin) DO UPDATE`
- Link district/village foreign keys (resolve or create administrative reference on first sync)
- Record sync job start/end/status in `sync_jobs` table
- Handle partial failures: continue processing remaining features if one fails, record error per feature
- Add `POST /api/v1/admin/adapters/{stateCode}/sync/trigger` endpoint (admin-triggered manual sync)
- Expose `GET /api/v1/admin/sync-jobs` with filtering by state code and status

---

### Milestone 3.3 — Administrative Hierarchy Data Ingestion

**Problem:** `districts`, `sub_districts`, `villages` tables are empty (no seed data other than the 5 parcels referencing unspecified district/village codes). The API cannot support India → State → District → Village navigation without this data.

**Tasks:**
- Integrate the [Census 2011 administrative hierarchy dataset](https://censusindia.gov.in/) or Bhuvan/LGDS administrative codes
- Write a one-time data loader script (or Flyway migration) to populate `states`, `districts`, `sub_districts`, `villages` with at minimum all states and districts, and all sub-districts/villages for Kerala (as the demo state)
- Implement `GET /api/v1/administrative-units` with `?parent_code=` query parameter for tree navigation
- Implement `GET /api/v1/states`, `GET /api/v1/states/{code}/districts`, `GET /api/v1/districts/{code}/subdistricts`, `GET /api/v1/subdistricts/{code}/villages`
- Expose village-level bounding boxes or centroid points to enable map navigation/zoom

---

### Milestone 3.4 — Administrative Boundary GIS Layers

**Problem:** The architecture requires state/district/subdistrict boundary polygons for map rendering and zoom-level-dependent display. None exist in the DB.

**Tasks:**
- Source administrative boundary shapefiles (Survey of India / Bhuvan administrative boundaries)
- Add PostGIS geometry column to `states`, `districts`, `sub_districts`, `villages` tables (migration)
- Write a shapefile-to-PostGIS loader (can use `shp2pgsql` or a Spring Batch job)
- Add spatial index on each boundary geometry column
- Implement `GET /api/v1/states/{code}/boundary` → GeoJSON of state boundary polygon
- Implement `GET /api/v1/districts/{code}/boundary` → GeoJSON
- Implement `GET /api/v1/subdistricts/{code}/boundary` → GeoJSON
- Implement `GET /api/v1/villages/{code}/boundary` → GeoJSON
- Ensure these endpoints are open to public (no auth required for boundary geometry)

---

### Milestone 3.5 — Map Tile Service

**Problem:** The frontend currently uses Leaflet with an OpenStreetMap tile background and renders parcels as GeoJSON polygons. For a production national platform this will not scale (cannot render thousands of parcels as GeoJSON).

**Tasks:**
- Evaluate and integrate a vector tile server (e.g., `pg_tileserv` or `martin` for PostGIS → MVT, or GeoServer WMS/WFS)
- Expose `GET /api/v1/tiles/{z}/{x}/{y}.mvt` endpoint (serving parcels as Mapbox Vector Tiles)
- Implement zoom-level–dependent layer switching: state boundaries at low zoom, district at medium, parcels at high zoom
- Configure tile caching (Redis or filesystem) for frequently requested tiles
- Update frontend to consume MVT tiles (switch from `L.geoJSON` to `L.vectorGrid` or MapLibre GL JS)

---

### Milestone 3.6 — Full Parcel Detail API

**Problem:** `ParcelController` only exposes bbox search and RoR. The architecture specifies separate endpoints for geometry, rights, registration, planning, restrictions, building permissions, and layers.

**Tasks:**
- `GET /api/v1/parcels/{ulpin}` — full canonical parcel details (non-geometry attributes)
- `GET /api/v1/parcels/{ulpin}/geometry` — parcel GeoJSON geometry
- `GET /api/v1/parcels/{ulpin}/rights` — RoR / ownership data (currently `/ror`)
- `GET /api/v1/parcels/{ulpin}/registration` — registration records (federated via adapter)
- `GET /api/v1/parcels/{ulpin}/planning` — planning/zoning data (federated via adapter)
- `GET /api/v1/parcels/{ulpin}/restrictions` — environmental/legal restrictions
- `GET /api/v1/parcels/{ulpin}/layers` — aggregated view of all available thematic layers
- For each endpoint: check adapter capability, route to correct adapter endpoint, apply field mappings, return canonical response
- Implement proper HTTP error codes (`404` parcel not found, `503` state adapter unavailable, `204` capability not supported)

---

## Phase 4 — Frontend Rebuild (Production UI)
> *Replace the prototype 3-page UI with a complete, production-quality GIS web application.*

### Milestone 4.1 — Map-First India Navigation

**Problem:** The current MapViewer hard-codes coordinates to Kochi, Kerala. It cannot navigate India → State → District → Village hierarchy as required by the architecture.

**Tasks:**
- Replace hardcoded Kochi center with full India view on load (center: `20.5937, 78.9629`, zoom 5)
- Implement zoom-level–dependent layer rendering:
  - Zoom 4–6: India outline + state boundaries
  - Zoom 7–9: District boundaries
  - Zoom 10–11: Sub-district / taluk boundaries
  - Zoom 12–13: Village boundaries
  - Zoom 14+: Cadastral parcel boundaries (fetched from tile server or bbox API)
- Add sidebar panel: India → State drill-down, click state → zoom to state, show districts, click district → zoom, etc.
- Add search bar: search by state name, district, village, or ULPIN
- Add "My Location" button (if GPS permission granted)

---

### Milestone 4.2 — Single Parcel View (Unified Panel)

**Problem:** Current click-on-parcel panel only shows RoR data from the mock Flask server. Needs to aggregate all available data layers from all adapters.

**Tasks:**
- Redesign parcel info panel with tabbed interface: Overview | Rights | Registration | Planning | Restrictions | Layers
- Each tab makes a separate API call to the corresponding `/api/v1/parcels/{ulpin}/{tab}` endpoint
- Show capability status: if a tab's data is not available (adapter capability missing), show informative "Not available from this state" message
- Add provenance footer per tab: "Source: Kerala State Land Records System | Last synced: 2026-09-01"
- Add loading/error states per tab
- Add parcel metadata: ULPIN, area (in local units + sq.m), local parcel ID, administrative context

---

### Milestone 4.3 — Complete Admin Dashboard

**Problem:** Admin Dashboard is a read-only table showing adapters and users. It lacks all CRUD operations, mapping configuration, and the adapter lifecycle workflow.

**Tasks:**
- **State Management section:** Table of all states with adapter status badges; button to register a new state (wizard); edit/delete per row
- **State Adapter Wizard** (multi-step form):
  - Step 1: State metadata (name, code, integration type checkboxes)
  - Step 2: Endpoints configuration (add/remove rows for each capability)
  - Step 3: Field mappings (table with source → canonical mapping, + "Discover fields" button)
  - Step 4: Test & Validate (live test results displayed)
  - Step 5: Activate
- **User Management section:** Create user form (username, email, password, roles); assign permissions (state-level and resource-level)
- **Sync Jobs section:** Table of sync job history with state, start time, end time, status, record count; "Trigger Sync" button per state
- **Canonical Fields section:** List canonical fields with permlevels; edit permlevel
- **Roles & Permissions matrix:** Grid of role × resource × permlevel (similar to Frappe's permission manager)

---

### Milestone 4.4 — Thematic Layers UI

**Problem:** The architecture requires thematic GIS layers (land use, watershed, soil, vegetation, restrictions). No UI for toggling/viewing these exists.

**Tasks:**
- Add layer control panel to MapViewer (collapsible sidebar)
- Layer groups: Administrative | Cadastral | Land Use | Environmental | Watershed | Remote Sensing
- Implement layer toggle (show/hide each layer)
- When a parcel is selected, show which thematic layers intersect it (from spatial query)
- Add legend panel for active layers
- Support WMS layer integration for external government datasets (e.g., Bhuvan WMS)

---

### Milestone 4.5 — Citizen Services UI

**Problem:** The architecture calls for citizen-facing views. Currently there are no citizen-specific screens.

**Tasks:**
- Add public-facing `/map` view (no login required for viewing public parcel data)
- Add parcel search by survey number / ULPIN / owner name (restricted results per permlevel)
- Add basic land record certificate download mock flow
- Implement rate limiting on public endpoints

---

## Phase 5 — Multi-State Integration
> *Go from 1 mock state to at least 2 real/realistic state integrations.*

### Milestone 5.1 — Real Kerala Integration Research & Adapter

**Problem:** The mock Flask server is not a real state API. A production adapter must connect to actual Kerala state systems (e-Rekha / KLIMS / WebLand).

**Tasks:**
- Research Kerala's publicly exposed land API endpoints (WebLand, e-Rekha) — document actual endpoint schema
- Update Kerala adapter configuration in DB to point to real endpoints (or best-available staging API)
- Update field mappings to match actual Kerala API response structure
- Test and validate the real Kerala adapter
- Document Kerala integration in `docs/backend/state-integrations/KL.md`

---

### Milestone 5.2 — Second State Integration (Karnataka or Maharashtra)

**Problem:** A single state does not demonstrate the federated adapter model. Two states prove the architecture works.

**Tasks:**
- Research Karnataka Bhoomi API or Maharashtra Mahabhulekh endpoints
- Register the second state through the Admin UI (not seed SQL) — this validates the adapter workflow is real
- Configure endpoints, field mappings (note: terminologies differ — Karnataka uses "Taluk" not "Tehsil")
- Configure CRS if state data uses a local CRS
- Run integration tests against real or best-available staging API
- Document second state integration in `docs/backend/state-integrations/KA.md` or `MH.md`

---

### Milestone 5.3 — Identifier Resolution Service

**Problem:** The architecture defines identifier resolution (ULPIN ↔ local survey number ↔ Khasra number etc). No `IdentifierResolutionService` exists.

**Tasks:**
- Implement `IdentifierResolutionService.resolveToCanonical(stateCode, identifierType, value)` → `ulpin`
- Support resolution by: ULPIN (pass-through), local parcel ID (survey no / khasra / dag no), administrative hierarchy + local id
- Add `parcel_identifiers` table: `(ulpin, identifier_type, identifier_value, state_code)` for multi-identifier indexing
- Expose `GET /api/v1/parcels/resolve?state=KL&type=survey_no&value=123/4` → canonical parcel
- Integrate resolution into all parcel endpoints

---

## Phase 6 — Observability, Reliability & Production-Readiness
> *A platform serving national land data must be observable, resilient, and auditable.*

### Milestone 6.1 — Comprehensive Audit Logging

**Problem:** The architecture requires audit trails (who, what, when, which parcel, which source, which operation). No audit logging exists.

**Tasks:**
- Create `audit_log` table: `(id, user_id, action, resource_type, resource_id, timestamp, ip_address, request_details, result)`
- Implement `AuditService.log(action, resourceType, resourceId, details)` 
- Add `@AuditLog` annotation and AOP aspect to automatically log all write operations and sensitive read operations
- Log all: parcel data fetches (with ULPIN), state sync operations, admin configuration changes, login events, permission changes
- Implement `GET /api/v1/admin/audit-logs` with pagination and filtering
- Ensure audit logs are immutable (no `UPDATE`/`DELETE` on audit table)
- Export audit logs to structured log format compatible with government compliance requirements

---

### Milestone 6.2 — Structured Logging & Distributed Tracing

**Problem:** Current logging is basic SLF4J/Logback. For a multi-adapter federated system, correlation between requests across components is essential.

**Tasks:**
- Add MDC (Mapped Diagnostic Context) with `requestId`, `userId`, `stateCode` to all log statements
- Integrate Spring Boot Actuator metrics (already `actuator/health` is permitted in security config — expand this)
- Add Micrometer metrics: request count, latency histograms, adapter call success/failure rates, sync job durations
- Configure Prometheus scraping endpoint (`/actuator/prometheus`)
- Set up Grafana dashboard template for LandStack metrics
- Add distributed trace IDs for adapter calls (so a failed Kerala request can be traced from frontend to state API)

---

### Milestone 6.3 — Resilience Patterns for Adapter Calls

**Problem:** `StateApiClient` uses bare `RestTemplate` with no timeout, no retry, no circuit breaker. If a state API is slow, it blocks a Spring thread indefinitely.

**Tasks:**
- Replace `RestTemplate` with `WebClient` (reactive, non-blocking) or configure `RestTemplate` with connection and read timeouts
- Integrate Resilience4j: configure `CircuitBreaker` per state adapter (open circuit after N failures, half-open probe)
- Configure `Retry` with exponential backoff for transient failures (5xx, network timeouts)
- Configure `TimeLimiter` (hard max 10 seconds per adapter call)
- When circuit is open, return cached/last-known data with `data_freshness` indicator in response
- Surface circuit state in admin dashboard (green/yellow/red per state adapter)

---

### Milestone 6.4 — Caching Layer

**Problem:** No caching exists. Every parcel map tile request would hit the state API (or DB) cold.

**Tasks:**
- Integrate Redis (add to `docker-compose.yml`)
- Cache administrative boundary geometries (state, district, village) — long TTL (24h), they rarely change
- Cache parcel geometries with configurable TTL per adapter (default: 1h for RoR data, 24h for boundaries)
- Cache map tile responses (Redis or disk) with ETags
- Implement cache invalidation on successful sync job completion
- Do NOT cache sensitive RoR ownership data by default — make caching opt-in per capability and role

---

### Milestone 6.5 — API Rate Limiting & API Gateway Features

**Problem:** No rate limiting, no API versioning enforcement, no request tracing headers exist.

**Tasks:**
- Implement rate limiting per user/IP (Bucket4j or Spring Cloud Gateway)
- Public endpoints: 30 req/min per IP; authenticated: 300 req/min per user; admin: 1000 req/min
- Add `X-Request-ID` header propagation through entire call chain
- Add `X-RateLimit-Remaining` and `X-RateLimit-Reset` response headers
- Enforce `Accept-Version` header for API versioning (v1 is current)
- Add OpenAPI/Swagger documentation for all endpoints (SpringDoc already partially configured in security — complete it)
- Add API key support for external system integrations (non-JWT)

---

### Milestone 6.6 — Comprehensive Error Handling

**Problem:** `GlobalExceptionHandler` exists but its implementation was not revealed to be complete. `ParcelController.getRecordOfRights()` returns `500` for all exceptions with no message.

**Tasks:**
- Implement `GlobalExceptionHandler` to handle: `EntityNotFoundException` → 404, `AdapterUnavailableException` → 503, `CapabilityNotSupportedException` → 204/404, `ValidationException` → 400, `AccessDeniedException` → 403
- Define standard error response schema: `{ error_code, message, details, request_id, timestamp }`
- Never expose internal stack traces to clients
- Map adapter-specific errors to platform error codes (so `Kerala RoR API returned 500` maps to a platform-level `ADAPTER_UPSTREAM_ERROR`)
- Add problem detail support (RFC 7807)

---

## Phase 7 — Spatial Analytics & Environmental Integration
> *Build the watershed/remote-sensing/land-use analytics layer described in the architecture.*

### Milestone 7.1 — Thematic Spatial Layers Integration

**Problem:** No thematic layers (land use, soil, vegetation, environmental restrictions) exist anywhere in the system.

**Tasks:**
- Source LULC (Land Use Land Cover) GeoJSON/shapefile from ISRO/Bhuvan or NRSC
- Create `thematic_layers` table: `(layer_id, layer_name, layer_type, source, data_format, geometry_column, last_updated)`
- Create `layer_data` table (or separate PostGIS tables per layer type) for spatial storage
- Write data loaders for LULC, soil (NBSS&LUP), forest (FSI), protected areas (MoEFCC)
- Implement `GET /api/v1/layers` — list all available thematic layers
- Implement `GET /api/v1/parcels/{ulpin}/intersecting-layers` — return all thematic features intersecting a given parcel (PostGIS `ST_Intersects`)
- Implement `GET /api/v1/layers/{layerId}/parcels?bbox=` — find parcels within a thematic zone

---

### Milestone 7.2 — Watershed Integration

**Problem:** Watershed data is a core requirement of SIH26014 and is entirely absent.

**Tasks:**
- Source watershed boundary data (WRIS/CWC/NRSC — India's River Basin shapefiles)
- Create `watersheds` table with PostGIS geometry column
- Create `sub_watersheds`, `drainage_networks` tables
- Load watershed data via ETL pipeline
- Implement `GET /api/v1/watersheds` — list watersheds (with bbox filter)
- Implement `GET /api/v1/watersheds/{id}` — watershed detail + boundary GeoJSON
- Implement `GET /api/v1/watersheds/{id}/parcels` — all parcels within a watershed (spatial query)
- Implement `GET /api/v1/parcels/{ulpin}/watershed` — which watershed a parcel belongs to
- Display watershed boundaries as a toggleable layer in the frontend map

---

### Milestone 7.3 — Remote Sensing / Satellite Integration

**Problem:** The architecture mentions change detection, NDVI, land-use classification from satellite imagery. Nothing exists.

**Tasks:**
- Integrate Google Earth Engine API or ISRO Bhuvan API for satellite imagery access
- Implement `GET /api/v1/parcels/{ulpin}/satellite-imagery?date_range=` — fetch available imagery for a parcel extent
- Implement basic NDVI calculation endpoint (vegetation index from NIR/Red bands) — demonstrate capability
- Implement a simplified change detection endpoint: compare two dates, return change percentage
- Store change detection results in `parcel_analytics` table with timestamps
- Display satellite image tiles in the frontend map (as a toggleable base layer option)

---

### Milestone 7.4 — Spatial Analytics API

**Problem:** No spatial analytics exist (intersection, buffering, proximity analysis).

**Tasks:**
- `POST /api/v1/spatial/buffer` — given a ULPIN and radius, return all parcels within radius
- `POST /api/v1/spatial/intersect` — given a GeoJSON polygon, return all parcels that intersect it
- `POST /api/v1/spatial/contains` — which administrative unit contains a coordinate
- `GET /api/v1/spatial/proximity?ulpin=&radius_m=&layer=` — find thematic features near a parcel
- All analytics endpoints must respect row-level security (state filter) and permlevel redaction
- Rate-limit analytics endpoints separately (expensive spatial queries)

---

## Phase 8 — Production Infrastructure & Deployment
> *Package the system for government cloud or on-premise data center deployment.*

### Milestone 8.1 — Production Docker / Kubernetes Setup

**Problem:** Current `docker-compose.yml` is dev-only (no secrets management, no replicas, no health checks).

**Tasks:**
- Create production `docker-compose.prod.yml` with proper environment variable injection (no hardcoded secrets)
- Write Kubernetes manifests: `Deployment`, `Service`, `Ingress`, `ConfigMap`, `Secret` for each component
- Configure liveness and readiness probes for Spring Boot app (use `/actuator/health`)
- Configure resource limits per container (CPU, memory)
- Set up Horizontal Pod Autoscaler for the main application
- Write Helm chart for full deployment
- Document deployment procedure for government NIC / cloud environments

---

### Milestone 8.2 — Secrets & Configuration Management

**Problem:** Database credentials, JWT secret key, and state API credentials are in plaintext `.env` file.

**Tasks:**
- Integrate HashiCorp Vault or Kubernetes Secrets for runtime secret injection
- Rotate JWT signing key mechanism (JWKS endpoint or Vault KV)
- Encrypt `auth_credentials` column in `state_adapters` table at rest (AES-256)
- Use `application-prod.yml` with no secrets (all from environment or vault)
- Document secret rotation procedures

---

### Milestone 8.3 — Database Production Setup

**Problem:** PostGIS runs as a single container with no backups, replication, or tuning.

**Tasks:**
- Configure PostgreSQL streaming replication (primary + replica)
- Set up automated `pg_dump` backups to object storage (daily full, hourly WAL)
- Configure PostGIS performance tuning (`shared_buffers`, `work_mem`, `random_page_cost` for SSD)
- Add connection pooling (PgBouncer)
- Define and monitor `pg_stat_user_tables` for bloat and slow queries
- Add monitoring for spatial index effectiveness (`idx_parcels_geom`)

---

### Milestone 8.4 — CI/CD Pipeline

**Problem:** No CI/CD pipeline exists.

**Tasks:**
- Set up GitHub Actions / GitLab CI pipeline
- Pipeline stages: Build → Unit Tests → Integration Tests → Docker Build → Push to Registry → Deploy to Staging
- Run Flyway migrations in staging before deploying application
- Enforce test coverage minimum (50% for initial target)
- Add OWASP dependency check for security vulnerabilities in Maven dependencies
- Add Trivy container image scanning
- Implement blue-green deployment for zero-downtime releases

---

### Milestone 8.5 — Testing Suite

**Problem:** There is only `LandStackApplicationTests.java` (presumably a Spring context load test). No unit tests, integration tests, or API contract tests exist.

**Tasks:**
- **Unit Tests:** `DynamicFieldMapper`, `CrsNormalizationService`, `PermlevelRedactionFilter` redaction logic, `IdentifierResolutionService`, `CapabilityCheck`
- **Integration Tests:** Full Spring Boot test with Testcontainers (PostgreSQL+PostGIS), test every API endpoint (happy path + error cases)
- **Security Tests:** Assert that unauthenticated requests to protected endpoints return 401; assert permlevel 0 user cannot see permlevel 1+ fields; assert state filter blocks cross-state access
- **Adapter Tests:** Mock state API server (WireMock), test full federated flow from API request to canonical response
- **Performance Tests:** JMeter / Gatling for bbox parcel query under load (target: `< 200ms p95` for 10,000 parcel viewport)

---

### Milestone 8.6 — Security Hardening

**Problem:** Several security concerns need to be addressed before production exposure.

**Tasks:**
- Enable HTTPS (TLS 1.2+) — configure `application-prod.yml` with keystore or use reverse proxy (Nginx/Traefik)
- Re-enable and configure CORS properly (currently `csrf.disable()` + permissive — tighten allowed origins)
- Add `Content-Security-Policy`, `X-Frame-Options`, `X-Content-Type-Options` headers
- Implement JWT token refresh (current: single token, no refresh mechanism)
- Implement account lockout after N failed login attempts
- Conduct OWASP Top 10 review for all API endpoints
- Add input validation/sanitization for all SQL-adjacent parameters (bbox coordinates, ULPIN format)
- Document data classification for all stored fields (public vs restricted vs sensitive)

---

## Summary Dashboard

| Phase | Milestone Count | Estimated Effort | Priority |
|---|---|---|---|
| **Phase 1** — Backend Foundation | 4 milestones | 2–3 weeks | 🔴 Critical |
| **Phase 2** — Adapter Lifecycle Engine | 6 milestones | 3–4 weeks | 🔴 Critical |
| **Phase 3** — Real GIS Pipeline | 6 milestones | 4–5 weeks | 🔴 Critical |
| **Phase 4** — Frontend Rebuild | 5 milestones | 3–4 weeks | 🔴 Critical |
| **Phase 5** — Multi-State Integration | 3 milestones | 2–3 weeks | 🟠 High |
| **Phase 6** — Observability & Reliability | 6 milestones | 2–3 weeks | 🟠 High |
| **Phase 7** — Spatial Analytics | 4 milestones | 4–6 weeks | 🟡 Medium |
| **Phase 8** — Production Infrastructure | 6 milestones | 2–3 weeks | 🟡 Medium |
| **Total** | **40 milestones** | **~22–31 weeks** | — |

---

## Critical Path (Minimum Viable Production)

To reach a **Minimum Viable Production** system (demonstrating the full federated architecture with 2 real states, working map, complete RBAC), the critical path is:

```
Phase 1 (Auth, Roles, Permlevel) 
    → Phase 2 (Adapter CRUD, Test Workflow) 
    → Phase 3 (CRS + Sync Pipeline + Admin Hierarchy + Tiles) 
    → Phase 4 (Map Navigation + Parcel Panel + Admin UI) 
    → Phase 5 (2 Real State Adapters + Identifier Resolution) 
    → Phase 6 (Error Handling + Audit + Resilience)
```

Phases 7 and 8 can be parallelized or deferred to a post-MVP release.

---

## Key Open Decisions (from Architecture Doc, Section 32)

These architectural questions are explicitly listed as **open** in the architecture document and need resolution before implementation in the corresponding milestone:

| Decision | Impact |
|---|---|
| **Exact CRS strategy** — production CRS for interoperable spatial ops | Phase 3.1 |
| **Map tile technology** — `pg_tileserv`, `martin`, GeoServer, or MapLibre | Phase 3.5, 4.1 |
| **State integration mechanisms** — REST vs OGC/WFS vs batch per state | Phase 5.1, 5.2 |
| **Caching strategy** — what is cached, TTL, invalidation | Phase 6.4 |
| **Deployment topology** — NIC cloud, government DC, or hybrid | Phase 8.1 |
| **API specification finalization** — versioning, pagination, error schema | Phase 6.5, 6.6 |
| **ULPIN generation algorithm** — must match DILRMP national standard | Phase 3.2 |
