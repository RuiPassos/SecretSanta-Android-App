package com.example.secretsanta.domain.draw

import com.example.secretsanta.domain.model.Group
import com.example.secretsanta.domain.model.Participant
import com.example.secretsanta.testing.FakeGroupRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class DrawGroupUseCaseTest {

    private val repository = FakeGroupRepository()
    private val drawGroup = DrawGroupUseCase(repository, SecretSantaDrawer(Random(3)))

    @Test
    fun `successful draw is stored and marks the group as drawn`() = runTest {
        val groupId = repository.saveGroup(Group(name = "Family"))
        listOf("Ana", "Bruno", "Carla", "Duarte").forEach {
            repository.saveParticipant(Participant(groupId = groupId, name = it))
        }

        val result = drawGroup(groupId)

        assertTrue(result is DrawResult.Success)
        val details = repository.observeGroupDetails(groupId).first()!!
        assertTrue(details.group.isDrawn)
        assertEquals(4, details.assignments.size)
        details.assignments.forEach { assertTrue(it.giver != it.receiver) }
    }

    @Test
    fun `exclusions stored in the repository are applied`() = runTest {
        val groupId = repository.saveGroup(Group(name = "Couples"))
        val ana = repository.saveParticipant(Participant(groupId = groupId, name = "Ana"))
        val bruno = repository.saveParticipant(Participant(groupId = groupId, name = "Bruno"))
        repository.saveParticipant(Participant(groupId = groupId, name = "Carla"))
        repository.saveParticipant(Participant(groupId = groupId, name = "Duarte"))
        repository.saveParticipant(
            Participant(id = ana, groupId = groupId, name = "Ana", excludedIds = setOf(bruno)),
        )

        repeat(20) {
            drawGroup(groupId)
            val details = repository.observeGroupDetails(groupId).first()!!
            assertTrue(details.assignmentFor(ana)!!.receiver.id != bruno)
        }
    }

    @Test
    fun `failed draw leaves the group undrawn`() = runTest {
        val groupId = repository.saveGroup(Group(name = "Too small"))
        repository.saveParticipant(Participant(groupId = groupId, name = "Ana"))
        repository.saveParticipant(Participant(groupId = groupId, name = "Bruno"))

        assertEquals(DrawResult.NotEnoughParticipants, drawGroup(groupId))
        assertFalse(repository.observeGroupDetails(groupId).first()!!.group.isDrawn)
    }

    @Test
    fun `unknown group is reported as not enough participants`() = runTest {
        assertEquals(DrawResult.NotEnoughParticipants, drawGroup(groupId = 999))
    }
}
