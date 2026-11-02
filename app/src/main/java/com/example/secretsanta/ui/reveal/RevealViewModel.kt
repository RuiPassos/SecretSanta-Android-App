package com.example.secretsanta.ui.reveal

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.secretsanta.domain.model.Assignment
import com.example.secretsanta.domain.model.GroupDetails
import com.example.secretsanta.domain.repository.GroupRepository
import com.example.secretsanta.ui.navigation.RevealRoute
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RevealUiState(
    val isLoading: Boolean = true,
    val details: GroupDetails? = null,
    /** The assignment currently on screen for its giver, if any. */
    val current: Assignment? = null,
    val isCardRevealed: Boolean = false,
) {
    val revealedCount: Int get() = details?.assignments?.count(Assignment::revealed) ?: 0
    val total: Int get() = details?.assignments?.size ?: 0
    val allRevealed: Boolean get() = total > 0 && revealedCount == total
}

/**
 * "Pass the phone around" flow for local groups: each person picks their own name and privately
 * reveals who they have to buy a present for.
 */
@HiltViewModel(assistedFactory = RevealViewModel.Factory::class)
class RevealViewModel @AssistedInject constructor(
    @Assisted route: RevealRoute,
    private val savedStateHandle: SavedStateHandle,
    private val repository: GroupRepository,
) : ViewModel() {

    private val groupId = route.groupId

    // Kept in SavedStateHandle so a rotation or process death never leaks or loses the current result.
    private val selectedGiverId = savedStateHandle.getStateFlow<Long?>(KEY_SELECTED, null)
    private val isCardRevealed = savedStateHandle.getStateFlow(KEY_REVEALED, false)

    val uiState: StateFlow<RevealUiState> = combine(
        repository.observeGroupDetails(groupId),
        selectedGiverId,
        isCardRevealed,
    ) { details, selected, revealed ->
        RevealUiState(
            isLoading = false,
            details = details,
            current = selected?.let { details?.assignmentFor(it) },
            isCardRevealed = revealed,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RevealUiState(),
    )

    fun select(giverId: Long) {
        savedStateHandle[KEY_REVEALED] = false
        savedStateHandle[KEY_SELECTED] = giverId
    }

    fun revealCard() {
        savedStateHandle[KEY_REVEALED] = true
    }

    /** Hides the result again; it only counts as seen if the card was actually revealed. */
    fun hide() {
        val giverId = selectedGiverId.value
        val wasRevealed = isCardRevealed.value
        savedStateHandle[KEY_SELECTED] = null
        savedStateHandle[KEY_REVEALED] = false
        if (giverId != null && wasRevealed) {
            viewModelScope.launch { repository.setRevealed(groupId, giverId, revealed = true) }
        }
    }

    private companion object {
        const val KEY_SELECTED = "selected_giver_id"
        const val KEY_REVEALED = "card_revealed"
    }

    @AssistedFactory
    interface Factory {
        fun create(route: RevealRoute): RevealViewModel
    }
}
