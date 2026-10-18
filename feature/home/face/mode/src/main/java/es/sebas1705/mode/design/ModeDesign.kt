package es.sebas1705.mode.design

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import es.sebas1705.ui.components.AppCard
import es.sebas1705.ui.components.PrimaryActionButton
import es.sebas1705.ui.components.SectionHeader
import es.sebas1705.ui.theme.AppDimens
import es.sebas1705.common.FaceState
import es.sebas1705.common.utlis.UiModePreviews
import es.sebas1705.core.resources.R
import es.sebas1705.models.Modes
import es.sebas1705.core.resources.Sounds
import es.sebas1705.ui.sound.LocalSoundPlayer
import es.sebas1705.ui.theme.AppTheme
import kotlinx.collections.immutable.persistentListOf

private const val TIMER_STEP_SECONDS = 30

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Suppress("LongMethod")
fun ModeDesign(
    modifier: Modifier = Modifier,
    faceState: FaceState = FaceState(),
    onSave: (mode: Modes, impostors: Int, showImpostorsInResult: Boolean, discussionTimerSeconds: Int, impostorsKnowEachOther: Boolean, selectedShowNumOfImpostors: Boolean) -> Unit = { _, _, _, _, _, _ -> },
    onBack: () -> Unit = {}
) {
    val selectedModeName = rememberSaveable { mutableStateOf(faceState.mode.name) }
    val selectedImpostors = rememberSaveable { mutableIntStateOf(faceState.impostors.coerceAtLeast(1)) }
    val selectedShowImpostorsInResult = rememberSaveable { mutableStateOf(faceState.showImpostorsInResult) }
    val selectedTimerSeconds =
        rememberSaveable { mutableIntStateOf(faceState.discussionTimerSeconds.coerceAtLeast(0)) }
    val selectedImpostorsKnowEachOther = rememberSaveable { mutableStateOf(faceState.impostorsKnowEachOther) }
    val selectedShowNumOfImpostors = rememberSaveable { mutableStateOf(faceState.showNumOfImpostors) }
    // Sync once from faceState in case the ViewModel loaded async after first composition.
    // rememberSaveable preserves user edits across rotation, so we only sync on first open.
    val syncedWithFaceState = rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(faceState) {
        if (!syncedWithFaceState.value) {
            syncedWithFaceState.value = true
            selectedModeName.value = faceState.mode.name
            selectedImpostors.intValue = faceState.impostors.coerceAtLeast(1)
            selectedShowImpostorsInResult.value = faceState.showImpostorsInResult
            selectedTimerSeconds.intValue = faceState.discussionTimerSeconds.coerceAtLeast(0)
            selectedImpostorsKnowEachOther.value = faceState.impostorsKnowEachOther
            selectedShowNumOfImpostors.value = faceState.showNumOfImpostors
        }
    }
    val sound = LocalSoundPlayer.current
    val spacing = AppDimens.spacing

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.core_resources_game_mode_title)) },
                navigationIcon = {
                    IconButton(onClick = { sound(Sounds.CLK_TAP); onBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.core_resources_back)
                        )
                    }
                }
            )
        },
        bottomBar = {
            PrimaryActionButton(
                text = stringResource(R.string.core_resources_game_mode_save),
                modifier = Modifier.padding(spacing.lg),
                onClick = {
                    onSave(
                        runCatching { Modes.valueOf(selectedModeName.value) }
                            .getOrDefault(Modes.Classic),
                        selectedImpostors.intValue,
                        selectedShowImpostorsInResult.value,
                        selectedTimerSeconds.intValue,
                        selectedImpostorsKnowEachOther.value,
                        selectedShowNumOfImpostors.value
                    )
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(spacing.lg),
            verticalArrangement = Arrangement.spacedBy(spacing.md)
        ) {
            item(contentType = "mode_title") {
                SectionHeader(
                    title = stringResource(R.string.core_resources_game_mode_select_mode),
                    subtitle = stringResource(R.string.core_resources_phase1_mode_select_subtitle)
                )
            }

            items(
                items = Modes.entries,
                key = { option -> option.name },
                contentType = { _ -> "mode_option" }
            ) { option ->
                val isSelected = selectedModeName.value == option.name
                AppCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { selected = isSelected },
                    title = when (option) {
                        Modes.Classic -> stringResource(R.string.core_resources_game_mode_classic_name)
                        Modes.Chaos -> stringResource(R.string.core_resources_game_mode_chaos_name)
                    },
                    icon = if (isSelected) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                    selected = isSelected,
                    onClick = { sound(Sounds.CLK_CASUAL); selectedModeName.value = option.name }
                ) {
                    Text(
                        text = when (option) {
                            Modes.Classic -> stringResource(R.string.core_resources_game_mode_classic_desc)
                            Modes.Chaos -> stringResource(R.string.core_resources_game_mode_chaos_desc)
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            item(contentType = "impostors") {
                AppCard(
                    modifier = Modifier.fillMaxWidth(),
                    title = stringResource(R.string.core_resources_game_mode_impostors),
                    icon = Icons.Outlined.Groups
                ) {
                    Text(
                        text = stringResource(R.string.core_resources_phase1_impostors_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    ModeStepper(
                        value = selectedImpostors.intValue.toString(),
                        decreaseDescription = stringResource(R.string.core_resources_decrease_impostors),
                        increaseDescription = stringResource(R.string.core_resources_increase_impostors),
                        canDecrease = selectedImpostors.intValue > 1,
                        canIncrease = (selectedImpostors.intValue + 1) != faceState.users.size,
                        onDecrease = {
                            sound(Sounds.CLK_CLOCK)
                            selectedImpostors.intValue =
                                (selectedImpostors.intValue - 1).coerceAtLeast(1)
                        },
                        onIncrease = {
                            sound(Sounds.CLK_CLOCK)
                            selectedImpostors.intValue += 1
                        }
                    )
                }
            }

            item(contentType = "timer") {
                AppCard(
                    modifier = Modifier.fillMaxWidth(),
                    title = stringResource(R.string.core_resources_game_mode_timer),
                    icon = Icons.Outlined.Timer
                ) {
                    Text(
                        text = stringResource(R.string.core_resources_phase1_timer_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    ModeStepper(
                        value = if (selectedTimerSeconds.intValue == 0) {
                            stringResource(R.string.core_resources_game_mode_timer_no_limit)
                        } else {
                            stringResource(
                                R.string.core_resources_game_mode_timer_seconds,
                                selectedTimerSeconds.intValue
                            )
                        },
                        decreaseDescription = stringResource(R.string.core_resources_phase1_decrease_timer),
                        increaseDescription = stringResource(R.string.core_resources_phase1_increase_timer),
                        canDecrease = selectedTimerSeconds.intValue > 0,
                        canIncrease = true,
                        onDecrease = {
                            sound(Sounds.CLK_CLOCK)
                            selectedTimerSeconds.intValue =
                                (selectedTimerSeconds.intValue - TIMER_STEP_SECONDS).coerceAtLeast(0)
                        },
                        onIncrease = {
                            sound(Sounds.CLK_CLOCK)
                            selectedTimerSeconds.intValue += TIMER_STEP_SECONDS
                        }
                    )
                }
            }

            item(contentType = "post_game") {
                AppCard(
                    modifier = Modifier.fillMaxWidth(),
                    title = stringResource(R.string.core_resources_game_mode_post_game),
                    icon = Icons.Outlined.EmojiEvents
                ) {
                    ModeSwitchRow(
                        title = stringResource(R.string.core_resources_game_mode_show_impostors),
                        description = stringResource(R.string.core_resources_game_mode_show_impostors_desc),
                        checked = selectedShowImpostorsInResult.value,
                        onCheckedChange = { selectedShowImpostorsInResult.value = it }
                    )
                    ModeSwitchRow(
                        title = stringResource(R.string.core_resources_game_mode_impostors_know),
                        description = stringResource(R.string.core_resources_game_mode_impostors_know_desc),
                        checked = selectedImpostorsKnowEachOther.value,
                        onCheckedChange = { selectedImpostorsKnowEachOther.value = it }
                    )
                    ModeSwitchRow(
                        title = stringResource(R.string.core_resources_game_mode_num_impostors_know),
                        description = stringResource(R.string.core_resources_game_mode_num_impostors_know_desc),
                        checked = selectedShowNumOfImpostors.value,
                        onCheckedChange = { selectedShowNumOfImpostors.value = it }
                    )
                }
            }
        }
    }
}

/** Minus / value / plus control; the buttons keep the default 48 dp touch target. */
@Composable
private fun ModeStepper(
    value: String,
    decreaseDescription: String,
    increaseDescription: String,
    canDecrease: Boolean,
    canIncrease: Boolean,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onDecrease, enabled = canDecrease) {
            Icon(imageVector = Icons.Filled.Remove, contentDescription = decreaseDescription)
        }
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onIncrease, enabled = canIncrease) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = increaseDescription)
        }
    }
}

/** Title + description with a switch; the whole row toggles, so the touch target is the full row. */
@Composable
private fun ModeSwitchRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange),
        horizontalArrangement = Arrangement.spacedBy(AppDimens.spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(AppDimens.spacing.xs)
        ) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = null)
    }
}

@UiModePreviews
@Composable
private fun Preview() {
    AppTheme {
        ModeDesign(faceState = FaceState(users = persistentListOf("Ana", "Bob", "Cris", "Dani")))
    }
}
