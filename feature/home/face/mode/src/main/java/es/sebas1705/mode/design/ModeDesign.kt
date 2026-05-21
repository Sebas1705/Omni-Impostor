package es.sebas1705.mode.design

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import es.sebas1705.common.FaceState
import es.sebas1705.common.utlis.UiModePreviews
import es.sebas1705.core.resources.R
import es.sebas1705.models.Modes
import es.sebas1705.core.resources.Sounds
import es.sebas1705.ui.sound.LocalSoundPlayer
import es.sebas1705.ui.theme.AppTheme

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
    val sound = LocalSoundPlayer.current

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
            FilledTonalButton(
                onClick = {
                    sound(Sounds.CLK_ARCADE)
                    onSave(
                        runCatching { Modes.valueOf(selectedModeName.value) }
                            .getOrDefault(Modes.Classic),
                        selectedImpostors.intValue,
                        selectedShowImpostorsInResult.value,
                        selectedTimerSeconds.intValue,
                        selectedImpostorsKnowEachOther.value,
                        selectedShowNumOfImpostors.value
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(stringResource(R.string.core_resources_game_mode_save))
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item(contentType = "contentType1") {
                Text(
                    text = stringResource(R.string.core_resources_game_mode_select_mode),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            item(contentType = "contentType2") {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(
                        items = Modes.entries,
                        key = { option -> option.name },
                        contentType = { _ -> "contentType1" }) { option ->
                        val isSelected = selectedModeName.value == option.name
                        Card(
                            onClick = { sound(Sounds.CLK_CASUAL); selectedModeName.value = option.name },
                            colors = if (isSelected) {
                                CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            } else {
                                CardDefaults.cardColors()
                            }
                        ) {
                            Column(
                                modifier = Modifier
                                    .width(280.dp)
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = when (option) {
                                        Modes.Classic -> stringResource(
                                            R.string.core_resources_game_mode_classic_name
                                        )

                                        Modes.Chaos -> stringResource(
                                            R.string.core_resources_game_mode_chaos_name
                                        )
                                    },
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = when (option) {
                                        Modes.Classic -> stringResource(
                                            R.string.core_resources_game_mode_classic_desc
                                        )

                                        Modes.Chaos -> stringResource(
                                            R.string.core_resources_game_mode_chaos_desc
                                        )
                                    },
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }

            item(contentType = "contentType4") {
                Text(
                    text = stringResource(R.string.core_resources_game_mode_impostors),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            item(contentType = "contentType5") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = {
                            sound(Sounds.CLK_CLOCK)
                            selectedImpostors.intValue =
                                (selectedImpostors.intValue - 1).coerceAtLeast(1)
                        },
                        enabled = selectedImpostors.intValue > 1
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Remove,
                            contentDescription = stringResource(R.string.core_resources_decrease_impostors)
                        )
                    }

                    Text(
                        text = selectedImpostors.intValue.toString(),
                        style = MaterialTheme.typography.headlineSmall
                    )

                    IconButton(
                        onClick = {
                            sound(Sounds.CLK_CLOCK)
                            selectedImpostors.intValue += 1
                        },
                        enabled = (selectedImpostors.intValue + 1) != faceState.users.size
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = stringResource(R.string.core_resources_increase_impostors)
                        )
                    }
                }
            }

            item(contentType = "contentType6") {
                Text(
                    text = stringResource(R.string.core_resources_game_mode_timer),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            item(contentType = "contentType7") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = {
                            sound(Sounds.CLK_CLOCK)
                            selectedTimerSeconds.intValue =
                                (selectedTimerSeconds.intValue - TIMER_STEP_SECONDS).coerceAtLeast(0)
                        },
                        enabled = selectedTimerSeconds.intValue > 0
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Remove,
                            contentDescription = null
                        )
                    }

                    Text(
                        text = if (selectedTimerSeconds.intValue == 0)
                            stringResource(R.string.core_resources_game_mode_timer_no_limit)
                        else
                            stringResource(
                                R.string.core_resources_game_mode_timer_seconds,
                                selectedTimerSeconds.intValue
                            ),
                        style = MaterialTheme.typography.headlineSmall
                    )

                    IconButton(
                        onClick = { sound(Sounds.CLK_CLOCK); selectedTimerSeconds.intValue += TIMER_STEP_SECONDS }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null
                        )
                    }
                }
            }

            item(contentType = "contentType8") {
                Text(
                    text = stringResource(R.string.core_resources_game_mode_post_game),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            item(contentType = "contentType9") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.core_resources_game_mode_show_impostors),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = stringResource(R.string.core_resources_game_mode_show_impostors_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = selectedShowImpostorsInResult.value,
                        onCheckedChange = { selectedShowImpostorsInResult.value = it }
                    )
                }
            }

            item(contentType = "contentType10") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.core_resources_game_mode_impostors_know),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = stringResource(R.string.core_resources_game_mode_impostors_know_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = selectedImpostorsKnowEachOther.value,
                        onCheckedChange = { selectedImpostorsKnowEachOther.value = it }
                    )
                }
            }

            item(contentType = "contentType11") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.core_resources_game_mode_num_impostors_know),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = stringResource(R.string.core_resources_game_mode_num_impostors_know_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = selectedShowNumOfImpostors.value,
                        onCheckedChange = { selectedShowNumOfImpostors.value = it }
                    )
                }
            }
        }
    }
}


@UiModePreviews
@Composable
private fun Preview() {
    AppTheme {
        ModeDesign()
    }
}
