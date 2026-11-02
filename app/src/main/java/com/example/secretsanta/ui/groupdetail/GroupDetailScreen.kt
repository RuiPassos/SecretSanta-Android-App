package com.example.secretsanta.ui.groupdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Euro
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.secretsanta.R
import com.example.secretsanta.ui.navigation.GroupDetailRoute
import com.example.secretsanta.domain.draw.SecretSantaDrawer
import com.example.secretsanta.domain.model.Assignment
import com.example.secretsanta.domain.model.DrawMode
import com.example.secretsanta.domain.model.Group
import com.example.secretsanta.domain.model.GroupDetails
import com.example.secretsanta.domain.model.Participant
import com.example.secretsanta.ui.components.BackButton
import com.example.secretsanta.ui.components.ConfirmDialog
import com.example.secretsanta.ui.components.EmptyState
import com.example.secretsanta.ui.components.InfoRow
import com.example.secretsanta.ui.components.LoadingIndicator
import com.example.secretsanta.ui.groups.labelRes
import com.example.secretsanta.ui.theme.SecretSantaTheme
import com.example.secretsanta.util.ResultMessageBuilder
import com.example.secretsanta.util.ShareIntents
import com.example.secretsanta.util.formatEpochMillis
import com.example.secretsanta.util.formatMedium
import com.example.secretsanta.util.tryStartActivity
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Channels a result can be delivered through. */
enum class DeliveryChannel { EMAIL, SMS, SHARE }

