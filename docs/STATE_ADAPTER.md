# STATE_ADAPTER.md

## 1. Purpose

The State Adapter is the interoperability boundary between the common Land Stack platform and a particular state's land, cadastral, registration, planning, fiscal, utility, and other authoritative systems.

It is **not merely a coordinate converter**.

It isolates state-specific differences while exposing a common integration contract to the core platform. The adapter allows the platform to work across India's heterogeneous land-administration systems without forcing every state to adopt the same internal API, database structure, terminology, identifiers, units, workflows, or CRS.

The State Adapter also preserves source identity, source terminology, provenance, and the relationship between the common representation and the authoritative state source.

The broader SIH26014 architecture requires a configurable, modular, scalable, interoperable state-level framework capable of integrating diverse land datasets and services around a parcel-centric spatial model. The State Adapter is the principal boundary that makes this possible.

---

# 2. Core Principle

The core platform must remain **state-agnostic**.

The core must never contain state-specific conditionals such as:

```python
if state == "KL":
    ...
elif state == "KA":
    ...
```

Instead:

```text
CORE PLATFORM
      │
      ▼
COMMON INTEGRATION CONTRACT
      │
      ▼
ADAPTER REGISTRY
      │
      ▼
STATE ADAPTER
      │
      ▼
STATE SYSTEMS / SERVICES
```

The core speaks a common language.

The State Adapter speaks both:

```text
Common Platform Language
          ↕
State-Specific Language
```

---

# 3. What the State Adapter Handles

A State Adapter handles state-specific differences including:

1. API/interface differences
2. Authentication differences
3. Request field mapping
4. Response field mapping
5. State-specific identifiers
6. Administrative identifiers
7. Terminology mapping
8. Schema mapping
9. Unit conversion
10. Language/transliteration where necessary
11. Response transformation
12. CRS configuration and transformation requirements
13. State-specific error handling
14. State-specific workflow behavior
15. Source metadata
16. Provenance
17. Integration capabilities
18. State-specific provider/service selection
19. Synchronous/asynchronous integration behavior where required

The adapter is therefore an interoperability boundary, not simply a GIS utility.

---

# 4. High-Level Boundary

```text
                         CORE PLATFORM
                              │
                              ▼
                   COMMON INTEGRATION CONTRACT
                              │
                              ▼
                       ADAPTER REGISTRY
                              │
                              ▼
                     ┌─────────────────────┐
                     │    STATE ADAPTER    │
                     │                     │
                     │ Configuration       │
                     │ Authentication      │
                     │ Request Mapping     │
                     │ Identifier Mapping  │
                     │ Schema Mapping      │
                     │ Terminology Mapping │
                     │ Unit Normalization  │
                     │ CRS Handling        │
                     │ Response Mapping    │
                     │ Error Handling      │
                     │ Provenance          │
                     └──────────┬──────────┘
                                │
                                ▼
                     STATE AUTHORITATIVE
                       SYSTEMS / SERVICES
```

The core does not need to know how the state implemented its backend.

---

# 5. Database-Driven Design

## 5.1 Source of Truth

The **database is the source of truth for State Adapter configuration**.

This is a fundamental architectural decision.

API configuration, mappings, capabilities, state metadata, CRS configuration, provider configuration, and similar integration metadata must be stored as database records.

The filesystem must not be treated as the authoritative runtime configuration store.

The flow is:

```text
                         ADMIN UI
                            │
                            ▼
                STATE INTEGRATION MANAGER
                            │
                            ▼
                         DATABASE
                  ← SOURCE OF TRUTH →
                            │
             ┌──────────────┼──────────────┐
             ▼              ▼              ▼
        State Metadata   API Config     Mappings
             │              │              │
             └──────────────┼──────────────┘
                            ▼
                     Adapter Runtime
                            │
                            ▼
                     State Services
```

## 5.2 Why the Database Is the Source of Truth

This provides:

- Centralized configuration
- UI-driven administration
- Configuration versioning
- Auditability
- Rollback
- Multi-instance consistency
- No dependence on local filesystem state
- No need to redeploy for ordinary configuration changes
- Easier state onboarding
- Better operational management

For example, if an administrator changes:

```text
survey_no → local_parcel_id
```

through the UI, the new mapping is persisted in the database and becomes the authoritative runtime configuration.

---

# 6. Configuration vs Code

The design follows a strict principle:

> **Configuration belongs in the database; behavior belongs in code.**

Use database configuration for differences that can be represented declaratively.

Examples:

