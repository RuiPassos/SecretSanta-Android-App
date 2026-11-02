package com.example.secretsanta.ui.groupdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.secretsanta.domain.draw.DrawGroupUseCase
import com.example.secretsanta.domain.draw.DrawResult
import com.example.secretsanta.domain.model.GroupDetails
import com.example.secretsanta.domain.repository.GroupRepository
import com.example.secretsanta.ui.navigation.GroupDetailRoute
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** One-off feedback shown in a snackbar or dialog. */
sealed interface GroupDetailMessage {
    data object DrawSucceeded : GroupDetailMessage
    data object NotEnoughParticipants : GroupDetailMessage
    data class DrawImpossible(val names: List<String>) : GroupDetailMessage
}

data class GroupDetailUiState(
    val isLoading: Boolean = true,
    val details: GroupDetails? = null,
    val isDrawing: Boolean = false,
    val message: GroupDetailMessage? = null,
    val isDeleted: Boolean = false,
)

@HiltViewModel(assistedFactory = GroupDetailViewModel.Factory::class)
class GroupDetailViewModel @AssistedInject constructor(
    @Assisted route: GroupDetailRoute,
    private val repository: GroupRepository,
    private val drawGroup: DrawGroupUseCase,
) : ViewModel() {

    val groupId = route.groupId

    private val transientState = MutableStateFlow(GroupDetailUiState())

    val uiState: StateFlow<GroupDetailUiState> = combine(
        repository.observeGroupDetails(groupId),
        transientState,
    ) { details, transient ->
        transient.copy(isLoading = false, details = details)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = GroupDetailUiState(),
    )

    fun draw() {
        if (transientState.value.isDrawing) return
        transientState.update { it.copy(isDrawing = true) }
        viewModelScope.launch {
            val message = when (val result = drawGroup(groupId)) {
                is DrawResult.Success -> GroupDetailMessage.DrawSucceeded
                DrawResult.NotEnoughParticipants -> GroupDetailMessage.NotEnoughParticipants
                is DrawResult.Impossible -> {
                    val participants = uiState.value.details?.participants.orEmpty()
                    GroupDetailMessage.DrawImpossible(
                        participants.filter { it.id in result.blockedParticipantIds }.map { it.name },
                    )
                }
            }
            transientState.update { it.copy(isDrawing = false, message = message) }
        }
    }

    fun resetDraw() = viewModelScope.launch { repository.resetDraw(groupId) }

    fun markResultDelivered(giverId: Long) = viewModelScope.launch {
        repository.setRevealed(groupId, giverId, revealed = true)
    }

    fun deleteGroup() = viewModelScope.launch {
        transientState.update { it.copy(isDeleted = true) }
        repository.deleteGroup(groupId)
    }

    fun onMessageShown() = transientState.update { it.copy(message = null) }

    @AssistedFactory
    interface Factory {
        fun create(route: GroupDetailRoute): GroupDetailViewModel
    }
}
