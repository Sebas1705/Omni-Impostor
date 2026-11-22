package es.sebas1705.settings.viewmodel

import android.content.Context
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import es.sebas1705.common.mvi.MVIBaseViewModel
import es.sebas1705.common.utlis.extensions.types.logW
import es.sebas1705.common.theme.ThemeContrast
import es.sebas1705.models.AppLanguage
import es.sebas1705.models.ClueHelp
import es.sebas1705.models.DarkThemePreference
import es.sebas1705.models.SettingsModel
import es.sebas1705.settings.ReadSettingsUseCase
import es.sebas1705.settings.UpdateSettingsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import javax.inject.Inject

@HiltViewModel
class AppSettingsViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val readSettingsUseCase: ReadSettingsUseCase,
    private val updateSettingsUseCase: UpdateSettingsUseCase,
) : MVIBaseViewModel<AppSettingsUiState, AppSettingsIntent>(context) {

    override fun initState(): AppSettingsUiState = AppSettingsUiState()

    override fun intentHandler(intent: AppSettingsIntent): Job =
        when (intent) {
            AppSettingsIntent.ObserveSettings -> observeSettings()
            is AppSettingsIntent.UpdateMusicVolume -> persistSettings { it.copy(musicVolume = intent.value) }
            is AppSettingsIntent.UpdateSoundVolume -> persistSettings { it.copy(soundVolume = intent.value) }
            is AppSettingsIntent.UpdateContrast -> persistSettings { it.copy(appContrast = intent.contrast) }
            is AppSettingsIntent.UpdateCompactTables -> persistSettings {
                it.copy(forceCompactTables = intent.enabled)
            }
            is AppSettingsIntent.SetShowTutorialOnNextStart -> persistSettings { it.copy(firstTime = intent.show) }
            is AppSettingsIntent.UpdateLanguage -> persistSettings { it.copy(appLanguage = intent.language) }
            is AppSettingsIntent.UpdateDarkTheme -> persistSettings { it.copy(darkThemePreference = intent.pref) }
            is AppSettingsIntent.UpdateClueHelp -> persistSettings { it.copy(clueHelp = intent.help) }
            AppSettingsIntent.ResetDefaults -> persistSettings {
                SettingsModel(
                    firstTime = false,
                    musicVolume = 1f,
                    soundVolume = 1f,
                    appContrast = ThemeContrast.Low,
                    appLanguage = resolveInitialLanguage(),
                    forceCompactTables = false,
                    darkThemePreference = DarkThemePreference.System,
                    clueHelp = ClueHelp.Normal,
                )
            }
        }

    private fun observeSettings() = execute(Dispatchers.IO) {
        readSettingsUseCase().collect { settings ->
            updateUi { it.copy(settings = settings) }
        }
    }

    private fun persistSettings(
        transform: (SettingsModel) -> SettingsModel
    ) = execute(Dispatchers.IO) {
        val currentSettings = _uiState.value.settings
        if (currentSettings == null) {
            logW("persistSettings skipped: settings not yet loaded")
            return@execute
        }
        updateSettingsUseCase(transform(currentSettings))
    }

    private fun resolveInitialLanguage(): AppLanguage {
        val currentLanguage = context.resources.configuration.locales[0]?.language.orEmpty()
        return if (currentLanguage.equals(AppLanguage.Spanish.code, ignoreCase = true))
            AppLanguage.Spanish
        else AppLanguage.English
    }
}