- API endpoints
- API types
- Capabilities
- Request field mappings
- Response field mappings
- Identifier mappings
- Administrative mappings
- Terminology mappings
- Unit mappings
- Unit conversions
- CRS metadata
- Provider metadata
- Authentication configuration/reference
- State metadata

Use custom code only when the state's behavior cannot reasonably be expressed through configuration.

Examples:

- Unusual authentication flows
- Complex request generation
- Complex response parsing
- Non-standard workflows
- State-specific business logic
- Custom protocol behavior

This avoids both extremes:

```text
Too much hard-coded state logic
             OR
Too much generic configuration
```

---

# 7. State Registration Through the UI

States are registered through an administrative interface.

The workflow is conceptually similar to creating/configuring a structured object such as a Frappe DocType.

```text
Administration
      │
      ▼
State Integrations
      │
      ▼
+ Register New State
      │
      ▼
State Metadata
      │
      ▼
Integration Configuration
      │
      ▼
API Discovery / Configuration
      │
      ▼
Mapping
      │
      ▼
Testing
      │
      ▼
Validation
      │
      ▼
Activation
```

A registration form may contain:

```text
State Name       [ Kerala             ]
State Code       [ KL                 ]

Integration Type
☑ REST API
☑ GIS Service
☐ OGC/WFS
☐ File Exchange

Capabilities
☑ Parcel Search
☑ Parcel Geometry
☑ Rights / RoR
☐ Registration
☐ Planning
☐ Restrictions
```

The exact UI is an implementation detail. The architectural requirement is that state integration is administratively configurable.

---

# 8. Adapter Lifecycle

A state should not become active immediately after registration.

Recommended lifecycle:

```text
DRAFT
  │
  ▼
CONFIGURING
  │
  ▼
MAPPING
  │
  ▼
TESTING
  │
  ▼
VALIDATED
  │
  ▼
ACTIVE
  │
  ▼
SUSPENDED
```

### DRAFT

The state exists but has not been configured.

### CONFIGURING

Endpoints, authentication references, integration mechanisms, providers, and capabilities are configured.

### MAPPING

State fields, identifiers, administrative concepts, terminology, units, and schemas are mapped.

### TESTING

The integration is tested against the actual configured state services.

### VALIDATED

Required integration tests have passed.

### ACTIVE

The adapter can be used by normal platform operations.

### SUSPENDED

The integration remains registered but is temporarily unavailable.

---

# 9. Adapter Registry

The Adapter Registry resolves a state to its configured adapter.

```text
Integration Request
        │
        ▼
Adapter Registry
        │
        ├── KL → Kerala Adapter
        ├── KA → Karnataka Adapter
        └── MH → Maharashtra Adapter
```

The registry should resolve adapters using stable state identifiers/codes and should ensure that only appropriately configured/active integrations are used for normal runtime operations.

The core therefore does not need direct knowledge of individual state implementations.

---

# 10. Capability Model

A state may not expose every land-related service.

Capabilities must therefore be explicitly represented.

Example:

```text
Kerala

Parcel Search       ✓
Parcel Geometry     ✓
Rights / RoR        ✓
Registration        ✓
Planning            ?
Restrictions        ?
```

Conceptually:

```json
{
  "parcel_search": true,
  "parcel_geometry": true,
  "rights": true,
  "registration": true,
  "planning": false,
  "restrictions": false
}
```

Before executing an operation, the core can ask the adapter:

```text
Does this state support this capability?
```

Capabilities are configuration/metadata, not hard-coded core logic.

---

# 11. Common Integration Contract

The core communicates with adapters through a common contract.

Conceptual operations include:

```text
getAdministrativeUnits()
resolveParcel()
getParcels()
getParcelGeometry()
getRights()
getRegistration()
getPlanning()
getRestrictions()
```

These names are conceptual and can be refined during implementation.

The fundamental flow is:

```text
CORE REQUEST
      │
      ▼
COMMON CONTRACT
      │
      ▼
STATE ADAPTER
      │
      ▼
STATE-SPECIFIC REQUEST
      │
      ▼
STATE SYSTEM
```

The core does not know whether the state uses REST, GIS, OGC, a batch service, or another supported mechanism.

---

# 12. Common Request Model

A common parcel request may conceptually contain:

```text
ParcelQueryRequest

administrative_context
spatial_extent
parcel_identifier
requested_crs
requested_fields
```

Example:

```json
{
  "administrative_context": {
    "state": "KL",
    "district": "EKM",
    "village": "..."
  },
  "spatial_extent": {
    "bbox": [ ... ]
  },
  "requested_crs": "..."
}
```

The adapter translates this common representation into the state's required request.

---

# 13. Request Mapping

