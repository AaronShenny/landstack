---
name: spring-boot-best-practices
description: Spring Boot best practices covering Cron scheduling and Bean definitions.
trigger: always_on
---

# Spring Boot Best Practices

- **Cron Expressions:** Always use **6-field** cron expressions (Second, Minute, Hour, Day of Month, Month, Day of Week) for `@Scheduled` or TaskSchedulers in Spring Boot. Never use 5-field UNIX expressions, which will cause an `IllegalArgumentException` and crash the application context.
- **Bean Definitions:** Always verify the full package path when creating a new class (e.g., `Controller` or `Service`) to avoid `ConflictingBeanDefinitionException` from identically named classes in similar packages.
