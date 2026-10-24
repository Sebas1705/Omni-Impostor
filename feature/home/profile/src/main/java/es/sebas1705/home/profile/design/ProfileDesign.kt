@file:Suppress("TooManyFunctions")

package es.sebas1705.home.profile.design

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.ManageAccounts
import androidx.compose.material.icons.outlined.OfflineBolt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import es.sebas1705.common.utlis.UiModePreviews
import es.sebas1705.core.resources.Sounds
import es.sebas1705.feature.home.profile.BuildConfig
import es.sebas1705.home.profile.components.ProfileBannerCard
import es.sebas1705.home.profile.models.OfflineProfileRecordRowUi
import es.sebas1705.home.profile.viewmodel.ProfileOfflineRecordSort
import es.sebas1705.home.profile.viewmodel.ProfileOfflineRecordSortColumn
import es.sebas1705.home.profile.viewmodel.ProfileRolePreference
import es.sebas1705.home.profile.viewmodel.ProfileSortDirection
import es.sebas1705.home.profile.viewmodel.ProfileTab
import es.sebas1705.models.Categories
import es.sebas1705.models.nameRes
import es.sebas1705.ui.adaptive.LocalForceCompactTables
import es.sebas1705.ui.adaptive.TableSortAnimation
import es.sebas1705.ui.components.AppCard
import es.sebas1705.ui.components.SectionHeader
import es.sebas1705.ui.components.StatusChip
import es.sebas1705.ui.components.StatusChipVariant
import es.sebas1705.ui.sound.LocalSoundPlayer
import es.sebas1705.ui.theme.AppDimens
import es.sebas1705.ui.theme.AppTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import es.sebas1705.core.resources.R as ResourceR

private val GoldColor = Color(0xFFFFD700)
private val MinRowHeight = 48.dp
private const val SORTED_COLUMN_ALPHA_HEADER = 0.16f
private const val SORTED_COLUMN_ALPHA_ROW = 0.08f
private const val STAT_TILES_MAX_FONT_SCALE = 1.3f

/** Fixed table layout: width and alignment of each column. */
private data class ProfileColumn(
    val column: ProfileOfflineRecordSortColumn,
    val width: Dp,
    val isNumeric: Boolean
)

private val ProfileColumns = listOf(
    ProfileColumn(ProfileOfflineRecordSortColumn.Position, 60.dp, isNumeric = true),
    ProfileColumn(ProfileOfflineRecordSortColumn.Player, 140.dp, isNumeric = false),
    ProfileColumn(ProfileOfflineRecordSortColumn.CivilianWins, 96.dp, isNumeric = true),
    ProfileColumn(ProfileOfflineRecordSortColumn.ImpostorWins, 96.dp, isNumeric = true),
    ProfileColumn(ProfileOfflineRecordSortColumn.TotalWins, 96.dp, isNumeric = true),
    ProfileColumn(ProfileOfflineRecordSortColumn.CurrentStreak, 96.dp, isNumeric = true)
)