Request mapping converts canonical concepts into state-specific parameters.

```text
COMMON REQUEST
      │
      ▼
Administrative Mapping
      │
      ▼
Identifier Mapping
      │
      ▼
Field Mapping
      │
      ▼
State Request Builder
      │
      ▼
STATE API
```

Example:

```text
Canonical              State API
────────────────────────────────────────
district_code       →  districtId
village_code        →  villageId
survey_number       →  surveyNo
spatial_extent      →  geometryBounds
```

Mappings should be database-driven whenever possible.

---

# 14. Response Mapping

The state response must not simply be passed directly to the core.

```text
STATE RESPONSE
      │
      ▼
Response Parser
      │
      ▼
Schema Validation
      │
      ▼
Field Mapping
      │
      ▼
Identifier Mapping
      │
      ▼
Terminology Mapping
      │
      ▼
Unit Normalization
      │
      ▼
CRS Handling
      │
      ▼
Quality Validation
      │
      ▼
Canonical Representation
      │
      ▼
Provenance
      │
      ▼
COMMON RESPONSE
```

---

# 15. API Discovery and Assisted Mapping

The administration UI should support API-assisted mapping.

The administrator provides/configures an endpoint.

The platform can then:

```text
State API
   │
   ▼
Connection Test
   │
   ▼
Schema / Sample Response Discovery
   │
   ▼
Field Extraction
   │
   ▼
Mapping Suggestions
   │
   ▼
Administrator Review
   │
   ▼
Confirmed Mapping
   │
   ▼
Database
```

The system should **suggest** mappings rather than blindly applying them.

For example, a response may contain:

```json
{
  "survey_no": "123/4",
  "village_cd": "V102",
  "dist_cd": "D07",
  "area_hect": 1.25,
  "geom": {}
}
```

The mapping assistant may suggest:

```text
State Field       Canonical Field
────────────────────────────────────
survey_no      →  local_parcel_id
village_cd     →  village_code
dist_cd        →  district_code
area_hect      →  area
geom           →  geometry
```

The administrator reviews and confirms the mappings.

This is **semi-automatic/assisted mapping**, not an assumption that arbitrary APIs can always be mapped perfectly by an algorithm.

---

# 16. Mapping Categories

The adapter configuration should distinguish different types of mapping.

## 16.1 Field Mapping

```text
state_field → canonical_field
```

## 16.2 Identifier Mapping

```text
state_identifier → canonical_identifier_concept
```

## 16.3 Administrative Mapping

```text
state_admin_field → canonical_admin_concept
```

## 16.4 Terminology Mapping

```text
state_term → canonical_semantic_concept
```

## 16.5 Unit Mapping

```text
state_unit → canonical_unit
```

## 16.6 CRS Configuration

```text
state_source_crs → common/target_crs
```

Keeping these categories explicit makes the adapter easier to configure, validate, and maintain.

---

# 17. Identifier Mapping

Identifiers require special treatment.

The adapter must not destroy state/local identifiers in favor of a single generic identifier.

The common model should preserve relationships such as:

```text
Common / Canonical Identity
             │
             ├── ULPIN / common identifier
             │
             └── State / Local Identity
                    │
                    ├── Survey Number
                    ├── Khasra Number
                    ├── Khata / Holding Reference
                    └── Other local identifiers
```

Example:

```text
Canonical Concept       State Field
────────────────────────────────────
local_parcel_id      →  survey_no
subdivision_id       →  sub_div_no
holding_reference    →  thandaper_no
```

The original state identifier remains available for source traceability and interaction with the authoritative source.

---

# 18. Administrative Mapping

States may use different administrative terminology and structures.

The adapter maps them into common semantic concepts.

Example:

```text
State Terminology       Canonical Concept
─────────────────────────────────────────
District             →  District
Taluk                →  Sub-District
Village              →  Village
```

The system should preserve both:

```text
Canonical Concept
+
Source Terminology
```

This allows the platform to provide common functionality without erasing state-specific meaning.

---

# 19. Schema Mapping

State schemas are not assumed to be identical.

The adapter transforms:

```text
STATE SCHEMA
     │
     ▼
MAPPING RULES
     │
     ▼
CANONICAL MODEL
```

Example:

```text
State:
"Thandaper No."

Canonical:
"holding_reference"
```

The original source field and value remain traceable.

---

# 20. Source Fidelity

Normalization must not destroy the original meaning.

Conceptually:

```text
source_field      = "Thandaper No."
source_value      = "123456"

canonical_field   = "holding_reference"
canonical_value   = "123456"
```

The platform gets a common semantic representation while retaining the source representation and provenance.

