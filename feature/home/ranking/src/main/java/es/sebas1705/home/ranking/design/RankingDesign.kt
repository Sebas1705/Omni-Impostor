package es.sebas1705.home.ranking.design

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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.CloudQueue
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import es.sebas1705.common.utlis.UiModePreviews
import es.sebas1705.home.ranking.components.RankingBannerCard
import es.sebas1705.home.ranking.models.OfflineRankingRowUi
import es.sebas1705.home.ranking.viewmodel.RankingOfflineSort
import es.sebas1705.home.ranking.viewmodel.RankingOfflineSortColumn
import es.sebas1705.home.ranking.viewmodel.RankingSortDirection
import es.sebas1705.home.ranking.viewmodel.RankingTab
import es.sebas1705.ui.adaptive.LocalForceCompactTables
import es.sebas1705.ui.adaptive.TableSortAnimation
import es.sebas1705.core.resources.Sounds
import es.sebas1705.ui.sound.LocalSoundPlayer
import es.sebas1705.ui.theme.AppTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import es.sebas1705.core.resources.R as ResourceR

private val GoldColor = Color(0xFFFFD700)
private val SilverColor = Color(0xFFB8B8C8)
private val BronzeColor = Color(0xFFCD8C4A)

private fun medalColor(position: Int): Color? = when (position) {
    1 -> GoldColor
    2 -> SilverColor
    3 -> BronzeColor
    else -> null
}

