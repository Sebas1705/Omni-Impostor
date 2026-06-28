# Module Standards

Standards for creating or refactoring modules in Omni-Impostor.

## Layer Intent

| Layer | Purpose |
|---|---|
| `core` | Shared building blocks — UI utils, design system, resources, common extensions |
| `data` | External/local data source implementations and repository adapters |
| `domain` | Business models, use cases, repository interfaces, service contracts |
| `feature` | UI flows, screens, ViewModels, and screen-level orchestration |

## Build Logic

- Apply the primary convention plugin for the layer (`buildlogic.android.core`, `buildlogic.android.data`, etc.)
- Keep module Gradle files minimal — declare dependencies only, no version literals
- All versions must be in `gradle/libs.versions.toml`
- Debug-only tooling must use `debugImplementation` / `releaseImplementation` with a no-op for release

## Naming and Package Conventions

- Lowercase module folder names with clear bounded-context names
- Package names aligned with module intent and layer
- Public APIs live in `domain`; never leak `data` implementation details into `feature`

## Testing Standards

- Unit tests required for business logic in `domain` and `data`
- Test names should be behavior-oriented (`given_when_then` style acceptable)
- Tests must be deterministic — no real network or clock without fakes/stubs
- Use shared test dependencies from `buildlogic.android.unit.test`
- At minimum: one smoke path (compile + detekt) per diagnostics-exposing module

## Static Analysis

- Detekt required in all modules via convention plugin
- Fix new findings in touched files before merging
- Baselines are for legacy findings only — shrink them over time

## CI Expectations

- `build + lint`, `detekt`, and `unit tests + coverage` must pass
- Attach report artifacts for failed jobs to speed up debugging

## Documentation Sync

When adding or changing modules:
- Update `docs/module-map.md`
- Update `settings.gradle.kts`
- Update `docs/ai/FLOW_CATALOG.md` if a new data flow is introduced
