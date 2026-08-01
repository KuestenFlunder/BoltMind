package com.boltmind.app.feature.browser

import com.boltmind.app.data.foto.FotoManager
import com.boltmind.app.data.model.Schritt
import com.boltmind.app.data.model.SchrittFoto
import com.boltmind.app.data.repository.ReparaturRepository
import com.boltmind.app.service.zeiterfassung.ReferenzTyp
import com.boltmind.app.service.zeiterfassung.ZeiterfassungService
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify
import java.time.Instant

/**
 * Tests fuer die Foto- und Schrittaktionen der Demontage.
 *
 * Zwei Regeln aus `docs/specs/governance.md` haben hier ihren Wachposten, und
 * beide teilen dieselbe Eigenschaft: **ihr Bruch ist im Betrieb unsichtbar.**
 *
 * - **EXIF-Stripping.** Faellt es weg, funktioniert die App unveraendert -- nur
 *   liegen ab dann GPS-Koordinaten und Zeitstempel in jedem Werkstattfoto. Ein
 *   Datenschutzfehler ohne Symptom.
 * - **Wiederholen loescht erst nach Erfolg.** Wird die Reihenfolge gedreht,
 *   merkt das niemand, solange die Kamera liefert. Erst ein Abbruch vernichtet
 *   dann ein Foto, das der Mechaniker fuer sicher hielt.
 *
 * Deshalb pruefen die Tests hier nicht nur *dass* etwas passiert, sondern in
 * welcher **Reihenfolge**.
 */
@DisplayName("BrowserFotoSteuerung")
class BrowserFotoSteuerungTest {

    private lateinit var repository: ReparaturRepository
    private lateinit var fotoManager: FotoManager
    private lateinit var zeiterfassung: ZeiterfassungService
    private lateinit var steuerung: BrowserFotoSteuerung

    private companion object {
        const val SCHRITT_ID = 42L
        const val FRISCHER_ID = 99L
        const val ALTES_FOTO_ID = 7L
        const val NEUER_PFAD = "/photos/schritt_neu.jpg"
        const val ALTER_PFAD = "/photos/schritt_alt.jpg"
        val T0: Instant = Instant.parse("2026-07-27T08:00:00Z")

        val ALTES_FOTO = SchrittFoto(
            id = ALTES_FOTO_ID,
            schrittId = SCHRITT_ID,
            pfad = ALTER_PFAD,
            reihenfolge = 3,
            aufgenommenAm = T0
        )
    }

    @BeforeEach
    fun setUp() {
        repository = mock {
            onBlocking { findFoto(ALTES_FOTO_ID) } doReturn ALTES_FOTO
        }
        fotoManager = mock()
        zeiterfassung = mock()
        steuerung = BrowserFotoSteuerung(repository, fotoManager, zeiterfassung)
    }

    @Nested
    @DisplayName("EXIF-Stripping am Produktivpfad")
    inner class Exif {

        @Test
        fun `entfernt die Metadaten, bevor das Foto in der Datenbank landet`() = runTest {
            // When: ein frisch aufgenommenes Foto wird uebernommen
            steuerung.fotoUebernehmen(SCHRITT_ID, NEUER_PFAD)

            // Then: erst bereinigen, dann persistieren. Andersherum zeigte die
            // Zeile eine Weile auf eine Datei mit GPS-Daten darin.
            inOrder(fotoManager, repository) {
                verify(fotoManager).entferneExifMetadaten(NEUER_PFAD)
                verify(repository).fotoAnhaengen(SCHRITT_ID, NEUER_PFAD)
            }
        }

        @Test
        fun `entfernt die Metadaten auch beim Ersetzen eines Fotos`() = runTest {
            // When: "Wiederholen" liefert eine neue Aufnahme
            steuerung.fotoErsetzen(ALTES_FOTO_ID, NEUER_PFAD)

            // Then: der Ersetzen-Pfad ist kein Schleichweg an der Regel vorbei
            inOrder(fotoManager, repository) {
                verify(fotoManager).entferneExifMetadaten(NEUER_PFAD)
                verify(repository).fotoErsetzen(ALTES_FOTO_ID, NEUER_PFAD)
            }
        }

        @Test
        fun `fasst das alte Foto beim Ersetzen nicht als zu bereinigen an`() = runTest {
            // When: ersetzen
            steuerung.fotoErsetzen(ALTES_FOTO_ID, NEUER_PFAD)

            // Then: bereinigt wird nur die neue Datei. Die alte wurde bei ihrer
            // eigenen Uebernahme schon bereinigt und verschwindet gleich.
            verify(fotoManager, never()).entferneExifMetadaten(ALTER_PFAD)
        }
    }

