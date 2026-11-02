package com.example.secretsanta.ui.participant

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.secretsanta.R
import com.example.secretsanta.ui.navigation.ParticipantEditRoute
import com.example.secretsanta.domain.model.Participant
import com.example.secretsanta.ui.components.BackButton
import com.example.secretsanta.ui.components.ConfirmDialog
import com.example.secretsanta.ui.components.LoadingIndicator
import com.example.secretsanta.ui.groupdetail.Avatar
import com.example.secretsanta.ui.theme.SecretSantaTheme

@Composable
fun ParticipantEditScreen(
    route: ParticipantEditRoute,
    onBack: () -> Unit,
    viewModel: ParticipantEditViewModel = hiltViewModel<ParticipantEditViewModel, ParticipantEditViewModel.Factory>(
        creationCallback = { factory -> factory.create(route) },
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.isFinished) {
        if (uiState.isFinished) onBack()
    }

    val addedMessage = uiState.lastAddedName?.let { stringResource(R.string.participant_added, it) }
    LaunchedEffect(addedMessage) {
        if (addedMessage != null) {
            viewModel.onLastAddedShown()
            snackbarHostState.showSnackbar(addedMessage)
        }
    }

    ParticipantEditContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onSave = { viewModel.save() },
        onSaveAndAddAnother = { viewModel.save(addAnother = true) },
        onDelete = viewModel::delete,
        onNameChange = viewModel::onNameChange,
        onEmailChange = viewModel::onEmailChange,
        onPhoneChange = viewModel::onPhoneChange,
        onWishesChange = viewModel::onWishesChange,
        onExclusionToggle = viewModel::onExclusionToggle,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParticipantEditContent(
    uiState: ParticipantEditUiState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onSaveAndAddAnother: () -> Unit,
    onDelete: () -> Unit,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onWishesChange: (String) -> Unit,
    onExclusionToggle: (Long) -> Unit,
) {
    var confirmDelete by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (uiState.isNew) R.string.participant_new_title else R.string.participant_edit_title,
                        ),
                    )
                },
                navigationIcon = { BackButton(onBack) },
                actions = {
                    if (!uiState.isNew && !uiState.isDrawLocked) {
                        IconButton(onClick = { confirmDelete = true }) {
                            Icon(Icons.Filled.Delete, stringResource(R.string.action_delete_participant))
                        }
                    }
                    TextButton(onClick = onSave, enabled = !uiState.isLoading) {
                        Text(stringResource(R.string.action_save))
                    }
                },
            )
        },
    ) { padding ->
        if (uiState.isLoading) {
            LoadingIndicator(Modifier.padding(padding))
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            val fieldModifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            OutlinedTextField(
                value = uiState.name,
                onValueChange = onNameChange,
                label = { Text(stringResource(R.string.participant_name)) },
                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                isError = uiState.nameError != null,
                supportingText = uiState.nameError?.let { error ->
                    {
                        Text(
                            stringResource(
                                when (error) {
                                    NameError.REQUIRED -> R.string.error_name_required
                                    NameError.DUPLICATE -> R.string.error_name_duplicate
                                },
                            ),
                        )
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next,
                ),
                modifier = fieldModifier,
            )
            OutlinedTextField(
                value = uiState.email,
                onValueChange = onEmailChange,
                label = { Text(stringResource(R.string.participant_email)) },
                leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                isError = uiState.emailError,
                supportingText = if (uiState.emailError) {
                    { Text(stringResource(R.string.error_email_invalid)) }
                } else {
                    { Text(stringResource(R.string.participant_contact_hint)) }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                modifier = fieldModifier,
            )
            OutlinedTextField(
                value = uiState.phone,
                onValueChange = onPhoneChange,
                label = { Text(stringResource(R.string.participant_phone)) },
                leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null) },
                isError = uiState.phoneError,
                supportingText = if (uiState.phoneError) {
                    { Text(stringResource(R.string.error_phone_invalid)) }
                } else {
                    null
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                modifier = fieldModifier,
            )
            OutlinedTextField(
                value = uiState.wishes,
                onValueChange = onWishesChange,
                label = { Text(stringResource(R.string.participant_wishes)) },
                placeholder = { Text(stringResource(R.string.participant_wishes_hint)) },
                minLines = 3,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = fieldModifier,
            )

            ExclusionsSection(
                otherParticipants = uiState.otherParticipants,
                excludedIds = uiState.excludedIds,
                isLocked = uiState.isDrawLocked,
                onToggle = onExclusionToggle,
            )

            if (uiState.isNew) {
                OutlinedButton(onClick = onSaveAndAddAnother, modifier = fieldModifier) {
                    Text(stringResource(R.string.action_save_add_another))
                }
            }
        }
    }

    if (confirmDelete) {
        ConfirmDialog(
            title = stringResource(R.string.dialog_delete_participant_title),
            message = stringResource(R.string.dialog_delete_participant_message, uiState.name),
            confirmLabel = stringResource(R.string.action_delete),
            icon = Icons.Filled.Delete,
            onConfirm = {
                confirmDelete = false
                onDelete()
            },
            onDismiss = { confirmDelete = false },
        )
    }
}

@Composable
private fun ExclusionsSection(
    otherParticipants: List<Participant>,
    excludedIds: Set<Long>,
    isLocked: Boolean,
    onToggle: (Long) -> Unit,
) {
    Column {
        Text(
            text = stringResource(R.string.participant_exclusions_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Text(
            text = stringResource(
                if (isLocked) R.string.participant_exclusions_locked else R.string.participant_exclusions_description,
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        )
        if (otherParticipants.isEmpty()) {
            Text(
                text = stringResource(R.string.participant_exclusions_empty),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
        // A plain list of checkboxes keeps every person reachable, even in long groups.
        otherParticipants.forEach { other ->
            val checked = other.id in excludedIds
            ListItem(
                modifier = Modifier.toggleable(
                    value = checked,
                    enabled = !isLocked,
                    role = Role.Checkbox,
                    onValueChange = { onToggle(other.id) },
                ),
                leadingContent = { Avatar(other.name) },
                headlineContent = { Text(other.name) },
                trailingContent = { Checkbox(checked = checked, onCheckedChange = null, enabled = !isLocked) },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ParticipantEditPreview() {
    SecretSantaTheme {
        ParticipantEditContent(
            uiState = ParticipantEditUiState(
                isLoading = false,
                name = "Ana",
                email = "ana@mail.com",
                wishes = "Books, chocolate",
                excludedIds = setOf(2),
                otherParticipants = listOf(
                    Participant(id = 2, groupId = 1, name = "Bruno"),
                    Participant(id = 3, groupId = 1, name = "Carla"),
                ),
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onBack = {},
            onSave = {},
            onSaveAndAddAnother = {},
            onDelete = {},
            onNameChange = {},
            onEmailChange = {},
            onPhoneChange = {},
            onWishesChange = {},
            onExclusionToggle = {},
        )
    }
}
