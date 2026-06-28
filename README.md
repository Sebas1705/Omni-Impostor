# Omni-Impostor

A **digital board game for groups** built with Kotlin and Jetpack Compose. Players are secretly assigned a word — most players share the same word, but one or more are *impostors* assigned a different one. The group debates, votes, and tries to identify the impostor before time runs out.

Built on a **multi-module Clean Architecture** with Firebase, Hilt, and Material Design 3.

---

## Features

- **Offline & online game modes** — play locally or with synced real-time data
- **Animated reveal card** — 3D flip animation reveals each player's secret word privately
- **Word categories** — powered by [OpenTriviaDB](https://opentdb.com/) + a local word bank
- **Ranking system** — persistent leaderboard across sessions
- **Google Sign-In + Firebase Auth** — identity management with profile screen
- **Material Design 3** — dynamic light/dark theme with 3 contrast levels (Low / Medium / High)
- **Custom typography** — Finger Paint (titles) · Gupter (body) · Raleway (display)

---

## Tech Stack

| Layer | Technologies |
|---|---|
| UI | Jetpack Compose · Material 3 · Navigation Compose |
| DI | Hilt |
| Data | Firebase Auth · Firestore · Realtime Database · Room · Couchbase · Retrofit · DataStore |
| Analytics | Firebase Analytics |
| Architecture | Multi-module Clean Architecture (app / core / data / domain / feature) |
| Quality | Detekt · LeakCanary · Chucker · CI (GitHub Actions) |

---

## Module Structure

```
app/
core/        ← common · designsystem · resources · ui
data/        ← authentication · couchbase · datastore · files · firestore · realtime · repositories · retrofit · room
domain/      ← managers · mappers · models · services · usecases (analytics · auth · game · ranking · opendb · settings)
feature/     ← splash · login · main · home (face · ranking · profile) · offlinegame · settings · debug
```

---

## Getting Started

### Prerequisites
- Android Studio Hedgehog or newer
- JDK 17+
- A Firebase project with **Authentication** (Google Sign-In) and **Firestore** enabled

### Setup

1. Clone the repo:
   ```bash
   git clone https://github.com/Sebas1705/Omni-Impostor.git
   cd Omni-Impostor
   ```

2. Add your `google-services.json` to the `app/` directory.

3. Build and run:
   ```powershell
   .\gradlew.bat assembleDebug
   ```

### Quality commands

```powershell
.\gradlew.bat lintDebug
.\gradlew.bat detekt
.\gradlew.bat coverageUnitTestAll
```

---

## Game Flow

```
Splash → Login → Home Dashboard
                      ├── Face (setup: categories + players + game mode)
                      │       └── Reveal → Discussion (timer) → Results
                      ├── Ranking
                      ├── Profile
                      └── Settings
```

---

## License

MIT
