package es.sebas1705.home.nav.models

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import es.sebas1705.home.nav.HomeGraph

internal data class HomeTab(
    val key: HomeGraph,
    @param:StringRes val labelRes: Int,
    val icon: ImageVector
)
