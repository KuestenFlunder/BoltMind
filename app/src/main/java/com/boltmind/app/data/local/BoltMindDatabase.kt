package com.boltmind.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
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

    companion object {
        fun create(context: Context): BoltMindDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                BoltMindDatabase::class.java,
                "boltmind.db"
            ).build()
        }
    }
}
