package es.sebas1705.game.design

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import es.sebas1705.common.utlis.UiModePreviews
import es.sebas1705.core.resources.Sounds
import es.sebas1705.ui.components.PrimaryActionButton
import es.sebas1705.ui.components.SectionHeader
import es.sebas1705.ui.components.StatusChip
import es.sebas1705.ui.components.StatusChipVariant
import es.sebas1705.ui.sound.LocalSoundPlayer
import es.sebas1705.ui.theme.AppDimens
import es.sebas1705.ui.theme.AppTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import es.sebas1705.core.resources.R as ResourceR

private const val MIN_PLAYERS = 3

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Suppress("LongMethod")
fun UserDesign(
    modifier: Modifier = Modifier,
    playerNames: ImmutableList<String> = persistentListOf(),
    onSave: (ImmutableList<String>) -> Unit = { },
    onBack: () -> Unit = {},
) {
    val playerNames = rememberSaveable(playerNames) {
        mutableStateListOf<String>().apply {
            addAll(playerNames)
            while (size < 3) add("")
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
                title = { Text(stringResource(ResourceR.string.core_resources_users_title)) },
                navigationIcon = {
                    IconButton(onClick = { sound(Sounds.CLK_TAP); onBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(ResourceR.string.core_resources_back)
                        )
                    }
                }
            )
        },
        bottomBar = {
            PrimaryActionButton(
                text = stringResource(ResourceR.string.core_resources_users_save),
                onClick = { onSave(playerNames.filter { it.isNotEmpty() }.toImmutableList()) },
                modifier = Modifier.padding(spacing.lg)
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
            item(contentType = "header") {
                Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
                    SectionHeader(
                        title = stringResource(ResourceR.string.core_resources_users_title),
                        subtitle = stringResource(ResourceR.string.core_resources_phase1_users_hint)
                    )
                    val filled = playerNames.count { it.isNotEmpty() }
                    val enough = filled >= MIN_PLAYERS
                    StatusChip(
                        text = if (enough) {
                            stringResource(ResourceR.string.core_resources_face_ready_players, filled)
                        } else {
                            stringResource(ResourceR.string.core_resources_face_warn_players)
                        },
                        variant = if (enough) StatusChipVariant.Ready else StatusChipVariant.Warning,
                        icon = if (enough) Icons.Outlined.CheckCircle else Icons.Outlined.Warning
                    )
                }
            }

            itemsIndexed(playerNames) { index, name ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            playerNames[index] = it
                        },
                        singleLine = true,
                        label = {
                            Text(
                                stringResource(
                                    ResourceR.string.core_resources_users_player_name_label,
                                    index + 1
                                )
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                    if (index > 2) IconButton(
                        onClick = { sound(Sounds.CLK_INSTANT); playerNames.removeAt(index) },
                        modifier = Modifier.padding(start = spacing.sm)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = stringResource(ResourceR.string.core_resources_remove_player)
                        )
                    }
                }
            }

            item {
                OutlinedButton(
                    onClick = { sound(Sounds.CLK_CASUAL); playerNames.add("") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = null,
                        modifier = Modifier.padding(end = spacing.sm)
                    )
                    Text(stringResource(ResourceR.string.core_resources_users_add_player))
                }
            }
        }
    }
}

@UiModePreviews
@Composable
private fun Preview() {
    AppTheme {
        UserDesign(playerNames = persistentListOf("Ana", "Bob"))
    }
}


