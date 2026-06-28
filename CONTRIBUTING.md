# Contributing Guide

## Scope

This repository contains the **Omni-Impostor** Android app and its CI/CD automation in `.github/`.

## Getting Started

1. Fork and clone the repository.
2. Set up secrets (requires Doppler CLI + `doppler login`):
   ```bash
   doppler setup          # auto-selects omni-impostor/dev_personal
   bash scripts/setup-secrets.sh   # or .\scripts\setup-secrets.ps1 on Windows
   ```
3. Create a branch from `develop` (features) or `main` (hotfixes).
4. Run checks locally:
   ```powershell
   .\gradlew.bat assembleDevDebug
   .\gradlew.bat detekt
   .\gradlew.bat coverageUnitTestAll
   ```

## Branch Strategy (GitFlow)

| Branch | Purpose |
|---|---|
| `main` | Production releases only, always tagged |
| `develop` | Integration branch — all features merge here first |
| `feature/*` | New features, branched from `develop` |
| `release/*` | Release prep, branched from `develop`, merged to `main` + `develop` |
| `hotfix/*` | Critical fixes, branched from `main`, merged to `main` + `develop` |

## Pull Request Rules

- Open PRs against `develop` (features) or `main` (hotfixes).
- Link the related issue when applicable.
- Keep changes scoped; explain migration impact for breaking changes.
- Update docs in the same PR when behavior changes.
- Apply at least one label: `bug`, `enhancement`, `documentation`, `ci`, `security`, or `breaking-change`.
- Ensure CI is green before requesting review.

## Commit Style

Short scoped messages:
- `feat(offlinegame): ...`
- `fix(auth): ...`
- `chore(ci): ...`
- `docs: ...`
- `build-logic: ...`

## Code of Conduct

Be respectful, constructive, and collaborative in issues and pull requests.
