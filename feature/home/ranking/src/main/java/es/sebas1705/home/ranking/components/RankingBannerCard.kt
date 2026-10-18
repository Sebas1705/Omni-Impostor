package es.sebas1705.home.ranking.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import es.sebas1705.core.resources.R
import es.sebas1705.ui.theme.cardShape

@Composable
internal fun RankingBannerCard() {
    Image(
        painter = painterResource(R.drawable.core_resources_il_face_banner),
        contentDescription = null,
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(MaterialTheme.cardShape),
        contentScale = ContentScale.Crop
    )
}
