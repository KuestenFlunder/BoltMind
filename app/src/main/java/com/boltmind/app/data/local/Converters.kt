package com.boltmind.app.data.local

import androidx.room.TypeConverter
import com.boltmind.app.data.model.VorgangStatus
import java.time.Instant

class Converters {

    @TypeConverter
    fun fromInstant(value: Instant?): Long? = value?.toEpochMilli()

    @TypeConverter
    fun toInstant(value: Long?): Instant? = value?.let { Instant.ofEpochMilli(it) }

    @TypeConverter
    fun fromVorgangStatus(value: VorgangStatus): String = value.name

    @TypeConverter
    fun toVorgangStatus(value: String): VorgangStatus = VorgangStatus.valueOf(value)
}
