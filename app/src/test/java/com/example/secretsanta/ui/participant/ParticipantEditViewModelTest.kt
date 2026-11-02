package com.example.secretsanta.ui.participant

import com.example.secretsanta.domain.model.Group
import com.example.secretsanta.domain.model.Participant
import com.example.secretsanta.ui.navigation.NEW_ID
import com.example.secretsanta.ui.navigation.ParticipantEditRoute
import com.example.secretsanta.testing.FakeGroupRepository
import com.example.secretsanta.testing.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ParticipantEditViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeGroupRepository()

    private fun newViewModel(groupId: Long, participantId: Long = NEW_ID) =
        ParticipantEditViewModel(ParticipantEditRoute(groupId, participantId), repository)

    @Test
    fun `blank name shows an error and nothing is saved`() = runTest {
        val groupId = repository.saveGroup(Group(name = "Family"))
        val viewModel = newViewModel(groupId)

        viewModel.save()
        viewModel.save()

        assertEquals(NameError.REQUIRED, viewModel.uiState.value.nameError)
        assertTrue(repository.observeGroupDetails(groupId).first()!!.participants.isEmpty())
    }

    @Test
    fun `duplicated name is rejected`() = runTest {
        val groupId = repository.saveGroup(Group(name = "Family"))
        repository.saveParticipant(Participant(groupId = groupId, name = "Ana"))
        val viewModel = newViewModel(groupId)

        viewModel.onNameChange(" ana ")
        viewModel.save()

        assertEquals(NameError.DUPLICATE, viewModel.uiState.value.nameError)
    }

    @Test
    fun `invalid email is rejected`() = runTest {
        val groupId = repository.saveGroup(Group(name = "Family"))
        val viewModel = newViewModel(groupId)

        viewModel.onNameChange("Ana")
        viewModel.onEmailChange("not-an-email")
        viewModel.save()

        assertTrue(viewModel.uiState.value.emailError)
    }

    @Test
    fun `save stores the participant with its exclusions`() = runTest {
        val groupId = repository.saveGroup(Group(name = "Family"))
        val bruno = repository.saveParticipant(Participant(groupId = groupId, name = "Bruno"))
        val viewModel = newViewModel(groupId)

        viewModel.onNameChange("Ana")
        viewModel.onWishesChange("Books")
        viewModel.onExclusionToggle(bruno)
        viewModel.save()

        assertTrue(viewModel.uiState.value.isFinished)
        val ana = repository.observeGroupDetails(groupId).first()!!.participants.single { it.name == "Ana" }
        assertEquals("Books", ana.wishes)
        assertEquals(setOf(bruno), ana.excludedIds)
    }

    @Test
    fun `save and add another clears the form and keeps the new person as option`() = runTest {
        val groupId = repository.saveGroup(Group(name = "Family"))
        val viewModel = newViewModel(groupId)

        viewModel.onNameChange("Ana")
        viewModel.save(addAnother = true)

        val state = viewModel.uiState.value
        assertEquals("", state.name)
        assertEquals("Ana", state.lastAddedName)
        assertEquals(listOf("Ana"), state.otherParticipants.map(Participant::name))
        viewModel.onLastAddedShown()
        assertNull(viewModel.uiState.value.lastAddedName)
    }

    @Test
    fun `exclusions cannot change once the group was drawn`() = runTest {
        val groupId = repository.saveGroup(Group(name = "Family"))
        val ana = repository.saveParticipant(Participant(groupId = groupId, name = "Ana"))
        val bruno = repository.saveParticipant(Participant(groupId = groupId, name = "Bruno"))
        repository.saveDraw(groupId, mapOf(ana to bruno, bruno to ana))
        val viewModel = newViewModel(groupId, ana)

        viewModel.onExclusionToggle(bruno)

        assertTrue(viewModel.uiState.value.isDrawLocked)
        assertTrue(viewModel.uiState.value.excludedIds.isEmpty())
    }
}
