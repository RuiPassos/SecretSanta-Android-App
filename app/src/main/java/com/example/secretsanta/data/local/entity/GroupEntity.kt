package com.example.secretsanta.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.secretsanta.domain.model.DrawMode
import java.time.LocalDate

@Entity(tableName = "groups")
data class GroupEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val mode: DrawMode,
    @ColumnInfo(name = "event_date") val eventDate: LocalDate?,
    val budget: String,
    val location: String,
    val notes: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "drawn_at") val drawnAt: Long?,
)
