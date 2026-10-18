# Flow Catalog

Reference list of implemented flows, where they start, and where data/state lives.

## Flow 1: Authentication Boot + Login

- Entry: `feature/main/.../MainViewModel.kt` and `feature/login/.../LoginViewModel.kt`
- Domain: `domain/usescases/authentication/...`
- Data:
  - `data/authentication/...`
  - `data/repositories/.../AuthenticationRepository.kt`

Replication notes:

1. Splash checks persisted/remote auth state through domain use cases.
2. Login screen triggers Google sign-in use case and updates UI state via MVI intents.
3. Avoid calling Firebase SDK directly from feature modules.

## Flow 2: Settings Read/Write + Localization

- Entry: `feature/settings/.../AppSettingsViewModel.kt`
- Domain: `domain/usescases/settings/...`
- Data:
  - `data/datastore/.../SettingsPreferencesDataSource.kt`
  - `data/repositories/.../AppAppSettingsRepository.kt`

Replication notes:

1. Keep first-run language bootstrap in datastore defaults.
2. Expose one `ReadSettingsUseCase` flow and small focused update use cases.
3. Apply locale in app shell (`feature/main/.../AppNav.kt`), not per-screen.
4. Keep table density override (`forceCompactTables`) in settings and provide it from app shell via CompositionLocal.

## Flow 3: Game Setup Persistence (Players/Categories/Mode)

- Entry: `feature/home/face/.../FaceViewModel.kt` + mode/categories dialogs
- Domain: `domain/usescases/game/...`
- Data:
  - `data/datastore/.../GamePreferencesDataSource.kt`
  - `data/repositories/.../GameSettingsRepository.kt`

Replication notes:

1. Keep setup state in datastore so dialogs and face screen stay synchronized.
2. Use one read use case plus focused update use cases to avoid large mutation APIs.
3. Keep enum/string mapping in mappers, not in feature layer.

## Flow 4: Opendb Trivia Fetch

- Entry: feature that requests trivia questions (if enabled)
- Domain: `domain/usescases/opendb/.../GetTriviaTenQuestionsUseCase.kt`
- Data:
  - `data/retrofit/.../OpendbApiDataSource.kt`
  - `data/repositories/.../OpendbRepository.kt`

Replication notes:

1. Keep DTO handling in data layer.
2. Domain use case should expose only the operation contract.
3. Debug/release networking differences stay in retrofit variant modules.

## Flow 5: Analytics Event + User Property Logging

- Entry: any ViewModel intent side effect
- Domain: `domain/usescases/analytics/...`
- Data:
  - `data/analytics/...`
  - `data/repositories/.../AnalyticsRepository.kt`

Replication notes:

1. Keep event names/properties centralized in analytics config.
2. Fire analytics from ViewModel side effects, not composables.
3. Avoid blocking UI work with analytics dispatch.

## Flow 6: Default Words Import (JSON assets -> Couchbase)

- Entry:
  - `feature/debug/...` manual trigger
  - startup paths via ensure/import use cases
- Domain:
  - `domain/usescases/game/.../ImportDefaultGameWordsUseCase.kt`
  - `domain/usescases/game/.../SearchGameWordsUseCase.kt`
- Data:
  - `data/files/.../WordFileDataSource.kt`
  - `data/couchbase/.../WordsEsCBDataSource.kt`
  - `data/couchbase/.../WordsEnCBDataSource.kt`
  - `data/repositories/.../WordRepository.kt`

Replication notes:

1. Import from `:data:files` JSON schema, not hardcoded in domain/feature.
2. Ensure import runs when DB is incomplete for expected asset content.
3. Keep stats query path lightweight for debug screen usage.

## Flow 7: Debug Diagnostics Console (Data/Quality/Performance/Device/History)

- Entry: `feature/debug/.../DebugToolsScreen.kt`
- ViewModel: `feature/debug/.../viewmodel/DebugToolsViewModel.kt`
- Domain:
  - `domain/usescases/game/.../words/GetWordsDbStatsUseCase.kt`
  - `domain/usescases/game/.../debug/GetDeviceDebugMetricsUseCase.kt`
  - `domain/usescases/game/.../debug/GetPerformanceDebugMetricsUseCase.kt`
  - `domain/usescases/game/.../debug/SaveDebugSnapshotUseCase.kt`
  - `domain/usescases/game/.../debug/ReadDebugSnapshotsUseCase.kt`
- Data:
  - `data/repositories/.../WordRepository.kt`
  - `data/repositories/.../DebugSnapshotRepository.kt`
  - `data/couchbase/.../DebugSnapshotDoc.kt`
  - `data/couchbase/.../DebugSnapshotCBDataSource.kt`

Replication notes:

1. Group metrics by tab to keep diagnostics scannable (data, quality, performance, device, history).
2. Record historical snapshots on each successful refresh for trend comparison.
3. Keep debug-only UI/actions in debug feature module.

## Flow 8: Language-aware Word Retrieval

- Entry: game runtime requests (`SearchGameWordsUseCase`)
- Domain:
  - `domain/usescases/game/.../SearchGameWordsUseCase.kt`
  - `domain/usescases/settings/.../ReadSettingsUseCase.kt`
- Data:
  - `data/repositories/.../IWordRepository.kt`
  - `data/couchbase/.../Words{Lang}CBDataSource.kt`

Replication notes:

1. Keep one Couchbase collection per language (`es`, `en`) for game word entries.
2. Resolve active language from settings preference before querying words.
3. Store clues as a fixed-size string list (5 items expected by JSON schema).
4. Keep category mapping resilient when converting persistence data into domain models.

## Flow 9: Offline Round Runtime (Reveal -> Discussion -> Result)

- Entry: `feature/home/face/nav/.../design/HomeDesign.kt`
- Runtime dialog: `feature/home/face/nav/.../offline/OfflineGameScreen.kt`
- ViewModel: `feature/home/face/nav/.../offline/viewmodel/OfflineGameViewModel.kt`
- Domain:
  - `domain/usescases/game/.../SearchGameWordsUseCase.kt`

Replication notes:

1. Start the offline round from Face setup only after players/categories are configured.
2. Resolve one random word from selected categories before reveal phase starts.
3. Keep per-player reveal state isolated from discussion/voting state.
4. End round when all impostors are voted out, impostors force win, tie condition is met, or impostors guess the word.

## Flow 10: Offline Ranking Collection

- Entry: `feature/home/ranking/.../RankingScreen.kt`
- ViewModel: `feature/home/ranking/.../viewmodel/RankingViewModel.kt`
- Runtime writer:
  - `feature/home/face/nav/.../offline/viewmodel/OfflineGameViewModel.kt`
- Domain:
  - `domain/usescases/ranking/.../ReadOfflineRankingUseCase.kt`
  - `domain/usescases/ranking/.../RecordOfflineMatchResultUseCase.kt`
- Data persistence:
  - `data/couchbase/.../OfflineRankingEntryDoc.kt`
  - `data/couchbase/.../OfflineRankingEntryCBDataSource.kt`
  - `data/repositories/.../IOfflineRankingRepository.kt`
  - `data/repositories/.../OfflineRankingRepository.kt`

Replication notes:

1. Persist offline ranking by player with independent counters for civilian/impostor wins plus streak metrics (`currentStreak`, `bestStreak`).
2. Update ranking counters only when a round reaches final result.
3. Reset streak for non-winner players when a round result is recorded (including tie rounds).
4. Render offline ranking as a table sorted by total wins.
5. Persist user-selected table sort (column + direction) per screen using ViewModel `SavedStateHandle`.
