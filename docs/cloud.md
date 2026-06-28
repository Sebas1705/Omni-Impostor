# Cloud Context

Cloud and external service context for this template.

## Services in Use

- Firebase (analytics, crashlytics, auth, firestore, realtime database, storage, messaging)
- Open Trivia DB via Retrofit (`data:retrofit`)
- Couchbase Lite for local embedded data (not cloud-hosted by default)

## Environment Notes

- Android app expects local setup files (for example Firebase config) in developer environments.
- CI and local release flows are orchestrated from repository root workflows/scripts.

## Security and Secrets

- Never commit secrets to Git.
- Keep local secret files out of source control.
- Treat API keys and service credentials as runtime environment concerns.

## Operational Workflows

- Release: repository root `.github/workflows/release.yml`
- Dependency report: repository root `.github/workflows/dependency-report-monthly.yml`
- Security checks: repository root security workflows

## Risks and Controls

- Dependency drift: mitigated with monthly dependency report and Dependabot.
- Release-note quality drift: mitigated with changelog label gate.
- Debug tooling leakage into release: mitigated with variant-scoped dependencies.

