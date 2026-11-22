# CI/CD Reference

## Workflows

| Workflow | Trigger | Purpose |
|---|---|---|
| `validate.yml` | Push/PR to `main`, `develop` | Build + lint, Detekt, unit tests + coverage (heavy jobs may run on the PC, see [Runner choice](#runner-choice-pc-or-github-hosted)) |
| `security.yml` | Push to `main`, PR, weekly | Dependency review, CodeQL analysis |
| `build-release-apk.yml` | Manual dispatch | Rebuild the signed AAB of a tag (`bundleProRelease`) or a dev APK |
| `release.yml` | Tag push `v*`, manual dispatch | Update CHANGELOG, publish GitHub Release, build and attach the signed AAB, upload it to the `PLAY_TRACK` Play track when that variable is set |
| `play-promote.yml` | Manual dispatch | Promote between Play tracks and change the rollout share |
| `dependency-report-monthly.yml` | 1st of month, manual | Generate outdated-dependency report |
| `changelog-label-gate.yml` | PR to `main` | Block merge if no changelog label is set |
| `label-sync.yml` | Push to `.github/labels.json` | Sync labels from file to repository |

## Runner choice (PC or GitHub-hosted)

The heavy Linux jobs can run on the owner's PC, a Docker self-hosted runner (labels `self-hosted, Linux, X64, pc, android`, image and watcher in `Sebas1705/ci-runner`). Each workflow starts with a small `pick-runner` job on `ubuntu-latest` that chooses the runner of those jobs:

- A watcher writes the repository variable `PC_RUNNER_HEARTBEAT` (epoch seconds, UTC) every minute, only while the PC is free and idle. When the heartbeat is under 180 seconds old the job gets `["self-hosted","pc"]`; otherwise (missing, stale or in the future) it gets `"ubuntu-latest"`. Nothing to configure: the fallback to GitHub-hosted runners is automatic.
- Only `push` (tag pushes included) and `workflow_dispatch` events may use the PC. Pull requests and every other event (`pull_request_target`, `schedule`, `workflow_run`, `issue_comment`, ...) always stay on `ubuntu-latest`, so the PC never runs untrusted fork code.
- Jobs that move: `build_lint`, `detekt` and `unit_tests` in `validate.yml`, the build in `distribute.yml`, `build_aab` in `release.yml` and `build_aab` in `build-release-apk.yml`. Everything else (release notes check, path filter, GitHub Release publishing, Play upload and promote, CodeQL, dependency review, labels, monthly report) stays on GitHub-hosted runners.
- Steps that only make sense on a GitHub-hosted image are guarded with `if: runner.environment == 'github-hosted'`; steps that only the PC image needs (Node.js for `npx firebase-tools`, python3 for the coverage summary, removing the signing files from the persistent workspace) are guarded with `'self-hosted'`. The PC image brings JDK 21 and the Android command-line tools (`ANDROID_HOME=/opt/android-sdk`); it has no Docker socket, so a job that needs Docker or service containers must not move.

## Composite Action — `android-ci-setup`

**Path:** `.github/actions/android-ci-setup/action.yml`

Shared setup used by `validate.yml` (3 jobs) and `dependency-report-monthly.yml`. Runs five steps in order:

1. Validate Gradle wrapper checksum (`gradle/actions/wrapper-validation@v4`)
2. Restore/save Gradle build cache (`gradle/actions/setup-gradle@v4`)
3. Install JDK 21 Temurin (`actions/setup-java@v4`)
4. Write `app/secrets.properties` stub (fake signing values + example URLs)
5. `chmod +x gradlew`

The secrets stub lets CI compile without real credentials. It is intentionally not used in `build-release-apk.yml` (which fetches real secrets from Doppler) or `security.yml` (which needs `Initialize CodeQL` inserted between JDK setup and the build step).

**When to update this action:** whenever the JDK version changes, `setup-gradle` or `setup-java` action versions are bumped, or a new required property is added to `secrets.properties`.

## Play Store deployment

After the GitHub Release, the `publish_play` job of `release.yml` uploads the AAB, the R8 mapping and the release notes (`scripts/release-notes.mjs`) to the track in the repository variable `PLAY_TRACK`, as a `draft` unless `PLAY_RELEASE_STATUS` says otherwise. While `PLAY_TRACK` is unset the job is skipped. `play-promote.yml` takes it from there.

Setup and day-to-day use: [play-release.md](play-release.md).

## Secrets

One GitHub Actions secret is required: `DOPPLER_TOKEN`. All other secrets live in Doppler (`prd` config):

| Doppler key | Used by | Description |
|---|---|---|
| `SIGNING_KEY_ALIAS` | `build-release-apk.yml`, `release.yml`, `distribute.yml` | Keystore key alias |
| `SIGNING_KEYSTORE_PASSWORD` | `build-release-apk.yml`, `release.yml`, `distribute.yml` | Keystore + key password |
| `KEYSTORE_BASE64` | `build-release-apk.yml`, `release.yml`, `distribute.yml` | Base64-encoded `.jks` file |
| `GOOGLE_SERVICES_JSON` | `build-release-apk.yml`, `release.yml`, `distribute.yml` | Firebase config JSON |
| `EXAMPLE_URL_DEV` / `EXAMPLE_URL_PRO` | All build workflows | Backend base URLs |
| `PLAY_SERVICE_ACCOUNT_JSON` | `release.yml`, `play-promote.yml` | Google Play API service account key (plain JSON) |

## Local validation

```bash
# Stub secrets (no Doppler needed):
mkdir -p app
echo "signing_keystore_password=fake" > app/secrets.properties
echo "signing_key_alias=fake" >> app/secrets.properties
echo "EXAMPLE_URL_DEV=https://dev.example.com" >> app/secrets.properties
echo "EXAMPLE_URL_PRO=https://pro.example.com" >> app/secrets.properties

# Run the same checks as CI:
./gradlew assembleDevDebug lintDebug detekt coverageUnitTestAll
```

For a real signed build, use `scripts/setup-secrets.sh` (Linux/Mac) or `scripts/setup-secrets.ps1` (Windows) after `doppler setup`.
