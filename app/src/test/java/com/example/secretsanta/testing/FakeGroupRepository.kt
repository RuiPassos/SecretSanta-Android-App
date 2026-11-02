package com.example.secretsanta.testing

import com.example.secretsanta.domain.model.Assignment
import com.example.secretsanta.domain.model.Group
import com.example.secretsanta.domain.model.GroupDetails
import com.example.secretsanta.domain.model.Participant
import com.example.secretsanta.domain.repository.GroupRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** In-memory [GroupRepository] that mimics the rules of the Room implementation. */
class FakeGroupRepository : GroupRepository {

    private data class State(
        val groups: Map<Long, Group> = emptyMap(),
        val participants: Map<Long, Participant> = emptyMap(),
        /** giverId -> (receiverId, revealed) */
        val assignments: Map<Long, Pair<Long, Boolean>> = emptyMap(),
    )

    private val state = MutableStateFlow(State())
    private var nextId = 1L

    override fun observeGroups(): Flow<List<Group>> = state.map { s ->
        s.groups.values
            .map { group -> group.copy(participantCount = s.participants.values.count { it.groupId == group.id }) }
            .sortedByDescending(Group::createdAt)
    }

    override fun observeGroupDetails(groupId: Long): Flow<GroupDetails?> = state.map { s ->
        val group = s.groups[groupId] ?: return@map null
        val participants = s.participants.values.filter { it.groupId == groupId }.sortedBy { it.name }
        val byId = participants.associateBy(Participant::id)
        val assignments = s.assignments.mapNotNull { (giver, value) ->
            val g = byId[giver] ?: return@mapNotNull null
            val r = byId[value.first] ?: return@mapNotNull null
            Assignment(g, r, value.second)
        }
        GroupDetails(group.copy(participantCount = participants.size), participants, assignments)
    }

    override suspend fun getGroup(groupId: Long): Group? = state.value.groups[groupId]

    override suspend fun saveGroup(group: Group): Long {
        val id = if (group.id == 0L) nextId++ else group.id
        state.update { it.copy(groups = it.groups + (id to group.copy(id = id))) }
        return id
    }

    override suspend fun deleteGroup(groupId: Long) = state.update { s ->
        val removed = s.participants.values.filter { it.groupId == groupId }.map(Participant::id).toSet()
        s.copy(
            groups = s.groups - groupId,
            participants = s.participants - removed,
            assignments = s.assignments - removed,
        )
    }

    override suspend fun getParticipant(participantId: Long): Participant? = state.value.participants[participantId]

    override suspend fun saveParticipant(participant: Participant): Long {
        val isNew = participant.id == 0L
        val id = if (isNew) nextId++ else participant.id
        val previous = state.value.participants[id]
        state.update { it.copy(participants = it.participants + (id to participant.copy(id = id))) }
        if (isNew || previous?.excludedIds != participant.excludedIds) resetDraw(participant.groupId)
        return id
    }

    override suspend fun deleteParticipant(participantId: Long) {
        val participant = state.value.participants[participantId] ?: return
        state.update { it.copy(participants = it.participants - participantId) }
        resetDraw(participant.groupId)
    }

    override suspend fun saveDraw(groupId: Long, pairs: Map<Long, Long>) = state.update { s ->
        val group = s.groups.getValue(groupId)
        s.copy(
            groups = s.groups + (groupId to group.copy(drawnAt = 1L)),
            assignments = s.assignments + pairs.mapValues { (_, receiver) -> receiver to false },
        )
    }

    override suspend fun resetDraw(groupId: Long) = state.update { s ->
        val group = s.groups[groupId] ?: return@update s
        val ids = s.participants.values.filter { it.groupId == groupId }.map(Participant::id).toSet()
        s.copy(groups = s.groups + (groupId to group.copy(drawnAt = null)), assignments = s.assignments - ids)
    }

    override suspend fun setRevealed(groupId: Long, giverId: Long, revealed: Boolean) = state.update { s ->
        val current = s.assignments[giverId] ?: return@update s
        s.copy(assignments = s.assignments + (giverId to current.copy(second = revealed)))
    }
}