---

# 21. Terminology, Language, and Transliteration

The adapter may handle:

- State-specific terminology
- Local labels
- Language normalization
- Transliteration
- Terminology aliases

The objective is not to erase local language.

Conceptually:

```text
SOURCE TERM
    │
    ├── Original value
    ├── Original language
    └── Canonical semantic concept
```

Both the source representation and canonical concept can therefore coexist.

---

# 22. Unit Conversion

States may use different units.

The adapter normalizes units when required.

Example:

```text
State:
area_sq_m = 10,000

        ↓

Unit Normalization

        ↓

Canonical:
area_hectare = 1
```

Deterministic unit conversions should preferably be configuration-driven and implemented through reusable platform conversion infrastructure.

---

# 23. CRS Handling

CRS differences are part of the interoperability boundary.

The adapter identifies/configures the source CRS and the required target/common CRS.

The actual transformation should be performed by reusable shared GIS/CRS infrastructure rather than a separate GIS implementation for every state.

```text
STATE GEOMETRY
      │
      │ Source CRS
      ▼
SHARED CRS TRANSFORMATION SERVICE
      │
      │ Target CRS
      ▼
COMMON SPATIAL REPRESENTATION
```

The adapter therefore provides the state-specific CRS configuration and invokes the shared transformation capability.

The platform must support native source CRSs and transform them into an appropriate common spatial reference before cross-dataset spatial operations.

---

# 24. Authentication

Authentication is state-specific and therefore belongs behind the adapter boundary.

Possible mechanisms include:

```text
API Key
OAuth / Token
Client Certificate
Institutional Authentication
Other approved mechanisms
```

Conceptually:

```text
State Adapter
      │
      ▼
Authentication Provider
      │
      ▼
State Service
```

Actual credentials/secrets must not be stored directly in generated source files.

The database configuration should contain a secure credential reference, while the actual secret is managed through appropriate secret-management infrastructure.

---

# 25. Integration Mechanisms

The adapter must not assume that every state exposes a REST API.

Potential integration mechanisms include:

```text
REST API
GIS Service
OGC Service
Scheduled Data Feed
Secure File Exchange
Other approved institutional mechanism
```

Conceptually:

```text
                  STATE ADAPTER
                        │
        ┌───────────────┼───────────────┐
        ▼               ▼               ▼
    REST Client      GIS/OGC        Batch/File
                        Client          Client
        │               │               │
        └───────────────┼───────────────┘
                        ▼
                 STATE SYSTEMS
```

The configured integration mechanism is state/service-specific.

---

# 26. Multiple State Providers

A state may have different systems for different domains.

The State Adapter must therefore be capable of coordinating multiple providers.

Example:

```text
                       STATE ADAPTER
                             │
             ┌───────────────┼───────────────┐
             ▼               ▼               ▼
        Cadastral        Rights / RoR    Registration
         Provider          Provider        Provider
             │               │               │
             ▼               ▼               ▼
        State Source     State Source     State Source
```

This allows:

```text
Parcel Geometry → Cadastral Provider
Rights          → RoR Provider
Registration    → Registration Provider
Planning        → Planning Provider
```

The provider abstraction prevents the State Adapter from becoming a monolithic implementation.

---

# 27. Generic Adapter Engine

The platform should provide reusable adapter infrastructure.

Conceptually:

```text
adapter/
│
├── engine/
│   ├── adapter_runtime
│   ├── request_pipeline
│   ├── response_pipeline
│   ├── validation
│   ├── identifier_resolution
│   ├── schema_mapping
│   ├── terminology_mapping
│   ├── unit_normalization
│   ├── provenance
│   └── error_mapping
│
├── transport/
│   ├── rest
│   ├── gis
│   ├── ogc
│   └── file
│
└── authentication/
    └── providers
```

The exact implementation technology is not fixed by this document.

---

# 28. State Adapter Scaffold

When a state is registered, the platform may provision a state-specific adapter scaffold.

The conceptual structure discussed for a state is:

```text
states/
└── KL/
    ├── adapter.py
    ├── config.yaml
    ├── identifiers.yaml
    ├── mappings.yaml
    ├── authentication.py
    ├── parcel.py
    └── rights.py
```

However, there is an important architectural distinction:

> These files are **not the source of truth for API configuration or mappings**.

The database remains authoritative.

The files can represent:

- Adapter entry-point/scaffold
- Generic runtime hooks
- Optional state-specific implementation code
- Developer-managed defaults/templates
- Custom behavior that cannot be represented as configuration

The actual API configuration and mappings used by runtime should come from the database.

---