    @Nested
    @DisplayName("Wiederholen loescht erst nach Erfolg")
    inner class Wiederholen {

        @Test
        fun `schreibt die neue Zeile, bevor die alte Datei verschwindet`() = runTest {
            // When: ein Foto wird ersetzt
            steuerung.fotoErsetzen(ALTES_FOTO_ID, NEUER_PFAD)

            // Then: die Reihenfolge ist die Regel. Bricht irgendetwas dazwischen
            // ab, ist im schlimmsten Fall eine Datei zu viel da -- nie eine zu
            // wenig (governance.md, "Wiederholen loescht erst nach Erfolg").
            inOrder(repository, fotoManager) {
                verify(repository).fotoErsetzen(ALTES_FOTO_ID, NEUER_PFAD)
                verify(fotoManager).loescheFoto(ALTER_PFAD)
            }
        }

        @Test
        fun `laesst alles stehen, wenn das zu ersetzende Foto nicht mehr existiert`() = runTest {
            // Given: die Zeile ist zwischenzeitlich verschwunden
            repository.stub { onBlocking { findFoto(ALTES_FOTO_ID) } doReturn null }

            // When: ersetzen
            steuerung.fotoErsetzen(ALTES_FOTO_ID, NEUER_PFAD)

            // Then: nichts geschrieben, nichts geloescht
            verify(repository, never()).fotoErsetzen(any(), any())
            verify(fotoManager, never()).loescheFoto(any())
        }
    }

    @Nested
    @DisplayName("Naechstes Teil")
    inner class NaechstesTeil {

        private val neuerSchritt = Schritt(
            id = 99L,
            reparaturvorgangId = 1L,
            schrittNummer = 5,
            gestartetAm = T0
        )

        @Test
        fun `schliesst den offenen Schritt ab und startet die Messung des neuen`() = runTest {
            // Given: Schritt 4 ist offen
            val offen = Schritt(
                id = SCHRITT_ID,
                reparaturvorgangId = 1L,
                schrittNummer = 4,
                gestartetAm = T0
            )
            repository.stub { onBlocking { schrittAnlegen(1L) } doReturn neuerSchritt }

            // When: "Naechstes Teil"
            steuerung.naechstesTeil(1L, offen)

            // Then: alter Schritt zu, alte Messung gestoppt, neue gestartet
            inOrder(repository, zeiterfassung) {
                verify(repository).schrittAbschliessen(SCHRITT_ID)
                verify(zeiterfassung).stoppeFallsLaeuft(SCHRITT_ID, ReferenzTyp.DEMONTAGE_SCHRITT)
                verify(repository).schrittAnlegen(1L)
                verify(zeiterfassung).starten(99L, ReferenzTyp.DEMONTAGE_SCHRITT)
            }
        }

        @Test
        fun `kommt ohne offenen Schritt aus`() = runTest {
            // Given: der allererste Schritt eines Vorgangs
            repository.stub { onBlocking { schrittAnlegen(1L) } doReturn neuerSchritt }

            // When: "Naechstes Teil" ohne Vorgaenger
            steuerung.naechstesTeil(1L, null)

            // Then: nichts abzuschliessen, aber die Messung laeuft an
            verify(repository, never()).schrittAbschliessen(any())
            verify(zeiterfassung).starten(99L, ReferenzTyp.DEMONTAGE_SCHRITT)
        }
    }