@Composable
fun GroupDetailScreen(
    route: GroupDetailRoute,
    onBack: () -> Unit,
    onEditGroup: (Long) -> Unit,
    onAddParticipant: (Long) -> Unit,
    onOpenParticipant: (groupId: Long, participantId: Long) -> Unit,
    onReveal: (Long) -> Unit,
    viewModel: GroupDetailViewModel = hiltViewModel<GroupDetailViewModel, GroupDetailViewModel.Factory>(
        creationCallback = { factory -> factory.create(route) },
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val messageBuilder = remember(context) { ResultMessageBuilder(context) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val noAppMessage = stringResource(R.string.error_no_app)
    val shareTitle = stringResource(R.string.share_chooser_title)

    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) onBack()
    }

    GroupDetailContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onEditGroup = { onEditGroup(viewModel.groupId) },
        onAddParticipant = { onAddParticipant(viewModel.groupId) },
        onOpenParticipant = { onOpenParticipant(viewModel.groupId, it) },
        onReveal = { onReveal(viewModel.groupId) },
        onDraw = viewModel::draw,
        onResetDraw = viewModel::resetDraw,
        onDeleteGroup = viewModel::deleteGroup,
        onMessageShown = viewModel::onMessageShown,
        onDeliver = { assignment, channel ->
            val group = uiState.details?.group ?: return@GroupDetailContent
            val subject = messageBuilder.subject(group)
            val body = messageBuilder.body(group, assignment)
            val intent = when (channel) {
                DeliveryChannel.EMAIL -> ShareIntents.email(assignment.giver.email, subject, body)
                DeliveryChannel.SMS -> ShareIntents.sms(assignment.giver.phone, body)
                DeliveryChannel.SHARE -> ShareIntents.share(subject, body, shareTitle)
            }
            if (context.tryStartActivity(intent)) {
                viewModel.markResultDelivered(assignment.giver.id)
            } else {
                scope.launch { snackbarHostState.showSnackbar(noAppMessage) }
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailContent(
    uiState: GroupDetailUiState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onEditGroup: () -> Unit,
    onAddParticipant: () -> Unit,
    onOpenParticipant: (Long) -> Unit,
    onReveal: () -> Unit,
    onDraw: () -> Unit,
    onResetDraw: () -> Unit,
    onDeleteGroup: () -> Unit,
    onMessageShown: () -> Unit,
    onDeliver: (Assignment, DeliveryChannel) -> Unit,
) {
    val details = uiState.details
    var menuExpanded by remember { mutableStateOf(false) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    var confirmReset by rememberSaveable { mutableStateOf(false) }
    var confirmRedraw by rememberSaveable { mutableStateOf(false) }

    MessageEffect(uiState.message, snackbarHostState, onMessageShown)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = details?.group?.name.orEmpty(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = { BackButton(onBack) },
                actions = {
                    if (details != null) {
                        IconButton(onClick = onEditGroup) {
                            Icon(Icons.Filled.Edit, stringResource(R.string.action_edit_group))
                        }
                        Box {
                            IconButton(onClick = { menuExpanded = true }) {
                                Icon(Icons.Filled.MoreVert, stringResource(R.string.action_more))
                            }
                            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                                if (details.group.isDrawn) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.action_redraw)) },
                                        leadingIcon = { Icon(Icons.Filled.Refresh, null) },
                                        onClick = {
                                            menuExpanded = false
                                            confirmRedraw = true
                                        },
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.action_reset_draw)) },
                                        leadingIcon = { Icon(Icons.Filled.RestartAlt, null) },
                                        onClick = {
                                            menuExpanded = false
                                            confirmReset = true
                                        },
                                    )
                                }
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.action_delete_group)) },
                                    leadingIcon = { Icon(Icons.Filled.Delete, null) },
                                    onClick = {
                                        menuExpanded = false
                                        confirmDelete = true
                                    },
                                )
                            }
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            if (details != null && !details.group.isDrawn) {
                FloatingActionButton(onClick = onAddParticipant) {
                    Icon(Icons.Filled.Add, stringResource(R.string.action_add_participant))
                }
            }
        },
    ) { padding ->
        when {
            uiState.isLoading -> LoadingIndicator(Modifier.padding(padding))
            details == null -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Text(stringResource(R.string.group_not_found))
            }
            else -> GroupDetailList(
                details = details,
                isDrawing = uiState.isDrawing,
                contentPadding = padding,
                onOpenParticipant = onOpenParticipant,
                onDraw = onDraw,
                onReveal = onReveal,
                onDeliver = onDeliver,
            )
        }
    }

    if (confirmDelete) {
        ConfirmDialog(
            title = stringResource(R.string.dialog_delete_group_title),
            message = stringResource(R.string.dialog_delete_group_message, details?.group?.name.orEmpty()),
            confirmLabel = stringResource(R.string.action_delete),
            icon = Icons.Filled.Delete,
            onConfirm = {
                confirmDelete = false
                onDeleteGroup()
            },
            onDismiss = { confirmDelete = false },
        )
    }
    if (confirmReset) {
        ConfirmDialog(
            title = stringResource(R.string.dialog_reset_title),
            message = stringResource(R.string.dialog_reset_message),
            confirmLabel = stringResource(R.string.action_reset_draw),
            icon = Icons.Filled.RestartAlt,
            onConfirm = {
                confirmReset = false
                onResetDraw()
            },
            onDismiss = { confirmReset = false },
        )
    }
    if (confirmRedraw) {
        ConfirmDialog(
            title = stringResource(R.string.dialog_redraw_title),
            message = stringResource(R.string.dialog_redraw_message),
            confirmLabel = stringResource(R.string.action_redraw),
            icon = Icons.Filled.Refresh,
            onConfirm = {
                confirmRedraw = false
                onDraw()
            },
            onDismiss = { confirmRedraw = false },
        )
    }
}

@Composable
private fun MessageEffect(
    message: GroupDetailMessage?,
    snackbarHostState: SnackbarHostState,
    onMessageShown: () -> Unit,
) {
    val text = when (message) {
        null -> null
        GroupDetailMessage.DrawSucceeded -> stringResource(R.string.draw_success)
        GroupDetailMessage.NotEnoughParticipants ->
            stringResource(R.string.draw_not_enough, SecretSantaDrawer.MIN_PARTICIPANTS)
        is GroupDetailMessage.DrawImpossible -> if (message.names.isEmpty()) {
            stringResource(R.string.draw_impossible_generic)
        } else {
            stringResource(R.string.draw_impossible, message.names.joinToString())
        }
    }
    LaunchedEffect(message) {
        if (text != null) {
            snackbarHostState.showSnackbar(text)
            onMessageShown()
        }
    }
}

