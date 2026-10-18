package es.sebas1705.main.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import es.sebas1705.ui.theme.AppDimens
import es.sebas1705.core.resources.R as ResourceR

@Composable
internal fun SettingsFloatingButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilledTonalIconButton(
        onClick = onClick,
        modifier = modifier
            .statusBarsPadding()
            .padding(AppDimens.spacing.sm)
    ) {
        Icon(
            imageVector = Icons.Outlined.Settings,
            contentDescription = stringResource(ResourceR.string.core_resources_open_settings)
        )
    }
}
