package com.example.secretsanta.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.example.secretsanta.data.local.entity.ExclusionEntity
import com.example.secretsanta.data.local.entity.ParticipantEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ParticipantDao {
    @Query("SELECT * FROM participants WHERE group_id = :groupId ORDER BY name COLLATE NOCASE")
    fun observeByGroup(groupId: Long): Flow<List<ParticipantEntity>>

    @Query("SELECT * FROM participants WHERE id = :id")
    suspend fun getById(id: Long): ParticipantEntity?

    @Upsert
    suspend fun upsert(participant: ParticipantEntity): Long

    @Query("DELETE FROM participants WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query(
        """
        SELECT exclusions.* FROM exclusions
        INNER JOIN participants ON participants.id = exclusions.participant_id
        WHERE participants.group_id = :groupId
        """,
    )
    fun observeExclusionsByGroup(groupId: Long): Flow<List<ExclusionEntity>>

    @Query("SELECT excluded_id FROM exclusions WHERE participant_id = :participantId")
    suspend fun getExcludedIds(participantId: Long): List<Long>

    @Query("DELETE FROM exclusions WHERE participant_id = :participantId")
    suspend fun clearExclusions(participantId: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertExclusions(exclusions: List<ExclusionEntity>)

    @Transaction
    suspend fun replaceExclusions(participantId: Long, excludedIds: Set<Long>) {
        clearExclusions(participantId)
        insertExclusions(excludedIds.map { ExclusionEntity(participantId, it) })
    }
}
