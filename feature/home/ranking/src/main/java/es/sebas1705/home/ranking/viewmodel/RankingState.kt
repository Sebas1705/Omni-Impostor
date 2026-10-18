package es.sebas1705.home.ranking.viewmodel

import es.sebas1705.common.mvi.MVIBaseState
import es.sebas1705.home.ranking.models.OfflineRankingRowUi
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class RankingState(
    val offlineRows: ImmutableList<OfflineRankingRowUi> = persistentListOf(),
    val offlineSort: RankingOfflineSort = RankingOfflineSort(),
    val errorMessage: String? = null
) : MVIBaseState

data class RankingOfflineSort(
    val column: RankingOfflineSortColumn = RankingOfflineSortColumn.TotalWins,
    val direction: RankingSortDirection = RankingSortDirection.Descending
)

enum class RankingOfflineSortColumn {
    Position,
    Player,
    CivilianWins,
    ImpostorWins,
    TotalWins
}

enum class RankingSortDirection {
    Ascending,
    Descending
}

