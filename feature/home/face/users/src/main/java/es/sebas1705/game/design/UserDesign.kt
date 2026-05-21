package es.sebas1705.game.design

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import es.sebas1705.ui.sound.LocalSoundPlayer
import es.sebas1705.ui.theme.AppTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import es.sebas1705.core.resources.R as ResourceR

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
        }
    }
    val sound = LocalSoundPlayer.current

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
            FilledTonalButton(
                onClick = {
                    sound(Sounds.CLK_ARCADE)
                    onSave(playerNames.filter { it.isNotEmpty() }.toImmutableList())
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(stringResource(ResourceR.string.core_resources_users_save))
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
                    if (index > 1) IconButton(
                        onClick = { sound(Sounds.CLK_INSTANT); playerNames.removeAt(index) },
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = stringResource(ResourceR.string.core_resources_remove_player)
                        )
                    }
                }
            }

            item {
                TextButton(
                    onClick = { sound(Sounds.CLK_CASUAL); playerNames.add("") },
                    modifier = Modifier.fillMaxWidth()
                ) {
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
        UserDesign()
    }
}


