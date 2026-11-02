package com.example.secretsanta.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.secretsanta.data.local.entity.GroupEntity
import com.example.secretsanta.data.local.entity.GroupWithCount
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDao {
    @Query(
        """
        SELECT groups.*, COUNT(participants.id) AS participant_count
        FROM groups
        LEFT JOIN participants ON participants.group_id = groups.id
        GROUP BY groups.id
        ORDER BY groups.created_at DESC
        """,
    )
    fun observeAllWithCount(): Flow<List<GroupWithCount>>

    @Query("SELECT * FROM groups WHERE id = :id")
    fun observeById(id: Long): Flow<GroupEntity?>

    @Query("SELECT * FROM groups WHERE id = :id")
    suspend fun getById(id: Long): GroupEntity?

    @Upsert
    suspend fun upsert(group: GroupEntity): Long

    @Query("UPDATE groups SET drawn_at = :drawnAt WHERE id = :id")
    suspend fun setDrawnAt(id: Long, drawnAt: Long?)

    @Query("DELETE FROM groups WHERE id = :id")
    suspend fun deleteById(id: Long)
}
