package es.sebas1705.home.ranking.viewmodel

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import es.sebas1705.common.mvi.MVIBaseViewModel
import es.sebas1705.home.ranking.models.OfflineRankingRowUi
import es.sebas1705.ranking.ReadOfflineRankingUseCase
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class RankingViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val readOfflineRankingUseCase: ReadOfflineRankingUseCase,
    private val savedStateHandle: SavedStateHandle
) : MVIBaseViewModel<RankingState, RankingIntent>(context) {

    override fun initState(): RankingState = RankingState(
        offlineSort = readSavedRankingSort(savedStateHandle)
    )

    override fun onInit() {
        super.onInit()
        startLoading()
    }

    override fun intentHandler(intent: RankingIntent) =
        when (intent) {
            RankingIntent.Load -> loadOfflineRanking()
            is RankingIntent.ToggleOfflineSort -> toggleOfflineSort(intent.column)
        }

    private fun loadOfflineRanking() = execute(Dispatchers.IO) {
        startLoading()
        updateUi { it.copy(errorMessage = null) }

        runCatching { readOfflineRankingUseCase() }
            .onSuccess { entries ->
                val currentSort = uiState.value.offlineSort
                stopLoading()
                updateUi {
                    it.copy(
                        offlineRows = sortOfflineRows(
                            rows = entries.mapIndexed { index, entry ->
                            OfflineRankingRowUi(
                                position = index,
                                playerName = entry.playerName,
                                civilianWins = entry.civilianWins,
                                impostorWins = entry.impostorWins,
                                totalWins = entry.totalWins
                            )
                        },
                            sort = currentSort
                        )
                    )
                }
            }
            .onFailure { throwable ->
                stopLoading()
                updateUi { it.copy(errorMessage = throwable.message) }
            }
    }

    private fun toggleOfflineSort(column: RankingOfflineSortColumn) = execute {
        updateUi { state ->
            val nextSort = nextSort(state.offlineSort, column)
            persistRankingSort(savedStateHandle, nextSort)
            state.copy(
                offlineSort = nextSort,
                offlineRows = sortOfflineRows(state.offlineRows, nextSort)
            )
        }
    }

    private fun nextSort(
        current: RankingOfflineSort,
        column: RankingOfflineSortColumn
    ): RankingOfflineSort {
        if (current.column != column) {
            return RankingOfflineSort(
                column = column,
                direction = if (column == RankingOfflineSortColumn.Player) {
                    RankingSortDirection.Ascending
                } else {
                    RankingSortDirection.Descending
                }
            )
        }

        val nextDirection = if (current.direction == RankingSortDirection.Ascending) {
            RankingSortDirection.Descending
        } else {
            RankingSortDirection.Ascending
        }
        return current.copy(direction = nextDirection)
    }

    private fun sortOfflineRows(rows: List<OfflineRankingRowUi>, sort: RankingOfflineSort) = rows
        .sortedWith(compareByDescending<OfflineRankingRowUi> { sortableValue(it, sort.column) }
            .thenBy { it.playerName.lowercase() })
        .let { sortedRows ->
            if (sort.direction == RankingSortDirection.Descending) {
                sortedRows
            } else {
                sortedRows.reversed()
            }
        }
        .mapIndexed { index, row -> row.copy(position = index + 1) }
        .toImmutableList()

    private fun sortableValue(
        row: OfflineRankingRowUi,
        column: RankingOfflineSortColumn
    ): Comparable<*> = when (column) {
        RankingOfflineSortColumn.Position -> row.position
        RankingOfflineSortColumn.Player -> row.playerName.lowercase()
        RankingOfflineSortColumn.CivilianWins -> row.civilianWins
        RankingOfflineSortColumn.ImpostorWins -> row.impostorWins
        RankingOfflineSortColumn.TotalWins -> row.totalWins
    }
}

private const val RANKING_SORT_COLUMN_KEY = "ranking.sort.column"
private const val RANKING_SORT_DIRECTION_KEY = "ranking.sort.direction"

private fun readSavedRankingSort(savedStateHandle: SavedStateHandle): RankingOfflineSort {
    val savedColumn = savedStateHandle.get<String>(RANKING_SORT_COLUMN_KEY)
    val savedDirection = savedStateHandle.get<String>(RANKING_SORT_DIRECTION_KEY)

    val column = savedColumn?.let {
        runCatching { RankingOfflineSortColumn.valueOf(it) }.getOrNull()
    } ?: RankingOfflineSortColumn.TotalWins

    val direction = savedDirection?.let {
        runCatching { RankingSortDirection.valueOf(it) }.getOrNull()
    } ?: RankingSortDirection.Descending

    return RankingOfflineSort(column = column, direction = direction)
}

private fun persistRankingSort(
    savedStateHandle: SavedStateHandle,
    sort: RankingOfflineSort
) {
    savedStateHandle[RANKING_SORT_COLUMN_KEY] = sort.column.name
    savedStateHandle[RANKING_SORT_DIRECTION_KEY] = sort.direction.name
}

