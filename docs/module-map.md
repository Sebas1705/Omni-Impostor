# Module Map

Quick module inventory grouped by layer. Source of truth: `settings.gradle.kts`.

## App

| Module | Purpose |
|---|---|
| `:app` | Application entry point, top-level Hilt wiring, nav host, flavor config |

## Core

| Module | Purpose |
|---|---|
| `:core:common` | Extensions, MVI base, logging utils, shared primitives |
| `:core:designsystem` | Theme, color palette, typography (Finger Paint / Gupter / Raleway), M3 tokens |
| `:core:resources` | String, drawable, and raw asset resources |
| `:core:ui` | Reusable composables and UI components shared across features |

## Data

| Module | Purpose |
|---|---|
| `:data:analytics` | Firebase Analytics + Crashlytics data source |
| `:data:authentication` | Firebase Auth data source (Google Sign-In, anonymous, Credential Manager) |
| `:data:couchbase` | Couchbase Lite data sources: words (ES/EN), ranking, debug snapshots |
| `:data:datastore` | DataStore-backed settings and game preferences |
| `:data:files` | JSON asset reader for default word collections |
| `:data:firestore` | Firestore data source (user profiles, online game state) |
| `:data:realtime` | Firebase Realtime Database data source (low-latency game sync) |
| `:data:repositories` | Repository implementations and Hilt bindings for all domain interfaces |
| `:data:retrofit` | OkHttp/Retrofit client + Open Trivia DB API data source |
| `:data:room` | Room database (reserved for future local relational storage) |

## Domain

| Module | Purpose |
|---|---|
| `:domain:managers` | Cross-cutting app-level managers (music, permissions, etc.) |
| `:domain:mappers` | DTO ↔ domain model converters |
| `:domain:models` | Shared data models and enums |
| `:domain:services` | Service interfaces consumed by features |
| `:domain:usecases:analytics` | Analytics event + user property use cases |
| `:domain:usecases:authentication` | Auth use cases (sign-in, sign-out, session, profile) |
| `:domain:usecases:game` | Game word search, import, ranking, debug metrics |
| `:domain:usecases:ranking` | Offline ranking read/write use cases |
| `:domain:usecases:opendb` | Open Trivia DB fetch use cases |
| `:domain:usecases:settings` | Settings read/write use cases |

## Feature

| Module | Purpose |
|---|---|
| `:feature:splash` | Animated splash screen with auth state check |
| `:feature:login` | Login screen — Google Sign-In + anonymous |
| `:feature:main` | App shell, navigation host, global nav bar |
| `:feature:home:face` | Game setup: category picker, player manager, mode selector |
| `:feature:home:face:nav` | Offline game runtime: Reveal → Discussion → Result screens |
| `:feature:home:face:categories` | Category selection dialog |
| `:feature:home:face:users` | Player management dialog |
| `:feature:home:ranking` | Ranking screen (offline + online placeholder) |
| `:feature:home:profile` | User profile screen |
| `:feature:offlinegame` | Offline game reveal card and discussion timer |
| `:feature:settings` | App settings screen (language, theme, contrast) |
| `:feature:debug` | Debug tools screen (data inspector, metrics, snapshots) — dev variant only |

## Notes

- When adding or removing modules: update this file **and** `settings.gradle.kts`.
- `usescases` is the actual package name in source (legacy typo preserved for now).