# 29. Meaning of the Proposed Files

If the scaffold is generated, the conceptual responsibilities are:

| File | Purpose |
|---|---|
| `adapter.py` | State adapter entry point/scaffold |
| `config.yaml` | Optional template/default representation; not authoritative runtime configuration |
| `identifiers.yaml` | Optional template/default identifier mapping representation; database is authoritative |
| `mappings.yaml` | Optional template/default mapping representation; database is authoritative |
| `authentication.py` | Authentication hooks/custom behavior where configuration is insufficient |
| `parcel.py` | Parcel-provider hooks/custom behavior |
| `rights.py` | Rights/RoR-provider hooks/custom behavior |

The exact file structure may evolve during implementation.

---

# 30. Why Not Make YAML the Source of Truth?

The rejected primary architecture is:

```text
UI
 ↓
Generate mappings.yaml
 ↓
Runtime reads YAML
```

The chosen architecture is:

```text
UI
 ↓
Database
 ↓
Adapter Runtime
 ↓
State System
```

YAML/JSON files may still be useful for:

- Templates
- Developer defaults
- Configuration export/import
- Testing fixtures
- Migration files
- Generated scaffolding

But they do not override the database as the authoritative runtime configuration.

---

# 31. Complete Runtime Pipeline

The State Adapter runtime can be represented as:

```text
                         CORE PLATFORM
                               │
                               ▼
                  COMMON INTEGRATION CONTRACT
                               │
                               ▼
                       ADAPTER REGISTRY
                               │
                               ▼
                    GENERIC ADAPTER ENGINE
                               │
                               ▼
                   Load State Configuration
                               │
                               ▼
                       Capability Check
                               │
                               ▼
                       Request Validation
                               │
                               ▼
                    Administrative Mapping
                               │
                               ▼
                     Identifier Mapping
                               │
                               ▼
                       Field Mapping
                               │
                               ▼
                    State Request Builder
                               │
                               ▼
                        Authentication
                               │
                               ▼
                       State Transport
                               │
                               ▼
                  STATE AUTHORITATIVE SYSTEM
                               │
                               ▼
                          Raw Response
                               │
                               ▼
                       Response Parsing
                               │
                               ▼
                       Schema Validation
                               │
                               ▼
                       Schema Mapping
                               │
                               ▼
                     Identifier Mapping
                               │
                               ▼
                    Terminology Mapping
                               │
                               ▼
                     Unit Normalization
                               │
                               ▼
                      CRS Transformation
                               │
                               ▼
                        Quality Checks
                               │
                               ▼
                    Canonical Transformation
                               │
                               ▼
                          Provenance
                               │
                               ▼
                       COMMON RESPONSE
                               │
                               ▼
                         CORE PLATFORM
```

---

# 32. Error Handling

State-specific errors must be translated into a common error model.

Example:

```text
STATE SERVICE
     │
     ├── Authentication failure
     ├── Parcel not found
     ├── Timeout
     ├── Rate limit
     ├── Source failure
     ├── Invalid geometry
     └── Schema mismatch
             │
             ▼
        STATE ADAPTER
             │
             ▼
       COMMON ERROR MODEL
```

Possible normalized errors:

```text
STATE_AUTHENTICATION_FAILED
STATE_IDENTIFIER_NOT_FOUND
STATE_TIMEOUT
STATE_RATE_LIMITED
STATE_SYSTEM_ERROR
INVALID_STATE_GEOMETRY
STATE_SCHEMA_MISMATCH
CRS_METADATA_UNAVAILABLE
```

The original state error should remain available for diagnosis and traceability.

---

# 33. Provenance

Every normalized result must remain traceable to its source.

A conceptual provenance record may contain:

```text
source_system
source_record_id
source_field
source_value
retrieval_time
transformation
adapter_version
configuration_version
```

Example:

```text
canonical_field = holding_reference
canonical_value = 123456

source_field = "Thandaper No."
source_system = "State RoR System"
source_record_id = "..."
retrieval_time = "..."
transformation = "State field mapped to canonical concept"
```

Provenance is essential for auditability, debugging, source verification, and source fidelity.

---

# 34. Authoritative Source Relationship

The State Adapter does not transfer legal authority to the platform.

The relationship remains:

```text
STATE AUTHORITATIVE SYSTEM
          │
          │ authoritative record
          ▼
     STATE ADAPTER
          │
          │ integration + normalization
          ▼
     COMMON PLATFORM
```

The platform may:

- Integrate
- Visualize
- Normalize
- Index
- Cache
- Analyze
- Expose services

But it must not silently become the legal source of truth.

