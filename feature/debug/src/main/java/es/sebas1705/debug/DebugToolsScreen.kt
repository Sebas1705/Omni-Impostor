package es.sebas1705.debug

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.sebas1705.debug.design.DebugToolsDesign
import es.sebas1705.debug.models.DebugTableData
import es.sebas1705.debug.models.DebugToolsActions
import es.sebas1705.debug.models.DebugToolsViewData
import es.sebas1705.debug.models.MetricCardData
import es.sebas1705.debug.viewmodel.DebugMetricsTab
import es.sebas1705.debug.viewmodel.DebugToolsIntent
import es.sebas1705.debug.viewmodel.DebugToolsViewModel
import kotlinx.collections.immutable.toImmutableList
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.math.roundToInt
import es.sebas1705.core.resources.R as ResourceR

@Composable
@Suppress("LongMethod")
fun DebugToolsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    debugToolsViewModel: DebugToolsViewModel = hiltViewModel()
) {
    val uiState by debugToolsViewModel.uiState.collectAsStateWithLifecycle()
    val loading by debugToolsViewModel.loading.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        debugToolsViewModel.eventHandler(DebugToolsIntent.RefreshAll)
    }

    val tabTitles = listOf(
        stringResource(ResourceR.string.core_resources_debug_tab_data),
        stringResource(ResourceR.string.core_resources_debug_tab_quality),
        stringResource(ResourceR.string.core_resources_debug_tab_performance),
        stringResource(ResourceR.string.core_resources_debug_tab_device),
        stringResource(ResourceR.string.core_resources_debug_tab_history)
    ).toImmutableList()

    val selectedLanguageTag = uiState.selectedLanguage.uppercase()
    val metrics = when (uiState.selectedTab) {
        DebugMetricsTab.Data -> listOf(
            MetricCardData(
                stringResource(ResourceR.string.core_resources_debug_metric_categories_lang, "ES"),
                uiState.categoriesEs.toString()
            ),
            MetricCardData(
                stringResource(ResourceR.string.core_resources_debug_metric_categories_lang, "EN"),
                uiState.categoriesEn.toString()
            ),
            MetricCardData(
                stringResource(ResourceR.string.core_resources_debug_metric_words_lang, "ES"),
                uiState.totalWordsEs.toString()
            ),
            MetricCardData(
                stringResource(ResourceR.string.core_resources_debug_metric_coverage_lang, "ES"),
                "${uiState.categoryCoverageEs.roundToInt()}%"
            ),
            MetricCardData(
                stringResource(ResourceR.string.core_resources_debug_metric_words_lang, "EN"),
                uiState.totalWordsEn.toString()
            ),
            MetricCardData(
                stringResource(ResourceR.string.core_resources_debug_metric_coverage_lang, "EN"),
                "${uiState.categoryCoverageEn.roundToInt()}%"
            ),
            MetricCardData(
                stringResource(ResourceR.string.core_resources_debug_actual_metric_words_lang, selectedLanguageTag),
                uiState.selectedLanguageWords.toString()
            ),
            MetricCardData(
                stringResource(
                    ResourceR.string.core_resources_debug_actual_metric_coverage_lang,
                    selectedLanguageTag
                ),
                "${uiState.categoryCoverageSelectedLanguage.roundToInt()}%"
            )
        )

        DebugMetricsTab.Quality -> listOf(
            MetricCardData(
                stringResource(ResourceR.string.core_resources_debug_metric_avg_clues_lang, selectedLanguageTag),
                uiState.averageCluesInSelectedLanguage.roundToInt().toString()
            ),
            MetricCardData(
                stringResource(ResourceR.string.core_resources_debug_metric_duplicates_lang, selectedLanguageTag),
                uiState.duplicateWordsInSelectedLanguage.toString()
            ),
            MetricCardData(
                stringResource(ResourceR.string.core_resources_debug_metric_pure_duplicates_lang, selectedLanguageTag),
                uiState.pureDuplicateEntriesInSelectedLanguage.toString()
            ),
            MetricCardData(
                stringResource(ResourceR.string.core_resources_debug_metric_invalid_clues_lang, selectedLanguageTag),
                uiState.invalidClueEntriesInSelectedLanguage.toString()
            ),
            MetricCardData(
                stringResource(ResourceR.string.core_resources_debug_metric_missing_categories_lang, selectedLanguageTag),
                uiState.missingCategoriesInSelectedLanguage.toString()
            ),
            MetricCardData(
                stringResource(
                    ResourceR.string.core_resources_debug_metric_valid_clues_coverage_lang,
                    selectedLanguageTag
                ),
                "${uiState.validClueEntriesCoverageInSelectedLanguage.roundToInt()}%"
            )
        )

        DebugMetricsTab.Performance -> listOf(
            MetricCardData(
                stringResource(ResourceR.string.core_resources_debug_metric_refresh_duration),
                "${uiState.refreshDurationMs} ms"
            ),
            MetricCardData(
                stringResource(ResourceR.string.core_resources_debug_metric_reset_duration),
                "${uiState.resetDurationMs} ms"
            ),
            MetricCardData(
                stringResource(ResourceR.string.core_resources_debug_metric_uptime),
                "${(uiState.appUptimeMs / 1000L)} s"
            ),
            MetricCardData(
                stringResource(ResourceR.string.core_resources_debug_metric_heap_used),
                "${uiState.usedHeapMb} MB"
            ),
            MetricCardData(
                stringResource(ResourceR.string.core_resources_debug_metric_heap_max),
                "${uiState.maxHeapMb} MB"
            )
        )

        DebugMetricsTab.Device -> listOf(
            MetricCardData(
                stringResource(ResourceR.string.core_resources_debug_metric_device_manufacturer),
                uiState.manufacturer
            ),
            MetricCardData(
                stringResource(ResourceR.string.core_resources_debug_metric_device_model),
                uiState.model
            ),
            MetricCardData(
                stringResource(ResourceR.string.core_resources_debug_metric_device_android),
                "${uiState.androidVersion} (SDK ${uiState.sdkInt})"
            ),
            MetricCardData(
                stringResource(ResourceR.string.core_resources_debug_metric_device_total_ram),
                "${uiState.totalRamMb} MB"
            ),
            MetricCardData(
                stringResource(ResourceR.string.core_resources_debug_metric_device_available_ram),
                "${uiState.availableRamMb} MB"
            ),
            MetricCardData(
                stringResource(ResourceR.string.core_resources_debug_metric_device_low_ram),
                uiState.isLowRamDevice.toString()
            ),
            MetricCardData(
                stringResource(ResourceR.string.core_resources_debug_metric_device_locale),
                uiState.localeTag
            )
        )

        DebugMetricsTab.History -> emptyList()
    }.toImmutableList()

    val historyTable = if (uiState.selectedTab == DebugMetricsTab.History) {
        val formatter = SimpleDateFormat("HH:mm:ss", LocalConfiguration.current.locales[0])
        DebugTableData(
            headers = listOf(
                stringResource(ResourceR.string.core_resources_debug_table_time),
                stringResource(ResourceR.string.core_resources_debug_table_lang),
                stringResource(ResourceR.string.core_resources_debug_table_words),
                stringResource(ResourceR.string.core_resources_debug_table_dup),
                stringResource(ResourceR.string.core_resources_debug_table_pure_dup),
                stringResource(ResourceR.string.core_resources_debug_table_invalid),
                stringResource(ResourceR.string.core_resources_debug_table_missing)
            ).toImmutableList(),
            rows = uiState.snapshots.map { snapshot ->
                listOf(
                    formatter.format(Date(snapshot.createdAtEpochMs)),
                    snapshot.selectedLanguage.uppercase(),
                    snapshot.selectedLanguageWords.toString(),
                    snapshot.duplicateWords.toString(),
                    snapshot.pureDuplicates.toString(),
                    snapshot.invalidClueEntries.toString(),
                    snapshot.missingCategories.toString()
                ).toImmutableList()
            }.toImmutableList()
        )
    } else {
        null
    }

    DebugToolsDesign(
        modifier = modifier,
        data = DebugToolsViewData(
            tabs = tabTitles,
            selectedTabIndex = uiState.selectedTab.ordinal,
            metrics = metrics,
            table = historyTable,
            selectedLanguage = uiState.selectedLanguage,
            latestWord = uiState.latestWordInSelectedLanguage,
            topCategory = uiState.topCategoryInSelectedLanguage,
            errorMessage = uiState.errorMessage
        ),
        actions = DebugToolsActions(
            onBack = onBack,
            onSelectTab = { index ->
                val tabs = DebugMetricsTab.entries
                if (index in tabs.indices) {
                    debugToolsViewModel.eventHandler(DebugToolsIntent.SelectTab(tabs[index]))
                }
            },
            onRefresh = { debugToolsViewModel.eventHandler(DebugToolsIntent.RefreshAll) },
            onResetDefaultWords = {
                debugToolsViewModel.eventHandler(DebugToolsIntent.ResetDefaultWords)
            }
        ),
        loading = loading
    )
}
