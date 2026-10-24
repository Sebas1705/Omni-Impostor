@file:Suppress("TooManyFunctions")

package es.sebas1705.home.ranking.design

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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import es.sebas1705.common.utlis.UiModePreviews
import es.sebas1705.core.resources.Sounds
import es.sebas1705.home.ranking.components.RankingBannerCard
import es.sebas1705.home.ranking.models.OfflineRankingRowUi
import es.sebas1705.home.ranking.viewmodel.RankingOfflineSort
import es.sebas1705.home.ranking.viewmodel.RankingOfflineSortColumn
import es.sebas1705.home.ranking.viewmodel.RankingSortDirection
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
private val SilverColor = Color(0xFFB8B8C8)
private val BronzeColor = Color(0xFFCD8C4A)
private val MedalContentColor = Color(0xFF1B1B1F)

private val MinRowHeight = 48.dp
private const val SORTED_COLUMN_ALPHA_HEADER = 0.16f
private const val SORTED_COLUMN_ALPHA_ROW = 0.08f

private fun medalColor(place: Int): Color? = when (place) {
    1 -> GoldColor
    2 -> SilverColor
    3 -> BronzeColor
    else -> null
}

/** Fixed table layout: width and alignment of each column. */
private data class RankingColumn(
    val column: RankingOfflineSortColumn,
    val width: Dp,
    val isNumeric: Boolean
)

private val RankingColumns = listOf(
    RankingColumn(RankingOfflineSortColumn.Position, 60.dp, isNumeric = true),
    RankingColumn(RankingOfflineSortColumn.Player, 140.dp, isNumeric = false),
    RankingColumn(RankingOfflineSortColumn.CivilianWins, 96.dp, isNumeric = true),
    RankingColumn(RankingOfflineSortColumn.ImpostorWins, 96.dp, isNumeric = true),
    RankingColumn(RankingOfflineSortColumn.TotalWins, 96.dp, isNumeric = true)
)

@Composable
@Suppress("LongMethod")
fun RankingDesign(
    modifier: Modifier = Modifier,
    offlineRows: ImmutableList<OfflineRankingRowUi> = persistentListOf(),
    offlineSort: RankingOfflineSort = RankingOfflineSort(),
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onToggleOfflineSort: (RankingOfflineSortColumn) -> Unit = {}
) {
    val spacing = AppDimens.spacing
    val forceCompactTables = LocalForceCompactTables.current
    val isVeryCompactPhone = forceCompactTables || LocalConfiguration.current.screenWidthDp < 360
    val tableScrollState = rememberScrollState()
    // Medals follow the real standing (total wins), not the order the user chose for the table.
    val standing = remember(offlineRows) {
        offlineRows
            .sortedWith(compareByDescending<OfflineRankingRowUi> { it.totalWins }.thenBy { it.playerName.lowercase() })
    }
    val placeByPlayer = remember(standing) {
        standing.mapIndexed { index, row -> row.playerName to index + 1 }.toMap()
    }

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
                RankingBannerCard()
            }
            item(contentType = "header") {
                SectionHeader(
                    title = stringResource(ResourceR.string.core_resources_ranking_title),
                    subtitle = stringResource(ResourceR.string.core_resources_ranking_subtitle),
                    modifier = Modifier.padding(horizontal = spacing.xs, vertical = spacing.xs)
                )
            }

            if (isLoading) {
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
            } else if (offlineRows.isEmpty()) {
                item(contentType = "empty") {
                    EmptyRankingState()
                }
            } else {
                item(contentType = "podium") {
                    OfflineRankingPodium(standing)
                }
                item(contentType = "tableHeader") {
                    RankingTableHeader(
                        scrollState = tableScrollState,
                        currentSort = offlineSort,
                        onSortBy = onToggleOfflineSort
                    )
                }
                itemsIndexed(
                    items = offlineRows,
                    key = { _, row -> row.playerName },
                    contentType = { _, _ -> "tableRow" }
                ) { index, row ->
                    RankingTableRow(
                        row = row,
                        place = placeByPlayer[row.playerName] ?: row.position,
                        isStriped = index % 2 == 1,
                        sortedColumn = offlineSort.column,
                        scrollState = tableScrollState,
                        textStyle = if (isVeryCompactPhone) {
                            MaterialTheme.typography.bodySmall
                        } else {
                            MaterialTheme.typography.bodyMedium
                        }
                    )
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
        }
    }
}

