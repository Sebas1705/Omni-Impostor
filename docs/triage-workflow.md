# Triage Workflow

A lightweight triage loop to keep the backlog healthy and response times predictable.

## Label System

| Label | Meaning |
|---|---|
| `bug` | Broken or incorrect behavior in the app |
| `enhancement` | Improvement or new feature request |
| `documentation` | Docs-only updates |
| `ci` | CI/CD pipeline changes |
| `security` | Security-related fix |
| `good first issue` | Suitable for new contributors |
| `breaking-change` | Requires migration notes in release |
| `skip-changelog` | Excludes PR from generated release notes |
| `priority/critical` | Same-day maintainer review target |
| `priority/high` | Priority triage within 1 business day |
| `priority/normal` | Standard triage cadence |

Label definitions are versioned in `.github/labels.json` and synced via `.github/workflows/label-sync.yml`.

## Weekly Issue Triage

1. Confirm reproducibility and expected behavior.
2. Apply labels and priority.
3. Assign owner and target milestone.
4. Close stale duplicates with references.

## Pull Request Triage

1. Confirm scope and link to issue if applicable.
2. Ensure CI checks pass (`Build + Lint`, `Detekt`, `Unit Tests + Coverage`).
3. Verify at least one changelog label exists.
4. Request changes or approve within SLA.
5. Merge with squash and a clear commit title.

## Escalation Rules

- `priority/critical`: same-day maintainer review.
- Security items: fast-track path and coordinated release (see `SECURITY.md`).

## CI Notes

- `Security` uses CodeQL with `:app:assembleDebug` to avoid release-only task failures.
- First run after cache invalidation may be slower due to Gradle cache warm-up.
- If `Security` exceeds timeout, check whether time is spent in CodeQL extraction vs Gradle build.
- `Detekt` runs with Gradle configuration cache; disable `--configuration-cache` first if regressions appear.
- If repository Code Scanning is disabled, `Security` skips CodeQL upload and emits a warning.
