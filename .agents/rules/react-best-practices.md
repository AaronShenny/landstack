---
name: react-best-practices
description: Core best practices for React components, specifically covering React Query and MapLibre integrations.
trigger: always_on
---

# React Best Practices

- **React Query:** Always wrap the root application (e.g., in `main.tsx` or `App.tsx`) with `<QueryClientProvider>` before using `useQuery` anywhere in the component tree to avoid fatal White Screen of Death (WSOD) errors.
- **MapLibre Integration:** When using `react-map-gl` with the open-source `maplibre-gl` library, ALWAYS import components from `react-map-gl/maplibre` (not `react-map-gl`) to prevent `mapbox-gl` dependency resolution errors during build or runtime.
