# Architecture

High-level architecture snapshot for Omni-Impostor.

## Layer Model

- `app`: Android application entry point and top-level wiring.
- `core`: shared UI, common utilities, resources, and design system.
- `data`: data source implementations and repository adapters.
- `domain`: models, mappers, services, and use cases.
- `feature`: screen-level flows and navigation.

## Dependency Direction

- `feature` depends on `domain` and selected `core` modules.
- `domain` depends on abstractions and shared models; avoids UI details.
- `data` implements repository interfaces and data-source details.
- `app` composes features and platform-specific runtime setup.

## Cross-Cutting Concerns

- Dependency injection: Hilt modules by feature/data boundary.
- Static analysis: Detekt shared config in `config/detekt/detekt.yml`.
- Build conventions: custom convention plugins in `build-logic/`.
- Testing and coverage: module-level tests plus `coverageUnitTestAll` aggregation.

## Debug Diagnostics

- LeakCanary in app debug variant.
- Chucker in retrofit debug variant.
- In-app debug diagnostics in `feature:debug`, launched from `feature:main` navigation.
- Full-screen app settings dialog in `feature:settings`, available globally via `feature:main`.

## Module Inventory

See `docs/module-map.md` for the full module list grouped by layer.

## Detailed Replication Guides

- Guardrails: `docs/ai/ARCHITECTURE_GUARDRAILS.md`
- Existing flows: `docs/ai/FLOW_CATALOG.md`
- Implementation recipes: `docs/ai/PATTERN_RECIPES.md`