@Composable
private fun GroupDetailList(
    details: GroupDetails,
    isDrawing: Boolean,
    contentPadding: PaddingValues,
    onOpenParticipant: (Long) -> Unit,
    onDraw: () -> Unit,
    onReveal: () -> Unit,
    onDeliver: (Assignment, DeliveryChannel) -> Unit,
) {
    val namesById = remember(details.participants) { details.participants.associate { it.id to it.name } }
    LazyColumn(
        contentPadding = PaddingValues(
            top = contentPadding.calculateTopPadding(),
            bottom = contentPadding.calculateBottomPadding() + 96.dp,
        ),
    ) {
        item { GroupInfoCard(details.group) }
        item {
            DrawStatusCard(
                details = details,
                isDrawing = isDrawing,
                onDraw = onDraw,
                onReveal = onReveal,
            )
        }
        item {
            Text(
                text = stringResource(R.string.participants_header, details.participants.size),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp),
            )
        }
        if (details.participants.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Filled.Groups,
                    title = stringResource(R.string.participants_empty_title),
                    message = stringResource(R.string.participants_empty_message),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        items(details.participants, key = Participant::id) { participant ->
            ParticipantRow(
                participant = participant,
                excludedNames = participant.excludedIds.mapNotNull(namesById::get),
                assignment = details.assignmentFor(participant.id),
                onClick = { onOpenParticipant(participant.id) },
                onDeliver = onDeliver,
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        }
    }
}

@Composable
private fun GroupInfoCard(group: Group) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            InfoRow(
                icon = if (group.mode == DrawMode.LOCAL) Icons.Filled.Groups else Icons.AutoMirrored.Filled.Send,
                text = stringResource(group.mode.labelRes()),
            )
            group.eventDate?.let { InfoRow(Icons.Filled.Event, it.formatMedium()) }
            group.budget.takeIf(String::isNotBlank)?.let {
                InfoRow(Icons.Filled.Euro, stringResource(R.string.info_budget, it))
            }
            group.location.takeIf(String::isNotBlank)?.let { InfoRow(Icons.Filled.Place, it) }
            group.notes.takeIf(String::isNotBlank)?.let { InfoRow(Icons.AutoMirrored.Filled.Notes, it) }
        }
    }
}