    /**
     * Bricht die Kamera ab, die einen Schritt gerade erst eroeffnet hat, wird der
     * Schritt-Start vollstaendig zurueckgenommen (workflow.md, "Rollback beim
     * Abbruch am frischen Schritt").
     *
     * Der zweite Teil -- den Vorgaenger wieder zu oeffnen -- ist der wichtigere.
     * Bliebe er abgeschlossen, haette der Vorgang danach **keinen** offenen
     * Schritt mehr: der grosse Kreis zeigte "ZURUECK ZU" ins Leere und der
     * Mechaniker kaeme nicht mehr weiter. Genau diese Sackgasse (#121) schliesst
     * die Invariante aus, dass die Demontage immer genau einen offenen Schritt hat.
     */
    @Nested
    @DisplayName("Rollback beim Abbruch am frischen Schritt")
    inner class Rollback {

        private val vorgaenger = Schritt(
            id = SCHRITT_ID,
            reparaturvorgangId = 1L,
            schrittNummer = 4,
            gestartetAm = T0,
            abgeschlossenAm = T0.plusSeconds(60)
        )

        private val frischer = Schritt(
            id = FRISCHER_ID,
            reparaturvorgangId = 1L,
            schrittNummer = 5,
            gestartetAm = T0.plusSeconds(60)
        )

        @BeforeEach
        fun gegeben() {
            repository.stub {
                onBlocking { findSchritt(FRISCHER_ID) } doReturn frischer
                onBlocking { holeSchritte(1L) } doReturn listOf(vorgaenger, frischer)
                onBlocking { holeFotos(FRISCHER_ID) } doReturn emptyList()
            }
        }

        @Test
        fun `loescht den fotolosen Schritt und oeffnet den Vorgaenger wieder`() = runTest {
            // When: die Kamera, die Schritt 5 eroeffnet hat, bricht ab
            val zurueckgerollt = steuerung.schrittStartZuruecknehmen(FRISCHER_ID)

            // Then: Schritt 5 ist weg, Schritt 4 wieder offen -- die exakte
            // Umkehrung des Schritt-Starts, inklusive der Zeitmessung
            assertTrue(zurueckgerollt)
            inOrder(repository, zeiterfassung) {
                verify(zeiterfassung).stoppeFallsLaeuft(FRISCHER_ID, ReferenzTyp.DEMONTAGE_SCHRITT)
                verify(repository).schrittVerwerfen(FRISCHER_ID)
                verify(repository).schrittWiederOeffnen(SCHRITT_ID)
                verify(zeiterfassung).starten(SCHRITT_ID, ReferenzTyp.DEMONTAGE_SCHRITT)
            }
        }

        @Test
        fun `laesst einen Schritt mit Fotos unangetastet`() = runTest {
            // Given: der Mechaniker hat in dieser Runde bereits ein Foto gemacht
            repository.stub { onBlocking { holeFotos(FRISCHER_ID) } doReturn listOf(ALTES_FOTO) }

            // When: die Kamera bricht ab
            val zurueckgerollt = steuerung.schrittStartZuruecknehmen(FRISCHER_ID)

            // Then: nichts wird verworfen -- ein dokumentierter Schritt bleibt
            assertFalse(zurueckgerollt)
            verify(repository, never()).schrittVerwerfen(any())
            verify(repository, never()).schrittWiederOeffnen(any())
        }

        @Test
        fun `laesst den ersten Schritt stehen, wenn es keinen Vorgaenger gibt`() = runTest {
            // Given: Schritt 1 eines frisch angelegten Vorgangs
            repository.stub { onBlocking { holeSchritte(1L) } doReturn listOf(frischer) }

            // When: die Kamera bricht ab
            val zurueckgerollt = steuerung.schrittStartZuruecknehmen(FRISCHER_ID)

            // Then: es gibt kein Ziel, auf das zurueckgerollt werden koennte.
            // Der Schritt bleibt offen und leer -- "Beenden" verwirft ihn spaeter
            assertFalse(zurueckgerollt)
            verify(repository, never()).schrittVerwerfen(any())
            verify(repository, never()).schrittWiederOeffnen(any())
        }

        @Test
        fun `meldet nichts zurueck, wenn der Schritt gar nicht mehr existiert`() = runTest {
            // Given: die Zeile ist zwischenzeitlich verschwunden
            repository.stub { onBlocking { findSchritt(FRISCHER_ID) } doReturn null }

            // When: die Kamera bricht ab
            val zurueckgerollt = steuerung.schrittStartZuruecknehmen(FRISCHER_ID)

            // Then: nichts zu tun, kein Absturz
            assertFalse(zurueckgerollt)
            verify(repository, never()).schrittVerwerfen(any())
        }
    }

    @Nested
    @DisplayName("Beenden")
    inner class Beenden {

        private val offen = Schritt(
            id = SCHRITT_ID,
            reparaturvorgangId = 1L,
            schrittNummer = 4,
            gestartetAm = T0
        )

        @Test
        fun `verwirft einen Schritt ohne Fotos, statt ihn abzuschliessen`() = runTest {
            // Given: der offene Schritt hat kein einziges Foto
            repository.stub { onBlocking { holeFotos(SCHRITT_ID) } doReturn emptyList() }

            // When: "Feierabend"
            steuerung.beenden(offen)

            // Then: geloescht -- sonst bliebe ein leeres Thumbnail zurueck und
            // die Schrittnummer waere verbrannt, obwohl sie vielleicht schon auf
            // einem Etikett klebt
            verify(repository).schrittVerwerfen(SCHRITT_ID)
            verify(repository, never()).schrittAbschliessen(any())
        }

        @Test
        fun `schliesst einen Schritt mit Fotos regulaer ab`() = runTest {
            // Given: der offene Schritt hat ein Foto
            repository.stub {
                onBlocking { holeFotos(SCHRITT_ID) } doReturn listOf(ALTES_FOTO)
            }

            // When: "Feierabend"
            steuerung.beenden(offen)

            // Then: abgeschlossen, nicht verworfen
            verify(repository).schrittAbschliessen(SCHRITT_ID)
            verify(repository, never()).schrittVerwerfen(any())
        }

        @Test
        fun `stoppt die Messung auch dann, wenn der Schritt verworfen wird`() = runTest {
            // Given: leerer offener Schritt
            repository.stub { onBlocking { holeFotos(SCHRITT_ID) } doReturn emptyList() }

            // When: "Feierabend"
            steuerung.beenden(offen)

            // Then: keine Messung bleibt offen zurueck
            verify(zeiterfassung).stoppeFallsLaeuft(SCHRITT_ID, ReferenzTyp.DEMONTAGE_SCHRITT)
        }
    }
}
