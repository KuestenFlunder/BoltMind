package com.boltmind.app.data.local

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Migrationstests.
 *
 * Der wichtigste Test des Umbaus: MIGRATION_2_3 fasst jede vorhandene Foto-Zeile
 * an. Ein Fehler hier kostet echte Nutzerdaten, und zwar unbemerkt -- die App
 * startet danach normal, nur die Fotos sind weg.
 *
 * Ausfuehren: ./gradlew connectedDebugAndroidTest  (Emulator oder Geraet noetig)
 */
@RunWith(AndroidJUnit4::class)
class MigrationTest {

    private companion object {
        const val TEST_DB = "migration-test.db"
    }

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        BoltMindDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    // ---------------------------------------------------------------
    // Hilfen
    // ---------------------------------------------------------------

    private fun SupportSQLiteDatabase.legeVorgangAn(id: Long, nummer: String) {
        insert(
            "reparaturvorgang", SQLiteDatabase.CONFLICT_FAIL,
            ContentValues().apply {
                put("id", id)
                put("auftragsnummer", nummer)
                put("status", "OFFEN")
                put("erstelltAm", 1_000L)
                put("aktualisiertAm", 1_000L)
            }
        )
    }

    private fun SupportSQLiteDatabase.legeSchrittV2An(
        id: Long,
        vorgangId: Long,
        nummer: Int,
        bauteilFoto: String?,
        ablageortFoto: String?,
        typ: String? = null,
        gestartetAm: Long = 5_000L,
        abgeschlossenAm: Long? = 9_000L
    ) {
        insert(
            "schritt", SQLiteDatabase.CONFLICT_FAIL,
            ContentValues().apply {
                put("id", id)
                put("reparaturvorgangId", vorgangId)
                put("schrittNummer", nummer)
                put("typ", typ)
                put("bauteilFotoPfad", bauteilFoto)
                put("ablageortFotoPfad", ablageortFoto)
                put("eingebautBeiMontage", 0)
                put("gestartetAm", gestartetAm)
                if (abgeschlossenAm == null) putNull("abgeschlossenAm")
                else put("abgeschlossenAm", abgeschlossenAm)
            }
        )
    }

    private class FotoZeile(
        val pfad: String,
        val reihenfolge: Int,
        val bauteil: Boolean,
        val uebersicht: Boolean,
        val ablageort: Boolean,
        val aufgenommenAm: Long
    )

    private fun SupportSQLiteDatabase.fotosVon(schrittId: Long): List<FotoZeile> {
        val liste = mutableListOf<FotoZeile>()
        query(
            "SELECT pfad, reihenfolge, istBauteil, istUebersicht, istAblageort, aufgenommenAm " +
                "FROM schritt_foto WHERE schrittId = $schrittId ORDER BY reihenfolge"
        ).use { c ->
            while (c.moveToNext()) {
                liste += FotoZeile(
                    pfad = c.getString(0),
                    reihenfolge = c.getInt(1),
                    bauteil = c.getInt(2) == 1,
                    uebersicht = c.getInt(3) == 1,
                    ablageort = c.getInt(4) == 1,
                    aufgenommenAm = c.getLong(5)
                )
            }
        }
        return liste
    }

    private fun SupportSQLiteDatabase.tabellenNamen(): Set<String> =
        query("SELECT name FROM sqlite_master WHERE type = 'table'").use { c ->
            buildSet { while (c.moveToNext()) add(c.getString(0)) }
        }

    private fun SupportSQLiteDatabase.indexNamen(tabelle: String): Set<String> =
        query("SELECT name FROM sqlite_master WHERE type = 'index' AND tbl_name = '$tabelle'").use { c ->
            buildSet { while (c.moveToNext()) c.getString(0)?.let { add(it) } }
        }

    private fun SupportSQLiteDatabase.spalten(tabelle: String): Set<String> =
        query("PRAGMA table_info($tabelle)").use { c ->
            buildSet { while (c.moveToNext()) add(c.getString(1)) }
        }

    // ---------------------------------------------------------------
    // 2 -> 3
    // ---------------------------------------------------------------

