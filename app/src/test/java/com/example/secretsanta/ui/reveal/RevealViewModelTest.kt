package com.example.secretsanta.ui.reveal

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.example.secretsanta.domain.model.Group
import com.example.secretsanta.domain.model.Participant
import com.example.secretsanta.testing.FakeGroupRepository
import com.example.secretsanta.testing.MainDispatcherRule
import com.example.secretsanta.ui.navigation.RevealRoute
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class RevealViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeGroupRepository()
    private var groupId = 0L
    private var ana = 0L
    private var bruno = 0L

    @Before
    fun setUp() = runTest {
        groupId = repository.saveGroup(Group(name = "Family"))
        ana = repository.saveParticipant(Participant(groupId = groupId, name = "Ana"))
        bruno = repository.saveParticipant(Participant(groupId = groupId, name = "Bruno"))
        repository.saveDraw(groupId, mapOf(ana to bruno, bruno to ana))
    }

    private fun newViewModel() = RevealViewModel(RevealRoute(groupId), SavedStateHandle(), repository)

    @Test
    fun `selecting a giver shows their hidden assignment`() = runTest {
        val viewModel = newViewModel()

        viewModel.uiState.test {
            skipItems(1)
            viewModel.select(ana)
            val state = expectMostRecentItem()
            assertEquals("Bruno", state.current!!.receiver.name)
            assertFalse(state.isCardRevealed)
        }
    }

    @Test
    fun `hiding after revealing marks the result as seen`() = runTest {
        val viewModel = newViewModel()

        viewModel.uiState.test {
            skipItems(1)
            viewModel.select(ana)
            viewModel.revealCard()
            viewModel.hide()
            val state = expectMostRecentItem()
            assertNull(state.current)
            assertEquals(1, state.revealedCount)
            assertFalse(state.allRevealed)
        }
    }

    @Test
    fun `cancelling before revealing does not count as seen`() = runTest {
        val viewModel = newViewModel()

        viewModel.uiState.test {
            skipItems(1)
            viewModel.select(ana)
            viewModel.hide()
            assertEquals(0, expectMostRecentItem().revealedCount)
        }
    }

    @Test
    fun `everyone revealed is detected`() = runTest {
        val viewModel = newViewModel()

        viewModel.uiState.test {
            skipItems(1)
            listOf(ana, bruno).forEach {
                viewModel.select(it)
                viewModel.revealCard()
                viewModel.hide()
            }
            assertTrue(expectMostRecentItem().allRevealed)
        }
    }
}
