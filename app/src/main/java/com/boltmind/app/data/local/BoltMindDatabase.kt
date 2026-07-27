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
import com.boltmind.app.data.model.SchrittFoto
import com.boltmind.app.service.zeiterfassung.ZeitMessung
import com.boltmind.app.service.zeiterfassung.ZeitMessungDao

@Database(
    entities = [
        Reparaturvorgang::class,
        Schritt::class,
        SchrittFoto::class,
        ZeitMessung::class
    ],
    version = 3,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class BoltMindDatabase : RoomDatabase() {

    abstract fun reparaturvorgangDao(): ReparaturvorgangDao

    abstract fun schrittDao(): SchrittDao

    abstract fun schrittFotoDao(): SchrittFotoDao

    abstract fun zeitMessungDao(): ZeitMessungDao

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

        /**
         * Version 2 -> 3: Das Foto-Modell und die Zeiterfassung.
         *
         * `schritt` verliert `typ`, `bauteilFotoPfad` und `ablageortFotoPfad`; die Fotos
         * ziehen in die neue Tabelle `schritt_foto` um. Zusaetzlich entsteht `zeit_messung`
         * (F-005).
         *
         * Die Reihenfolge der Schritte ist verbindlich: die Alt-Pfade muessen gesichert
         * werden, **bevor** `schritt` ersetzt wird, und `schritt_foto` darf erst danach
         * befuellt werden -- sonst raeumt der CASCADE beim `DROP TABLE schritt` die frisch
         * eingefuegten Fotozeilen wieder ab.
         *
         * Spec: docs/specs/F-003-demontage/README.md, Abschnitt "DB-Migration 2 -> 3"
         *
         * Abweichung von der Spec: `zeit_messung` war urspruenglich fuer eine eigene
         * Migration 3 -> 4 vorgesehen. Beide Tabellen sind in keinem ausgelieferten Build
         * enthalten, deshalb entsteht hier eine einzige Migration statt zweier.
         */
        internal val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Alt-Pfade sichern, bevor `schritt` ersetzt wird.
                db.execSQL(
                    """CREATE TABLE schritt_alt_foto (
                        schrittId INTEGER NOT NULL,
                        bauteilFotoPfad TEXT,
                        ablageortFotoPfad TEXT,
                        gestartetAm INTEGER NOT NULL,
                        abgeschlossenAm INTEGER
                    )"""
                )
                db.execSQL(
                    """INSERT INTO schritt_alt_foto (schrittId, bauteilFotoPfad, ablageortFotoPfad, gestartetAm, abgeschlossenAm)
                       SELECT id, bauteilFotoPfad, ablageortFotoPfad, gestartetAm, abgeschlossenAm FROM schritt"""
                )

                // 2. `schritt` ohne die drei Spalten neu aufbauen. SQLite kann Spalten
                //    nicht direkt entfernen.
                db.execSQL(
                    """CREATE TABLE schritt_neu (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        reparaturvorgangId INTEGER NOT NULL,
                        schrittNummer INTEGER NOT NULL,
                        eingebautBeiMontage INTEGER NOT NULL,
                        gestartetAm INTEGER NOT NULL,
                        abgeschlossenAm INTEGER,
                        FOREIGN KEY(reparaturvorgangId) REFERENCES reparaturvorgang(id) ON UPDATE NO ACTION ON DELETE CASCADE
                    )"""
                )
                db.execSQL(
                    """INSERT INTO schritt_neu (id, reparaturvorgangId, schrittNummer, eingebautBeiMontage, gestartetAm, abgeschlossenAm)
                       SELECT id, reparaturvorgangId, schrittNummer, eingebautBeiMontage, gestartetAm, abgeschlossenAm FROM schritt"""
                )
                db.execSQL("DROP TABLE schritt")
                db.execSQL("ALTER TABLE schritt_neu RENAME TO schritt")
                // Der Index aus Schema v2 muss wieder entstehen, sonst schlaegt Rooms
                // Schema-Pruefung fehl.
                db.execSQL("CREATE INDEX IF NOT EXISTS index_schritt_reparaturvorgangId ON schritt (reparaturvorgangId)")

                // 3. Neue Foto-Tabelle.
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS schritt_foto (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        schrittId INTEGER NOT NULL,
                        pfad TEXT NOT NULL,
                        reihenfolge INTEGER NOT NULL,
                        istBauteil INTEGER NOT NULL,
                        istUebersicht INTEGER NOT NULL,
                        istAblageort INTEGER NOT NULL,
                        aufgenommenAm INTEGER NOT NULL,
                        FOREIGN KEY(schrittId) REFERENCES schritt(id) ON UPDATE NO ACTION ON DELETE CASCADE
                    )"""
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_schritt_foto_schrittId ON schritt_foto (schrittId)")

                // 4. Bauteil-Fotos uebernehmen, immer Position 0.
                db.execSQL(
                    """INSERT INTO schritt_foto (schrittId, pfad, reihenfolge, istBauteil, istUebersicht, istAblageort, aufgenommenAm)
                       SELECT schrittId, bauteilFotoPfad, 0, 1, 0, 0, gestartetAm
                       FROM schritt_alt_foto WHERE bauteilFotoPfad IS NOT NULL"""
                )

                // 5. Ablageort-Fotos uebernehmen. Position 1 nur, wenn ein Bauteil-Foto
                //    davor liegt -- die Reihenfolge muss bei 0 beginnen und lueckenlos sein.
                db.execSQL(
                    """INSERT INTO schritt_foto (schrittId, pfad, reihenfolge, istBauteil, istUebersicht, istAblageort, aufgenommenAm)
                       SELECT schrittId,
                              ablageortFotoPfad,
                              CASE WHEN bauteilFotoPfad IS NULL THEN 0 ELSE 1 END,
                              0, 0, 1,
                              COALESCE(abgeschlossenAm, gestartetAm)
                       FROM schritt_alt_foto WHERE ablageortFotoPfad IS NOT NULL"""
                )

                // 6. Hilfstabelle wieder loeschen.
                db.execSQL("DROP TABLE schritt_alt_foto")

                // 7. Zeiterfassung (F-005). Bewusst ohne Foreign Key: der Service kennt
                //    die Consumer-Tabellen nicht.
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS zeit_messung (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        referenzId INTEGER NOT NULL,
                        referenzTyp TEXT NOT NULL,
                        gestartetAm INTEGER NOT NULL,
                        gestopptAm INTEGER
                    )"""
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_zeit_messung_referenzTyp_referenzId ON zeit_messung (referenzTyp, referenzId)"
                )
            }
        }

        fun create(context: Context): BoltMindDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                BoltMindDatabase::class.java,
                "boltmind.db"
            )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build()
        }
    }
}
