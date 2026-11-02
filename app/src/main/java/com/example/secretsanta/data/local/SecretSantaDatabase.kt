package com.example.secretsanta.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.secretsanta.data.local.dao.AssignmentDao
import com.example.secretsanta.data.local.dao.GroupDao
import com.example.secretsanta.data.local.dao.ParticipantDao
import com.example.secretsanta.data.local.entity.AssignmentEntity
import com.example.secretsanta.data.local.entity.ExclusionEntity
import com.example.secretsanta.data.local.entity.GroupEntity
import com.example.secretsanta.data.local.entity.ParticipantEntity

@Database(
    entities = [
        GroupEntity::class,
        ParticipantEntity::class,
        ExclusionEntity::class,
        AssignmentEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class SecretSantaDatabase : RoomDatabase() {
    abstract fun groupDao(): GroupDao
    abstract fun participantDao(): ParticipantDao
    abstract fun assignmentDao(): AssignmentDao

    companion object {
        const val NAME = "secret_santa.db"
    }
}
