package com.boltmind.app.feature.neuervorgang

import com.boltmind.app.data.model.Reparaturvorgang
import com.boltmind.app.data.model.VorgangStatus
import com.boltmind.app.data.repository.ReparaturRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class NeuerVorgangViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: ReparaturRepository
    private lateinit var viewModel: NeuerVorgangViewModel

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mock()
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun erstelleViewModel(): NeuerVorgangViewModel =
        NeuerVorgangViewModel(repository)

    @Nested
    inner class `US-002_1 Fahrzeug fotografieren` {

        @Test
        fun `startet im Kamera-Schritt`() = runTest {
            // Given: Mechaniker hat auf "+" getippt
            // When: Anlage-Flow startet
            viewModel = erstelleViewModel()

            // Then: Kamera oeffnet sich sofort (Kamera ist der initiale Schritt)
            val state = viewModel.uiState.value
            assertEquals(NeuerVorgangSchritt.Kamera, state.schritt)
            assertNull(state.fotoPfad)
        }

        @Test
        fun `wechselt zu Formular nach Foto-Aufnahme`() = runTest {
            // Given: Kamera ist geoeffnet
            viewModel = erstelleViewModel()

            // When: Mechaniker tippt Ausloeser-Button
            viewModel.onFotoAufgenommen("/photos/fahrzeug_001.jpg")
            advanceUntilIdle()

            // Then: Foto wird als Preview angezeigt (Formular-Schritt)
            val state = viewModel.uiState.value
            assertEquals(NeuerVorgangSchritt.Formular, state.schritt)
            assertEquals("/photos/fahrzeug_001.jpg", state.fotoPfad)
        }

        @Test
        fun `legt keinen Vorgang an bei Abbruch ohne Foto`() = runTest {
            // Given: Kamera ist geoeffnet
            viewModel = erstelleViewModel()

            // When: System-Back-Button gedrueckt (kein Foto aufgenommen)
            // Then: Kein Vorgang angelegt (Repository wird nie aufgerufen)
            val state = viewModel.uiState.value
            assertNull(state.fotoPfad)
            assertNull(state.erstellterVorgangId)
            verify(repository, never()).erstelleVorgang(any())
        }
    }

    @Nested
    inner class `US-002_2 Auftragsdaten erfassen und Vorgang starten` {

        @Test
        fun `zeigt Formular mit Foto-Preview nach Foto-Aufnahme`() = runTest {
            // Given: Fahrzeugfoto wurde aufgenommen
            viewModel = erstelleViewModel()
            viewModel.onFotoAufgenommen("/photos/fahrzeug.jpg")
            advanceUntilIdle()

            // When: Foto-Preview wird angezeigt
            val state = viewModel.uiState.value

            // Then: Foto oben sichtbar, Formular mit leeren Feldern
            assertEquals(NeuerVorgangSchritt.Formular, state.schritt)
            assertEquals("/photos/fahrzeug.jpg", state.fotoPfad)
            assertEquals("", state.auftragsnummer)
            assertEquals("", state.beschreibung)
        }

        @Test
        fun `speichert Vorgang mit Foto und Auftragsnummer in DB`() = runTest {
            // Given: Foto aufgenommen, Auftragsnummer eingegeben
            whenever(repository.erstelleVorgang(any())).thenReturn(42L)
            viewModel = erstelleViewModel()
            viewModel.onFotoAufgenommen("/photos/fahrzeug.jpg")
            viewModel.onAuftragsnummerGeaendert("2024-0815")
            advanceUntilIdle()

            // When: "Starten" getippt
            viewModel.onStartenGetippt()
            advanceUntilIdle()

            // Then: Vorgang in DB gespeichert, Navigation zu Demontage-Flow
            verify(repository).erstelleVorgang(
                argThat { vorgang ->
                    vorgang.fahrzeugFotoPfad == "/photos/fahrzeug.jpg" &&
                        vorgang.auftragsnummer == "2024-0815" &&
                        vorgang.status == VorgangStatus.OFFEN
                },
            )
            assertEquals(42L, viewModel.uiState.value.erstellterVorgangId)
        }

        @Test
        fun `zeigt Validierungsfehler bei fehlender Auftragsnummer`() = runTest {
            // Given: Formular angezeigt, keine Auftragsnummer
            viewModel = erstelleViewModel()
            viewModel.onFotoAufgenommen("/photos/fahrzeug.jpg")
            advanceUntilIdle()

            // When: "Starten" ohne Auftragsnummer
            viewModel.onStartenGetippt()
            advanceUntilIdle()

            // Then: Validierungsmeldung angezeigt
            assertNotNull(viewModel.uiState.value.validierungsFehler)
            assertNull(viewModel.uiState.value.erstellterVorgangId)
            verify(repository, never()).erstelleVorgang(any())
        }

        @Test
        fun `zeigt Validierungsfehler bei leerer Auftragsnummer mit Leerzeichen`() = runTest {
            // Given: Formular angezeigt, Auftragsnummer nur Leerzeichen
            viewModel = erstelleViewModel()
            viewModel.onFotoAufgenommen("/photos/fahrzeug.jpg")
            viewModel.onAuftragsnummerGeaendert("   ")
            advanceUntilIdle()

            // When: "Starten" getippt
            viewModel.onStartenGetippt()
            advanceUntilIdle()

            // Then: Validierungsmeldung angezeigt
            assertNotNull(viewModel.uiState.value.validierungsFehler)
            verify(repository, never()).erstelleVorgang(any())
        }

        @Test
        fun `speichert Vorgang auch ohne Beschreibung`() = runTest {
            // Given: Foto und Auftragsnummer vorhanden, keine Beschreibung
            whenever(repository.erstelleVorgang(any())).thenReturn(7L)
            viewModel = erstelleViewModel()
            viewModel.onFotoAufgenommen("/photos/fahrzeug.jpg")
            viewModel.onAuftragsnummerGeaendert("2024-0815")
            advanceUntilIdle()

            // When: "Starten" getippt (Beschreibung leer)
            viewModel.onStartenGetippt()
            advanceUntilIdle()

            // Then: Vorgang wird gespeichert (Beschreibung ist optional → null)
            verify(repository).erstelleVorgang(
                argThat { vorgang ->
                    vorgang.beschreibung == null &&
                        vorgang.auftragsnummer == "2024-0815"
                },
            )
            assertEquals(7L, viewModel.uiState.value.erstellterVorgangId)
        }

        @Test
        fun `speichert Vorgang mit Beschreibung wenn angegeben`() = runTest {
            // Given: Foto, Auftragsnummer und Beschreibung vorhanden
            whenever(repository.erstelleVorgang(any())).thenReturn(10L)
            viewModel = erstelleViewModel()
            viewModel.onFotoAufgenommen("/photos/fahrzeug.jpg")
            viewModel.onAuftragsnummerGeaendert("2024-0815")
            viewModel.onBeschreibungGeaendert("Bremsen vorne wechseln")
            advanceUntilIdle()

            // When: "Starten" getippt
            viewModel.onStartenGetippt()
            advanceUntilIdle()

            // Then: Vorgang mit Beschreibung gespeichert
            verify(repository).erstelleVorgang(
                argThat { vorgang ->
                    vorgang.beschreibung == "Bremsen vorne wechseln" &&
                        vorgang.auftragsnummer == "2024-0815"
                },
            )
            assertEquals(10L, viewModel.uiState.value.erstellterVorgangId)
        }

        @Test
        fun `loescht Validierungsfehler bei Eingabe der Auftragsnummer`() = runTest {
            // Given: Validierungsfehler wird angezeigt
            viewModel = erstelleViewModel()
            viewModel.onFotoAufgenommen("/photos/fahrzeug.jpg")
            advanceUntilIdle()
            viewModel.onStartenGetippt()
            advanceUntilIdle()
            assertNotNull(viewModel.uiState.value.validierungsFehler)

            // When: Mechaniker gibt Auftragsnummer ein
            viewModel.onAuftragsnummerGeaendert("2024-0815")
            advanceUntilIdle()

            // Then: Validierungsfehler verschwindet
            assertNull(viewModel.uiState.value.validierungsFehler)
        }

        @Test
        fun `setzt isSpeichernd waehrend DB-Speicherung`() = runTest {
            // Given: Gueltige Daten eingegeben
            whenever(repository.erstelleVorgang(any())).thenReturn(42L)
            viewModel = erstelleViewModel()
            viewModel.onFotoAufgenommen("/photos/fahrzeug.jpg")
            viewModel.onAuftragsnummerGeaendert("2024-0815")
            advanceUntilIdle()

            // When: "Starten" getippt
            viewModel.onStartenGetippt()

            // Then: isSpeichernd ist true waehrend der Operation
            assertTrue(viewModel.uiState.value.isSpeichernd)

            advanceUntilIdle()

            // Then: Nach Abschluss ist erstellterVorgangId gesetzt
            assertNotNull(viewModel.uiState.value.erstellterVorgangId)
        }
    }

    @Nested
    inner class `US-002_3 Foto wiederholen` {

        @Test
        fun `oeffnet Kamera erneut bei Bild wiederholen`() = runTest {
            // Given: Formular mit Foto-Preview wird angezeigt
            viewModel = erstelleViewModel()
            viewModel.onFotoAufgenommen("/photos/fahrzeug_alt.jpg")
            advanceUntilIdle()
            assertEquals(NeuerVorgangSchritt.Formular, viewModel.uiState.value.schritt)

            // When: "Bild wiederholen" getippt
            viewModel.onBildWiederholen()
            advanceUntilIdle()

            // Then: Kamera oeffnet sich erneut
            assertEquals(NeuerVorgangSchritt.Kamera, viewModel.uiState.value.schritt)
        }

        @Test
        fun `ersetzt altes Foto durch neues nach Wiederholung`() = runTest {
            // Given: Foto-Preview angezeigt, "Bild wiederholen" → Kamera geoeffnet
            viewModel = erstelleViewModel()
            viewModel.onFotoAufgenommen("/photos/fahrzeug_alt.jpg")
            advanceUntilIdle()
            viewModel.onBildWiederholen()
            advanceUntilIdle()

            // When: Neues Foto aufgenommen
            viewModel.onFotoAufgenommen("/photos/fahrzeug_neu.jpg")
            advanceUntilIdle()

            // Then: Neues Foto ersetzt altes
            val state = viewModel.uiState.value
            assertEquals(NeuerVorgangSchritt.Formular, state.schritt)
            assertEquals("/photos/fahrzeug_neu.jpg", state.fotoPfad)
        }

        @Test
        fun `behaelt Formulardaten bei Foto-Wiederholung`() = runTest {
            // Given: Auftragsnummer und Beschreibung eingegeben
            viewModel = erstelleViewModel()
            viewModel.onFotoAufgenommen("/photos/fahrzeug_alt.jpg")
            viewModel.onAuftragsnummerGeaendert("2024-0815")
            viewModel.onBeschreibungGeaendert("Bremsen vorne")
            advanceUntilIdle()

            // When: "Bild wiederholen" → Back (zurueck zum Formular mit altem Foto)
            viewModel.onBildWiederholen()
            advanceUntilIdle()

            // Then: Formulardaten sind erhalten
            val state = viewModel.uiState.value
            assertEquals("2024-0815", state.auftragsnummer)
            assertEquals("Bremsen vorne", state.beschreibung)
        }

        @Test
        fun `behaelt altes Foto bei Back aus Kamera nach Bild-Wiederholung`() = runTest {
            // Given: Kamera nach "Bild wiederholen" geoeffnet
            viewModel = erstelleViewModel()
            viewModel.onFotoAufgenommen("/photos/fahrzeug_alt.jpg")
            advanceUntilIdle()
            viewModel.onBildWiederholen()
            advanceUntilIdle()

            // When: Back gedrueckt (kein neues Foto aufgenommen)
            viewModel.onKameraAbgebrochen()
            advanceUntilIdle()

            // Then: Zurueck zum Formular mit vorherigem Foto
            val state = viewModel.uiState.value
            assertEquals(NeuerVorgangSchritt.Formular, state.schritt)
            assertEquals("/photos/fahrzeug_alt.jpg", state.fotoPfad)
        }
    }

    @Nested
    inner class `Navigation nach Erstellung` {

        @Test
        fun `setzt erstellterVorgangId zurueck nach Navigation`() = runTest {
            // Given: Vorgang wurde erstellt, Navigation steht an
            whenever(repository.erstelleVorgang(any())).thenReturn(42L)
            viewModel = erstelleViewModel()
            viewModel.onFotoAufgenommen("/photos/fahrzeug.jpg")
            viewModel.onAuftragsnummerGeaendert("2024-0815")
            advanceUntilIdle()
            viewModel.onStartenGetippt()
            advanceUntilIdle()
            assertEquals(42L, viewModel.uiState.value.erstellterVorgangId)

            // When: Navigation abgeschlossen
            viewModel.onNavigationAbgeschlossen()

            // Then: erstellterVorgangId ist null
            assertNull(viewModel.uiState.value.erstellterVorgangId)
        }
    }
}
