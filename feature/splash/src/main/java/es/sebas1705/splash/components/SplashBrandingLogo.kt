package es.sebas1705.splash.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import es.sebas1705.splash.models.SplashBrandingData
import es.sebas1705.ui.theme.AppDimens

@Composable
internal fun SplashBrandingLogo(
    data: SplashBrandingData
) {
    Image(
        painter = painterResource(data.logoDrawableRes),
        contentDescription = stringResource(data.logoContentDescriptionRes),
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .fillMaxHeight(0.5f)
            .padding(bottom = AppDimens.spacing.sm)
    )
}
