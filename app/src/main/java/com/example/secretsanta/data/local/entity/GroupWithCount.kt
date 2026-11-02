package com.example.secretsanta.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded

data class GroupWithCount(
    @Embedded val group: GroupEntity,
    @ColumnInfo(name = "participant_count") val participantCount: Int,
)
