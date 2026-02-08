package com.boltmind.app.data.local

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.boltmind.app.data.model.Reparaturvorgang
import com.boltmind.app.data.model.Schritt
import com.boltmind.app.data.model.VorgangStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class BoltMindDatabaseTest {

    private lateinit var db: BoltMindDatabase
    private lateinit var reparaturvorgangDao: ReparaturvorgangDao
    private lateinit var schrittDao: SchrittDao

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, BoltMindDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        reparaturvorgangDao = db.reparaturvorgangDao()
        schrittDao = db.schrittDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun sollteReparaturvorgangEinfuegenUndAbfragen() = runBlocking {
        val vorgang = Reparaturvorgang(
            fahrzeug = "BMW 320d Blau",
            auftragsnummer = "#2024-0815",
            beschreibung = "Bremsen vorne wechseln",
            anzahlAblageorte = 5,
            status = VorgangStatus.OFFEN,
            erstelltAm = Instant.now()
        )

        val id = reparaturvorgangDao.insert(vorgang)
        val loaded = reparaturvorgangDao.getById(id)

        assertEquals("BMW 320d Blau", loaded?.fahrzeug)
        assertEquals("#2024-0815", loaded?.auftragsnummer)
        assertEquals(VorgangStatus.OFFEN, loaded?.status)
    }

    @Test
    fun sollteVorgaengeMitSchrittanzahlLaden() = runBlocking {
        val vorgangId = reparaturvorgangDao.insert(
            Reparaturvorgang(
                fahrzeug = "Audi A4",
                auftragsnummer = "#2024-0001",
                beschreibung = "Getriebe",
                anzahlAblageorte = 3,
                status = VorgangStatus.OFFEN,
                erstelltAm = Instant.now()
            )
        )

        schrittDao.insert(
            Schritt(
                reparaturvorgangId = vorgangId,
                fotoPath = "/fake/path1.jpg",
                ablageortNummer = "1",
                reihenfolge = 1
            )
        )
        schrittDao.insert(
            Schritt(
                reparaturvorgangId = vorgangId,
                fotoPath = "/fake/path2.jpg",
                ablageortNummer = "2",
                reihenfolge = 2
            )
        )

        val result = reparaturvorgangDao.getAllByStatusMitAnzahl(VorgangStatus.OFFEN).first()
        assertEquals(1, result.size)
        assertEquals(2, result[0].schrittAnzahl)
        assertEquals("Audi A4", result[0].vorgang.fahrzeug)
    }

    @Test
    fun sollteCascadeDeleteSchritteLoeschen() = runBlocking {
        val vorgang = Reparaturvorgang(
            fahrzeug = "VW Golf",
            auftragsnummer = "#2024-0002",
            beschreibung = "Kupplung",
            anzahlAblageorte = 4,
            status = VorgangStatus.OFFEN,
            erstelltAm = Instant.now()
        )
        val vorgangId = reparaturvorgangDao.insert(vorgang)

        schrittDao.insert(
            Schritt(
                reparaturvorgangId = vorgangId,
                fotoPath = "/fake/path.jpg",
                ablageortNummer = "1",
                reihenfolge = 1
            )
        )

        val vorgangMitId = vorgang.copy(id = vorgangId)
        reparaturvorgangDao.delete(vorgangMitId)

        val schritte = schrittDao.getAllByVorgangIdEinmalig(vorgangId)
        assertTrue(schritte.isEmpty())
    }
}
