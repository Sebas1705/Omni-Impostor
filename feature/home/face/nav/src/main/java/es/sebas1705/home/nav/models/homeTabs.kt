package es.sebas1705.home.nav.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.ManageAccounts
import androidx.compose.material.icons.outlined.SportsEsports
import es.sebas1705.core.resources.R
import es.sebas1705.home.nav.HomeGraph

internal val homeTabs = listOf(
    HomeTab(HomeGraph.FaceScreen, R.string.core_resources_home_tab_play, Icons.Outlined.SportsEsports),
    HomeTab(HomeGraph.RankingScreen, R.string.core_resources_home_tab_ranking, Icons.Outlined.EmojiEvents),
    HomeTab(HomeGraph.ProfileScreen, R.string.core_resources_home_tab_profile, Icons.Outlined.ManageAccounts)
)
