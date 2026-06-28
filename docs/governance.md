# Governance

Ownership, SLAs, and decision rules for Omni-Impostor.

## Roles

| Role | Responsibilities |
|---|---|
| Primary maintainer | Release owner, final approver for high-impact changes |
| Backup maintainer | Triage and merge backup when primary is unavailable |
| Contributors | Propose changes via PRs following `CONTRIBUTING.md` |

## Service Levels

- New issues triaged within **2 business days**.
- Pull requests acknowledged within **2 business days**.
- Critical security updates prioritized and merged once required checks pass.

## Merge Policy

- At least one maintainer approval required.
- Required checks before merge:
  - `Build + Lint`
  - `Detekt`
  - `Unit Tests + Coverage`
  - `Changelog Label Gate`
- `CODEOWNERS` must be updated when ownership boundaries change.

## Decision Model

| Scope | Process |
|---|---|
| Small, backward-compatible | Maintainers decide in PR discussion |
| Architectural changes | RFC PR with analysis; see `docs/template-evolution.md` |
| Breaking changes | Migration guidance required in release notes and `CHANGELOG.md` |

## Review Cadence

- **Weekly:** issue triage and priority review
- **Monthly:** release readiness — quality gates, changelog labels, dependency report
