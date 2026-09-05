---
name: flyway-best-practices
description: Vital constraints for working with Flyway database migration scripts.
trigger: always_on
---

# Flyway Best Practices

- **Immutability:** Never modify a Flyway migration script (e.g., `V3__seed_data.sql`) that has already been executed against the database. 
- **Fixing Errors:** If a script fails during execution or needs updating, you must run a flyway repair or execute `DELETE FROM flyway_schema_history WHERE version = 'X'` before re-running it. Alternatively, create a new sequential `V{n+1}` script for the change.
