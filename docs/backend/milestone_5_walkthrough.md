# Walkthrough: Milestone 5 Completion

**Date:** 2026-09-04
**Milestones Covered:** Core Services & Map APIs (M5)

## 1. Parcel Spatial Queries
We enabled the map frontend to fetch vector boundaries directly from PostGIS.
*   **Repository (`ParcelRepository.java`):** We added a native PostGIS/Hibernate Spatial query: `@Query("SELECT p FROM Parcel p WHERE within(p.geom, :bbox) = true")`. This performs a highly optimized bounding box intersection search.
*   **Service (`ParcelService.java`):** Implemented `getParcelsInBoundingBox` which dynamically constructs a JTS `Polygon` from the provided min/max lat/lon coordinates.
*   **Controller (`ParcelController.java`):** Exposed `GET /api/v1/parcels?minLon=...&minLat=...&maxLon=...&maxLat=...` to serve GeoJSON/Entity data to the web map.

## 2. Federated Governance APIs (The Core Engine)
We implemented the primary mechanism of the Land Stack—fetching live data without centralized storage.
*   **The Endpoint:** `GET /api/v1/parcels/{ulpin}/ror`
*   **How it Works:** 
    1. The API receives a ULPIN and queries our local `ParcelRepository` to find the parcel.
    2. It determines the `state_code` and the `local_parcel_id`.
    3. It fetches the `StateAdapter` and the `ROR` capability `AdapterEndpoint` for that state.
    4. It invokes the `StateApiClient`, passing the `local_parcel_id`, to fetch the live Record of Rights data from the state's government server.
    5. The raw JSON is intercepted by the `DynamicFieldMapper`, transformed into the Canonical JSON format, and returned to the client.

## 3. End-to-End Security Interception
*   Because we built the **Permlevel Redaction Filter** in Milestone 3, any canonical JSON returned by `ParcelController` is automatically intercepted before reaching the user. If the JSON contains sensitive owner data, and the user's role lacks the necessary clearance, those fields are stripped out implicitly.

## Conclusion
With Milestone 5 complete, the backend for **SIH26014 Land Stack** has reached full functional completeness for the prototype phase. The system architecture is completely built out as a Modular Monolith leveraging Spring Boot, PostGIS, GeoTools, and Frappe-inspired Security!
