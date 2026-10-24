package es.sebas1705.home.nav.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.LocalLibrary
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.TimerOff
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import es.sebas1705.common.FaceState
import es.sebas1705.common.utlis.UiModePreviews
import es.sebas1705.core.resources.R
import es.sebas1705.core.resources.Sounds
import es.sebas1705.home.nav.components.GameCoverCard
import es.sebas1705.models.Categories
import es.sebas1705.models.Modes
import es.sebas1705.ui.components.AppCard
import es.sebas1705.ui.components.PrimaryActionButton
import es.sebas1705.ui.components.SectionHeader
import es.sebas1705.ui.components.StatusChip
import es.sebas1705.ui.components.StatusChipVariant
import es.sebas1705.ui.sound.LocalSoundPlayer
import es.sebas1705.ui.theme.AppDimens
import es.sebas1705.ui.theme.AppTheme
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf

private const val TABLET_MIN_WIDTH_DP = 600
private const val MIN_PLAYERS = 3

/** Same rule the start button has always used: players, a category and fewer impostors than players. */
private fun FaceState.canStart(): Boolean =
    users.size >= MIN_PLAYERS && categoriesStates.any { it.value } && impostors < users.size

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
    onOpenSettings: () -> Unit = {}
) {
    val sound = LocalSoundPlayer.current
    val spacing = AppDimens.spacing
    // Keeps the last items clear of the settings FAB.
    val listPadding = PaddingValues(top = spacing.sm, bottom = spacing.xxl * 2 + spacing.lg)

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
            if (maxWidth >= TABLET_MIN_WIDTH_DP.dp) {
                Row(Modifier.fillMaxSize()) {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(end = spacing.sm),
                        contentPadding = listPadding,
                        verticalArrangement = Arrangement.spacedBy(spacing.sm)
                    ) {
                        item(contentType = "cover") { GameCoverCard() }
                        if (!faceState.errorMessage.isNullOrBlank()) {
                            item(contentType = "error") { FaceError(faceState.errorMessage!!) }
                        }
                    }
                    Box(
                        Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(start = spacing.md),
                        contentPadding = listPadding,
                        verticalArrangement = Arrangement.spacedBy(spacing.md)
                    ) {
                        setupItems(faceState, onOpenCategories, onOpenUser, onOpenMode)
                        item(contentType = "start") { FaceStartArea(faceState, onStartOfflineGame) }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = listPadding,
                    verticalArrangement = Arrangement.spacedBy(spacing.md)
                ) {
                    item(contentType = "cover") { GameCoverCard() }
                    setupItems(faceState, onOpenCategories, onOpenUser, onOpenMode)
                    item(contentType = "start") { FaceStartArea(faceState, onStartOfflineGame) }
                    if (!faceState.errorMessage.isNullOrBlank()) {
                        item(contentType = "error") { FaceError(faceState.errorMessage!!) }
                    }
                }
            }
        }
    }
}

private fun LazyListScope.setupItems(
    faceState: FaceState,
    onOpenCategories: () -> Unit,
    onOpenUser: () -> Unit,
    onOpenMode: () -> Unit,
) {
    item(contentType = "setup_title") {
        SectionHeader(
            title = stringResource(R.string.core_resources_face_edit_mode),
            subtitle = stringResource(R.string.core_resources_phase1_face_subtitle)
        )
    }
    item(contentType = "cat_card") { FaceCategoriesCard(faceState, onOpenCategories) }
    item(contentType = "users_card") { FaceUsersCard(faceState, onOpenUser) }
    item(contentType = "mode_card") { FaceModeCard(faceState, onOpenMode) }
}

@Composable
private fun FaceError(message: String) {
    Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.error
    )
}

@Composable
private fun FaceSummaryRow(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppDimens.spacing.sm)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun FaceCategoriesCard(faceState: FaceState, onOpenCategories: () -> Unit) {
    val sound = LocalSoundPlayer.current
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        title = stringResource(R.string.core_resources_face_categories),
        icon = Icons.Outlined.LocalLibrary,
        onClick = { sound(Sounds.CLK_CASUAL); onOpenCategories() }
    ) {
        FaceSummaryRow(
            stringResource(
                R.string.core_resources_face_categories_selected,
                faceState.categoriesStates.count { it.value }
            )
        )
    }
}

@Composable
private fun FaceUsersCard(faceState: FaceState, onOpenUser: () -> Unit) {
    val sound = LocalSoundPlayer.current
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        title = stringResource(R.string.core_resources_face_users),
        icon = Icons.Outlined.People,
        onClick = { sound(Sounds.CLK_CASUAL); onOpenUser() }
    ) {
        FaceSummaryRow(stringResource(R.string.core_resources_face_users_count, faceState.users.size))
    }
}

