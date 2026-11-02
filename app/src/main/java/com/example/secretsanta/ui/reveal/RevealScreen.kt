package com.example.secretsanta.ui.reveal

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.secretsanta.R
import com.example.secretsanta.ui.navigation.RevealRoute
import com.example.secretsanta.domain.model.Assignment
import com.example.secretsanta.domain.model.Group
import com.example.secretsanta.domain.model.GroupDetails
import com.example.secretsanta.domain.model.Participant
import com.example.secretsanta.ui.components.BackButton
import com.example.secretsanta.ui.components.ConfirmDialog
import com.example.secretsanta.ui.components.LoadingIndicator
import com.example.secretsanta.ui.groupdetail.Avatar
import com.example.secretsanta.ui.theme.SecretSantaTheme

@Composable
fun RevealScreen(
    route: RevealRoute,
    onBack: () -> Unit,
    viewModel: RevealViewModel = hiltViewModel<RevealViewModel, RevealViewModel.Factory>(
        creationCallback = { factory -> factory.create(route) },
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    RevealContent(
        uiState = uiState,
        onBack = onBack,
        onSelect = viewModel::select,
        onRevealCard = viewModel::revealCard,
        onHide = viewModel::hide,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RevealContent(
    uiState: RevealUiState,
    onBack: () -> Unit,
    onSelect: (Long) -> Unit,
    onRevealCard: () -> Unit,
    onHide: () -> Unit,
) {
    val current = uiState.current
    BackHandler(enabled = current != null, onBack = onHide)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.reveal_title)) },
                navigationIcon = { BackButton(if (current != null) onHide else onBack) },
            )
        },
    ) { padding ->
        when {
            uiState.isLoading -> LoadingIndicator(Modifier.padding(padding))
            current != null -> SecretCard(
                group = uiState.details?.group,
                assignment = current,
                isRevealed = uiState.isCardRevealed,
                onReveal = onRevealCard,
                onHide = onHide,
                modifier = Modifier.padding(padding),
            )
            else -> PickYourName(
                uiState = uiState,
                contentPadding = padding,
                onSelect = onSelect,
            )
        }
    }
}

@Composable
private fun PickYourName(
    uiState: RevealUiState,
    contentPadding: PaddingValues,
    onSelect: (Long) -> Unit,
) {
    var pending by rememberSaveable { mutableStateOf<Long?>(null) }
    val assignments = uiState.details?.assignments.orEmpty().sortedBy { it.giver.name.lowercase() }

    LazyColumn(contentPadding = contentPadding) {
        item {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (uiState.allRevealed) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        ),
                    ) {
                        ListItem(
                            leadingContent = { Icon(Icons.Filled.Celebration, contentDescription = null) },
                            headlineContent = { Text(stringResource(R.string.reveal_all_done)) },
                            colors = ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            ),
                        )
                    }
                }
                Text(
                    text = stringResource(R.string.reveal_instructions),
                    style = MaterialTheme.typography.bodyLarge,
                )
                LinearProgressIndicator(
                    progress = { if (uiState.total == 0) 0f else uiState.revealedCount.toFloat() / uiState.total },
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = stringResource(R.string.draw_progress_seen, uiState.revealedCount, uiState.total),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
        items(assignments, key = { it.giver.id }) { assignment ->
            ListItem(
                modifier = Modifier.clickable { pending = assignment.giver.id },
                leadingContent = { Avatar(assignment.giver.name) },
                headlineContent = { Text(assignment.giver.name) },
                supportingContent = {
                    Text(
                        stringResource(
                            if (assignment.revealed) R.string.reveal_already_seen else R.string.reveal_tap_to_see,
                        ),
                    )
                },
                trailingContent = {
                    Icon(
                        imageVector = if (assignment.revealed) Icons.Filled.CheckCircle else Icons.Filled.TouchApp,
                        contentDescription = null,
                        tint = if (assignment.revealed) {
                            MaterialTheme.colorScheme.secondary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                },
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        }
    }

    val pendingAssignment = assignments.firstOrNull { it.giver.id == pending }
    if (pendingAssignment != null) {
        val name = pendingAssignment.giver.name
        ConfirmDialog(
            title = stringResource(R.string.reveal_confirm_title, name),
            message = stringResource(
                if (pendingAssignment.revealed) R.string.reveal_confirm_again else R.string.reveal_confirm_message,
                name,
            ),
            confirmLabel = stringResource(R.string.reveal_confirm_button, name),
            onConfirm = {
                pending = null
                onSelect(pendingAssignment.giver.id)
            },
            onDismiss = { pending = null },
        )
    }
}

@Composable
private fun SecretCard(
    group: Group?,
    assignment: Assignment,
    isRevealed: Boolean,
    onReveal: () -> Unit,
    onHide: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Text(
            text = stringResource(R.string.reveal_greeting, assignment.giver.name),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.reveal_you_are_santa_of),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        ElevatedCard(
            onClick = onReveal,
            enabled = !isRevealed,
            modifier = Modifier.fillMaxWidth().heightIn(min = 240.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                disabledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                disabledContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
        ) {
            AnimatedContent(
                targetState = isRevealed,
                transitionSpec = {
                    (fadeIn(tween(400)) + scaleIn(tween(400), initialScale = 0.8f)) togetherWith fadeOut(tween(200))
                },
                label = "reveal",
                modifier = Modifier.fillMaxWidth().padding(24.dp),
            ) { revealed ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Redeem,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    if (revealed) {
                        Text(
                            text = assignment.receiver.name,
                            style = MaterialTheme.typography.displaySmall,
                            textAlign = TextAlign.Center,
                        )
                        if (assignment.receiver.wishes.isNotBlank()) {
                            Text(
                                text = stringResource(R.string.reveal_wishes, assignment.receiver.wishes),
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                            )
                        }
                        group?.budget?.takeIf(String::isNotBlank)?.let {
                            Text(
                                text = stringResource(R.string.info_budget, it),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    } else {
                        Text(
                            text = stringResource(R.string.reveal_tap_card),
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
        if (isRevealed) {
            Text(
                text = stringResource(R.string.reveal_keep_secret),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
        }
        Button(onClick = onHide, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.VisibilityOff, contentDescription = null)
            Text(
                text = stringResource(if (isRevealed) R.string.reveal_hide else R.string.action_cancel),
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SecretCardPreview() {
    val ana = Participant(id = 1, groupId = 1, name = "Ana")
    val bruno = Participant(id = 2, groupId = 1, name = "Bruno", wishes = "A good book")
    SecretSantaTheme {
        RevealContent(
            uiState = RevealUiState(
                isLoading = false,
                details = GroupDetails(Group(id = 1, name = "Family", budget = "20 €"), listOf(ana, bruno), emptyList()),
                current = Assignment(ana, bruno),
                isCardRevealed = true,
            ),
            onBack = {},
            onSelect = {},
            onRevealCard = {},
            onHide = {},
        )
    }
}
