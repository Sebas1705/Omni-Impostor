# Release Policy

## Versioning

Omni-Impostor uses semantic versioning: `vMAJOR.MINOR.PATCH`

| Increment | When |
|---|---|
| `MAJOR` | Breaking architectural changes or major feature overhauls |
| `MINOR` | Backward-compatible new features |
| `PATCH` | Bug fixes, CI changes, dependency updates, docs |

Current series: `v1.x.x`

## Branch Strategy

- `main` — stable, always tagged at release
- `develop` — integration; features land here before promoting to release
- `release/x.y.z` — short-lived release prep branch; merged into `main` + `develop`, then deleted
- `hotfix/*` — critical fixes branched from `main`

## Release Checklist

1. All CI checks green: `build + lint`, `detekt`, `unit tests + coverage`.
2. CHANGELOG.md updated with the new version section.
3. Version code and name bumped in `app/build.gradle.kts`.
4. No debug-only libraries leaking into release variant.
5. Signed AAB produced by the CI workflow (`bundleProRelease`).
6. Git tag pushed to `main` after merge.
7. AI context docs reviewed for any architectural or operational changes (`AGENTS.md`, `ARCHITECTURE.md`, `docs/cloud.md`, `docs/module-map.md`, `AI_INDEX.md`).

## CI/CD

- Workflow: `.github/workflows/build-release-apk.yml`
  - Trigger: manual dispatch with `release_tag` and `build_type` inputs
  - Builds signed AAB (`bundleProRelease`) and uploads to the GitHub release
- Dependency report: `.github/workflows/dependency-report-monthly.yml`
  - Run monthly; review output with `./gradlew dependencyUpdates --no-parallel`

## Secrets

Managed via Doppler (`omni-impostor` project, `prd` config for CI).
See `doppler.yaml` and `scripts/` for local setup.

## Cadence

- **Monthly:** dependency update review + tagged release if pending changes
- **On demand:** hotfix releases for critical bugs
