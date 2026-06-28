# Pattern Recipes

Copy-ready architecture recipes based on existing code.

## Recipe A: New MVI Screen with Existing Domain Use Case

1. Create/extend ViewModel in `feature/<module>/...` using `MVIBaseViewModel`.
2. Inject use case from `domain`.
3. Expose immutable `uiState` with state + intent pair.
4. Keep IO work inside `execute(Dispatchers.IO)`.
5. Route events through intents only.

Reference: `feature/home/face/.../FaceViewModel.kt`

## Recipe B: New MVI Screen Intent Handling

1. Extend `MVIBaseViewModel<State, Intent>`.
2. Implement `initState()`.
3. Route incoming intents in `intentHandler(...)`.
4. Update state with `updateUi { ... }`.

Reference: `feature/main/.../MainViewModel.kt`

## Recipe C: Add New Domain Use Case + Repository Wiring

1. Add use case under `domain/usescases/<scope>/`.
2. Consume repository interface (not implementation).
3. Add binding in `data/repositories/.../RepositoriesDataModule.kt` if needed.
4. Implement repository in `data/repositories/.../repos/`.
5. Delegate storage/network to a data source module.

References:

- `domain/usescases/settings/.../ReadSettingsUseCase.kt`
- `data/repositories/.../repos/WordRepository.kt`
- `data/repositories/.../RepositoriesDataModule.kt`

## Recipe D: Debug-Only Network Inspection

1. Keep runtime client wiring in `data:retrofit`.
2. Put diagnostic interceptors in `src/debug` modules.
3. Use release-safe fallback in `src/release`.
4. Add dependencies through version catalog + convention plugin.

References:

- `data/retrofit/src/debug/.../NetworkClientModule.kt`
- `data/retrofit/src/release/.../NetworkClientModule.kt`
- `build-logic/convention/src/main/kotlin/libraries/RetrofitConventionPlugin.kt`

