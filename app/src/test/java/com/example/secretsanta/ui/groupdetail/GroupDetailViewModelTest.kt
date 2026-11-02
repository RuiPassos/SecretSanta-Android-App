package com.example.secretsanta.ui.groupdetail

import app.cash.turbine.test
import com.example.secretsanta.domain.draw.DrawGroupUseCase
import com.example.secretsanta.domain.draw.SecretSantaDrawer
import com.example.secretsanta.domain.model.Group
import com.example.secretsanta.domain.model.Participant
import com.example.secretsanta.testing.FakeGroupRepository
import com.example.secretsanta.testing.MainDispatcherRule
import com.example.secretsanta.ui.navigation.GroupDetailRoute
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.random.Random

class GroupDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeGroupRepository()

    private fun newViewModel(groupId: Long) = GroupDetailViewModel(
        GroupDetailRoute(groupId),
        repository,
        DrawGroupUseCase(repository, SecretSantaDrawer(Random(5))),
    )

    private suspend fun groupWith(vararg names: String): Long {
        val groupId = repository.saveGroup(Group(name = "Family"))
        names.forEach { repository.saveParticipant(Participant(groupId = groupId, name = it)) }
        return groupId
    }

    @Test
    fun `drawing a valid group stores the result and reports success`() = runTest {
        val viewModel = newViewModel(groupWith("Ana", "Bruno", "Carla"))

        viewModel.uiState.test {
            skipItems(1)
            viewModel.draw()
            val state = expectMostRecentItem()
            assertTrue(state.details!!.group.isDrawn)
            assertEquals(3, state.details.assignments.size)
            assertEquals(GroupDetailMessage.DrawSucceeded, state.message)

            viewModel.onMessageShown()
            assertNull(awaitItem().message)
        }
    }

    @Test
    fun `impossible draw names the blocked participants`() = runTest {
        val groupId = groupWith()
        val bruno = repository.saveParticipant(Participant(groupId = groupId, name = "Bruno"))
        val carla = repository.saveParticipant(Participant(groupId = groupId, name = "Carla"))
        repository.saveParticipant(
            Participant(groupId = groupId, name = "Ana", excludedIds = setOf(bruno, carla)),
        )
        val viewModel = newViewModel(groupId)

        viewModel.uiState.test {
            skipItems(1)
            viewModel.draw()
            assertEquals(GroupDetailMessage.DrawImpossible(listOf("Ana")), expectMostRecentItem().message)
        }
    }

    @Test
    fun `too few participants is reported`() = runTest {
        val viewModel = newViewModel(groupWith("Ana", "Bruno"))

        viewModel.uiState.test {
            skipItems(1)
            viewModel.draw()
            assertEquals(GroupDetailMessage.NotEnoughParticipants, expectMostRecentItem().message)
        }
    }

    @Test
    fun `reset draw clears the results`() = runTest {
        val viewModel = newViewModel(groupWith("Ana", "Bruno", "Carla"))

        viewModel.uiState.test {
            skipItems(1)
            viewModel.draw()
            viewModel.resetDraw()
            val state = expectMostRecentItem()
            assertTrue(state.details!!.assignments.isEmpty())
            assertTrue(!state.details.group.isDrawn)
        }
    }
}
