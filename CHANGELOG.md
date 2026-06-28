# Changelog

All notable changes to Omni-Impostor are documented here.
Format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

## [1.1.3] - 2026-06-28

### Added
- CI/CD pipeline: GitHub Actions workflow produces signed AAB (`bundleProRelease`) for Play Store.
- Doppler integration: secrets centralised — single `DOPPLER_TOKEN` replaces 6 individual GitHub secrets.
- `scripts/setup-secrets.sh` and `scripts/setup-secrets.ps1` for one-command local secret bootstrap.
- `doppler.yaml` for automatic project/config selection after cloning.
- `gradle.properties` and `gradle-wrapper.properties` committed to repo.

### Fixed
- Google sign-in (Credential Manager): pass `Activity` context through all layers for Android ≤ 13.
- Google sign-in fallback: add `GetSignInWithGoogleOption` so sign-in works on all devices.
- Rotation: `showPassPhoneOverlay` uses `rememberSaveable` — no longer dismissed on screen rotation.
- Game setup: guard start button when `impostors ≥ players` to prevent instant-win state.
- Game setup: `showNumOfImpostors` copied from intent; Chaos mode impostor count capped at `maxImpostors`.

### Changed
- Project renamed from `ImpostorAndroidGame` to **Omni-Impostor** across all documentation and CI.

## [1.1.1] - 2026-05-20

### Fixed
- Word collections: separate EN/ES lists so the language setting is respected during gameplay.
- Music: pause background music when app goes to background.

## [1.0.0] - 2026-05-17

### Added
- Initial Play Store release.
- Offline multiplayer party game: secret word assignment, discussion phase, impostor voting.
- Game modes: Classic, Chaos, Timed.
- Firebase Auth (Google sign-in + anonymous), Firestore, Realtime Database.
- Ranking, profile, and settings screens.
- Material Design 3 with light/dark mode and three contrast levels.
- Multi-module Clean Architecture: `app / core / data / domain / feature`.
