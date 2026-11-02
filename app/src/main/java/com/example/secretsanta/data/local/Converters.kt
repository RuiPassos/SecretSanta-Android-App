package com.example.secretsanta.data.local

import androidx.room.TypeConverter
import com.example.secretsanta.domain.model.DrawMode
import java.time.LocalDate

class Converters {
    @TypeConverter
    fun localDateToEpochDay(date: LocalDate?): Long? = date?.toEpochDay()

    @TypeConverter
    fun epochDayToLocalDate(epochDay: Long?): LocalDate? = epochDay?.let(LocalDate::ofEpochDay)

    @TypeConverter
    fun drawModeToString(mode: DrawMode): String = mode.name

    @TypeConverter
    fun stringToDrawMode(value: String): DrawMode = DrawMode.valueOf(value)
}
