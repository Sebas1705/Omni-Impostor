package es.sebas1705.home.ranking

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.sebas1705.home.ranking.design.RankingDesign
import es.sebas1705.home.ranking.viewmodel.RankingIntent
import es.sebas1705.home.ranking.viewmodel.RankingViewModel

@Composable
fun RankingScreen(
    modifier: Modifier = Modifier,
    rankingViewModel: RankingViewModel = hiltViewModel()
) {
    val uiState by rankingViewModel.uiState.collectAsStateWithLifecycle()
    val loading by rankingViewModel.loading.collectAsStateWithLifecycle()

    LaunchedEffect(null) {
        rankingViewModel.eventHandler(RankingIntent.Load)
    }

    RankingDesign(
        modifier = modifier,
        offlineRows = uiState.offlineRows,
        offlineSort = uiState.offlineSort,
        isLoading = loading,
        errorMessage = uiState.errorMessage,
        onToggleOfflineSort = { rankingViewModel.eventHandler(RankingIntent.ToggleOfflineSort(it)) }
    )
}