@Composable
private fun DrawStatusCard(
    details: GroupDetails,
    isDrawing: Boolean,
    onDraw: () -> Unit,
    onReveal: () -> Unit,
) {
    val group = details.group
    val count = details.participants.size
    val drawnAt = group.drawnAt
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (drawnAt != null) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.primaryContainer
            },
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (drawnAt == null) {
                val missing = SecretSantaDrawer.MIN_PARTICIPANTS - count
                Text(stringResource(R.string.draw_ready_title), style = MaterialTheme.typography.titleMedium)
                Text(
                    text = if (missing > 0) {
                        pluralStringResource(R.plurals.draw_missing_participants, missing, missing)
                    } else {
                        stringResource(R.string.draw_ready_message)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
                Button(
                    onClick = onDraw,
                    enabled = missing <= 0 && !isDrawing,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (isDrawing) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Filled.Shuffle, contentDescription = null)
                    }
                    Text(stringResource(R.string.action_draw), modifier = Modifier.padding(start = 8.dp))
                }
            } else {
                val delivered = details.assignments.count(Assignment::revealed)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Celebration, contentDescription = null)
                    Text(
                        text = stringResource(R.string.draw_done_title),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
                Text(
                    text = stringResource(R.string.draw_done_on, drawnAt.formatEpochMillis()),
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    text = stringResource(
                        if (group.mode == DrawMode.LOCAL) R.string.draw_done_local else R.string.draw_done_online,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = stringResource(
                        if (group.mode == DrawMode.LOCAL) R.string.draw_progress_seen else R.string.draw_progress_sent,
                        delivered,
                        details.assignments.size,
                    ),
                    style = MaterialTheme.typography.labelLarge,
                )
                if (group.mode == DrawMode.LOCAL) {
                    Button(onClick = onReveal, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Filled.Visibility, contentDescription = null)
                        Text(stringResource(R.string.action_reveal), modifier = Modifier.padding(start = 8.dp))
                    }
                }
                Text(
                    text = stringResource(R.string.draw_locked_hint),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun ParticipantRow(
    participant: Participant,
    excludedNames: List<String>,
    assignment: Assignment?,
    onClick: () -> Unit,
    onDeliver: (Assignment, DeliveryChannel) -> Unit,
) {
    val contact = listOf(participant.email, participant.phone).filter(String::isNotBlank).joinToString(" · ")
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        leadingContent = { Avatar(participant.name) },
        headlineContent = { Text(participant.name) },
        supportingContent = {
            Column {
                if (contact.isNotEmpty()) {
                    Text(contact, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                if (excludedNames.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Block,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.error,
                        )
                        Text(
                            text = stringResource(R.string.participant_excludes, excludedNames.joinToString()),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(start = 4.dp),
                        )
                    }
                }
            }
        },
        trailingContent = assignment?.let { { DeliveryActions(it, onDeliver) } },
    )
}

@Composable
private fun DeliveryActions(
    assignment: Assignment,
    onDeliver: (Assignment, DeliveryChannel) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (assignment.revealed) {
            Icon(
                Icons.Filled.CheckCircle,
                contentDescription = stringResource(R.string.participant_result_delivered),
                tint = MaterialTheme.colorScheme.secondary,
            )
        }
        Box {
            IconButton(onClick = { expanded = true }) {
                Icon(Icons.AutoMirrored.Filled.Send, stringResource(R.string.action_send_result, assignment.giver.name))
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.send_email)) },
                    leadingIcon = { Icon(Icons.Filled.Email, null) },
                    enabled = assignment.giver.email.isNotBlank(),
                    onClick = {
                        expanded = false
                        onDeliver(assignment, DeliveryChannel.EMAIL)
                    },
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.send_sms)) },
                    leadingIcon = { Icon(Icons.Filled.Sms, null) },
                    enabled = assignment.giver.phone.isNotBlank(),
                    onClick = {
                        expanded = false
                        onDeliver(assignment, DeliveryChannel.SMS)
                    },
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.send_share)) },
                    leadingIcon = { Icon(Icons.Filled.Share, null) },
                    onClick = {
                        expanded = false
                        onDeliver(assignment, DeliveryChannel.SHARE)
                    },
                )
            }
        }
    }
}

@Composable
fun Avatar(name: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(40.dp)
            .background(MaterialTheme.colorScheme.tertiaryContainer, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = name.firstOrNull()?.uppercase().orEmpty(),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GroupDetailPreview() {
    val ana = Participant(id = 1, groupId = 1, name = "Ana", email = "ana@mail.com", excludedIds = setOf(2))
    val bruno = Participant(id = 2, groupId = 1, name = "Bruno", phone = "+351 912 345 678")
    val carla = Participant(id = 3, groupId = 1, name = "Carla")
    SecretSantaTheme {
        GroupDetailContent(
            uiState = GroupDetailUiState(
                isLoading = false,
                details = GroupDetails(
                    group = Group(
                        id = 1,
                        name = "Family Christmas",
                        eventDate = LocalDate.of(2026, 12, 24),
                        budget = "25 €",
                        drawnAt = 1_767_000_000_000,
                    ),
                    participants = listOf(ana, bruno, carla),
                    assignments = listOf(
                        Assignment(ana, carla, revealed = true),
                        Assignment(bruno, ana),
                        Assignment(carla, bruno),
                    ),
                ),
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onBack = {},
            onEditGroup = {},
            onAddParticipant = {},
            onOpenParticipant = {},
            onReveal = {},
            onDraw = {},
            onResetDraw = {},
            onDeleteGroup = {},
            onMessageShown = {},
            onDeliver = { _, _ -> },
        )
    }
}
