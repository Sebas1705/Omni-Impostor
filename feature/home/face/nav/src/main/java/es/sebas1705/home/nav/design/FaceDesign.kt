package es.sebas1705.home.nav.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CloudQueue
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.LocalLibrary
import androidx.compose.material.icons.outlined.OfflineBolt
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.TimerOff
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import es.sebas1705.common.FaceState
import es.sebas1705.common.utlis.UiModePreviews
import es.sebas1705.core.resources.R
import es.sebas1705.home.nav.components.GameCoverCard
import es.sebas1705.home.nav.components.ModeCard
import es.sebas1705.core.resources.Sounds
import es.sebas1705.ui.sound.LocalSoundPlayer
import es.sebas1705.ui.theme.AppTheme
import es.sebas1705.ui.theme.makeTitle

@Composable
@Suppress("LongMethod")
fun FaceDesign(
    modifier: Modifier = Modifier,
    faceState: FaceState = FaceState(),
    isLoading: Boolean = false,
    onOpenUser: () -> Unit = {},
    onOpenCategories: () -> Unit = {},
    onOpenMode: () -> Unit = {},
    onStartOfflineGame: () -> Unit = {},
    onStartOnlineGame: (networkMode: String) -> Unit = {},
    onOpenSettings: () -> Unit = {}
) {
    val selectedOffline = rememberSaveable { mutableStateOf(true) }
    val sound = LocalSoundPlayer.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(onClick = { sound(Sounds.CLK_TAP); onOpenSettings() }) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = stringResource(R.string.core_resources_settings_title)
                )
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (maxWidth >= 600.dp) {
                Row(Modifier.fillMaxSize()) {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(end = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item(contentType = "cover") { GameCoverCard() }
                        item(contentType = "env_title") {
                            Text(
                                text = stringResource(R.string.core_resources_face_environment),
                                style = MaterialTheme.typography.titleLarge.makeTitle(),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        item(contentType = "mode_selector") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ModeCard(
                                    title = stringResource(R.string.core_resources_face_offline_mode),
                                    description = stringResource(R.string.core_resources_face_offline_mode_desc),
                                    icon = Icons.Outlined.OfflineBolt,
                                    onClick = { sound(Sounds.CLK_CASUAL); selectedOffline.value = true },
                                    modifier = Modifier.weight(1f),
                                    selected = selectedOffline.value
                                )
                                ModeCard(
                                    title = stringResource(R.string.core_resources_face_online_mode),
                                    description = stringResource(R.string.core_resources_face_online_mode_desc),
                                    icon = Icons.Outlined.CloudQueue,
                                    onClick = { sound(Sounds.CLK_CASUAL); selectedOffline.value = false },
                                    modifier = Modifier.weight(1f),
                                    selected = !selectedOffline.value
                                )
                            }
                        }
                        if (!faceState.errorMessage.isNullOrBlank()) {
                            item(contentType = "error") {
                                Text(
                                    text = faceState.errorMessage!!,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                    Box(
                        Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )
                    if (selectedOffline.value) {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(start = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item(contentType = "edit_title") {
                                Text(
                                    text = stringResource(R.string.core_resources_face_edit_mode),
                                    style = MaterialTheme.typography.titleLarge.makeTitle(),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            item(contentType = "cat_card") {
                                FaceCategoriesCard(
                                    faceState,
                                    onOpenCategories
                                )
                            }
                            item(contentType = "users_card") {
                                FaceUsersCard(
                                    faceState,
                                    onOpenUser
                                )
                            }
                            item(contentType = "mode_card") { FaceModeCard(faceState, onOpenMode) }
                            item(contentType = "readiness") {
                                GameConfigChips(faceState)
                            }
                            item(contentType = "start") {
                                FilledTonalButton(
                                    onClick = { sound(Sounds.CLK_ARCADE); onStartOfflineGame() },
                                    enabled = faceState.users.size >= 3 && faceState.categoriesStates.any { it.value } && faceState.impostors < faceState.users.size,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp)
                                ) {
                                    Text(stringResource(R.string.core_resources_face_start_offline_game))
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(start = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item(contentType = "online_edit_title_r") {
                                Text(
                                    text = stringResource(R.string.core_resources_face_edit_mode),
                                    style = MaterialTheme.typography.titleLarge.makeTitle(),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            item(contentType = "online_cat_r") {
                                FaceCategoriesCard(faceState, onOpenCategories)
                            }
                            item(contentType = "online_mode_r") {
                                FaceModeCard(faceState, onOpenMode)
                            }
                            item(contentType = "online_chips_r") {
                                OnlineConfigChips(faceState)
                            }
                            item(contentType = "online_local_r") {
                                FilledTonalButton(
                                    onClick = { onStartOnlineGame("Local") },
                                    enabled = faceState.categoriesStates.any { it.value },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.OfflineBolt,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = stringResource(R.string.core_resources_face_start_local_game),
                                        modifier = Modifier.padding(start = 6.dp)
                                    )
                                }
                            }
                            item(contentType = "online_internet_r") {
                                FilledTonalButton(
                                    onClick = { onStartOnlineGame("Internet") },
                                    enabled = faceState.categoriesStates.any { it.value },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.CloudQueue,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = stringResource(R.string.core_resources_face_start_online_game),
                                        modifier = Modifier.padding(start = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item(contentType = "contentType1") { GameCoverCard() }
                    item(contentType = "contentType2") {
                        Text(
                            text = stringResource(R.string.core_resources_face_environment),
                            style = MaterialTheme.typography.titleLarge.makeTitle(),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    item(contentType = "contentType3") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ModeCard(
                                title = stringResource(R.string.core_resources_face_offline_mode),
                                description = stringResource(R.string.core_resources_face_offline_mode_desc),
                                icon = Icons.Outlined.OfflineBolt,
                                onClick = { sound(Sounds.CLK_CASUAL); selectedOffline.value = true },
                                modifier = Modifier.weight(1f),
                                selected = selectedOffline.value
                            )
                            ModeCard(
                                title = stringResource(R.string.core_resources_face_online_mode),
                                description = stringResource(R.string.core_resources_face_online_mode_desc),
                                icon = Icons.Outlined.CloudQueue,
                                onClick = { sound(Sounds.CLK_CASUAL); selectedOffline.value = false },
                                modifier = Modifier.weight(1f),
                                selected = !selectedOffline.value
                            )
                        }
                    }
                    if (selectedOffline.value) {
                        item(contentType = "contentType4") {
                            Text(
                                text = stringResource(R.string.core_resources_face_edit_mode),
                                style = MaterialTheme.typography.titleLarge.makeTitle(),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        item(contentType = "contentType5") {
                            FaceCategoriesCard(
                                faceState,
                                onOpenCategories
                            )
                        }
                        item(contentType = "contentType6") { FaceUsersCard(faceState, onOpenUser) }
                        item(contentType = "contentType7") { FaceModeCard(faceState, onOpenMode) }
                        item(contentType = "contentType_readiness") {
                            GameConfigChips(faceState)
                        }
                        item(contentType = "contentType8") {
                            FilledTonalButton(
                                onClick = { sound(Sounds.CLK_ARCADE); onStartOfflineGame() },
                                enabled = faceState.users.size >= 3 && faceState.categoriesStates.any { it.value } && faceState.impostors < faceState.users.size,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            ) {
                                Text(stringResource(R.string.core_resources_face_start_offline_game))
                            }
                        }
                    } else {
                        item(contentType = "contentType_online_edit") {
                            Text(
                                text = stringResource(R.string.core_resources_face_edit_mode),
                                style = MaterialTheme.typography.titleLarge.makeTitle(),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        item(contentType = "contentType_online_cat") {
                            FaceCategoriesCard(faceState, onOpenCategories)
                        }
                        item(contentType = "contentType_online_mode") {
                            FaceModeCard(faceState, onOpenMode)
                        }
                        item(contentType = "contentType_online_chips") {
                            OnlineConfigChips(faceState)
                        }
                        item(contentType = "contentType_online_local") {
                            FilledTonalButton(
                                onClick = { onStartOnlineGame("Local") },
                                enabled = faceState.categoriesStates.any { it.value },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.OfflineBolt,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = stringResource(R.string.core_resources_face_start_local_game),
                                    modifier = Modifier.padding(start = 6.dp)
                                )
                            }
                        }
                        item(contentType = "contentType_online_internet") {
                            FilledTonalButton(
                                onClick = { onStartOnlineGame("Internet") },
                                enabled = faceState.categoriesStates.any { it.value },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CloudQueue,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = stringResource(R.string.core_resources_face_start_online_game),
                                    modifier = Modifier.padding(start = 6.dp)
                                )
                            }
                        }
                    }
                    if (!faceState.errorMessage.isNullOrBlank()) {
                        item(contentType = "contentType10") {
                            Text(
                                text = faceState.errorMessage!!,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FaceCategoriesCard(faceState: FaceState, onOpenCategories: () -> Unit) {
    val sound = LocalSoundPlayer.current
    OutlinedButton(
        onClick = { sound(Sounds.CLK_CASUAL); onOpenCategories() },
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Outlined.LocalLibrary,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = stringResource(
                R.string.core_resources_face_categories_selected,
                faceState.categoriesStates.count { it.value }
            ),
            modifier = Modifier.padding(start = 6.dp)
        )
    }
}

@Composable
private fun FaceUsersCard(faceState: FaceState, onOpenUser: () -> Unit) {
    val sound = LocalSoundPlayer.current
    OutlinedButton(
        onClick = { sound(Sounds.CLK_CASUAL); onOpenUser() },
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Outlined.People,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = stringResource(R.string.core_resources_face_users_count, faceState.users.size),
            modifier = Modifier.padding(start = 6.dp)
        )
    }
}

@Composable
private fun FaceModeCard(faceState: FaceState, onOpenMode: () -> Unit) {
    val sound = LocalSoundPlayer.current
    OutlinedButton(
        onClick = { sound(Sounds.CLK_CASUAL); onOpenMode() },
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Outlined.Settings,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = stringResource(
                R.string.core_resources_face_mode_summary,
                faceState.mode.name,
                faceState.impostors
            ),
            modifier = Modifier.padding(start = 6.dp)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GameConfigChips(faceState: FaceState) {
    val playerCount = faceState.users.size
    val selectedCategories = faceState.categoriesStates.count { it.value }
    val playersOk = playerCount >= 3
    val categoriesOk = selectedCategories >= 1

    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        SuggestionChip(
            onClick = {},
            label = {
                Text(
                    text = if (playersOk)
                        stringResource(R.string.core_resources_face_ready_players, playerCount)
                    else stringResource(R.string.core_resources_face_warn_players)
                )
            },
            icon = {
                Icon(
                    imageVector = if (playersOk) Icons.Outlined.CheckCircle else Icons.Outlined.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(SuggestionChipDefaults.IconSize),
                    tint = if (playersOk) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            },
            colors = SuggestionChipDefaults.suggestionChipColors(
                containerColor = if (playersOk) MaterialTheme.colorScheme.primaryContainer.copy(
                    alpha = 0.4f
                )
                else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
            )
        )
        SuggestionChip(
            onClick = {},
            label = {
                Text(
                    text = if (categoriesOk)
                        stringResource(
                            R.string.core_resources_face_ready_categories,
                            selectedCategories
                        )
                    else stringResource(R.string.core_resources_face_warn_categories)
                )
            },
            icon = {
                Icon(
                    imageVector = if (categoriesOk) Icons.Outlined.Category else Icons.Outlined.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(SuggestionChipDefaults.IconSize),
                    tint = if (categoriesOk) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            },
            colors = SuggestionChipDefaults.suggestionChipColors(
                containerColor = if (categoriesOk) MaterialTheme.colorScheme.primaryContainer.copy(
                    alpha = 0.4f
                )
                else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
            )
        )
        SuggestionChip(
            onClick = {},
            label = { Text(text = "${faceState.mode.name} ×${faceState.impostors}") },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(SuggestionChipDefaults.IconSize),
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            colors = SuggestionChipDefaults.suggestionChipColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            )
        )
        SuggestionChip(
            onClick = {},
            label = {
                Text(
                    text = if (faceState.discussionTimerSeconds <= 0)
                        stringResource(R.string.core_resources_game_mode_timer_no_limit)
                    else stringResource(
                        R.string.core_resources_game_mode_timer_seconds,
                        faceState.discussionTimerSeconds
                    )
                )
            },
            icon = {
                Icon(
                    imageVector = if (faceState.discussionTimerSeconds <= 0) Icons.Outlined.TimerOff else Icons.Outlined.Timer,
                    contentDescription = null,
                    modifier = Modifier.size(SuggestionChipDefaults.IconSize),
                    tint = MaterialTheme.colorScheme.secondary
                )
            },
            colors = SuggestionChipDefaults.suggestionChipColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
            )
        )
        if (faceState.impostorsKnowEachOther) {
            SuggestionChip(
                onClick = {},
                label = { Text(text = stringResource(R.string.core_resources_game_mode_impostors_know)) },
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Groups,
                        contentDescription = null,
                        modifier = Modifier.size(SuggestionChipDefaults.IconSize),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                },
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
                )
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OnlineConfigChips(faceState: FaceState) {
    val selectedCategories = faceState.categoriesStates.count { it.value }
    val categoriesOk = selectedCategories >= 1

    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        SuggestionChip(
            onClick = {},
            label = {
                Text(
                    text = if (categoriesOk)
                        stringResource(R.string.core_resources_face_ready_categories, selectedCategories)
                    else stringResource(R.string.core_resources_face_warn_categories)
                )
            },
            icon = {
                Icon(
                    imageVector = if (categoriesOk) Icons.Outlined.CheckCircle else Icons.Outlined.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(SuggestionChipDefaults.IconSize),
                    tint = if (categoriesOk) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            },
            colors = SuggestionChipDefaults.suggestionChipColors(
                containerColor = if (categoriesOk) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
            )
        )
        SuggestionChip(
            onClick = {},
            label = { Text(text = "${faceState.mode.name} ×${faceState.impostors}") },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(SuggestionChipDefaults.IconSize),
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            colors = SuggestionChipDefaults.suggestionChipColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            )
        )
    }
}

@UiModePreviews
@Composable
private fun Preview() {
    AppTheme {
        FaceDesign()
    }
}