@Composable
private fun FaceModeCard(faceState: FaceState, onOpenMode: () -> Unit) {
    val sound = LocalSoundPlayer.current
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        title = stringResource(R.string.core_resources_face_mode),
        icon = Icons.Outlined.Settings,
        onClick = { sound(Sounds.CLK_CASUAL); onOpenMode() }
    ) {
        FaceSummaryRow(
            stringResource(
                R.string.core_resources_face_mode_summary,
                modeLabel(faceState.mode),
                faceState.impostors
            )
        )
    }
}

@Composable
private fun modeLabel(mode: Modes): String = when (mode) {
    Modes.Classic -> stringResource(R.string.core_resources_game_mode_classic_name)
    Modes.Chaos -> stringResource(R.string.core_resources_game_mode_chaos_name)
}

/** Readiness status plus the main "start game" action. */
@Composable
private fun FaceStartArea(faceState: FaceState, onStartOfflineGame: () -> Unit) {
    val spacing = AppDimens.spacing
    val canStart = faceState.canStart()
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        title = stringResource(R.string.core_resources_phase1_start_title),
        icon = if (canStart) Icons.Outlined.CheckCircle else Icons.Outlined.Warning
    ) {
        GameConfigChips(faceState)
        Text(
            text = stringResource(
                if (canStart) R.string.core_resources_phase1_start_ready
                else R.string.core_resources_phase1_start_missing
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = spacing.xs)
        )
        PrimaryActionButton(
            text = stringResource(R.string.core_resources_face_start_offline_game),
            onClick = onStartOfflineGame,
            enabled = canStart,
            modifier = Modifier.padding(top = spacing.sm)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GameConfigChips(faceState: FaceState) {
    val spacing = AppDimens.spacing
    val playerCount = faceState.users.size
    val selectedCategories = faceState.categoriesStates.count { it.value }
    val playersOk = playerCount >= MIN_PLAYERS
    val categoriesOk = selectedCategories >= 1
    val impostorsOk = faceState.impostors < playerCount

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        verticalArrangement = Arrangement.spacedBy(spacing.sm)
    ) {
        StatusChip(
            text = if (playersOk) stringResource(R.string.core_resources_face_ready_players, playerCount)
            else stringResource(R.string.core_resources_face_warn_players),
            variant = if (playersOk) StatusChipVariant.Ready else StatusChipVariant.Warning,
            icon = if (playersOk) Icons.Outlined.CheckCircle else Icons.Outlined.Warning
        )
        StatusChip(
            text = if (categoriesOk) {
                stringResource(R.string.core_resources_face_ready_categories, selectedCategories)
            } else stringResource(R.string.core_resources_face_warn_categories),
            variant = if (categoriesOk) StatusChipVariant.Ready else StatusChipVariant.Warning,
            icon = if (categoriesOk) Icons.Outlined.Category else Icons.Outlined.Warning
        )
        StatusChip(
            text = if (impostorsOk) {
                stringResource(R.string.core_resources_phase1_ready_impostors, faceState.impostors)
            } else stringResource(R.string.core_resources_phase1_warn_impostors),
            variant = if (impostorsOk) StatusChipVariant.Ready else StatusChipVariant.Error,
            icon = if (impostorsOk) Icons.Outlined.Groups else Icons.Outlined.Error
        )
        StatusChip(
            text = if (faceState.discussionTimerSeconds <= 0) {
                stringResource(R.string.core_resources_game_mode_timer_no_limit)
            } else stringResource(
                R.string.core_resources_game_mode_timer_seconds,
                faceState.discussionTimerSeconds
            ),
            icon = if (faceState.discussionTimerSeconds <= 0) Icons.Outlined.TimerOff else Icons.Outlined.Timer
        )
        if (faceState.impostorsKnowEachOther) {
            StatusChip(
                text = stringResource(R.string.core_resources_game_mode_impostors_know),
                icon = Icons.Outlined.Groups
            )
        }
    }
}

@UiModePreviews
@Composable
private fun Preview() {
    AppTheme {
        FaceDesign()
    }
}

@UiModePreviews
@Composable
private fun ReadyPreview() {
    AppTheme {
        FaceDesign(
            faceState = FaceState(
                categoriesStates = persistentMapOf(Categories.entries.first() to true),
                users = persistentListOf("Ana", "Bob", "Cris", "Dani"),
            )
        )
    }
}
