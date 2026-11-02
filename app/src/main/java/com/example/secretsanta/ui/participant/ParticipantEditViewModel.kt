package com.example.secretsanta.ui.participant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.secretsanta.domain.model.Participant
import com.example.secretsanta.domain.repository.GroupRepository
import com.example.secretsanta.ui.navigation.NEW_ID
import com.example.secretsanta.ui.navigation.ParticipantEditRoute
import com.example.secretsanta.util.isValidEmail
import com.example.secretsanta.util.isValidPhone
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class NameError { REQUIRED, DUPLICATE }

data class ParticipantEditUiState(
    val isNew: Boolean = true,
    val isLoading: Boolean = true,
    /** When the group was already drawn, exclusions can't change and nobody can be removed. */
    val isDrawLocked: Boolean = false,
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val wishes: String = "",
    val excludedIds: Set<Long> = emptySet(),
    val otherParticipants: List<Participant> = emptyList(),
    val nameError: NameError? = null,
    val emailError: Boolean = false,
    val phoneError: Boolean = false,
    /** Name of the participant that was just stored with "save and add another". */
    val lastAddedName: String? = null,
    val isFinished: Boolean = false,
) {
    val hasErrors: Boolean get() = nameError != null || emailError || phoneError
}

@HiltViewModel(assistedFactory = ParticipantEditViewModel.Factory::class)
class ParticipantEditViewModel @AssistedInject constructor(
    @Assisted private val route: ParticipantEditRoute,
    private val repository: GroupRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ParticipantEditUiState(isNew = route.participantId == NEW_ID))
    val uiState: StateFlow<ParticipantEditUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    private fun load() = viewModelScope.launch {
        val details = repository.observeGroupDetails(route.groupId).first()
        val participant = details?.participants?.firstOrNull { it.id == route.participantId }
        _uiState.update { state ->
            state.copy(
                isLoading = false,
                isDrawLocked = details?.group?.isDrawn == true,
                otherParticipants = details?.participants.orEmpty().filter { it.id != route.participantId },
                name = participant?.name.orEmpty(),
                email = participant?.email.orEmpty(),
                phone = participant?.phone.orEmpty(),
                wishes = participant?.wishes.orEmpty(),
                excludedIds = participant?.excludedIds.orEmpty(),
            )
        }
    }

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value, nameError = null) }
    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value, emailError = false) }
    fun onPhoneChange(value: String) = _uiState.update { it.copy(phone = value, phoneError = false) }
    fun onWishesChange(value: String) = _uiState.update { it.copy(wishes = value) }

    fun onExclusionToggle(participantId: Long) = _uiState.update { state ->
        if (state.isDrawLocked) return@update state
        val excluded = state.excludedIds
        state.copy(excludedIds = if (participantId in excluded) excluded - participantId else excluded + participantId)
    }

    /** @param addAnother keep the screen open with an empty form to quickly add the next person. */
    fun save(addAnother: Boolean = false) {
        val state = _uiState.value
        val validated = validate(state)
        if (validated.hasErrors) {
            _uiState.value = validated
            return
        }
        viewModelScope.launch {
            val id = repository.saveParticipant(
                Participant(
                    id = route.participantId,
                    groupId = route.groupId,
                    name = state.name,
                    email = state.email,
                    phone = state.phone,
                    wishes = state.wishes,
                    excludedIds = state.excludedIds,
                ),
            )
            if (addAnother) {
                val saved = Participant(id = id, groupId = route.groupId, name = state.name.trim())
                _uiState.value = ParticipantEditUiState(
                    isLoading = false,
                    otherParticipants = state.otherParticipants + saved,
                    lastAddedName = saved.name,
                )
            } else {
                _uiState.update { it.copy(isFinished = true) }
            }
        }
    }

    fun delete() {
        if (_uiState.value.isNew || _uiState.value.isDrawLocked) return
        viewModelScope.launch {
            repository.deleteParticipant(route.participantId)
            _uiState.update { it.copy(isFinished = true) }
        }
    }

    fun onLastAddedShown() = _uiState.update { it.copy(lastAddedName = null) }

    private fun validate(state: ParticipantEditUiState): ParticipantEditUiState {
        val name = state.name.trim()
        val nameError = when {
            name.isEmpty() -> NameError.REQUIRED
            state.otherParticipants.any { it.name.trim().equals(name, ignoreCase = true) } -> NameError.DUPLICATE
            else -> null
        }
        return state.copy(
            nameError = nameError,
            emailError = state.email.isNotBlank() && !state.email.isValidEmail(),
            phoneError = state.phone.isNotBlank() && !state.phone.isValidPhone(),
        )
    }

    @AssistedFactory
    interface Factory {
        fun create(route: ParticipantEditRoute): ParticipantEditViewModel
    }
}
