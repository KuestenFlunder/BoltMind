package com.boltmind.app.data.repository

import com.boltmind.app.data.local.ReparaturvorgangDao
import com.boltmind.app.data.local.SchrittDao
import com.boltmind.app.data.model.Reparaturvorgang
import com.boltmind.app.data.model.ReparaturvorgangMitAnzahl
import com.boltmind.app.data.model.Schritt
import com.boltmind.app.data.model.SchrittTyp
import com.boltmind.app.data.model.VorgangStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant

class ReparaturRepositoryTest {

    private lateinit var vorgangDao: ReparaturvorgangDao
    private lateinit var schrittDao: SchrittDao
    private lateinit var repository: ReparaturRepository

    @BeforeEach
    fun setup() {
        vorgangDao = mock()
        schrittDao = mock()
        repository = ReparaturRepository(vorgangDao, schrittDao)
    }

    private fun testVorgang(
        id: Long = 1L,
        auftragsnummer: String = "TEST-001",
        status: VorgangStatus = VorgangStatus.OFFEN,
        aktualisiertAm: Instant = Instant.now()
    ) = Reparaturvorgang(
        id = id,
        fahrzeugFotoPfad = "/test/foto.jpg",
        auftragsnummer = auftragsnummer,
        status = status,
        aktualisiertAm = aktualisiertAm
    )

    private fun testSchritt(
        id: Long = 1L,
        vorgangId: Long = 1L,
        schrittNummer: Int = 1,
        bauteilFotoPfad: String? = null,
        typ: SchrittTyp? = null
    ) = Schritt(
        id = id,
        reparaturvorgangId = vorgangId,
        schrittNummer = schrittNummer,
        bauteilFotoPfad = bauteilFotoPfad,
        typ = typ
    )

    @Nested
    inner class `Offene Vorgaenge` {

        @Test
        fun `liefert nur offene Vorgaenge`() = runTest {
            // Given
            val offeneVorgaenge = listOf(testVorgang(status = VorgangStatus.OFFEN))
            whenever(vorgangDao.beobachteNachStatus(VorgangStatus.OFFEN))
                .thenReturn(flowOf(offeneVorgaenge))

            // When
            val result = repository.beobachteOffeneVorgaenge().first()

            // Then
            assertEquals(1, result.size)
            assertEquals(VorgangStatus.OFFEN, result.first().status)
        }

        @Test
        fun `liefert offene Vorgaenge mit Schrittanzahl`() = runTest {
            // Given
            val vorgang = testVorgang(id = 1L, status = VorgangStatus.OFFEN)
            val vorgaengeMitAnzahl = listOf(
                ReparaturvorgangMitAnzahl(vorgang = vorgang, schrittAnzahl = 7)
            )
            whenever(vorgangDao.beobachteNachStatusMitAnzahl(VorgangStatus.OFFEN))
                .thenReturn(flowOf(vorgaengeMitAnzahl))

            // When
            val result = repository.beobachteOffeneVorgaengeMitAnzahl().first()

            // Then
            assertEquals(1, result.size)
            assertEquals(VorgangStatus.OFFEN, result.first().vorgang.status)
            assertEquals(7, result.first().schrittAnzahl)
        }
    }

    @Nested
    inner class `Archivierte Vorgaenge` {

        @Test
        fun `liefert nur archivierte Vorgaenge`() = runTest {
            // Given
            val archivierteVorgaenge = listOf(testVorgang(status = VorgangStatus.ARCHIVIERT))
            whenever(vorgangDao.beobachteNachStatus(VorgangStatus.ARCHIVIERT))
                .thenReturn(flowOf(archivierteVorgaenge))

            // When
            val result = repository.beobachteArchivierteVorgaenge().first()

            // Then
            assertEquals(1, result.size)
            assertEquals(VorgangStatus.ARCHIVIERT, result.first().status)
        }
    }

    @Nested
    inner class `Vorgang erstellen` {

        @Test
        fun `erstellt Vorgang und gibt ID zurueck`() = runTest {
            // Given
            val vorgang = testVorgang()
            whenever(vorgangDao.einfuegen(vorgang)).thenReturn(42L)

            // When
            val id = repository.erstelleVorgang(vorgang)

            // Then
            assertEquals(42L, id)
            verify(vorgangDao).einfuegen(vorgang)
        }
    }

    @Nested
    inner class `Vorgang finden` {

        @Test
        fun `findet Vorgang per ID`() = runTest {
            // Given
            val vorgang = testVorgang(id = 1L)
            whenever(vorgangDao.findById(1L)).thenReturn(vorgang)

            // When
            val result = repository.findVorgangById(1L)

            // Then
            assertEquals(vorgang, result)
        }

        @Test
        fun `gibt null zurueck wenn Vorgang nicht existiert`() = runTest {
            // Given
            whenever(vorgangDao.findById(999L)).thenReturn(null)

            // When
            val result = repository.findVorgangById(999L)

            // Then
            assertNull(result)
        }
    }

