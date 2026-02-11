package com.boltmind.app.feature.uebersicht

import com.boltmind.app.data.model.Reparaturvorgang
import com.boltmind.app.data.model.ReparaturvorgangMitAnzahl
import com.boltmind.app.data.model.Schritt
import com.boltmind.app.data.model.VorgangStatus
import com.boltmind.app.data.repository.ReparaturRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class UebersichtViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: ReparaturRepository
    private lateinit var viewModel: UebersichtViewModel

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mock()
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun testVorgang(
        id: Long = 1L,
        auftragsnummer: String = "TEST-001",
        status: VorgangStatus = VorgangStatus.OFFEN,
        erstelltAm: Instant = Instant.now(),
    ) = Reparaturvorgang(
        id = id,
        fahrzeugFotoPfad = "/test/foto.jpg",
        auftragsnummer = auftragsnummer,
        status = status,
        erstelltAm = erstelltAm,
        aktualisiertAm = erstelltAm,
    )

    private fun testVorgangMitAnzahl(
        id: Long = 1L,
        auftragsnummer: String = "TEST-001",
        status: VorgangStatus = VorgangStatus.OFFEN,
        erstelltAm: Instant = Instant.now(),
        schrittAnzahl: Int = 0,
    ) = ReparaturvorgangMitAnzahl(
        vorgang = testVorgang(
            id = id,
            auftragsnummer = auftragsnummer,
            status = status,
            erstelltAm = erstelltAm,
        ),
        schrittAnzahl = schrittAnzahl,
    )

    private fun setupDefaultMocks() {
        whenever(repository.beobachteOffeneVorgaengeMitAnzahl()).thenReturn(flowOf(emptyList()))
        whenever(repository.beobachteArchivierteVorgaengeMitAnzahl()).thenReturn(flowOf(emptyList()))
    }

    private fun erstelleViewModel(): UebersichtViewModel =
        UebersichtViewModel(repository)

    @Nested
    inner class `US-001_1 Offene Vorgaenge anzeigen` {

        @Test
        fun `zeigt alle offenen Vorgaenge sortiert nach letzter Bearbeitung`() = runTest {
            // Given: offene Vorgaenge existieren (DAO liefert bereits sortiert)
            val gestern = Instant.now().minusSeconds(86400)
            val heute = Instant.now()
            val vorgaengeMitAnzahl = listOf(
                testVorgangMitAnzahl(id = 1L, auftragsnummer = "NEU", erstelltAm = heute, schrittAnzahl = 5),
                testVorgangMitAnzahl(id = 2L, auftragsnummer = "ALT", erstelltAm = gestern, schrittAnzahl = 3),
            )
            whenever(repository.beobachteOffeneVorgaengeMitAnzahl()).thenReturn(flowOf(vorgaengeMitAnzahl))
            whenever(repository.beobachteArchivierteVorgaengeMitAnzahl()).thenReturn(flowOf(emptyList()))

            // When: ViewModel wird initialisiert
            viewModel = erstelleViewModel()
            advanceUntilIdle()

            // Then: Vorgaenge werden angezeigt
            val state = viewModel.uiState.value
            assertEquals(2, state.vorgaenge.size)
            assertEquals("NEU", state.vorgaenge[0].auftragsnummer)
            assertEquals(5, state.vorgaenge[0].anzahlSchritte)
            assertEquals("ALT", state.vorgaenge[1].auftragsnummer)
            assertEquals(3, state.vorgaenge[1].anzahlSchritte)
        }

        @Test
        fun `zeigt Hinweis wenn keine offenen Vorgaenge existieren`() = runTest {
            // Given: keine offenen Vorgaenge
            setupDefaultMocks()

            // When: ViewModel wird initialisiert
            viewModel = erstelleViewModel()
            advanceUntilIdle()

            // Then: leere Liste
            assertTrue(viewModel.uiState.value.vorgaenge.isEmpty())
        }

        @Test
        fun `zeigt Schrittanzahl pro Vorgang an`() = runTest {
            // Given: Vorgang mit 12 Schritten
            val vorgangMitAnzahl = testVorgangMitAnzahl(id = 1L, auftragsnummer = "TEST", schrittAnzahl = 12)
            whenever(repository.beobachteOffeneVorgaengeMitAnzahl()).thenReturn(flowOf(listOf(vorgangMitAnzahl)))
            whenever(repository.beobachteArchivierteVorgaengeMitAnzahl()).thenReturn(flowOf(emptyList()))

            // When: ViewModel wird initialisiert
            viewModel = erstelleViewModel()
            advanceUntilIdle()

            // Then: Schrittanzahl korrekt
            assertEquals(12, viewModel.uiState.value.vorgaenge[0].anzahlSchritte)
        }

        @Test
        fun `formatiert heutiges Datum als Heute`() = runTest {
            // Given: Vorgang von heute
            val vorgangMitAnzahl = testVorgangMitAnzahl(id = 1L, erstelltAm = Instant.now())
            whenever(repository.beobachteOffeneVorgaengeMitAnzahl()).thenReturn(flowOf(listOf(vorgangMitAnzahl)))
            whenever(repository.beobachteArchivierteVorgaengeMitAnzahl()).thenReturn(flowOf(emptyList()))

            // When
            viewModel = erstelleViewModel()
            advanceUntilIdle()

            // Then
            assertEquals(DatumAnzeige.Heute, viewModel.uiState.value.vorgaenge[0].erstelltAm)
        }

        @Test
        fun `formatiert gestriges Datum als Gestern`() = runTest {
            // Given: Vorgang von gestern
            val gestern = LocalDate.now().minusDays(1)
                .atStartOfDay(ZoneId.systemDefault()).toInstant()
            val vorgangMitAnzahl = testVorgangMitAnzahl(id = 1L, erstelltAm = gestern)
            whenever(repository.beobachteOffeneVorgaengeMitAnzahl()).thenReturn(flowOf(listOf(vorgangMitAnzahl)))
            whenever(repository.beobachteArchivierteVorgaengeMitAnzahl()).thenReturn(flowOf(emptyList()))

            // When
            viewModel = erstelleViewModel()
            advanceUntilIdle()

            // Then
            assertEquals(DatumAnzeige.Gestern, viewModel.uiState.value.vorgaenge[0].erstelltAm)
        }

        @Test
        fun `formatiert aelteres Datum als dd MM yyyy`() = runTest {
            // Given: Vorgang von vor 10 Tagen
            val vorZehnTagen = LocalDate.now().minusDays(10)
                .atStartOfDay(ZoneId.systemDefault()).toInstant()
            val vorgangMitAnzahl = testVorgangMitAnzahl(id = 1L, erstelltAm = vorZehnTagen)
            whenever(repository.beobachteOffeneVorgaengeMitAnzahl()).thenReturn(flowOf(listOf(vorgangMitAnzahl)))
            whenever(repository.beobachteArchivierteVorgaengeMitAnzahl()).thenReturn(flowOf(emptyList()))

            // When
            viewModel = erstelleViewModel()
            advanceUntilIdle()

            // Then: dd.MM.yyyy Format
            val erwartetesDatum = LocalDate.now().minusDays(10)
                .format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy"))
            assertEquals(DatumAnzeige.Formatiert(erwartetesDatum), viewModel.uiState.value.vorgaenge[0].erstelltAm)
        }

        @Test
        fun `zeigt Fahrzeugfoto-Pfad pro Vorgang`() = runTest {
            // Given: Vorgang mit Foto
            val vorgangMitAnzahl = testVorgangMitAnzahl(id = 1L)
            whenever(repository.beobachteOffeneVorgaengeMitAnzahl()).thenReturn(flowOf(listOf(vorgangMitAnzahl)))
            whenever(repository.beobachteArchivierteVorgaengeMitAnzahl()).thenReturn(flowOf(emptyList()))

            // When
            viewModel = erstelleViewModel()
            advanceUntilIdle()

            // Then: Foto-Pfad wird durchgereicht
            assertEquals("/test/foto.jpg", viewModel.uiState.value.vorgaenge[0].fahrzeugFotoPfad)
        }
    }

    @Nested
    inner class `US-001_3 Neuen Vorgang starten` {

        @Test
        fun `initialer State ist leere Liste ohne Loading`() = runTest {
            // Given: Repository liefert leeren Flow
            setupDefaultMocks()

            // When: ViewModel wird erstellt
            viewModel = erstelleViewModel()

            // Then: initialer State ist korrekt (vor collect)
            val initialState = viewModel.uiState.value
            assertTrue(initialState.vorgaenge.isEmpty())
            assertEquals(false, initialState.isLoading)
        }
    }

    @Nested
    inner class `US-001_2 Vorgang fuer Weiterarbeit oeffnen` {

        @Test
        fun `oeffnet direkt Demontage-Flow bei Vorgang mit 0 Schritten`() = runTest {
            // Given: offener Vorgang mit 0 Schritten (frisch angelegt)
            val vorgangMitAnzahl = testVorgangMitAnzahl(id = 42L, auftragsnummer = "AUF-001", schrittAnzahl = 0)
            whenever(repository.beobachteOffeneVorgaengeMitAnzahl()).thenReturn(flowOf(listOf(vorgangMitAnzahl)))
            whenever(repository.beobachteArchivierteVorgaengeMitAnzahl()).thenReturn(flowOf(emptyList()))
            viewModel = erstelleViewModel()
            advanceUntilIdle()

            // When: Mechaniker tippt Vorgang an
            viewModel.onVorgangGetippt(42L)
            advanceUntilIdle()

            // Then: Demontage-Flow (F-003) oeffnet sich direkt
            val state = viewModel.uiState.value
            assertEquals(NavigationsZiel.Demontage(42L), state.navigationsZiel)
            assertNull(state.auswahlDialog)
        }

        @Test
        fun `zeigt Auswahl-Dialog bei Vorgang mit mindestens 1 Schritt`() = runTest {
            // Given: offener Vorgang mit mindestens 1 Schritt
            val vorgang = testVorgang(id = 7L, auftragsnummer = "AUF-002")
            val vorgangMitAnzahl = testVorgangMitAnzahl(id = 7L, auftragsnummer = "AUF-002", schrittAnzahl = 3)
            whenever(repository.beobachteOffeneVorgaengeMitAnzahl()).thenReturn(flowOf(listOf(vorgangMitAnzahl)))
            whenever(repository.beobachteArchivierteVorgaengeMitAnzahl()).thenReturn(flowOf(emptyList()))
            whenever(repository.findVorgangById(7L)).thenReturn(vorgang)
            viewModel = erstelleViewModel()
            advanceUntilIdle()

            // When: Mechaniker tippt Vorgang an
            viewModel.onVorgangGetippt(7L)
            advanceUntilIdle()

            // Then: Auswahl-Dialog erscheint mit Vorgang-Daten
            val state = viewModel.uiState.value
            assertNull(state.navigationsZiel)
            val dialog = state.auswahlDialog
            assertEquals(7L, dialog?.vorgangId)
            assertEquals("AUF-002", dialog?.auftragsnummer)
            assertEquals("/test/foto.jpg", dialog?.fahrzeugFotoPfad)
        }

        @Test
        fun `navigiert zu Demontage bei Auswahl Weiter demontieren`() = runTest {
            // Given: Auswahl-Dialog ist sichtbar
            val vorgang = testVorgang(id = 7L, auftragsnummer = "AUF-002")
            val vorgangMitAnzahl = testVorgangMitAnzahl(id = 7L, auftragsnummer = "AUF-002", schrittAnzahl = 3)
            whenever(repository.beobachteOffeneVorgaengeMitAnzahl()).thenReturn(flowOf(listOf(vorgangMitAnzahl)))
            whenever(repository.beobachteArchivierteVorgaengeMitAnzahl()).thenReturn(flowOf(emptyList()))
            whenever(repository.findVorgangById(7L)).thenReturn(vorgang)
            viewModel = erstelleViewModel()
            advanceUntilIdle()
            viewModel.onVorgangGetippt(7L)
            advanceUntilIdle()

            // When: Mechaniker waehlt "Weiter demontieren"
            viewModel.onWeiterDemontierenGewaehlt()

            // Then: Navigation zu Demontage-Flow
            val state = viewModel.uiState.value
            assertEquals(NavigationsZiel.Demontage(7L), state.navigationsZiel)
            assertNull(state.auswahlDialog)
        }

        @Test
        fun `navigiert zu Montage bei Auswahl Montage starten`() = runTest {
            // Given: Auswahl-Dialog ist sichtbar
            val vorgang = testVorgang(id = 7L, auftragsnummer = "AUF-002")
            val vorgangMitAnzahl = testVorgangMitAnzahl(id = 7L, auftragsnummer = "AUF-002", schrittAnzahl = 3)
            whenever(repository.beobachteOffeneVorgaengeMitAnzahl()).thenReturn(flowOf(listOf(vorgangMitAnzahl)))
            whenever(repository.beobachteArchivierteVorgaengeMitAnzahl()).thenReturn(flowOf(emptyList()))
            whenever(repository.findVorgangById(7L)).thenReturn(vorgang)
            viewModel = erstelleViewModel()
            advanceUntilIdle()
            viewModel.onVorgangGetippt(7L)
            advanceUntilIdle()

            // When: Mechaniker waehlt "Montage starten"
            viewModel.onMontageStartenGewaehlt()

            // Then: Navigation zu Montage-Flow
            val state = viewModel.uiState.value
            assertEquals(NavigationsZiel.Montage(7L), state.navigationsZiel)
            assertNull(state.auswahlDialog)
        }

        @Test
        fun `setzt NavigationsZiel zurueck nach Navigation`() = runTest {
            // Given: Navigation zu Demontage aktiv
            val vorgangMitAnzahl = testVorgangMitAnzahl(id = 42L, auftragsnummer = "AUF-001", schrittAnzahl = 0)
            whenever(repository.beobachteOffeneVorgaengeMitAnzahl()).thenReturn(flowOf(listOf(vorgangMitAnzahl)))
            whenever(repository.beobachteArchivierteVorgaengeMitAnzahl()).thenReturn(flowOf(emptyList()))
            viewModel = erstelleViewModel()
            advanceUntilIdle()
            viewModel.onVorgangGetippt(42L)
            advanceUntilIdle()

            // When: Navigation abgeschlossen
            viewModel.onNavigationAbgeschlossen()

            // Then: NavigationsZiel ist null
            assertNull(viewModel.uiState.value.navigationsZiel)
        }

        @Test
        fun `schliesst Dialog bei Verwerfen`() = runTest {
            // Given: Auswahl-Dialog ist sichtbar
            val vorgang = testVorgang(id = 7L, auftragsnummer = "AUF-002")
            val vorgangMitAnzahl = testVorgangMitAnzahl(id = 7L, auftragsnummer = "AUF-002", schrittAnzahl = 3)
            whenever(repository.beobachteOffeneVorgaengeMitAnzahl()).thenReturn(flowOf(listOf(vorgangMitAnzahl)))
            whenever(repository.beobachteArchivierteVorgaengeMitAnzahl()).thenReturn(flowOf(emptyList()))
            whenever(repository.findVorgangById(7L)).thenReturn(vorgang)
            viewModel = erstelleViewModel()
            advanceUntilIdle()
            viewModel.onVorgangGetippt(7L)
            advanceUntilIdle()

            // When: Dialog wird verworfen
            viewModel.onDialogVerworfen()

            // Then: Dialog ist geschlossen
            assertNull(viewModel.uiState.value.auswahlDialog)
        }
    }

    @Nested
    inner class `US-001_4 Vorgang loeschen` {

        @Test
        fun `zeigt Bestaetigungsdialog vor Loeschung`() = runTest {
            // Given: Vorgang existiert in der Liste
            val vorgaenge = listOf(
                testVorgangMitAnzahl(id = 1L, auftragsnummer = "AUF-001", schrittAnzahl = 3),
            )
            whenever(repository.beobachteOffeneVorgaengeMitAnzahl()).thenReturn(flowOf(vorgaenge))
            whenever(repository.beobachteArchivierteVorgaengeMitAnzahl()).thenReturn(flowOf(emptyList()))

            viewModel = erstelleViewModel()
            advanceUntilIdle()

            // When: Mechaniker initiiert Loeschung (Swipe + Loeschen-Button)
            viewModel.onLoeschenAngefragt(1L)
            advanceUntilIdle()

            // Then: Bestaetigungsdialog erscheint mit Vorgang-Info
            val dialogState = viewModel.uiState.value.loeschenDialog
            assertNotNull(dialogState)
            assertEquals(1L, dialogState!!.vorgangId)
            assertEquals("AUF-001", dialogState.auftragsnummer)
        }

        @Test
        fun `loescht Vorgang nach Bestaetigung`() = runTest {
            // Given: Bestaetigungsdialog ist sichtbar
            val vorgaenge = listOf(
                testVorgangMitAnzahl(id = 1L, auftragsnummer = "AUF-001", schrittAnzahl = 3),
            )
            whenever(repository.beobachteOffeneVorgaengeMitAnzahl()).thenReturn(flowOf(vorgaenge))
            whenever(repository.beobachteArchivierteVorgaengeMitAnzahl()).thenReturn(flowOf(emptyList()))

            viewModel = erstelleViewModel()
            advanceUntilIdle()

            viewModel.onLoeschenAngefragt(1L)
            advanceUntilIdle()

            // When: Mechaniker bestaetigt "Loeschen"
            viewModel.onLoeschenBestaetigt()
            advanceUntilIdle()

            // Then: repository.loescheVorgang() wird aufgerufen, Dialog wird geschlossen
            verify(repository).loescheVorgang(1L)
            assertNull(viewModel.uiState.value.loeschenDialog)
        }

        @Test
        fun `bricht Loeschung bei Abbrechen ab`() = runTest {
            // Given: Bestaetigungsdialog ist sichtbar
            val vorgaenge = listOf(
                testVorgangMitAnzahl(id = 1L, auftragsnummer = "AUF-001", schrittAnzahl = 3),
            )
            whenever(repository.beobachteOffeneVorgaengeMitAnzahl()).thenReturn(flowOf(vorgaenge))
            whenever(repository.beobachteArchivierteVorgaengeMitAnzahl()).thenReturn(flowOf(emptyList()))

            viewModel = erstelleViewModel()
            advanceUntilIdle()

            viewModel.onLoeschenAngefragt(1L)
            advanceUntilIdle()
            assertNotNull(viewModel.uiState.value.loeschenDialog)

            // When: Mechaniker waehlt "Abbrechen"
            viewModel.onLoeschenAbgebrochen()
            advanceUntilIdle()

            // Then: Vorgang bleibt erhalten, Dialog wird geschlossen
            assertNull(viewModel.uiState.value.loeschenDialog)
            // Vorgang ist weiterhin in der Liste
            assertEquals(1, viewModel.uiState.value.vorgaenge.size)
            assertEquals("AUF-001", viewModel.uiState.value.vorgaenge[0].auftragsnummer)
        }
    }

    @Nested
    inner class `US-001_5 Archivierte Vorgaenge einsehen` {

        @Test
        fun `zeigt archivierte Vorgaenge im Archiv-Tab`() = runTest {
            // Given: archivierte Vorgaenge existieren
            val archivVorgaenge = listOf(
                testVorgangMitAnzahl(
                    id = 10L,
                    auftragsnummer = "ARCH-001",
                    status = VorgangStatus.ARCHIVIERT,
                    schrittAnzahl = 5,
                ),
            )
            val schritte = listOf(
                Schritt(
                    id = 1L,
                    reparaturvorgangId = 10L,
                    schrittNummer = 1,
                    bauteilFotoPfad = "/test/foto1.jpg",
                    gestartetAm = Instant.parse("2026-01-10T08:00:00Z"),
                    abgeschlossenAm = Instant.parse("2026-01-10T08:30:00Z"),
                ),
                Schritt(
                    id = 2L,
                    reparaturvorgangId = 10L,
                    schrittNummer = 2,
                    bauteilFotoPfad = "/test/foto2.jpg",
                    gestartetAm = Instant.parse("2026-01-10T08:30:00Z"),
                    abgeschlossenAm = Instant.parse("2026-01-10T09:00:00Z"),
                ),
            )
            whenever(repository.beobachteOffeneVorgaengeMitAnzahl()).thenReturn(flowOf(emptyList()))
            whenever(repository.beobachteArchivierteVorgaengeMitAnzahl()).thenReturn(flowOf(archivVorgaenge))
            whenever(repository.holeSchritte(10L)).thenReturn(schritte)

            // When: ViewModel wird initialisiert
            viewModel = erstelleViewModel()
            advanceUntilIdle()

            // Then: archivierte Vorgaenge sind im State
            val state = viewModel.uiState.value
            assertEquals(1, state.archivierteVorgaenge.size)
            assertEquals("ARCH-001", state.archivierteVorgaenge[0].auftragsnummer)
            assertEquals(5, state.archivierteVorgaenge[0].anzahlSchritte)
        }

        @Test
        fun `zeigt Gesamtdauer pro archiviertem Vorgang`() = runTest {
            // Given: archivierter Vorgang mit Schritten die Zeitdaten haben
            val archivVorgaenge = listOf(
                testVorgangMitAnzahl(
                    id = 10L,
                    auftragsnummer = "ARCH-001",
                    status = VorgangStatus.ARCHIVIERT,
                    schrittAnzahl = 2,
                ),
            )
            // Schritt 1: 30 min, Schritt 2: 53 min = 1h 23min
            val schritte = listOf(
                Schritt(
                    id = 1L,
                    reparaturvorgangId = 10L,
                    schrittNummer = 1,
                    bauteilFotoPfad = "/test/foto1.jpg",
                    gestartetAm = Instant.parse("2026-01-10T08:00:00Z"),
                    abgeschlossenAm = Instant.parse("2026-01-10T08:30:00Z"),
                ),
                Schritt(
                    id = 2L,
                    reparaturvorgangId = 10L,
                    schrittNummer = 2,
                    bauteilFotoPfad = "/test/foto2.jpg",
                    gestartetAm = Instant.parse("2026-01-10T08:30:00Z"),
                    abgeschlossenAm = Instant.parse("2026-01-10T09:23:00Z"),
                ),
            )
            whenever(repository.beobachteOffeneVorgaengeMitAnzahl()).thenReturn(flowOf(emptyList()))
            whenever(repository.beobachteArchivierteVorgaengeMitAnzahl()).thenReturn(flowOf(archivVorgaenge))
            whenever(repository.holeSchritte(10L)).thenReturn(schritte)

            // When: ViewModel wird initialisiert
            viewModel = erstelleViewModel()
            advanceUntilIdle()

            // Then: Gesamtdauer wird korrekt berechnet und formatiert
            assertEquals(DauerAnzeige.StundenMinuten(1, 23), viewModel.uiState.value.archivierteVorgaenge[0].gesamtdauer)
        }

        @Test
        fun `zeigt leeren Zustand wenn keine archivierten Vorgaenge`() = runTest {
            // Given: keine archivierten Vorgaenge
            setupDefaultMocks()

            // When: ViewModel wird initialisiert
            viewModel = erstelleViewModel()
            advanceUntilIdle()

            // Then: leere Archiv-Liste
            assertTrue(viewModel.uiState.value.archivierteVorgaenge.isEmpty())
        }

        @Test
        fun `formatiert Gesamtdauer als Stunden und Minuten`() = runTest {
            // Given: archivierter Vorgang mit 45 Minuten Gesamtdauer
            val archivVorgaenge = listOf(
                testVorgangMitAnzahl(
                    id = 20L,
                    auftragsnummer = "ARCH-002",
                    status = VorgangStatus.ARCHIVIERT,
                    schrittAnzahl = 1,
                ),
            )
            val schritte = listOf(
                Schritt(
                    id = 3L,
                    reparaturvorgangId = 20L,
                    schrittNummer = 1,
                    bauteilFotoPfad = "/test/foto3.jpg",
                    gestartetAm = Instant.parse("2026-01-10T10:00:00Z"),
                    abgeschlossenAm = Instant.parse("2026-01-10T10:45:00Z"),
                ),
            )
            whenever(repository.beobachteOffeneVorgaengeMitAnzahl()).thenReturn(flowOf(emptyList()))
            whenever(repository.beobachteArchivierteVorgaengeMitAnzahl()).thenReturn(flowOf(archivVorgaenge))
            whenever(repository.holeSchritte(20L)).thenReturn(schritte)

            // When: ViewModel wird initialisiert
            viewModel = erstelleViewModel()
            advanceUntilIdle()

            // Then: Nur Minuten wenn unter einer Stunde
            assertEquals(DauerAnzeige.Minuten(45), viewModel.uiState.value.archivierteVorgaenge[0].gesamtdauer)
        }

        @Test
        fun `wechselt Tab bei onTabGewaehlt`() = runTest {
            // Given: ViewModel ist initialisiert im Tab "Offen" (0)
            setupDefaultMocks()
            viewModel = erstelleViewModel()
            advanceUntilIdle()
            assertEquals(0, viewModel.uiState.value.selectedTab)

            // When: Mechaniker wechselt auf Tab "Archiv" (1)
            viewModel.onTabGewaehlt(1)
            advanceUntilIdle()

            // Then: Tab ist gewechselt
            assertEquals(1, viewModel.uiState.value.selectedTab)
        }

        @Test
        fun `formatiert Gesamtdauer unter einer Minute`() = runTest {
            // Given: archivierter Vorgang mit weniger als 1 Minute Gesamtdauer
            val archivVorgaenge = listOf(
                testVorgangMitAnzahl(
                    id = 30L,
                    auftragsnummer = "ARCH-003",
                    status = VorgangStatus.ARCHIVIERT,
                    schrittAnzahl = 1,
                ),
            )
            val schritte = listOf(
                Schritt(
                    id = 4L,
                    reparaturvorgangId = 30L,
                    schrittNummer = 1,
                    bauteilFotoPfad = "/test/foto4.jpg",
                    gestartetAm = Instant.parse("2026-01-10T10:00:00Z"),
                    abgeschlossenAm = Instant.parse("2026-01-10T10:00:30Z"),
                ),
            )
            whenever(repository.beobachteOffeneVorgaengeMitAnzahl()).thenReturn(flowOf(emptyList()))
            whenever(repository.beobachteArchivierteVorgaengeMitAnzahl()).thenReturn(flowOf(archivVorgaenge))
            whenever(repository.holeSchritte(30L)).thenReturn(schritte)

            // When: ViewModel wird initialisiert
            viewModel = erstelleViewModel()
            advanceUntilIdle()

            // Then: "< 1 min" fuer sehr kurze Dauern
            assertEquals(DauerAnzeige.WenigerAlsEineMinute, viewModel.uiState.value.archivierteVorgaenge[0].gesamtdauer)
        }

        @Test
        fun `zeigt Abschlussdatum pro archiviertem Vorgang`() = runTest {
            // Given: archivierter Vorgang mit bekanntem aktualisiertAm (= Abschlussdatum)
            val abschlussDatum = LocalDate.now().minusDays(3)
                .atStartOfDay(ZoneId.systemDefault()).toInstant()
            val archivVorgaenge = listOf(
                testVorgangMitAnzahl(
                    id = 40L,
                    auftragsnummer = "ARCH-004",
                    status = VorgangStatus.ARCHIVIERT,
                    erstelltAm = abschlussDatum,
                    schrittAnzahl = 1,
                ),
            )
            val schritte = listOf(
                Schritt(
                    id = 5L,
                    reparaturvorgangId = 40L,
                    schrittNummer = 1,
                    bauteilFotoPfad = "/test/foto5.jpg",
                    gestartetAm = Instant.parse("2026-01-10T10:00:00Z"),
                    abgeschlossenAm = Instant.parse("2026-01-10T10:15:00Z"),
                ),
            )
            whenever(repository.beobachteOffeneVorgaengeMitAnzahl()).thenReturn(flowOf(emptyList()))
            whenever(repository.beobachteArchivierteVorgaengeMitAnzahl()).thenReturn(flowOf(archivVorgaenge))
            whenever(repository.holeSchritte(40L)).thenReturn(schritte)

            // When: ViewModel wird initialisiert
            viewModel = erstelleViewModel()
            advanceUntilIdle()

            // Then: Abschlussdatum wird formatiert angezeigt
            val erwartetesDatum = LocalDate.now().minusDays(3)
                .format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy"))
            assertEquals(DatumAnzeige.Formatiert(erwartetesDatum), viewModel.uiState.value.archivierteVorgaenge[0].abschlussDatum)
        }
    }
}
