# Architecture Evolution

Process for meaningful architectural changes so they land safely.

## When to Open an RFC

Open a lightweight RFC pull request when changing:

- Convention plugin behavior in `build-logic/`.
- Dependency version strategy in `gradle/libs.versions.toml`.
- Module boundaries or layer contracts.
- Quality gates that could break existing CI.

## RFC Format

1. Goal and scope.
2. Impacted modules or plugins.
3. Backward-compatibility assessment.
4. Rollout and rollback steps.
5. Validation evidence (CI jobs + local command output).
6. Documentation sync plan (`README.md`, `docs/*`, AI context docs when applicable).

## Deprecation Policy

- Announce deprecations one release before removal.
- Include replacement guidance in the PR.
- Remove only after the next release unless a security fix requires earlier action.

## Required Documentation Sync

| What changed | Docs to update |
|---|---|
| Module boundaries | `docs/module-map.md`, `ARCHITECTURE.md` |
| Operational workflows or secrets | `AGENTS.md`, `AI_INDEX.md`, `docs/cloud.md` |
| Build logic or Gradle config | `build-logic/BUILD_LOGIC_BEST_PRACTICES.md` |
| Release process | `docs/release-policy.md`, `CHANGELOG.md` |