    @Nested
    inner class `Vorgang aktualisieren` {

        @Test
        fun `aktualisiert bestehenden Vorgang`() = runTest {
            // Given
            val vorgang = testVorgang(id = 1L, auftragsnummer = "UPDATED-001")

            // When
            repository.aktualisiereVorgang(vorgang)

            // Then
            verify(vorgangDao).aktualisieren(vorgang)
        }
    }

    @Nested
    inner class `Vorgang loeschen` {

        @Test
        fun `loescht Vorgang per ID`() = runTest {
            // When
            repository.loescheVorgang(1L)

            // Then
            verify(vorgangDao).loeschen(1L)
        }
    }

    @Nested
    inner class `Schritte verwalten` {

        @Test
        fun `zaehlt Schritte eines Vorgangs`() = runTest {
            // Given
            whenever(vorgangDao.zaehleSchritte(1L)).thenReturn(5)

            // When
            val count = repository.zaehleSchritte(1L)

            // Then
            assertEquals(5, count)
        }

        @Test
        fun `erstellt Schritt und gibt ID zurueck`() = runTest {
            // Given
            val schritt = Schritt(
                reparaturvorgangId = 1L,
                schrittNummer = 1,
                bauteilFotoPfad = "/test/schritt.jpg"
            )
            whenever(schrittDao.einfuegen(schritt)).thenReturn(10L)

            // When
            val id = repository.erstelleSchritt(schritt)

            // Then
            assertEquals(10L, id)
            verify(schrittDao).einfuegen(schritt)
        }

        @Test
        fun `aktualisiert bestehenden Schritt`() = runTest {
            // Given
            val schritt = Schritt(
                id = 5L,
                reparaturvorgangId = 1L,
                schrittNummer = 1,
                bauteilFotoPfad = "/test/schritt.jpg",
                eingebautBeiMontage = true
            )

            // When
            repository.aktualisiereSchritt(schritt)

            // Then
            verify(schrittDao).aktualisieren(schritt)
        }
    }

    @Nested
    inner class `Demontage-Operationen` {

        @Test
        fun `schrittAnlegen erstellt Schritt mit korrekter Nummer`() = runTest {
            // Given
            whenever(schrittDao.holeNaechsteSchrittNummer(1L)).thenReturn(3)
            whenever(schrittDao.einfuegen(any())).thenReturn(10L)

            // When
            val schritt = repository.schrittAnlegen(1L)

            // Then
            assertEquals(3, schritt.schrittNummer)
            assertEquals(1L, schritt.reparaturvorgangId)
            assertNull(schritt.bauteilFotoPfad)
            assertNull(schritt.typ)
            verify(schrittDao).einfuegen(any())
        }

        @Test
        fun `bauteilFotoBestaetigen delegiert an DAO`() = runTest {
            // When
            repository.bauteilFotoBestaetigen(5L, "/photos/bauteil_5.jpg")

            // Then
            verify(schrittDao).aktualisiereBauteilFoto(5L, "/photos/bauteil_5.jpg")
        }

        @Test
        fun `ablageortFotoBestaetigen setzt Foto und Abschluss`() = runTest {
            // When
            repository.ablageortFotoBestaetigen(5L, "/photos/ablageort_5.jpg")

            // Then
            verify(schrittDao).aktualisiereAblageortFotoUndAbschluss(
                eq(5L),
                eq("/photos/ablageort_5.jpg"),
                any()
            )
        }

        @Test
        fun `schrittTypSetzen delegiert an DAO mit String-Konvertierung`() = runTest {
            // When
            repository.schrittTypSetzen(5L, SchrittTyp.AUSGEBAUT)

            // Then
            verify(schrittDao).aktualisiereTyp(5L, "AUSGEBAUT")
        }

        @Test
        fun `schrittAbschliessen setzt Typ und Abschluss-Timestamp`() = runTest {
            // When
            repository.schrittAbschliessen(5L, SchrittTyp.AM_FAHRZEUG)

            // Then
            verify(schrittDao).aktualisiereTypUndAbschluss(
                eq(5L),
                eq("AM_FAHRZEUG"),
                any()
            )
        }

        @Test
        fun `findUnabgeschlossenenSchritt delegiert an DAO`() = runTest {
            // Given
            val schritt = Schritt(
                id = 5L,
                reparaturvorgangId = 1L,
                schrittNummer = 3,
                bauteilFotoPfad = "/test.jpg"
            )
            whenever(schrittDao.findUnabgeschlossenenSchritt(1L)).thenReturn(schritt)

            // When
            val result = repository.findUnabgeschlossenenSchritt(1L)

            // Then
            assertEquals(schritt, result)
        }

        @Test
        fun `findUnabgeschlossenenSchritt gibt null wenn keiner offen`() = runTest {
            // Given
            whenever(schrittDao.findUnabgeschlossenenSchritt(1L)).thenReturn(null)

            // When
            val result = repository.findUnabgeschlossenenSchritt(1L)

            // Then
            assertNull(result)
        }
    }
}
