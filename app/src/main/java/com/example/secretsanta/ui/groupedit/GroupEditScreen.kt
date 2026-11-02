package com.example.secretsanta.ui.groupedit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.secretsanta.R
import com.example.secretsanta.ui.navigation.GroupEditRoute
import com.example.secretsanta.domain.model.DrawMode
import com.example.secretsanta.ui.components.BackButton
import com.example.secretsanta.ui.components.LoadingIndicator
import com.example.secretsanta.ui.groups.labelRes
import com.example.secretsanta.ui.theme.SecretSantaTheme
import com.example.secretsanta.util.formatMedium
import com.example.secretsanta.util.toUtcMillis
import com.example.secretsanta.util.utcMillisToLocalDate
import java.time.LocalDate

@Composable
fun GroupEditScreen(
    route: GroupEditRoute,
    onBack: () -> Unit,
    onGroupCreated: (Long) -> Unit,
    viewModel: GroupEditViewModel = hiltViewModel<GroupEditViewModel, GroupEditViewModel.Factory>(
        creationCallback = { factory -> factory.create(route) },
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.savedGroupId) {
        val id = uiState.savedGroupId ?: return@LaunchedEffect
        if (uiState.isNew) onGroupCreated(id) else onBack()
    }

    GroupEditContent(
        uiState = uiState,
        onBack = onBack,
        onSave = viewModel::save,
        onNameChange = viewModel::onNameChange,
        onModeChange = viewModel::onModeChange,
        onEventDateChange = viewModel::onEventDateChange,
        onBudgetChange = viewModel::onBudgetChange,
        onLocationChange = viewModel::onLocationChange,
        onNotesChange = viewModel::onNotesChange,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupEditContent(
    uiState: GroupEditUiState,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onNameChange: (String) -> Unit,
    onModeChange: (DrawMode) -> Unit,
    onEventDateChange: (LocalDate?) -> Unit,
    onBudgetChange: (String) -> Unit,
    onLocationChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
) {
    var showDatePicker by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(if (uiState.isNew) R.string.group_new_title else R.string.group_edit_title))
                },
                navigationIcon = { BackButton(onBack) },
                actions = {
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                value = uiState.name,
                onValueChange = onNameChange,
                label = { Text(stringResource(R.string.group_name)) },
                placeholder = { Text(stringResource(R.string.group_name_hint)) },
                isError = uiState.showNameError,
                supportingText = if (uiState.showNameError) {
                    { Text(stringResource(R.string.error_name_required)) }
                } else {
                    null
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next,
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            Text(stringResource(R.string.group_mode), style = MaterialTheme.typography.titleMedium)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                DrawMode.entries.forEachIndexed { index, mode ->
                    SegmentedButton(
                        selected = uiState.mode == mode,
                        onClick = { onModeChange(mode) },
                        shape = SegmentedButtonDefaults.itemShape(index, DrawMode.entries.size),
                    ) {
                        Text(stringResource(mode.labelRes()))
                    }
                }
            }
            Text(
                text = stringResource(
                    when (uiState.mode) {
                        DrawMode.LOCAL -> R.string.mode_local_description
                        DrawMode.ONLINE -> R.string.mode_online_description
                    },
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(stringResource(R.string.group_extra_info), style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = uiState.eventDate?.formatMedium().orEmpty(),
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.group_event_date)) },
                leadingIcon = { Icon(Icons.Filled.Event, contentDescription = null) },
                trailingIcon = {
                    if (uiState.eventDate != null) {
                        IconButton(onClick = { onEventDateChange(null) }) {
                            Icon(Icons.Filled.Clear, stringResource(R.string.action_clear_date))
                        }
                    } else {
                        TextButton(onClick = { showDatePicker = true }) {
                            Text(stringResource(R.string.action_pick_date))
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = uiState.budget,
                onValueChange = onBudgetChange,
                label = { Text(stringResource(R.string.group_budget)) },
                placeholder = { Text(stringResource(R.string.group_budget_hint)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = uiState.location,
                onValueChange = onLocationChange,
                label = { Text(stringResource(R.string.group_location)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = uiState.notes,
                onValueChange = onNotesChange,
                label = { Text(stringResource(R.string.group_notes)) },
                minLines = 3,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = (uiState.eventDate ?: LocalDate.now()).toUtcMillis(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        onEventDateChange(datePickerState.selectedDateMillis?.utcMillisToLocalDate())
                        showDatePicker = false
                    },
                ) { Text(stringResource(R.string.action_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GroupEditPreview() {
    SecretSantaTheme {
        GroupEditContent(
            uiState = GroupEditUiState(name = "Family Christmas", budget = "20 €"),
            onBack = {},
            onSave = {},
            onNameChange = {},
            onModeChange = {},
            onEventDateChange = {},
            onBudgetChange = {},
            onLocationChange = {},
            onNotesChange = {},
        )
    }
}