Caching does not change source authority.

---

# 35. Synchronous Integration

For fast operations:

```text
Request
   │
   ▼
State Adapter
   │
   ▼
State API
   │
   ▼
Response
   │
   ▼
Transformation
   │
   ▼
Core
```

This is appropriate when the state service can provide a response within the expected latency.

---

# 36. Asynchronous Integration

Some state operations may be slow or long-running.

The adapter architecture therefore supports asynchronous integration:

```text
Request
   │
   ▼
Request ID
   │
   ▼
Queue
   │
   ▼
Integration Worker
   │
   ▼
State Adapter
   │
   ▼
State System
   │
   ▼
Transformation
   │
   ▼
Result
```

This accommodates:

- Long-running state operations
- Variable state response times
- Heavy processing
- Rate limits
- Temporary availability constraints

The overall integration architecture therefore supports both synchronous and asynchronous execution.

---

# 37. Caching

Caching can be used for performance and resilience where appropriate.

However:

```text
CACHE ≠ AUTHORITATIVE LAND DATABASE
```

Cached state data must retain appropriate provenance and freshness information.

Caching policies should be configurable and should never imply that cached data has replaced the state's authoritative record.

---

# 38. Integration Testing

A state adapter must be tested before activation.

The administration UI should provide integration testing such as:

```text
Test Connection
      │
      ▼
Authentication Test
      │
      ▼
Endpoint Test
      │
      ▼
Sample Request
      │
      ▼
Sample Response
      │
      ▼
Schema Validation
      │
      ▼
Identifier Mapping Test
      │
      ▼
Administrative Mapping Test
      │
      ▼
CRS Test
      │
      ▼
Canonical Transformation Test
      │
      ▼
Provenance Test
      │
      ▼
Validation
```

Example:

```text
Kerala Integration

✓ Authentication
✓ API Connectivity
✓ Parcel Query
✓ Response Schema
✓ Identifier Mapping
✓ Administrative Mapping
✓ CRS Transformation
✓ Canonical Transformation
✓ Provenance

Status: VALIDATED
```

---

# 39. State Registration Workflow

The complete intended onboarding workflow is:

```text
1. Administrator opens State Integrations
                     │
                     ▼
2. Register State
                     │
                     ▼
3. Enter State Metadata
                     │
                     ▼
4. Configure Integration Mechanism
                     │
                     ▼
5. Configure / Discover API
                     │
                     ▼
6. Test Authentication
                     │
                     ▼
7. Discover Sample Response / Schema
                     │
                     ▼
8. Generate Mapping Suggestions
                     │
                     ▼
9. Administrator Reviews Mapping
                     │
                     ▼
10. Configure Identifier Mapping
                     │
                     ▼
11. Configure Administrative Mapping
                     │
                     ▼
12. Configure Terminology / Units
                     │
                     ▼
13. Configure CRS
                     │
                     ▼
14. Configure Capabilities
                     │
                     ▼
15. Run Integration Tests
                     │
                     ▼
16. Validate
                     │
                     ▼
17. Activate State
```

---

# 40. Adding a New State

The target architecture is that onboarding a normal new state should not require changing the core platform.

```text
EXISTING CORE
     │
     ├── Common Contract
     ├── Adapter Engine
     ├── Mapping Engine
     ├── CRS Service
     ├── Provenance
     └── Adapter Registry
              │
              ▼
       Register New State
              │
              ▼
       Configure State
              │
              ▼
          Map State
              │
              ▼
          Test State
              │
              ▼
         Validate State
              │
              ▼
         Activate State
```

The new state plugs into the existing interoperability framework.

---

# 41. Example State Integration

A conceptual state registration may look like:

```text
State:
    Kerala

Code:
    KL

Status:
    ACTIVE

Capabilities:
    parcel_search
    parcel_geometry
    rights
    ...

Providers:
    cadastral
    rights
    ...

Configuration:
    API endpoints
    authentication reference
    CRS
    mappings
    identifiers
```

Runtime:

```text
Core
 │
 ▼
Common Contract
 │
 ▼
Adapter Registry
 │
 ▼
KL Adapter
 │
 ├── Cadastral Provider
 │       │
 │       ▼
 │   State Cadastral System
 │
 └── Rights Provider
         │
         ▼
     State RoR System
```

This is a conceptual example of the adapter architecture, not a statement of the exact current APIs or systems of any particular state.

---

# 42. Security

The State Adapter is a security-sensitive integration boundary.

Requirements include:

