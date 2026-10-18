package es.sebas1705.splash.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import es.sebas1705.common.utlis.UiModePreviews
import es.sebas1705.splash.components.SplashBrandingLogo
import es.sebas1705.splash.models.SplashBrandingData
import es.sebas1705.ui.theme.AppDimens
import es.sebas1705.ui.theme.AppTheme

/**
 * Splash screen design of the app
 *
 * @since 0.1.0
 * @author Sebastian Ramiro Entrerrios García
 */
@Composable
fun SplashDesign(modifier: Modifier = Modifier) {
    val spacing = AppDimens.spacing
    val branding = SplashBrandingData(
        logoDrawableRes = es.sebas1705.core.resources.R.drawable.core_resources_ic_app_logo,
        logoContentDescriptionRes = es.sebas1705.core.resources.R.string.core_resources_icon_content
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(spacing.lg),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SplashBrandingLogo(data = branding)
        LinearProgressIndicator(
            modifier = Modifier
                .padding(top = spacing.sm)
                .widthIn(max = 240.dp)
        )
    }
}

@UiModePreviews
@Composable
private fun Preview() {
    AppTheme {
        SplashDesign()
    }
}
