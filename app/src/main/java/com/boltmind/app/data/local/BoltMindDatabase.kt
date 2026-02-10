package com.boltmind.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.boltmind.app.data.model.Reparaturvorgang
import com.boltmind.app.data.model.Schritt

@Database(
    entities = [Reparaturvorgang::class, Schritt::class],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class BoltMindDatabase : RoomDatabase() {

    abstract fun reparaturvorgangDao(): ReparaturvorgangDao

    abstract fun schrittDao(): SchrittDao

    companion object {
        internal val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """CREATE TABLE schritt_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        reparaturvorgangId INTEGER NOT NULL,
                        schrittNummer INTEGER NOT NULL,
                        typ TEXT,
                        bauteilFotoPfad TEXT,
                        ablageortFotoPfad TEXT,
                        eingebautBeiMontage INTEGER NOT NULL DEFAULT 0,
                        gestartetAm INTEGER NOT NULL,
                        abgeschlossenAm INTEGER,
                        FOREIGN KEY(reparaturvorgangId) REFERENCES reparaturvorgang(id) ON DELETE CASCADE
                    )""".trimIndent()
                )
                db.execSQL(
                    """INSERT INTO schritt_new (id, reparaturvorgangId, schrittNummer, bauteilFotoPfad, eingebautBeiMontage, gestartetAm, abgeschlossenAm)
                        SELECT id, reparaturvorgangId, reihenfolge, fotoPfad, eingebautBeiMontage, gestartetAm, abgeschlossenAm
                        FROM schritt""".trimIndent()
                )
                db.execSQL("DROP TABLE schritt")
                db.execSQL("ALTER TABLE schritt_new RENAME TO schritt")
                db.execSQL("CREATE INDEX index_schritt_reparaturvorgangId ON schritt(reparaturvorgangId)")
            }
        }

        fun create(context: Context): BoltMindDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                BoltMindDatabase::class.java,
                "boltmind.db"
            )
                .addMigrations(MIGRATION_1_2)
                .build()
        }
    }
}