- Do not hard-code credentials.
- Store secrets through secure credential references.
- Authenticate state services using their supported mechanisms.
- Validate incoming responses.
- Validate outgoing requests.
- Apply role-based access to integration configuration.
- Audit configuration changes.
- Version configuration.
- Restrict activation/deactivation privileges.
- Protect source-service credentials.
- Avoid unnecessary exposure of sensitive source responses.
- Preserve audit and provenance records.

---

# 43. Observability

Each adapter should expose operational information such as:

```text
State
Adapter Status
Last Successful Request
Last Failure
Failure Rate
Average Response Time
Authentication Status
Endpoint Health
Capability Status
Configuration Version
```

Example:

```text
Kerala

Status: ACTIVE

API Health: Healthy
Authentication: Healthy
Parcel Service: Healthy
Rights Service: Degraded
Last Successful Request: ...
Configuration Version: 12
```

This integrates with the platform's broader monitoring and operational management.

---

# 44. Configuration Versioning

State integration configuration should be versioned.

Conceptually:

```text
Configuration v1
       │
       ▼
Configuration v2
       │
       ▼
Configuration v3
```

A configuration version should allow the platform to determine:

- Which endpoint configuration was active
- Which mappings were active
- Which CRS configuration was active
- Which capabilities were active
- When the configuration changed
- Who changed it
- Whether the version passed validation

This is especially important when a state changes its API.

---

# 45. State API Changes

If a state changes from API version 1 to API version 2:

```text
STATE API v1
     │
     ▼
Adapter Configuration v1
```

can evolve into:

```text
STATE API v2
     │
     ▼
Updated Adapter Configuration
     │
     ▼
Testing
     │
     ▼
Validation
     │
     ▼
Activation
```

The common core contract should remain stable.

Only state-specific configuration and, where necessary, custom state-specific code should change.

---

# 46. State-Specific Custom Code

Not every difference can be represented declaratively.

When custom behavior is genuinely required, it remains isolated inside the state adapter/provider boundary.

Example:

```text
states/
└── KL/
    ├── adapter.py
    ├── authentication.py
    ├── parcel.py
    └── rights.py
```

Custom code must not leak state-specific behavior into the core.

The rule is:

```text
Normal difference
      ↓
Database configuration

Exceptional behavior
      ↓
State-specific adapter/provider code

Common behavior
      ↓
Shared platform engine
```

---

# 47. Responsibility Matrix

| Responsibility | State Adapter | Shared Platform |
|---|---:|---:|
| State API differences | ✓ | |
| Authentication strategy | ✓ | Infrastructure support |
| Request field mapping | ✓ | Mapping engine |
| Response field mapping | ✓ | Mapping engine |
| State identifiers | ✓ | Canonical identity support |
| Administrative identifiers | ✓ | Common administrative model |
| Terminology mapping | ✓ | Reusable mapping engine |
| Schema mapping | ✓ | Reusable mapping engine |
| Unit conversion rules | ✓ | Shared conversion engine |
| Language/transliteration rules | ✓ | Shared language infrastructure |
| State workflow behavior | ✓ | |
| State-specific errors | ✓ | Common error model |
| Source metadata | ✓ | |
| Provenance data | ✓ | Provenance infrastructure |
| CRS requirements | ✓ | |
| CRS transformation engine | | ✓ |
| GIS operations | | ✓ |
| Spatial indexing | | ✓ |
| Queuing | | ✓ |
| Retry infrastructure | | ✓ |
| Monitoring | State-specific metrics | ✓ |
| Audit | State configuration events | ✓ |
| Canonical model | | ✓ |
| Core business logic | | ✓ |

---

# 48. What the State Adapter Must Not Do

The State Adapter must not:

1. Become a national replacement for state land systems.
2. Make the core state-aware.
3. Store authoritative land records as its own legal source.
4. Hard-code ordinary state mappings into the core.
5. Assume every state uses the same API.
6. Assume every state uses the same identifiers.
7. Assume every state uses the same administrative structure.
8. Assume every state uses the same CRS.
9. Assume every state exposes the same capabilities.
10. Treat cached data as authoritative.
11. Destroy original state terminology or identifiers.
12. Hide provenance.
13. Put secrets into generated configuration files.
14. Require core-platform changes for ordinary state onboarding.
15. Treat API discovery as infallible automatic mapping.

---

# 49. Final Logical Component Model

