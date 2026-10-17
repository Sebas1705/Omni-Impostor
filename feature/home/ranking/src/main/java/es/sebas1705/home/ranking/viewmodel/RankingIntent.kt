package es.sebas1705.home.ranking.viewmodel

import es.sebas1705.common.mvi.MVIBaseIntent

sealed interface RankingIntent : MVIBaseIntent {
    data object Load : RankingIntent
    data class ToggleOfflineSort(val column: RankingOfflineSortColumn) : RankingIntent
}

