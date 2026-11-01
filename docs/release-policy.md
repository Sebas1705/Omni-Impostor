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
2. User-facing changes listed in CHANGELOG.md under `Unreleased` (the release moves them under the new version).
3. No debug-only libraries leaking into release variant.
4. Git tag `vX.Y.Z` pushed on a commit of `main` (`git tag v1.4.0 && git push origin v1.4.0`, or `scripts/new-release.ps1 -Version 1.4.0 -Push`). There is no version bump: `versionName` comes from the tag and `versionCode` from `git rev-list --count HEAD` at the tag (175 at v1.3.0, well above 7, the last hand-set code on Play). The values in `app/build.gradle.kts` are only the defaults of local builds.
5. Signed AAB produced by `release.yml` (`bundleProRelease`) and attached to the GitHub Release; once `PLAY_TRACK` is set, also uploaded to Google Play ([play-release.md](play-release.md)).
6. AI context docs reviewed for any architectural or operational changes (`AGENTS.md`, `ARCHITECTURE.md`, `docs/cloud.md`, `docs/module-map.md`, `AI_INDEX.md`).

## CI/CD

- Workflow: `.github/workflows/release.yml`
  - Trigger: `v*` tag
  - Updates CHANGELOG.md, publishes the GitHub Release, builds the signed AAB and attaches it; uploads it to Google Play when `PLAY_TRACK` is set
- Workflow: `.github/workflows/play-promote.yml`
  - Trigger: manual dispatch; promotes between Play tracks and changes the rollout share
- Workflow: `.github/workflows/build-release-apk.yml`
  - Trigger: manual dispatch with `release_tag` and `build_type` inputs
  - Rebuilds the signed AAB of a tag (`bundleProRelease`) and uploads it to the GitHub release
- Dependency report: `.github/workflows/dependency-report-monthly.yml`
  - Run monthly; review output with `./gradlew dependencyUpdates --no-parallel`

## Secrets

Managed via Doppler (`omni-impostor` project, `prd` config for CI).
See `doppler.yaml` and `scripts/` for local setup.

## Cadence

- **Monthly:** dependency update review + tagged release if pending changes
- **On demand:** hotfix releases for critical bugs
