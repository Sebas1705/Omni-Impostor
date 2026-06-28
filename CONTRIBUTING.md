# Contributing Guide
Thanks for helping improve this template.
## Scope
This repository contains the **Omni-Impostor** app and its repository-level automation in `.github/`.
## Getting Started
1. Fork and clone the repository.
2. Create a branch from `main`.
3. Run checks locally from `AndroidModApp`:
```powershell
cd AndroidModApp
.\gradlew.bat assembleDebug
.\gradlew.bat detekt
.\gradlew.bat coverageUnitTestAll
```
## Pull Request Rules
- Link the related issue or RFC when applicable.
- Keep changes scoped and explain migration impact.
- Update docs/templates when behavior changes.
- Apply at least one release/changelog label (`bug`, `enhancement`, `documentation`, `ci`, `security`, `breaking-change`) unless the PR should be excluded with `skip-changelog`.
- The repository root `.github/workflows/changelog-label-gate.yml` enforces this label requirement in CI.
- Ensure required checks are green before requesting merge.
## Commit Style
Use short scoped messages, for example:
- `chore(ci): ...`
- `docs: ...`
- `build-logic: ...`
- `feat(template): ...`
## Breaking Changes
For changes that affect consumers of the template:
1. Open an RFC in a pull request.
2. Include migration notes.
3. Mark deprecations one release before removal.
4. Add the `breaking-change` label so release notes and changelog generation place it correctly.
## Code of Conduct
Be respectful, constructive, and collaborative in issues and pull requests.
