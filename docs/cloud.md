# Cloud & External Services

Runtime services and secrets context for Omni-Impostor.

## Services

| Service | Module | Purpose |
|---|---|---|
| Firebase Auth | `data:authentication` | Google Sign-In + anonymous auth |
| Firebase Firestore | `data:firestore` | User profiles, online game state |
| Firebase Realtime Database | `data:realtime` | Low-latency game sync |
| Firebase Analytics | `data:analytics` | Event and user property tracking |
| Firebase Crashlytics | `data:analytics` | Crash reporting |
| Open Trivia DB | `data:retrofit` | Remote word/trivia question fetch |
| Couchbase Lite | `data:couchbase` | Local embedded DB (words, ranking, debug snapshots) |

## Secrets Management

Secrets are managed by **Doppler** (`omni-impostor` project):

| Config | Usage |
|---|---|
| `prd` | GitHub Actions CI (read via `DOPPLER_TOKEN` secret) |
| `dev_personal` | Local development (auto-selected via `doppler.yaml`) |

Required secrets: `SIGNING_KEYSTORE_PASSWORD`, `SIGNING_KEY_ALIAS`, `KEYSTORE_BASE64`, `GOOGLE_SERVICES_JSON`, `EXAMPLE_URL_DEV`, `EXAMPLE_URL_PRO`

Local bootstrap:
```bash
doppler setup
bash scripts/setup-secrets.sh    # or .\scripts\setup-secrets.ps1
```
This generates `app/secrets.properties`, `app/google-services.json`, and `app/keystore.jks`.

## Environment Notes

- `google-services.json` is gitignored; generated at build time from Doppler.
- CI uses a Doppler service token (`prd` config); local dev uses personal token (`dev_personal`).
- Debug builds use a stub `google-services.json` with placeholder Firebase credentials.

## Operational Workflows

| Workflow | File |
|---|---|
| Build & publish AAB | `.github/workflows/build-release-apk.yml` |
| Dependency report | `.github/workflows/dependency-report-monthly.yml` |
| Security scan | `.github/workflows/security.yml` |

## Risks and Controls

| Risk | Control |
|---|---|
| Dependency drift | Monthly dependency report + Dependabot |
| Secret leakage | Secrets in Doppler only; gitignore enforced |
| Debug tooling in release | Variant-scoped dependencies (`debugImplementation`) |
