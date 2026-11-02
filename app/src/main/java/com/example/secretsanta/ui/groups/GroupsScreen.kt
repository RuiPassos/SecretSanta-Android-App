package com.example.secretsanta.ui.groups

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.secretsanta.R
import com.example.secretsanta.domain.model.DrawMode
import com.example.secretsanta.domain.model.Group
import com.example.secretsanta.ui.components.EmptyState
import com.example.secretsanta.ui.components.InfoRow
import com.example.secretsanta.ui.components.LoadingIndicator
import com.example.secretsanta.ui.theme.SecretSantaTheme
import com.example.secretsanta.util.formatMedium
import java.time.LocalDate

@Composable
fun GroupsScreen(
    onCreateGroup: () -> Unit,
    onOpenGroup: (Long) -> Unit,
    viewModel: GroupsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    GroupsContent(uiState = uiState, onCreateGroup = onCreateGroup, onOpenGroup = onOpenGroup)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsContent(
    uiState: GroupsUiState,
    onCreateGroup: () -> Unit,
    onOpenGroup: (Long) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.groups_title)) },
                scrollBehavior = scrollBehavior,
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateGroup,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.groups_new)) },
            )
        },
    ) { padding ->
        when (uiState) {
            GroupsUiState.Loading -> LoadingIndicator(Modifier.padding(padding))
            is GroupsUiState.Success -> if (uiState.groups.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    EmptyState(
                        icon = Icons.Filled.CardGiftcard,
                        title = stringResource(R.string.groups_empty_title),
                        message = stringResource(R.string.groups_empty_message),
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = padding.calculateTopPadding() + 8.dp,
                        // Leave room so the FAB never covers the last card.
                        bottom = padding.calculateBottomPadding() + 96.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(uiState.groups, key = Group::id) { group ->
                        GroupCard(group = group, onClick = { onOpenGroup(group.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun GroupCard(group: Group, onClick: () -> Unit) {
    ElevatedCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = group.name, style = MaterialTheme.typography.titleLarge)
            InfoRow(
                icon = Icons.Filled.People,
                text = pluralStringResource(
                    R.plurals.participant_count,
                    group.participantCount,
                    group.participantCount,
                ),
            )
            group.eventDate?.let { InfoRow(icon = Icons.Filled.Event, text = it.formatMedium()) }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = onClick,
                    label = { Text(stringResource(group.mode.labelRes())) },
                )
                if (group.isDrawn) {
                    AssistChip(
                        onClick = onClick,
                        label = { Text(stringResource(R.string.group_status_drawn)) },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(AssistChipDefaults.IconSize),
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            leadingIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        ),
                    )
                }
            }
        }
    }
}

fun DrawMode.labelRes(): Int = when (this) {
    DrawMode.LOCAL -> R.string.mode_local
    DrawMode.ONLINE -> R.string.mode_online
}

@Preview(showBackground = true)
@Composable
private fun GroupsContentPreview() {
    SecretSantaTheme {
        GroupsContent(
            uiState = GroupsUiState.Success(
                listOf(
                    Group(
                        id = 1,
                        name = "Family Christmas",
                        eventDate = LocalDate.of(2026, 12, 24),
                        participantCount = 6,
                        drawnAt = 1L,
                    ),
                    Group(id = 2, name = "Office", mode = DrawMode.ONLINE, participantCount = 12),
                ),
            ),
            onCreateGroup = {},
            onOpenGroup = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GroupsEmptyPreview() {
    SecretSantaTheme {
        GroupsContent(uiState = GroupsUiState.Success(emptyList()), onCreateGroup = {}, onOpenGroup = {})
    }
}
