package com.example.secretsanta.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/** [participantId] must never draw [excludedId]. */
@Entity(
    tableName = "exclusions",
    primaryKeys = ["participant_id", "excluded_id"],
    foreignKeys = [
        ForeignKey(
            entity = ParticipantEntity::class,
            parentColumns = ["id"],
            childColumns = ["participant_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ParticipantEntity::class,
            parentColumns = ["id"],
            childColumns = ["excluded_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("excluded_id")],
)
data class ExclusionEntity(
    @ColumnInfo(name = "participant_id") val participantId: Long,
    @ColumnInfo(name = "excluded_id") val excludedId: Long,
)