```text
                         ADMINISTRATION UI
                                │
                                ▼
                   STATE INTEGRATION MANAGER
                                │
                                ▼
                         CONFIGURATION DB
                                │
             ┌──────────────────┼──────────────────┐
             ▼                  ▼                  ▼
       State Metadata       API Config          Mappings
             │                  │                  │
             └──────────────────┼──────────────────┘
                                ▼
                         ADAPTER REGISTRY
                                │
                                ▼
                       GENERIC ADAPTER ENGINE
                                │
             ┌──────────────────┼──────────────────┐
             ▼                  ▼                  ▼
        Request Pipeline   Transport Layer   Response Pipeline
             │                  │                  │
             ▼                  ▼                  ▼
       State-specific      REST/GIS/OGC      Canonical Mapping
       request behavior    /File service      + Provenance
                                │
                                ▼
                     STATE AUTHORITATIVE
                       SYSTEMS / SERVICES
```

---

# 50. Relationship to the Overall Land Stack Architecture

The State Adapter is part of the interoperability layer rather than the entire platform.

```text
                         CORE SERVICES
                              │
                              ▼
                    INTEROPERABILITY LAYER
                              │
       ┌──────────────┬───────┼────────┬──────────────┐
       ▼              ▼       ▼        ▼              ▼
 Administrative    Identity  Normal-   CRS       Integration
    Master Data    Resolution ization  Service    Orchestration
                              │
                              ▼
                       ADAPTER REGISTRY
                              │
                  ┌───────────┼───────────┐
                  ▼           ▼           ▼
               Kerala      Karnataka   Maharashtra
               Adapter      Adapter      Adapter
                  │           │           │
                  ▼           ▼           ▼
              State Systems / Services / Sources
```

The adapter therefore remains a focused interoperability boundary while shared services remain shared platform capabilities.

---

# 51. Design Decisions Summary

The State Adapter design is based on the following decisions:

### Decision 1 — State-specific boundary

Every state's implementation differences are isolated behind a State Adapter.

### Decision 2 — UI-driven registration

States are registered/configured through an administrative UI.

### Decision 3 — Database as source of truth

API configuration, mappings, capabilities, identifiers, CRS configuration, and other integration metadata are stored in the database.

### Decision 4 — Files are not authoritative configuration

Generated YAML/code files may exist as scaffolding or implementation artifacts, but they do not override database configuration.

### Decision 5 — Configuration-driven integration

Declarative differences are represented as configuration rather than hard-coded state logic.

### Decision 6 — Custom code only when necessary

State-specific code exists only for behavior that cannot reasonably be expressed through configuration.

### Decision 7 — Assisted API mapping

The platform can inspect a state API/schema and suggest mappings, but an administrator confirms the mapping.

### Decision 8 — Capability-driven integration

A state explicitly declares which services it supports.

### Decision 9 — Source fidelity

State identifiers, terminology, and source information are preserved rather than discarded during normalization.

### Decision 10 — Provenance

Normalized information remains traceable to its source.

### Decision 11 — Shared GIS/CRS infrastructure

The adapter configures state CRS requirements; reusable platform infrastructure performs GIS/CRS processing.

### Decision 12 — Authoritative source remains external

The state system remains the authoritative source for its official records.

### Decision 13 — Validation before activation

A state integration passes testing and validation before becoming active.

### Decision 14 — Core remains state-agnostic

Adding a normally configurable state should not require changing core platform logic.

---

# 52. Final Principle

The State Adapter architecture can be summarized as:

> **The core platform speaks a common language. The State Adapter translates between that common language and the state's language.**

```text
                 COMMON PLATFORM
                        │
                        │ Common Contract
                        ▼
                ┌───────────────┐
                │ STATE ADAPTER │
                │               │
                │ Configuration │
                │ Mapping       │
                │ Authentication│
                │ Transformation│
                │ Provenance    │
                │ Custom Logic  │
                └───────┬───────┘
                        │
                        ▼
                 STATE SYSTEMS
```

The administrator registers a state through the UI.

The database stores the integration configuration and mappings as the source of truth.

The Adapter Registry selects the appropriate integration.

The generic Adapter Engine executes the configured integration.

API/schema discovery assists the administrator in creating mappings.

Custom state-specific code is used only when configuration is insufficient.

The state remains the authoritative source of its official records.

This makes state onboarding a controlled **integration workflow** rather than a core-platform development task.

---

# 53. Implementation Boundary

This document defines the architectural behavior and responsibilities of the State Adapter.

It does not yet prescribe:

- Exact programming language
- Exact backend framework
- Exact database technology
- Exact database table/DocType names
- Exact API specification
- Exact endpoint naming
- Exact authentication library
- Exact secret-management technology
- Exact queue technology
- Exact GIS/CRS library
- Exact UI framework
- Exact deployment model
- Exact provider implementation
- Exact state API implementations

Those are implementation-level decisions and can be selected without changing the architectural principles in this document.

