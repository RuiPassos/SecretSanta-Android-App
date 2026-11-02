package com.example.secretsanta.domain.repository

import com.example.secretsanta.domain.model.Group
import com.example.secretsanta.domain.model.GroupDetails
import com.example.secretsanta.domain.model.Participant
import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth for groups, participants and draw results.
 *
 * The UI only depends on this interface, so the local Room implementation could be swapped for (or
 * combined with) a cloud backend without touching the presentation layer.
 */
interface GroupRepository {
    fun observeGroups(): Flow<List<Group>>

    fun observeGroupDetails(groupId: Long): Flow<GroupDetails?>

    suspend fun getGroup(groupId: Long): Group?

    /** Inserts or updates the group and returns its id. */
    suspend fun saveGroup(group: Group): Long

    suspend fun deleteGroup(groupId: Long)

    suspend fun getParticipant(participantId: Long): Participant?

    /**
     * Inserts or updates a participant (including its exclusions) and returns its id.
     * Changing who takes part or their exclusions invalidates an existing draw.
     */
    suspend fun saveParticipant(participant: Participant): Long

    suspend fun deleteParticipant(participantId: Long)

    /** Persists the draw result, replacing any previous one. */
    suspend fun saveDraw(groupId: Long, pairs: Map<Long, Long>)

    suspend fun resetDraw(groupId: Long)

    suspend fun setRevealed(groupId: Long, giverId: Long, revealed: Boolean)
}
