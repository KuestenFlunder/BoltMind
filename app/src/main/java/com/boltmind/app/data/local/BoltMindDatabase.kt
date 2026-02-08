package com.boltmind.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.boltmind.app.data.model.Reparaturvorgang
import com.boltmind.app.data.model.Schritt

@Database(
    entities = [Reparaturvorgang::class, Schritt::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class BoltMindDatabase : RoomDatabase() {
    abstract fun reparaturvorgangDao(): ReparaturvorgangDao
    abstract fun schrittDao(): SchrittDao
}
