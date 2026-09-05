# Walkthrough: Milestone 4 Completion

**Date:** 2026-09-04
**Milestones Covered:** Integration Engine & State Adapters (M4)

## 1. Dynamic API Client
We built the generic HTTP client responsible for communicating with external state systems.
*   **Component (`StateApiClient.java`):** A Spring `@Service` utilizing `RestTemplate` to make outbound HTTP calls. 
*   **How it Works:** It accepts a `StateAdapter` and an `AdapterEndpoint` as parameters. It dynamically constructs the target URL (e.g., `adapter.getBaseUrl() + endpoint.getPath()`) and automatically injects authentication headers if the adapter is configured to use an API Key. This ensures we never hardcode state-specific URLs or credentials in our Java code.

## 2. Dynamic Field Mapper
To normalize heterogeneous JSON structures into our standard format, we built a generic JSON mapping service.
*   **Component (`DynamicFieldMapper.java`):** Utilizing Jackson `ObjectMapper` and `JsonNode`.
*   **How it Works:** It takes the raw JSON string returned by a state API and a list of `AdapterFieldMapping` configuration records from the database. It transverses the raw JSON and copies the value of each `sourceField` into a new, canonical JSON object under the `canonicalField` name. 

## 3. Dynamic Sync Scheduler
We implemented the periodic polling mechanism to fetch parcel boundary updates without relying on state webhooks.
*   **Component (`DynamicSyncScheduler.java`):** A Spring `@Component` using the programmatic `TaskScheduler` instead of fixed `@Scheduled` annotations.
*   **How it Works:** It reads the `syncCronExpression` from the database for each active `StateAdapter` and schedules a generic Java `Runnable`. This allows each state to have a completely different sync frequency (e.g., daily vs. weekly) that can be changed on the fly in the Admin UI without restarting the application.

## 4. CRS Normalization Service
Because states use different surveying coordinate reference systems (e.g., Cassini-Soldner, UTM), we stubbed the transformation service.
*   **Component (`CrsNormalizationService.java`):** A spatial utility service.
*   **How it Works:** It takes an incoming JTS `Geometry` and its source EPSG code. If the code is not `EPSG:4326` (WGS84), it will eventually use the GeoTools library to apply a math transform, standardizing all parcel polygons before they are inserted into the central PostGIS database.

## Next Steps
With the core integration engines completed, the final step is **Milestone 5 (Core Services & Map APIs)**. This will involve exposing the REST controllers for the frontend Web GIS to fetch bounding boxes and query federated parcel data!