@Composable
@Suppress("LongMethod", "LongParameterList", "CyclomaticComplexMethod")
fun ProfileDesign(
    modifier: Modifier = Modifier,
    selectedTab: ProfileTab = ProfileTab.OfflineRecord,
    isLoadingOfflineRecords: Boolean = false,
    offlineRecordRows: ImmutableList<OfflineProfileRecordRowUi> = persistentListOf(),
    offlineRecordSort: ProfileOfflineRecordSort = ProfileOfflineRecordSort(),
    rolePreference: ProfileRolePreference = ProfileRolePreference.ImpostorHunter,
    favoriteCategory: Categories? = null,
    matchesPlayed: Int = 0,
    currentStreak: Int = 0,
    bestStreak: Int = 0,
    errorMessage: String? = null,
    onSelectTab: (ProfileTab) -> Unit = {},
    onToggleOfflineRecordSort: (ProfileOfflineRecordSortColumn) -> Unit = {},
    onSignOut: () -> Unit = {},
    onDebugNav: () -> Unit = {}
) {
    val spacing = AppDimens.spacing
    val forceCompactTables = LocalForceCompactTables.current
    val sound = LocalSoundPlayer.current
    val isCompactPhone = forceCompactTables || LocalConfiguration.current.screenWidthDp < 420
    val isVeryCompactPhone = forceCompactTables || LocalConfiguration.current.screenWidthDp < 360
    val tableScrollState = rememberScrollState()
    val tabEdgePadding = if (isCompactPhone) spacing.xs else spacing.md

    val tabs = listOf(
        ProfileTab.OfflineRecord to stringResource(ResourceR.string.core_resources_profile_tab_offline_record),
        ProfileTab.Data to stringResource(ResourceR.string.core_resources_profile_tab_data)
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(spacing.sm),
            verticalArrangement = Arrangement.spacedBy(spacing.sm)
        ) {
            item(contentType = "banner") {
                ProfileBannerCard()
            }
            item(contentType = "header") {
                SectionHeader(
                    title = stringResource(ResourceR.string.core_resources_profile_title),
                    subtitle = stringResource(ResourceR.string.core_resources_profile_subtitle),
                    modifier = Modifier.padding(horizontal = spacing.xs, vertical = spacing.xs)
                )
            }

            item(contentType = "tabs") {
                PrimaryScrollableTabRow(
                    selectedTabIndex = tabs.indexOfFirst { it.first == selectedTab },
                    edgePadding = tabEdgePadding
                ) {
                    tabs.forEach { (tab, label) ->
                        Tab(
                            selected = selectedTab == tab,
                            onClick = { sound(Sounds.CLK_CASUAL); onSelectTab(tab) },
                            text = {
                                Text(
                                    text = label,
                                    style = if (isVeryCompactPhone) {
                                        MaterialTheme.typography.labelMedium
                                    } else {
                                        MaterialTheme.typography.titleSmall
                                    },
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        )
                    }
                }
            }

            when (selectedTab) {
                ProfileTab.OfflineRecord -> {
                    if (isLoadingOfflineRecords) {
                        item(contentType = "loading") {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = spacing.xl),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    } else if (offlineRecordRows.isEmpty()) {
                        item(contentType = "empty") {
                            EmptyRecordState()
                        }
                    } else {
                        item(contentType = "tableHeader") {
                            ProfileOfflineRecordHeader(
                                scrollState = tableScrollState,
                                currentSort = offlineRecordSort,
                                onSortBy = onToggleOfflineRecordSort
                            )
                        }
                        itemsIndexed(
                            items = offlineRecordRows,
                            key = { _, row -> row.playerName },
                            contentType = { _, _ -> "tableRow" }
                        ) { index, row ->
                            ProfileOfflineRecordRow(
                                row = row,
                                isStriped = index % 2 == 1,
                                sortedColumn = offlineRecordSort.column,
                                scrollState = tableScrollState,
                                textStyle = if (isVeryCompactPhone) {
                                    MaterialTheme.typography.bodySmall
                                } else {
                                    MaterialTheme.typography.bodyMedium
                                }
                            )
                        }
                    }
                }

                ProfileTab.Data -> {
                    item(contentType = "statRole") {
                        val rolePreferenceLabel = when (rolePreference) {
                            ProfileRolePreference.ImpostorHunter ->
                                stringResource(ResourceR.string.core_resources_profile_role_impostor_hunter)
                            ProfileRolePreference.ImpostorSpecialist ->
                                stringResource(ResourceR.string.core_resources_profile_role_impostor_specialist)
                        }
                        val roleIcon = when (rolePreference) {
                            ProfileRolePreference.ImpostorHunter -> Icons.Outlined.Groups
                            ProfileRolePreference.ImpostorSpecialist -> Icons.Outlined.ManageAccounts
                        }
                        val roleColor = when (rolePreference) {
                            ProfileRolePreference.ImpostorHunter -> MaterialTheme.colorScheme.secondary
                            ProfileRolePreference.ImpostorSpecialist -> MaterialTheme.colorScheme.primary
                        }
                        ProfileStatCard(
                            icon = roleIcon,
                            label = stringResource(ResourceR.string.core_resources_phase3_profile_stat_role),
                            value = rolePreferenceLabel,
                            tint = roleColor,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item(contentType = "statCategory") {
                        val favoriteCategoryLabel = favoriteCategory?.let { stringResource(it.nameRes) }
                            ?: stringResource(ResourceR.string.core_resources_debug_no_data)
                        ProfileStatCard(
                            icon = Icons.Outlined.Category,
                            label = stringResource(ResourceR.string.core_resources_phase3_profile_stat_category),
                            value = favoriteCategoryLabel,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item(contentType = "statNumbers") {
                        ProfileNumberStats(
                            matchesPlayed = matchesPlayed,
                            currentStreak = currentStreak,
                            bestStreak = bestStreak,
                            useTiles = !isCompactPhone && LocalDensity.current.fontScale <= STAT_TILES_MAX_FONT_SCALE
                        )
                    }
                }
            }

            if (!errorMessage.isNullOrBlank()) {
                item(contentType = "error") {
                    StatusChip(
                        text = errorMessage,
                        variant = StatusChipVariant.Error,
                        icon = Icons.Outlined.ErrorOutline
                    )
                }
            }

            item(contentType = "signOut") {
                OutlinedButton(
                    onClick = { sound(Sounds.CLK_INSTANT); onSignOut() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = MinRowHeight),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(ResourceR.string.core_resources_settings_sign_out))
                }
            }

            if (BuildConfig.DEBUG) {
                item(contentType = "debug") {
                    Button(
                        onClick = { sound(Sounds.CLK_TAP); onDebugNav() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = MinRowHeight)
                    ) {
                        Text(stringResource(ResourceR.string.core_resources_settings_open_debug_tools))
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyRecordState() {
    val spacing = AppDimens.spacing
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacing.md)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.History,
                    contentDescription = null,
                    modifier = Modifier.size(44.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Text(
                text = stringResource(ResourceR.string.core_resources_profile_offline_empty),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(ResourceR.string.core_resources_phase3_profile_empty_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/** Icon in a tinted circle followed by a small label and a prominent value. */
@Composable
private fun ProfileStatCard(
    icon: ImageVector,
    label: String,
    value: String,
    tint: Color,
    modifier: Modifier = Modifier,
    stacked: Boolean = false
) {
    val spacing = AppDimens.spacing
    AppCard(modifier = modifier.semantics(mergeDescendants = true) {}) {
        val iconBadge: @Composable () -> Unit = {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        val texts: @Composable () -> Unit = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = if (stacked) {
                    MaterialTheme.typography.headlineSmall
                } else {
                    MaterialTheme.typography.titleMedium
                },
                fontWeight = FontWeight.SemiBold
            )
        }
        if (stacked) {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                iconBadge()
                Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) { texts() }
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.md)
            ) {
                iconBadge()
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(spacing.xs)
                ) { texts() }
            }
        }
    }
}

@Composable
private fun ProfileNumberStats(
    matchesPlayed: Int,
    currentStreak: Int,
    bestStreak: Int,
    useTiles: Boolean
) {
    val spacing = AppDimens.spacing
    val matches = stringResource(ResourceR.string.core_resources_phase3_profile_stat_matches)
    val streak = stringResource(ResourceR.string.core_resources_phase3_profile_stat_streak)
    val best = stringResource(ResourceR.string.core_resources_phase3_profile_stat_best_streak)
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    if (useTiles) {
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
            ProfileStatCard(
                icon = Icons.Outlined.Groups, label = matches, value = matchesPlayed.toString(),
                tint = primary, modifier = Modifier.weight(1f), stacked = true
            )
            ProfileStatCard(
                icon = Icons.Outlined.OfflineBolt, label = streak, value = currentStreak.toString(),
                tint = secondary, modifier = Modifier.weight(1f), stacked = true
            )
            ProfileStatCard(
                icon = Icons.Outlined.EmojiEvents, label = best, value = bestStreak.toString(),
                tint = GoldColor, modifier = Modifier.weight(1f), stacked = true
            )
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
            ProfileStatCard(
                icon = Icons.Outlined.Groups, label = matches, value = matchesPlayed.toString(),
                tint = primary, modifier = Modifier.fillMaxWidth()
            )
            ProfileStatCard(
                icon = Icons.Outlined.OfflineBolt, label = streak, value = currentStreak.toString(),
                tint = secondary, modifier = Modifier.fillMaxWidth()
            )
            ProfileStatCard(
                icon = Icons.Outlined.EmojiEvents, label = best, value = bestStreak.toString(),
                tint = GoldColor, modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ProfileOfflineRecordHeader(
    scrollState: ScrollState,
    currentSort: ProfileOfflineRecordSort = ProfileOfflineRecordSort(),
    onSortBy: (ProfileOfflineRecordSortColumn) -> Unit = {}
) {
    ProfileOfflineRecordCells(
        labels = listOf(
            stringResource(ResourceR.string.core_resources_ranking_col_position),
            stringResource(ResourceR.string.core_resources_ranking_col_player),
            stringResource(ResourceR.string.core_resources_ranking_col_civilian_wins),
            stringResource(ResourceR.string.core_resources_ranking_col_impostor_wins),
            stringResource(ResourceR.string.core_resources_ranking_col_total),
            stringResource(ResourceR.string.core_resources_profile_col_current_streak)
        ),
        textStyle = MaterialTheme.typography.titleSmall,
        scrollState = scrollState,
        isHeader = true,
        isStriped = false,
        currentSort = currentSort,
        onSortBy = onSortBy
    )
}

@Composable
private fun ProfileOfflineRecordRow(
    row: OfflineProfileRecordRowUi,
    isStriped: Boolean,
    sortedColumn: ProfileOfflineRecordSortColumn,
    scrollState: ScrollState,
    textStyle: TextStyle
) {
    ProfileOfflineRecordCells(
        labels = listOf(
            row.position.toString(),
            row.playerName,
            row.civilianWins.toString(),
            row.impostorWins.toString(),
            row.totalWins.toString(),
            row.currentStreak.toString()
        ),
        textStyle = textStyle,
        scrollState = scrollState,
        isHeader = false,
        isStriped = isStriped,
        currentSort = ProfileOfflineRecordSort(column = sortedColumn)
    )
}

@Composable
@Suppress("LongParameterList")
private fun ProfileOfflineRecordCells(
    labels: List<String>,
    textStyle: TextStyle,
    scrollState: ScrollState,
    isHeader: Boolean,
    isStriped: Boolean,
    currentSort: ProfileOfflineRecordSort,
    onSortBy: (ProfileOfflineRecordSortColumn) -> Unit = {}
) {
    val spacing = AppDimens.spacing
    val rowBackground = when {
        isHeader -> MaterialTheme.colorScheme.surfaceContainerHigh
        isStriped -> MaterialTheme.colorScheme.surfaceContainerLow
        else -> Color.Transparent
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(if (isHeader) MaterialTheme.shapes.small else MaterialTheme.shapes.extraSmall)
            .background(rowBackground)
    ) {
        Row(
            modifier = Modifier
                .horizontalScroll(scrollState)
                .padding(horizontal = spacing.sm)
        ) {
            ProfileColumns.forEachIndexed { index, spec ->
                val isSorted = currentSort.column == spec.column
                ProfileTableCell(
                    text = labels[index],
                    style = textStyle,
                    spec = spec,
                    isHeader = isHeader,
                    isSorted = isSorted,
                    sortDirection = currentSort.direction.takeIf { isHeader && isSorted },
                    onClick = { onSortBy(spec.column) }
                )
            }
        }
    }
}

@Composable
@Suppress("LongMethod", "LongParameterList")
private fun ProfileTableCell(
    text: String,
    style: TextStyle,
    spec: ProfileColumn,
    isHeader: Boolean,
    isSorted: Boolean,
    sortDirection: ProfileSortDirection?,
    onClick: () -> Unit
) {
    val sound = LocalSoundPlayer.current
    val tint = if (isSorted) {
        MaterialTheme.colorScheme.primary.copy(
            alpha = if (isHeader) SORTED_COLUMN_ALPHA_HEADER else SORTED_COLUMN_ALPHA_ROW
        )
    } else {
        Color.Transparent
    }
    val cellModifier = Modifier
        .width(spec.width)
        .heightIn(min = MinRowHeight)
        .background(tint)
        .padding(horizontal = AppDimens.spacing.xs)

    if (!isHeader) {
        Box(
            modifier = cellModifier,
            contentAlignment = if (spec.isNumeric) Alignment.Center else Alignment.CenterStart
        ) {
            Text(
                text = text,
                style = style,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = if (spec.isNumeric) TextAlign.Center else TextAlign.Start,
                fontWeight = if (isSorted) FontWeight.SemiBold else FontWeight.Normal
            )
        }
        return
    }

    val animatedHeaderColor = animateColorAsState(
        targetValue = if (isSorted) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(durationMillis = TableSortAnimation.HeaderColorDurationMs),
        label = "profileHeaderSortColor"
    )
    Row(
        modifier = cellModifier.clickable { sound(Sounds.CLK_CLOCK); onClick() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (spec.isNumeric) Arrangement.Center else Arrangement.Start
    ) {
        Text(
            text = text,
            style = style,
            color = animatedHeaderColor.value,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = if (isSorted) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.weight(1f, fill = false)
        )
        AnimatedVisibility(
            visible = sortDirection != null,
            enter = fadeIn(animationSpec = tween(TableSortAnimation.IconEnterDurationMs)) +
                scaleIn(
                    initialScale = TableSortAnimation.IconScaleCollapsed,
                    animationSpec = tween(TableSortAnimation.IconEnterDurationMs)
                ),
            exit = fadeOut(animationSpec = tween(TableSortAnimation.IconExitDurationMs)) +
                scaleOut(
                    targetScale = TableSortAnimation.IconScaleCollapsed,
                    animationSpec = tween(TableSortAnimation.IconExitDurationMs)
                )
        ) {
            val ascending = sortDirection == ProfileSortDirection.Ascending
            Icon(
                imageVector = if (ascending) Icons.Outlined.ArrowUpward else Icons.Outlined.ArrowDownward,
                contentDescription = stringResource(
                    if (ascending) {
                        ResourceR.string.core_resources_phase3_sorted_ascending
                    } else {
                        ResourceR.string.core_resources_phase3_sorted_descending
                    }
                ),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

private val PreviewRows = persistentListOf(
    OfflineProfileRecordRowUi(1, "Ana", 5, 4, 9, 3),
    OfflineProfileRecordRowUi(2, "Luis", 4, 3, 7, 0),
    OfflineProfileRecordRowUi(3, "Marta", 2, 4, 6, 1)
)

@UiModePreviews
@Composable
private fun Preview() {
    AppTheme {
        ProfileDesign(offlineRecordRows = PreviewRows)
    }
}

@UiModePreviews
@Composable
private fun EmptyPreview() {
    AppTheme {
        ProfileDesign()
    }
}

@UiModePreviews
@Composable
private fun DataPreview() {
    AppTheme {
        ProfileDesign(
            selectedTab = ProfileTab.Data,
            matchesPlayed = 12,
            currentStreak = 2,
            bestStreak = 5
        )
    }
}
