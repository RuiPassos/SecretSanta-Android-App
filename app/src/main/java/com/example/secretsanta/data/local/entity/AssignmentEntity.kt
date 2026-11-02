package com.example.secretsanta.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "assignments",
    foreignKeys = [
        ForeignKey(
            entity = GroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["group_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ParticipantEntity::class,
            parentColumns = ["id"],
            childColumns = ["giver_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ParticipantEntity::class,
            parentColumns = ["id"],
            childColumns = ["receiver_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("group_id"), Index("receiver_id")],
)
data class AssignmentEntity(
    @PrimaryKey @ColumnInfo(name = "giver_id") val giverId: Long,
    @ColumnInfo(name = "group_id") val groupId: Long,
    @ColumnInfo(name = "receiver_id") val receiverId: Long,
    val revealed: Boolean = false,
)