    @Test
    fun migration2Zu3_ueberfuehrt_alle_vier_altdaten_kombinationen() {
        helper.createDatabase(TEST_DB, 2).use { db ->
            db.legeVorgangAn(1, "2026-0815")
            // (a) Schritt ohne Fotos
            db.legeSchrittV2An(10, 1, 1, bauteilFoto = null, ablageortFoto = null)
            // (b) nur Bauteil
            db.legeSchrittV2An(20, 1, 2, bauteilFoto = "/photos/b20.jpg", ablageortFoto = null)
            // (c) Bauteil + Ablageort
            db.legeSchrittV2An(
                30, 1, 3,
                bauteilFoto = "/photos/b30.jpg", ablageortFoto = "/photos/a30.jpg",
                typ = "AUSGEBAUT", gestartetAm = 5_000L, abgeschlossenAm = 9_000L
            )
            // (d) nur Ablageort
            db.legeSchrittV2An(
                40, 1, 4,
                bauteilFoto = null, ablageortFoto = "/photos/a40.jpg",
                typ = "AM_FAHRZEUG"
            )
        }

        val db = helper.runMigrationsAndValidate(
            TEST_DB, 3, true, BoltMindDatabase.MIGRATION_2_3
        )

        assertEquals("Schritt ohne Fotos bleibt ohne Fotos", 0, db.fotosVon(10).size)

        db.fotosVon(20).let { fotos ->
            assertEquals(1, fotos.size)
            assertEquals("/photos/b20.jpg", fotos[0].pfad)
            assertEquals(0, fotos[0].reihenfolge)
            assertTrue(fotos[0].bauteil)
            assertTrue(!fotos[0].ablageort)
        }

        db.fotosVon(30).let { fotos ->
            assertEquals(2, fotos.size)
            assertEquals("/photos/b30.jpg", fotos[0].pfad)
            assertEquals(0, fotos[0].reihenfolge)
            assertTrue(fotos[0].bauteil)
            assertEquals("/photos/a30.jpg", fotos[1].pfad)
            assertEquals(1, fotos[1].reihenfolge)
            assertTrue(fotos[1].ablageort)
            assertTrue("Ablageort-Foto ist nicht zugleich Bauteil", !fotos[1].bauteil)
        }

        db.fotosVon(40).let { fotos ->
            assertEquals(1, fotos.size)
            assertEquals("/photos/a40.jpg", fotos[0].pfad)
            assertEquals(
                "Ohne Bauteil-Foto muss der Ablageort auf Position 0 liegen -- " +
                    "die Reihenfolge beginnt bei 0 und ist lueckenlos",
                0, fotos[0].reihenfolge
            )
            assertTrue(fotos[0].ablageort)
        }
    }

    @Test
    fun migration2Zu3_entfernt_die_alten_spalten_und_erhaelt_den_index() {
        helper.createDatabase(TEST_DB, 2).use { db ->
            db.legeVorgangAn(1, "2026-0815")
            db.legeSchrittV2An(10, 1, 1, "/photos/b.jpg", null)
        }

        val db = helper.runMigrationsAndValidate(
            TEST_DB, 3, true, BoltMindDatabase.MIGRATION_2_3
        )

        val spalten = db.spalten("schritt")
        assertTrue("typ ist entfernt", "typ" !in spalten)
        assertTrue("bauteilFotoPfad ist entfernt", "bauteilFotoPfad" !in spalten)
        assertTrue("ablageortFotoPfad ist entfernt", "ablageortFotoPfad" !in spalten)
        assertTrue("schrittNummer bleibt", "schrittNummer" in spalten)

        assertTrue(
            "Der Index aus Schema v2 muss die Migration ueberleben, " +
                "sonst schlaegt Rooms Schemapruefung fehl",
            "index_schritt_reparaturvorgangId" in db.indexNamen("schritt")
        )
        assertTrue(
            "index_schritt_foto_schrittId" in db.indexNamen("schritt_foto")
        )
        assertTrue("Hilfstabelle ist wieder weg", "schritt_alt_foto" !in db.tabellenNamen())
        assertTrue("zeit_messung ist angelegt", "zeit_messung" in db.tabellenNamen())
    }

    @Test
    fun migration2Zu3_laesst_vorgangsdaten_unangetastet() {
        helper.createDatabase(TEST_DB, 2).use { db ->
            db.legeVorgangAn(7, "2026-0793")
            db.legeSchrittV2An(70, 7, 1, "/photos/x.jpg", null)
        }

        val db = helper.runMigrationsAndValidate(
            TEST_DB, 3, true, BoltMindDatabase.MIGRATION_2_3
        )

        db.query("SELECT auftragsnummer, status FROM reparaturvorgang WHERE id = 7").use { c ->
            assertTrue(c.moveToFirst())
            assertEquals("2026-0793", c.getString(0))
            assertEquals("OFFEN", c.getString(1))
        }
        db.query("SELECT schrittNummer FROM schritt WHERE id = 70").use { c ->
            assertTrue(c.moveToFirst())
            assertEquals(1, c.getInt(0))
        }
    }

    // ---------------------------------------------------------------
    // 1 -> 3, der Weg eines sehr alten Bestands
    // ---------------------------------------------------------------

    @Test
    fun migration1Zu3_laeuft_durch_beide_stufen() {
        helper.createDatabase(TEST_DB, 1).use { db ->
            db.legeVorgangAn(1, "2026-0001")
            db.insert(
                "schritt", SQLiteDatabase.CONFLICT_FAIL,
                ContentValues().apply {
                    put("id", 11)
                    put("reparaturvorgangId", 1)
                    put("reihenfolge", 1)
                    put("fotoPfad", "/photos/alt.jpg")
                    // In Schema v1 war der Ablageort noch eine Nummer am Schritt.
                    // MIGRATION_1_2 laesst sie ersatzlos fallen.
                    put("ablageortNummer", 3)
                    put("eingebautBeiMontage", 0)
                    put("gestartetAm", 1_000L)
                    put("abgeschlossenAm", 2_000L)
                }
            )
        }

        val db = helper.runMigrationsAndValidate(
            TEST_DB, 3, true,
            BoltMindDatabase.MIGRATION_1_2, BoltMindDatabase.MIGRATION_2_3
        )

        db.fotosVon(11).let { fotos ->
            assertEquals("Das alte fotoPfad-Feld wird zum Bauteil-Foto", 1, fotos.size)
            assertEquals("/photos/alt.jpg", fotos[0].pfad)
            assertEquals(0, fotos[0].reihenfolge)
            assertTrue(fotos[0].bauteil)
        }
    }
}