@Composable
@Suppress("LongMethod")
fun RankingDesign(
    modifier: Modifier = Modifier,
    selectedTab: RankingTab = RankingTab.Offline,
    offlineRows: ImmutableList<OfflineRankingRowUi> = persistentListOf(),
    offlineSort: RankingOfflineSort = RankingOfflineSort(),
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onSelectTab: (RankingTab) -> Unit = {},
    onToggleOfflineSort: (RankingOfflineSortColumn) -> Unit = {}
) {
    val forceCompactTables = LocalForceCompactTables.current
    val sound = LocalSoundPlayer.current
    val isCompactPhone = forceCompactTables || LocalConfiguration.current.screenWidthDp < 420
    val isVeryCompactPhone = forceCompactTables || LocalConfiguration.current.screenWidthDp < 360
    val tableScrollState = rememberScrollState()
    val tabEdgePadding = if (isCompactPhone) 4.dp else 12.dp

    val tabs = listOf(
        RankingTab.Offline to stringResource(ResourceR.string.core_resources_ranking_tab_offline),
        RankingTab.Online to stringResource(ResourceR.string.core_resources_ranking_tab_online)
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
            item(contentType = "banner") {
                RankingBannerCard()
            }
            item(contentType = "title") {
                Text(
                    text = stringResource(ResourceR.string.core_resources_ranking_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            item(contentType = "subtitle") {
                Text(
                    text = stringResource(ResourceR.string.core_resources_ranking_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                RankingTab.Online -> {
                    item(contentType = "online") {
                        OnlineComingSoon()
                    }
                }

                RankingTab.Offline -> {
                    if (isLoading) {
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
                    } else if (offlineRows.isEmpty()) {
                        item(contentType = "empty") {
                            EmptyRankingState()
                        }
                    } else {
                        item(contentType = "podium") {
                            OfflineRankingPodium(offlineRows)
                        }
                        item(contentType = "tableHeader") {
                            RankingTableHeader(
                                scrollState = tableScrollState,
                                currentSort = offlineSort,
                                onSortBy = onToggleOfflineSort
                            )
                        }
                        items(
                            items = offlineRows,
                            key = { row -> row.playerName },
                            contentType = { _ -> "tableRow" }
                        ) { row ->
                            RankingTableRow(
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
        }
    }
}

@Composable
private fun OfflineRankingPodium(rows: ImmutableList<OfflineRankingRowUi>) {
    val first = rows.getOrNull(0)
    val second = rows.getOrNull(1)
    val third = rows.getOrNull(2)

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            if (second != null) {
                PodiumSlot(name = second.playerName, wins = second.totalWins, place = 2, modifier = Modifier.weight(1f))
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
            if (first != null) {
                PodiumSlot(name = first.playerName, wins = first.totalWins, place = 1, modifier = Modifier.weight(1f))
            }
            if (third != null) {
                PodiumSlot(name = third.playerName, wins = third.totalWins, place = 3, modifier = Modifier.weight(1f))
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun PodiumSlot(name: String, wins: Int, place: Int, modifier: Modifier = Modifier) {
    val color = when (place) {
        1 -> GoldColor
        2 -> SilverColor
        else -> BronzeColor
    }
    val podiumHeight = when (place) {
        1 -> 72.dp
        2 -> 52.dp
        else -> 40.dp
    }
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.EmojiEvents,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(ResourceR.string.core_resources_ranking_col_total) + ": $wins",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(podiumHeight)
                .background(
                    color = color.copy(alpha = 0.15f),
                    shape = MaterialTheme.shapes.small
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "#$place",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun EmptyRankingState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.EmojiEvents,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        )
        Text(
            text = stringResource(ResourceR.string.core_resources_ranking_offline_empty),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun OnlineComingSoon() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.CloudQueue,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        )
        Text(
            text = stringResource(ResourceR.string.core_resources_ranking_online_not_implemented),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun RankingTableHeader(
    scrollState: androidx.compose.foundation.ScrollState,
    currentSort: RankingOfflineSort,
    onSortBy: (RankingOfflineSortColumn) -> Unit
) {
    RankingTableCells(
        position = stringResource(ResourceR.string.core_resources_ranking_col_position),
        player = stringResource(ResourceR.string.core_resources_ranking_col_player),
        civilianWins = stringResource(ResourceR.string.core_resources_ranking_col_civilian_wins),
        impostorWins = stringResource(ResourceR.string.core_resources_ranking_col_impostor_wins),
        totalWins = stringResource(ResourceR.string.core_resources_ranking_col_total),
        textStyle = MaterialTheme.typography.titleSmall,
        scrollState = scrollState,
        isHeader = true,
        currentSort = currentSort,
        onSortBy = onSortBy
    )
}

@Composable
private fun RankingTableRow(
    row: OfflineRankingRowUi,
    scrollState: androidx.compose.foundation.ScrollState,
    textStyle: TextStyle
) {
    RankingTableCells(
        position = row.position.toString(),
        player = row.playerName,
        civilianWins = row.civilianWins.toString(),
        impostorWins = row.impostorWins.toString(),
        totalWins = row.totalWins.toString(),
        textStyle = textStyle,
        scrollState = scrollState,
        positionMedalColor = medalColor(row.position)
    )
}

@Composable
@Suppress("LongParameterList", "LongMethod")
private fun RankingTableCells(
    position: String,
    player: String,
    civilianWins: String,
    impostorWins: String,
    totalWins: String,
    textStyle: TextStyle,
    scrollState: androidx.compose.foundation.ScrollState,
    isHeader: Boolean = false,
    currentSort: RankingOfflineSort = RankingOfflineSort(),
    onSortBy: (RankingOfflineSortColumn) -> Unit = {},
    positionMedalColor: Color? = null
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
        RankingTableCell(
            text = position,
            style = textStyle,
            modifier = Modifier.width(44.dp),
            isHeader = isHeader,
            sortDirection = currentSort.direction.takeIf {
                isHeader && currentSort.column == RankingOfflineSortColumn.Position
            },
            onClick = { onSortBy(RankingOfflineSortColumn.Position) },
            medalColor = if (!isHeader) positionMedalColor else null
        )
        RankingTableCell(
            text = player,
            style = textStyle,
            modifier = Modifier.width(140.dp),
            isHeader = isHeader,
            sortDirection = currentSort.direction.takeIf {
                isHeader && currentSort.column == RankingOfflineSortColumn.Player
            },
            onClick = { onSortBy(RankingOfflineSortColumn.Player) },
            medalColor = if (!isHeader) positionMedalColor else null
        )
        RankingTableCell(
            text = civilianWins,
            style = textStyle,
            modifier = Modifier.width(92.dp),
            isHeader = isHeader,
            sortDirection = currentSort.direction.takeIf {
                isHeader && currentSort.column == RankingOfflineSortColumn.CivilianWins
            },
            onClick = { onSortBy(RankingOfflineSortColumn.CivilianWins) }
        )
        RankingTableCell(
            text = impostorWins,
            style = textStyle,
            modifier = Modifier.width(92.dp),
            isHeader = isHeader,
            sortDirection = currentSort.direction.takeIf {
                isHeader && currentSort.column == RankingOfflineSortColumn.ImpostorWins
            },
            onClick = { onSortBy(RankingOfflineSortColumn.ImpostorWins) }
        )
        RankingTableCell(
            text = totalWins,
            style = textStyle,
            modifier = Modifier.width(92.dp),
            isHeader = isHeader,
            sortDirection = currentSort.direction.takeIf {
                isHeader && currentSort.column == RankingOfflineSortColumn.TotalWins
            },
            onClick = { onSortBy(RankingOfflineSortColumn.TotalWins) }
        )
    }
}

@Composable
@Suppress("LongMethod")
private fun RankingTableCell(
    text: String,
    style: TextStyle,
    modifier: Modifier,
    isHeader: Boolean,
    sortDirection: RankingSortDirection?,
    onClick: () -> Unit,
    medalColor: Color? = null
) {
    val sound = LocalSoundPlayer.current
    if (!isHeader) {
        Text(
            text = text,
            style = style,
            modifier = modifier,
            color = medalColor ?: MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = if (medalColor != null) FontWeight.Bold else FontWeight.Normal
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
            label = "rankingHeaderSortColor"
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
                imageVector = if (sortDirection == RankingSortDirection.Ascending) {
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
        RankingDesign()
    }
}
