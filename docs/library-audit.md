# Library Audit

Audit of `gradle/libs.versions.toml` and current module usage.

## Current Strengths

- Solid Android architecture baseline: Hilt, Retrofit/OkHttp, Room, DataStore, Firebase, Couchbase Lite.
- Quality tooling already in place: Detekt, JaCoCo, unit test libraries.
- Centralized version catalog (`gradle/libs.versions.toml`) with modularized build logic.

## Implemented Additions

### Leak Detection (debug builds)
- Library: `com.squareup.leakcanary:leakcanary-android`
- Location: `app` as `debugImplementation`
- Why: catches lifecycle and context leaks early

### Network Inspector (debug builds)
- Library: `com.github.chuckerteam.chucker:library`
- Location: `buildlogic.android.retrofit` — `library-no-op` stub in release
- Why: inspect REST traffic directly on device

### Dependency Update Task
- Plugin: `benmanes-versions` (declared at root)
- Run: `./gradlew dependencyUpdates --no-parallel`
- Workflow: `.github/workflows/dependency-report-monthly.yml`
- Why: monthly report of outdated dependencies

## Couchbase Runtime Inspection

Couchbase Lite on Android does not expose plain SQLite files. Options:

1. **In-app debug inspector** (`feature:debug`) — refresh and display all documents on screen
2. **Sync to backend** — replicate to Sync Gateway / Capella and inspect from server tooling
3. **Export snapshot** — debug-only action to dump documents as JSON for QA sessions

## Monthly Review Policy

1. Run `./gradlew dependencyUpdates --no-parallel` and review the report artifact.
2. Prioritize security and stable updates first.
3. Track alpha/beta upgrades separately unless needed for platform compatibility.
4. Open issues for high-value updates and link to the monthly milestone.
