# How to Add State Adapters

The Land Stack platform is designed to be a dynamically federated system. Instead of hardcoding API routes for every state in India, the system reads configurations from the `state_adapters` database tables.

You can view the Admin UI for this in the frontend at `http://localhost:5173/admin`. (Note: The UI currently displays mocked data for the hackathon prototype. To make it functional, you would need to build out the `AdminController` in the Spring Boot backend).

To configure a real State Adapter that the **Integration Engine** will actually use, you need to insert the configuration directly into your PostgreSQL database.

## 1. The Core Tables

The system relies on three tables to federate data:
1. `state_adapters`: Defines the base URL and authentication method for a state.
2. `adapter_endpoints`: Defines the specific paths for different data types (e.g., `PARCEL_GEOMETRY`, `RECORD_OF_RIGHTS`).
3. `adapter_field_mappings`: Maps the state's chaotic JSON keys into our Canonical Land Stack JSON keys.

## 2. Example: Adding "Kerala Land Records" (KL)

Connect to your `landstack_db` PostgreSQL database (using pgAdmin or `psql`) and run the following SQL scripts to configure Kerala's API.

### Step 1: Register the State
First, ensure the State exists in the base table.
```sql
INSERT INTO states (state_code, state_name) 
VALUES ('KL', 'Kerala') 
ON CONFLICT DO NOTHING;
```

### Step 2: Configure the Adapter
Configure the base URL and polling frequency (Cron expression).
```sql
INSERT INTO state_adapters (state_code, base_url, auth_type, status, sync_cron_expression) 
VALUES (
    'KL', 
    'https://mock-kerala-api.gov.in/v1', 
    'NONE', 
    'ACTIVE', 
    '0 0 * * *' -- Sync boundaries every day at midnight
);
```

### Step 3: Define the Endpoints
Tell the Integration Engine exactly which URL path handles which type of data.
```sql
-- Endpoint for fetching the Record of Rights (RoR)
INSERT INTO adapter_endpoints (state_code, endpoint_type, path_template, http_method)
VALUES (
    'KL', 
    'RECORD_OF_RIGHTS', 
    '/land/{ulpin}/ror', 
    'GET'
);

-- Endpoint for fetching Parcel Boundaries (Vector Sync)
INSERT INTO adapter_endpoints (state_code, endpoint_type, path_template, http_method)
VALUES (
    'KL', 
    'PARCEL_GEOMETRY', 
    '/spatial/parcels?bbox={bbox}', 
    'GET'
);
```

### Step 4: Configure Field Mappings (The Magic)
Kerala's API might return JSON that looks like `{"owner_name": "Aaronshenny", "land_area_sqm": 450}`. We need to map those to our canonical standard.

```sql
INSERT INTO adapter_field_mappings (state_code, canonical_field, state_json_path, data_type)
VALUES 
    ('KL', 'ownerName', '$.owner_name', 'STRING'),
    ('KL', 'area', '$.land_area_sqm', 'DECIMAL'),
    ('KL', 'landUseType', '$.usage_category', 'STRING');
```

## 3. How It Works in Action

Once these rows exist in the database:
1. You click a parcel in the Web Map (ULPIN: `KL-123456`).
2. The frontend calls `GET /api/v1/parcels/KL-123456/ror`.
3. The backend `ParcelController` looks up the `state_code` (`KL`).
4. The `IntegrationEngine` queries the `state_adapters` table for `KL`, builds the URL (`https://mock-kerala-api.gov.in/v1/land/KL-123456/ror`), and fetches the data.
5. The `DynamicFieldMapper` reads the `adapter_field_mappings` table to transform Kerala's JSON into the Canonical format.
6. The `PermlevelRedactionFilter` strips any fields the logged-in user isn't allowed to see.
7. The data is returned to the Map Viewer!
