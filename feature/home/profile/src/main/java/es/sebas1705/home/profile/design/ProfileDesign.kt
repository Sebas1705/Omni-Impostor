package es.sebas1705.home.profile.design

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.ManageAccounts
import androidx.compose.material.icons.outlined.OfflineBolt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import es.sebas1705.common.utlis.UiModePreviews
import es.sebas1705.feature.home.profile.BuildConfig
import es.sebas1705.home.profile.components.ProfileHeaderCard
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
import es.sebas1705.core.resources.Sounds
import es.sebas1705.ui.sound.LocalSoundPlayer
import es.sebas1705.ui.theme.AppTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import es.sebas1705.core.resources.R as ResourceR

@Composable
@Suppress("LongMethod", "LongParameterList", "CyclomaticComplexMethod")
fun ProfileDesign(
    modifier: Modifier = Modifier,
    userName: String? = null,
    userEmail: String? = null,
    userPhotoUrl: String? = null,
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
    val forceCompactTables = LocalForceCompactTables.current
    val sound = LocalSoundPlayer.current
    val isCompactPhone = forceCompactTables || LocalConfiguration.current.screenWidthDp < 420
    val isVeryCompactPhone = forceCompactTables || LocalConfiguration.current.screenWidthDp < 360
    val tableScrollState = rememberScrollState()
    val tabEdgePadding = if (isCompactPhone) 4.dp else 12.dp

    val tabs = listOf(
        ProfileTab.OfflineRecord to stringResource(ResourceR.string.core_resources_profile_tab_offline_record),
        ProfileTab.Online to stringResource(ResourceR.string.core_resources_profile_tab_online),
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
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item(contentType = "header") {
                ProfileHeaderCard(
                    displayName = userName,
                    email = userEmail,
                    photoUrl = userPhotoUrl,
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
                                    .padding(vertical = 24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    } else if (offlineRecordRows.isEmpty()) {
                        item(contentType = "empty") {
                            Text(
                                text = stringResource(ResourceR.string.core_resources_profile_offline_empty),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        item(contentType = "tableHeader") {
                            ProfileOfflineRecordHeader(
                                scrollState = tableScrollState,
                                currentSort = offlineRecordSort,
                                onSortBy = onToggleOfflineRecordSort
                            )
                        }
                        items(
                            items = offlineRecordRows,
                            key = { row -> row.playerName },
                            contentType = { _ -> "tableRow" }
                        ) { row ->
                            ProfileOfflineRecordRow(
                                row = row,
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

                ProfileTab.Online -> {
                    item(contentType = "online") {
                        Text(
                            text = stringResource(ResourceR.string.core_resources_profile_online_pending),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            textAlign = TextAlign.Center
                        )
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
                        ProfileStatRow(
                            icon = roleIcon,
                            text = stringResource(
                                ResourceR.string.core_resources_profile_row_role_preference,
                                rolePreferenceLabel
                            ),
                            tint = roleColor
                        )
                    }
                    item(contentType = "statCategory") {
                        val favoriteCategoryLabel = favoriteCategory?.let { stringResource(it.nameRes) }
                            ?: stringResource(ResourceR.string.core_resources_debug_no_data)
                        ProfileStatRow(
                            icon = Icons.Outlined.Category,
                            text = stringResource(
                                ResourceR.string.core_resources_profile_row_favorite_category,
                                favoriteCategoryLabel
                            ),
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    item(contentType = "statMatches") {
                        ProfileStatRow(
                            icon = Icons.Outlined.Groups,
                            text = stringResource(
                                ResourceR.string.core_resources_profile_row_matches_played,
                                matchesPlayed
                            ),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    item(contentType = "statStreak") {
                        ProfileStatRow(
                            icon = Icons.Outlined.OfflineBolt,
                            text = stringResource(
                                ResourceR.string.core_resources_profile_row_current_streak,
                                currentStreak
                            ),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                    item(contentType = "statBestStreak") {
                        ProfileStatRow(
                            icon = Icons.Outlined.EmojiEvents,
                            text = stringResource(
                                ResourceR.string.core_resources_profile_row_best_streak,
                                bestStreak
                            ),
                            tint = Color(0xFFFFD700)
                        )
                    }
                }
            }

            if (!errorMessage.isNullOrBlank()) {
                item(contentType = "error") {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            item(contentType = "signOut") {
                OutlinedButton(
                    onClick = { sound(Sounds.CLK_INSTANT); onSignOut() },
                    modifier = Modifier.fillMaxWidth(),
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
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(ResourceR.string.core_resources_settings_open_debug_tools))
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileStatRow(
    icon: ImageVector,
    text: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ProfileOfflineRecordHeader(
    scrollState: androidx.compose.foundation.ScrollState,
    currentSort: ProfileOfflineRecordSort = ProfileOfflineRecordSort(),
    onSortBy: (ProfileOfflineRecordSortColumn) -> Unit = {}
) {
    ProfileOfflineRecordCells(
        position = stringResource(ResourceR.string.core_resources_ranking_col_position),
        player = stringResource(ResourceR.string.core_resources_ranking_col_player),
        civilianWins = stringResource(ResourceR.string.core_resources_ranking_col_civilian_wins),
        impostorWins = stringResource(ResourceR.string.core_resources_ranking_col_impostor_wins),
        totalWins = stringResource(ResourceR.string.core_resources_ranking_col_total),
        currentStreak = stringResource(ResourceR.string.core_resources_profile_col_current_streak),
        textStyle = MaterialTheme.typography.titleSmall,
        scrollState = scrollState,
        isHeader = true,
        currentSort = currentSort,
        onSortBy = onSortBy
    )
}

@Composable
private fun ProfileOfflineRecordRow(
    row: OfflineProfileRecordRowUi,
    scrollState: androidx.compose.foundation.ScrollState,
    textStyle: TextStyle
) {
    ProfileOfflineRecordCells(
        position = row.position.toString(),
        player = row.playerName,
        civilianWins = row.civilianWins.toString(),
        impostorWins = row.impostorWins.toString(),
        totalWins = row.totalWins.toString(),
        currentStreak = row.currentStreak.toString(),
        textStyle = textStyle,
        scrollState = scrollState
    )
}

@Composable
@Suppress("LongMethod", "LongParameterList")
private fun ProfileOfflineRecordCells(
    position: String,
    player: String,
    civilianWins: String,
    impostorWins: String,
    totalWins: String,
    currentStreak: String,
    textStyle: TextStyle,
    scrollState: androidx.compose.foundation.ScrollState,
    isHeader: Boolean = false,
    currentSort: ProfileOfflineRecordSort = ProfileOfflineRecordSort(),
    onSortBy: (ProfileOfflineRecordSortColumn) -> Unit = {}
) {
    Row(
        modifier = Modifier
            .then(
                if (isHeader) {
                    Modifier.background(MaterialTheme.colorScheme.background)
                } else {
                    Modifier
                }
            )
            .horizontalScroll(scrollState)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        ProfileTableCell(
            text = position,
            style = textStyle,
            modifier = Modifier.width(44.dp),
            isHeader = isHeader,
            sortDirection = currentSort.direction.takeIf {
                isHeader && currentSort.column == ProfileOfflineRecordSortColumn.Position
            },
            onClick = { onSortBy(ProfileOfflineRecordSortColumn.Position) }
        )
        ProfileTableCell(
            text = player,
            style = textStyle,
            modifier = Modifier.width(140.dp),
            isHeader = isHeader,
            sortDirection = currentSort.direction.takeIf {
                isHeader && currentSort.column == ProfileOfflineRecordSortColumn.Player
            },
            onClick = { onSortBy(ProfileOfflineRecordSortColumn.Player) }
        )
        ProfileTableCell(
            text = civilianWins,
            style = textStyle,
            modifier = Modifier.width(92.dp),
            isHeader = isHeader,
            sortDirection = currentSort.direction.takeIf {
                isHeader && currentSort.column == ProfileOfflineRecordSortColumn.CivilianWins
            },
            onClick = { onSortBy(ProfileOfflineRecordSortColumn.CivilianWins) }
        )
        ProfileTableCell(
            text = impostorWins,
            style = textStyle,
            modifier = Modifier.width(92.dp),
            isHeader = isHeader,
            sortDirection = currentSort.direction.takeIf {
                isHeader && currentSort.column == ProfileOfflineRecordSortColumn.ImpostorWins
            },
            onClick = { onSortBy(ProfileOfflineRecordSortColumn.ImpostorWins) }
        )
        ProfileTableCell(
            text = totalWins,
            style = textStyle,
            modifier = Modifier.width(92.dp),
            isHeader = isHeader,
            sortDirection = currentSort.direction.takeIf {
                isHeader && currentSort.column == ProfileOfflineRecordSortColumn.TotalWins
            },
            onClick = { onSortBy(ProfileOfflineRecordSortColumn.TotalWins) }
        )
        ProfileTableCell(
            text = currentStreak,
            style = textStyle,
            modifier = Modifier.width(92.dp),
            isHeader = isHeader,
            sortDirection = currentSort.direction.takeIf {
                isHeader && currentSort.column == ProfileOfflineRecordSortColumn.CurrentStreak
            },
            onClick = { onSortBy(ProfileOfflineRecordSortColumn.CurrentStreak) }
        )
    }
}

@Composable
@Suppress("LongMethod")
private fun ProfileTableCell(
    text: String,
    style: TextStyle,
    modifier: Modifier,
    isHeader: Boolean,
    sortDirection: ProfileSortDirection?,
    onClick: () -> Unit
) {
    val sound = LocalSoundPlayer.current
    if (!isHeader) {
        Text(
            text = text,
            style = style,
            modifier = modifier,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = FontWeight.Normal
        )
        return
    }

    Row(
        modifier = modifier.clickable { sound(Sounds.CLK_CLOCK); onClick() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        val animatedHeaderColor = animateColorAsState(
            targetValue = if (sortDirection != null) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            animationSpec = tween(durationMillis = TableSortAnimation.HeaderColorDurationMs),
            label = "profileHeaderSortColor"
        )
        Text(
            text = text,
            style = style,
            color = animatedHeaderColor.value,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = if (sortDirection != null) FontWeight.SemiBold else FontWeight.Normal
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
            Icon(
                imageVector = if (sortDirection == ProfileSortDirection.Ascending) {
                    Icons.Outlined.ArrowUpward
                } else {
                    Icons.Outlined.ArrowDownward
                },
                contentDescription = null,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

@UiModePreviews
@Composable
private fun Preview() {
    AppTheme {
        ProfileDesign()
    }
}
