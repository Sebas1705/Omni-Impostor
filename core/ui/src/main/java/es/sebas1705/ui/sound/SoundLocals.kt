package es.sebas1705.ui.sound

import androidx.compose.runtime.compositionLocalOf
import es.sebas1705.core.resources.Sounds

val LocalSoundPlayer = compositionLocalOf<(Sounds) -> Unit> { {} }
