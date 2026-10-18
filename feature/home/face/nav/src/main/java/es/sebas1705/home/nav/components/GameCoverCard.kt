package es.sebas1705.home.nav.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import es.sebas1705.core.resources.R
import es.sebas1705.ui.theme.AppDimens

@Composable
internal fun GameCoverCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(AppDimens.spacing.sm)
            .padding(bottom = AppDimens.spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.core_resources_ic_app_logo),
            contentDescription = stringResource(R.string.core_resources_icon_content),
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .fillMaxHeight(0.5f)
        )
    }
}