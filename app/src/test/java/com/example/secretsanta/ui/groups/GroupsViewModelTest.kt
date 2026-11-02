package com.example.secretsanta.ui.groups

import app.cash.turbine.test
import com.example.secretsanta.domain.model.Group
import com.example.secretsanta.domain.model.Participant
import com.example.secretsanta.testing.FakeGroupRepository
import com.example.secretsanta.testing.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class GroupsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeGroupRepository()

    @Test
    fun `emits groups with their participant count, newest first`() = runTest {
        val older = repository.saveGroup(Group(name = "Office", createdAt = 1))
        repository.saveGroup(Group(name = "Family", createdAt = 2))
        repository.saveParticipant(Participant(groupId = older, name = "Ana"))

        GroupsViewModel(repository).uiState.test {
            val state = awaitItem() as GroupsUiState.Success
            assertEquals(listOf("Family", "Office"), state.groups.map(Group::name))
            assertEquals(listOf(0, 1), state.groups.map(Group::participantCount))
        }
    }

    @Test
    fun `empty repository emits an empty list`() = runTest {
        GroupsViewModel(repository).uiState.test {
            assertEquals(GroupsUiState.Success(emptyList()), awaitItem())
        }
    }
}
