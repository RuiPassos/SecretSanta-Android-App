package com.example.secretsanta.ui.groupedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.secretsanta.domain.model.DrawMode
import com.example.secretsanta.domain.model.Group
import com.example.secretsanta.domain.repository.GroupRepository
import com.example.secretsanta.ui.navigation.GroupEditRoute
import com.example.secretsanta.ui.navigation.NEW_ID
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class GroupEditUiState(
    val isNew: Boolean = true,
    val isLoading: Boolean = false,
    val name: String = "",
    val mode: DrawMode = DrawMode.LOCAL,
    val eventDate: LocalDate? = null,
    val budget: String = "",
    val location: String = "",
    val notes: String = "",
    val showNameError: Boolean = false,
    /** Set once the group was stored; the screen reacts by navigating away. */
    val savedGroupId: Long? = null,
)

@HiltViewModel(assistedFactory = GroupEditViewModel.Factory::class)
class GroupEditViewModel @AssistedInject constructor(
    @Assisted route: GroupEditRoute,
    private val repository: GroupRepository,
) : ViewModel() {

    private val groupId = route.groupId
    private var original: Group? = null

    private val _uiState = MutableStateFlow(GroupEditUiState(isNew = groupId == NEW_ID))
    val uiState: StateFlow<GroupEditUiState> = _uiState.asStateFlow()

    init {
        if (groupId != NEW_ID) load()
    }

    private fun load() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true) }
        val group = repository.getGroup(groupId)
        original = group
        _uiState.update { state ->
            if (group == null) {
                state.copy(isLoading = false)
            } else {
                state.copy(
                    isLoading = false,
                    name = group.name,
                    mode = group.mode,
                    eventDate = group.eventDate,
                    budget = group.budget,
                    location = group.location,
                    notes = group.notes,
                )
            }
        }
    }

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value, showNameError = false) }
    fun onModeChange(value: DrawMode) = _uiState.update { it.copy(mode = value) }
    fun onEventDateChange(value: LocalDate?) = _uiState.update { it.copy(eventDate = value) }
    fun onBudgetChange(value: String) = _uiState.update { it.copy(budget = value) }
    fun onLocationChange(value: String) = _uiState.update { it.copy(location = value) }
    fun onNotesChange(value: String) = _uiState.update { it.copy(notes = value) }

    fun save() {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.update { it.copy(showNameError = true) }
            return
        }
        viewModelScope.launch {
            val base = original ?: Group(name = state.name)
            val id = repository.saveGroup(
                base.copy(
                    name = state.name,
                    mode = state.mode,
                    eventDate = state.eventDate,
                    budget = state.budget,
                    location = state.location,
                    notes = state.notes,
                ),
            )
            _uiState.update { it.copy(savedGroupId = id) }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(route: GroupEditRoute): GroupEditViewModel
    }
}