@Composable
private fun OfflineRankingPodium(standing: List<OfflineRankingRowUi>) {
    val spacing = AppDimens.spacing
    val first = standing.getOrNull(0)
    val second = standing.getOrNull(1)
    val third = standing.getOrNull(2)

    AppCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
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
    val spacing = AppDimens.spacing
    val color = medalColor(place) ?: BronzeColor
    val podiumHeight = when (place) {
        1 -> 104.dp
        2 -> 76.dp
        else -> 56.dp
    }
    val iconSize = if (place == 1) 40.dp else 28.dp
    val description = stringResource(ResourceR.string.core_resources_phase3_ranking_podium_place, place, name, wins)
    Column(
        modifier = modifier.semantics(mergeDescendants = true) { contentDescription = description },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.xs)
    ) {
        Icon(
            imageVector = Icons.Outlined.EmojiEvents,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(iconSize)
        )
        Text(
            text = name,
            style = if (place == 1) MaterialTheme.typography.titleSmall else MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(ResourceR.string.core_resources_ranking_col_total) + ": $wins",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(podiumHeight)
                .background(
                    color = color.copy(alpha = if (place == 1) 0.45f else 0.30f),
                    shape = MaterialTheme.shapes.small
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "#$place",
                style = if (place == 1) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun EmptyRankingState() {
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
                    imageVector = Icons.Outlined.EmojiEvents,
                    contentDescription = null,
                    modifier = Modifier.size(44.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Text(
                text = stringResource(ResourceR.string.core_resources_phase3_ranking_empty_title),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(ResourceR.string.core_resources_ranking_offline_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(ResourceR.string.core_resources_phase3_ranking_empty_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun RankingTableHeader(
    scrollState: ScrollState,
    currentSort: RankingOfflineSort,
    onSortBy: (RankingOfflineSortColumn) -> Unit
) {
    RankingTableCells(
        labels = listOf(
            stringResource(ResourceR.string.core_resources_ranking_col_position),
            stringResource(ResourceR.string.core_resources_ranking_col_player),
            stringResource(ResourceR.string.core_resources_ranking_col_civilian_wins),
            stringResource(ResourceR.string.core_resources_ranking_col_impostor_wins),
            stringResource(ResourceR.string.core_resources_ranking_col_total)
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
private fun RankingTableRow(
    row: OfflineRankingRowUi,
    place: Int,
    isStriped: Boolean,
    sortedColumn: RankingOfflineSortColumn,
    scrollState: ScrollState,
    textStyle: TextStyle
) {
    RankingTableCells(
        labels = listOf(
            row.position.toString(),
            row.playerName,
            row.civilianWins.toString(),
            row.impostorWins.toString(),
            row.totalWins.toString()
        ),
        textStyle = textStyle,
        scrollState = scrollState,
        isHeader = false,
        isStriped = isStriped,
        currentSort = RankingOfflineSort(column = sortedColumn),
        positionMedalColor = medalColor(place)
    )
}

@Composable
@Suppress("LongParameterList", "LongMethod")
private fun RankingTableCells(
    labels: List<String>,
    textStyle: TextStyle,
    scrollState: ScrollState,
    isHeader: Boolean,
    isStriped: Boolean,
    currentSort: RankingOfflineSort,
    onSortBy: (RankingOfflineSortColumn) -> Unit = {},
    positionMedalColor: Color? = null
) {
    val spacing = AppDimens.spacing
    val rowBackground = when {
        isHeader -> MaterialTheme.colorScheme.surfaceContainerHigh
        isStriped -> MaterialTheme.colorScheme.surfaceContainerLow
        else -> Color.Transparent
    }
    val headerShape = if (isHeader) MaterialTheme.shapes.small else MaterialTheme.shapes.extraSmall
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(headerShape)
            .background(rowBackground)
    ) {
        Row(
            modifier = Modifier
                .horizontalScroll(scrollState)
                .padding(horizontal = spacing.sm)
        ) {
            RankingColumns.forEachIndexed { index, spec ->
                val isSorted = currentSort.column == spec.column
                RankingTableCell(
                    text = labels[index],
                    style = textStyle,
                    spec = spec,
                    isHeader = isHeader,
                    isSorted = isSorted,
                    sortDirection = currentSort.direction.takeIf { isHeader && isSorted },
                    onClick = { onSortBy(spec.column) },
                    medalColor = positionMedalColor.takeIf {
                        !isHeader && spec.column == RankingOfflineSortColumn.Position
                    },
                    isBold = !isHeader && positionMedalColor != null &&
                        spec.column == RankingOfflineSortColumn.Player
                )
            }
        }
    }
}

@Composable
@Suppress("LongMethod", "LongParameterList")
private fun RankingTableCell(
    text: String,
    style: TextStyle,
    spec: RankingColumn,
    isHeader: Boolean,
    isSorted: Boolean,
    sortDirection: RankingSortDirection?,
    onClick: () -> Unit,
    medalColor: Color?,
    isBold: Boolean
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
    val alignment = if (spec.isNumeric) Alignment.Center else Alignment.CenterStart

    if (!isHeader) {
        Box(modifier = cellModifier, contentAlignment = alignment) {
            if (medalColor != null) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(medalColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = text,
                        style = style,
                        fontWeight = FontWeight.Bold,
                        color = MedalContentColor,
                        maxLines = 1
                    )
                }
            } else {
                Text(
                    text = text,
                    style = style,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = if (spec.isNumeric) TextAlign.Center else TextAlign.Start,
                    fontWeight = if (isBold || isSorted) FontWeight.SemiBold else FontWeight.Normal
                )
            }
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
        label = "rankingHeaderSortColor"
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
            val ascending = sortDirection == RankingSortDirection.Ascending
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
    OfflineRankingRowUi(1, "Ana", 5, 4, 9),
    OfflineRankingRowUi(2, "Luis", 4, 3, 7),
    OfflineRankingRowUi(3, "Marta", 2, 4, 6),
    OfflineRankingRowUi(4, "Pablo", 1, 2, 3)
)

@UiModePreviews
@Composable
private fun Preview() {
    AppTheme {
        RankingDesign(offlineRows = PreviewRows)
    }
}

@UiModePreviews
@Composable
private fun EmptyPreview() {
    AppTheme {
        RankingDesign()
    }
}

@UiModePreviews
@Composable
private fun ErrorPreview() {
    AppTheme {
        RankingDesign(errorMessage = "Could not load the ranking")
    }
}
