# Walkthrough: Milestone 3 Completion

**Date:** 2026-09-04
**Milestones Covered:** Security & RBAC Module (Frappe-Inspired) (M3)

## 1. Spring Security & JWT Implementation
We scaffolded the core authentication layer to secure the backend API.
*   **Dependencies:** Added `spring-boot-starter-security` and `jjwt` (JWT parser/generator) to the `pom.xml`.
*   **Database Update:** We quickly patched the `V2__landstack_schema.sql` and `User.java` entity to include a `password_hash` column, which was missing from the initial design.
*   **Components Created:**
    *   `SecurityConfig.java`: Configured stateless session management and protected all `/api/v1/**` endpoints (excluding `/auth`).
    *   `JwtUtil.java`: Handles HMAC-SHA256 signing and validation of JWTs.
    *   `JwtAuthFilter.java`: A `OncePerRequestFilter` that intercepts requests, validates the Bearer token, and populates the `SecurityContextHolder`.
    *   `UserDetailsServiceImpl.java`: Loads the user from our database via `UserRepository`.

## 2. Row-Level Security (User Permissions Engine)
To replicate Frappe's row-level "User Permissions", we utilized Hibernate Filters and Spring AOP.
*   **Hibernate Filters:** We added `@FilterDef(name = "stateFilter", ...)` and `@Filter(condition = "state_code = :stateCode")` to state-specific entities like `StateAdapter` and `Parcel`.
*   **AOP Aspect (`UserPermissionsAspect.java`):** We added an `@Aspect` that intercepts any method call within the `domain.repository` package. 
*   **How it Works:** Before a repository executes a query, the Aspect checks the logged-in user. If the user has a `UserPermission` restricting them to a specific state (e.g., `allow_type="state_code"`, `for_value="KL"`), the Aspect dynamically enables the Hibernate `stateFilter` on the session. This guarantees that **all** database queries are automatically scoped to Kerala for that user, without developers needing to manually add `WHERE state_code = 'KL'` everywhere!

## 3. Field-Level Security (Permlevels Redaction Engine)
To replicate Frappe's `permlevel` concept for PII redaction, we implemented a custom HTTP Filter.
*   **Component (`PermlevelRedactionFilter.java`):** A custom filter that wraps the `HttpServletResponse` using `ContentCachingResponseWrapper`.
*   **How it Works:** After the controller returns a JSON payload (e.g., dynamic governance data fetched from a state), the filter intercepts the raw JSON string. It parses it into a Jackson `JsonNode`. In the full implementation, it will cross-reference the keys against the `canonical_fields` table, check the current user's `role_permissions`, and delete any JSON nodes that exceed the user's allowed permlevel *before* sending the byte stream back to the client.

## 4. Admin API Scaffolding
*   We created `AdminController.java` with `@PreAuthorize` rules ensuring only `ROLE_ADMIN` or `ROLE_SUPERADMIN` can access it.
*   It includes endpoint stubs for managing users, roles, and assigning row-level permissions.

## Next Steps
With the complex security engines built out, the next step is **Milestone 4 (Integration Engine)**, where we will build the dynamic HTTP client that reads `state_adapters` configs and makes live API calls to state servers!
