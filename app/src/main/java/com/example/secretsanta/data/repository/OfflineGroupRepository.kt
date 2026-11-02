package com.example.secretsanta.data.repository

import androidx.room.withTransaction
import com.example.secretsanta.data.local.SecretSantaDatabase
import com.example.secretsanta.data.local.entity.AssignmentEntity
import com.example.secretsanta.data.toDomain
import com.example.secretsanta.data.toEntity
import com.example.secretsanta.domain.model.Assignment
import com.example.secretsanta.domain.model.Group
import com.example.secretsanta.domain.model.GroupDetails
import com.example.secretsanta.domain.model.Participant
import com.example.secretsanta.domain.repository.GroupRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [GroupRepository] backed by the local Room database. The app works fully offline and
 * without any account, like the original app.
 */
@Singleton
class OfflineGroupRepository @Inject constructor(
    private val database: SecretSantaDatabase,
) : GroupRepository {

    private val groupDao = database.groupDao()
    private val participantDao = database.participantDao()
    private val assignmentDao = database.assignmentDao()

    override fun observeGroups(): Flow<List<Group>> =
        groupDao.observeAllWithCount().map { groups -> groups.map { it.toDomain() } }

    override fun observeGroupDetails(groupId: Long): Flow<GroupDetails?> = combine(
        groupDao.observeById(groupId),
        participantDao.observeByGroup(groupId),
        participantDao.observeExclusionsByGroup(groupId),
        assignmentDao.observeByGroup(groupId),
    ) { group, participantEntities, exclusions, assignmentEntities ->
        group ?: return@combine null

        val excludedByParticipant = exclusions.groupBy(
            keySelector = { it.participantId },
            valueTransform = { it.excludedId },
        )
        val participants = participantEntities.map {
            it.toDomain(excludedByParticipant[it.id].orEmpty().toSet())
        }
        val byId = participants.associateBy { it.id }
        val assignments = assignmentEntities.mapNotNull { entity ->
            val giver = byId[entity.giverId] ?: return@mapNotNull null
            val receiver = byId[entity.receiverId] ?: return@mapNotNull null
            Assignment(giver, receiver, entity.revealed)
        }

        GroupDetails(
            group = group.toDomain(participantCount = participants.size),
            participants = participants,
            assignments = assignments,
        )
    }

    override suspend fun getGroup(groupId: Long): Group? = groupDao.getById(groupId)?.toDomain()

    override suspend fun saveGroup(group: Group): Long {
        val rowId = groupDao.upsert(group.toEntity())
        return if (group.id == 0L) rowId else group.id
    }

    override suspend fun deleteGroup(groupId: Long) = groupDao.deleteById(groupId)

    override suspend fun getParticipant(participantId: Long): Participant? {
        val entity = participantDao.getById(participantId) ?: return null
        return entity.toDomain(participantDao.getExcludedIds(participantId).toSet())
    }

    override suspend fun saveParticipant(participant: Participant): Long = database.withTransaction {
        val isNew = participant.id == 0L
        val previousExclusions = if (isNew) {
            emptySet()
        } else {
            participantDao.getExcludedIds(participant.id).toSet()
        }

        val rowId = participantDao.upsert(participant.toEntity())
        val id = if (isNew) rowId else participant.id
        val exclusions = participant.excludedIds - id
        participantDao.replaceExclusions(id, exclusions)

        // A new person or different exclusions make the previous result invalid.
        if (isNew || exclusions != previousExclusions) {
            clearDraw(participant.groupId)
        }
        id
    }

    override suspend fun deleteParticipant(participantId: Long) = database.withTransaction {
        val participant = participantDao.getById(participantId) ?: return@withTransaction
        participantDao.deleteById(participantId)
        clearDraw(participant.groupId)
    }

    override suspend fun saveDraw(groupId: Long, pairs: Map<Long, Long>) = database.withTransaction {
        assignmentDao.deleteByGroup(groupId)
        assignmentDao.insertAll(
            pairs.map { (giver, receiver) ->
                AssignmentEntity(giverId = giver, groupId = groupId, receiverId = receiver)
            },
        )
        groupDao.setDrawnAt(groupId, System.currentTimeMillis())
    }

    override suspend fun resetDraw(groupId: Long) = database.withTransaction {
        clearDraw(groupId)
    }

    override suspend fun setRevealed(groupId: Long, giverId: Long, revealed: Boolean) =
        assignmentDao.setRevealed(groupId, giverId, revealed)

    private suspend fun clearDraw(groupId: Long) {
        assignmentDao.deleteByGroup(groupId)
        groupDao.setDrawnAt(groupId, null)
    }
}
