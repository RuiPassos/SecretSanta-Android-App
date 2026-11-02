package com.example.secretsanta.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.secretsanta.data.local.entity.AssignmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AssignmentDao {
    @Query("SELECT * FROM assignments WHERE group_id = :groupId")
    fun observeByGroup(groupId: Long): Flow<List<AssignmentEntity>>

    @Insert
    suspend fun insertAll(assignments: List<AssignmentEntity>)

    @Query("DELETE FROM assignments WHERE group_id = :groupId")
    suspend fun deleteByGroup(groupId: Long)

    @Query("UPDATE assignments SET revealed = :revealed WHERE group_id = :groupId AND giver_id = :giverId")
    suspend fun setRevealed(groupId: Long, giverId: Long, revealed: Boolean)
}
