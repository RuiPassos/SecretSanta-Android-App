package com.example.secretsanta.ui.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.secretsanta.domain.model.Group
import com.example.secretsanta.domain.repository.GroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed interface GroupsUiState {
    data object Loading : GroupsUiState
    data class Success(val groups: List<Group>) : GroupsUiState
}

@HiltViewModel
class GroupsViewModel @Inject constructor(
    repository: GroupRepository,
) : ViewModel() {

    val uiState: StateFlow<GroupsUiState> = repository.observeGroups()
        .map<List<Group>, GroupsUiState>(GroupsUiState::Success)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = GroupsUiState.Loading,
        )
}
