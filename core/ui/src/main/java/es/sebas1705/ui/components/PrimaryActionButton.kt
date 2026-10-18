package es.sebas1705.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import es.sebas1705.common.utlis.UiModePreviews
import es.sebas1705.core.resources.Sounds
import es.sebas1705.ui.sound.LocalSoundPlayer
import es.sebas1705.ui.theme.AppTheme

/** Full-width main action (e.g. "Start game"): 48 dp minimum height and the arcade click sound. */
@Composable
fun PrimaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val sound = LocalSoundPlayer.current
    FilledTonalButton(
        onClick = {
            sound(Sounds.CLK_ARCADE)
            onClick()
        },
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp),
    ) {
        Text(text)
    }
}

@UiModePreviews
@Composable
private fun PrimaryActionButtonPreview() {
    AppTheme {
        PrimaryActionButton(text = "Start game", onClick = {}, modifier = Modifier.padding(16.dp))
    }
}
