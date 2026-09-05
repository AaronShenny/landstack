---
name: hibernate-best-practices
description: Important guidelines for Hibernate ORM mappings to prevent startup crashes.
trigger: always_on
---

# Hibernate Best Practices

- **Global Definitions:** Never duplicate `@FilterDef` annotations across multiple entities. It must be defined exactly once globally per persistence unit, even if the corresponding `@Filter` is applied to many different entities.
- **Type Mapping Validation:** Ensure Java types map exactly to the underlying Flyway SQL types. For example, explicitly annotate `Double` fields with `@Column(columnDefinition = "numeric(...)")` if the PostgreSQL schema uses `numeric`. Otherwise, Hibernate schema validation will fail and halt Spring Boot.
