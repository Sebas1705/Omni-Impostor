# Cloud & External Services

Runtime services and secrets context for Omni-Impostor.

## Services

| Service | Module | Purpose |
|---|---|---|
| Firebase Auth | `data:authentication` | Google Sign-In + email/password + anonymous auth |
| Firebase Firestore | `data:firestore` | Post-game clue ratings (`clue_ratings`) |
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

## Account deletion

Profile > Delete account deletes the Firebase Auth user (`DeleteCurrentUserUseCase`). Firebase rejects it unless the sign-in is
recent, so the view model asks for the password or the Google account and retries once.

There is no per-user server data to delete: `clue_ratings` holds anonymous counters with no uid, Analytics and Crashlytics never
get a user id, and everything else is local to the device. **If a per-user collection is ever added, delete its documents in
`DeleteCurrentUserUseCase` before the account, and update `docs/account-deletion.html` and the privacy policy.**

Requests from people without the app arrive by email: delete the user by hand in Firebase Console > Authentication.

The public page is `docs/account-deletion.html`, served by GitHub Pages from `main:/docs`
(<https://sebas1705.github.io/Omni-Impostor/account-deletion.html>). Play Console declares that URL in App content > Data safety.

Cancel on the email verification panel only signs out, so unverified accounts stay in Firebase Auth. Signing in again resumes
the verification.

## Firestore rules

`firestore.rules` only allows the exact writes the app makes to `clue_ratings` (three short strings and one counter that grows by 1);
everything else is denied. Test them against the local emulator before deploying:

```bash
cd scripts/firestore-rules && npm install && npm test   # needs the Firebase CLI and a JDK
```

Rules are deployed manually (`firebase deploy --only firestore:rules`); nothing in CI deploys them.
