package com.example.secretsanta.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.secretsanta.data.local.SecretSantaDatabase
import com.example.secretsanta.data.repository.OfflineGroupRepository
import com.example.secretsanta.domain.model.Group
import com.example.secretsanta.domain.model.Participant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class OfflineGroupRepositoryTest {

    private lateinit var database: SecretSantaDatabase
    private lateinit var repository: OfflineGroupRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, SecretSantaDatabase::class.java).build()
        repository = OfflineGroupRepository(database)
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun groupWithParticipantsAndExclusionsIsPersisted() = runTest {
        val groupId = repository.saveGroup(Group(name = "Family", eventDate = LocalDate.of(2026, 12, 24)))
        val ana = repository.saveParticipant(Participant(groupId = groupId, name = "Ana"))
        val bruno = repository.saveParticipant(Participant(groupId = groupId, name = "Bruno"))
        repository.saveParticipant(Participant(id = ana, groupId = groupId, name = "Ana", excludedIds = setOf(bruno)))

        val details = repository.observeGroupDetails(groupId).first()!!

        assertEquals(LocalDate.of(2026, 12, 24), details.group.eventDate)
        assertEquals(2, details.group.participantCount)
        assertEquals(setOf(bruno), details.participants.single { it.id == ana }.excludedIds)
        assertEquals(2, repository.observeGroups().first().single().participantCount)
    }

    @Test
    fun changingExclusionsResetsTheDrawButEditingContactsDoesNot() = runTest {
        val groupId = repository.saveGroup(Group(name = "Family"))
        val ana = repository.saveParticipant(Participant(groupId = groupId, name = "Ana"))
        val bruno = repository.saveParticipant(Participant(groupId = groupId, name = "Bruno"))
        repository.saveDraw(groupId, mapOf(ana to bruno, bruno to ana))

        repository.saveParticipant(Participant(id = ana, groupId = groupId, name = "Ana", email = "ana@mail.com"))
        assertTrue(repository.observeGroupDetails(groupId).first()!!.group.isDrawn)

        repository.saveParticipant(Participant(id = ana, groupId = groupId, name = "Ana", excludedIds = setOf(bruno)))
        val details = repository.observeGroupDetails(groupId).first()!!
        assertFalse(details.group.isDrawn)
        assertTrue(details.assignments.isEmpty())
    }

    @Test
    fun revealStateIsStored() = runTest {
        val groupId = repository.saveGroup(Group(name = "Family"))
        val ana = repository.saveParticipant(Participant(groupId = groupId, name = "Ana"))
        val bruno = repository.saveParticipant(Participant(groupId = groupId, name = "Bruno"))
        repository.saveDraw(groupId, mapOf(ana to bruno, bruno to ana))

        repository.setRevealed(groupId, ana, revealed = true)

        val assignments = repository.observeGroupDetails(groupId).first()!!.assignments
        assertTrue(assignments.single { it.giver.id == ana }.revealed)
        assertFalse(assignments.single { it.giver.id == bruno }.revealed)
    }

    @Test
    fun deletingGroupCascadesToEverything() = runTest {
        val groupId = repository.saveGroup(Group(name = "Family"))
        val ana = repository.saveParticipant(Participant(groupId = groupId, name = "Ana"))

        repository.deleteGroup(groupId)

        assertNull(repository.observeGroupDetails(groupId).first())
        assertNull(repository.getParticipant(ana))
    }
}
