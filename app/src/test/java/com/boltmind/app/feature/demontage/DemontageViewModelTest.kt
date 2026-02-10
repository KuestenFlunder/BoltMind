package com.boltmind.app.feature.demontage

import androidx.lifecycle.SavedStateHandle
import com.boltmind.app.data.foto.FotoManager
import com.boltmind.app.data.model.Schritt
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
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import com.boltmind.app.data.model.SchrittTyp
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.File
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class DemontageViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: ReparaturRepository
    private lateinit var fotoManager: FotoManager
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var viewModel: DemontageViewModel

    private fun testSchritt(
        id: Long = 10L,
        vorgangId: Long = 1L,
        nummer: Int = 1
    ) = Schritt(
        id = id,
        reparaturvorgangId = vorgangId,
        schrittNummer = nummer,
        gestartetAm = Instant.now()
    )

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mock()
        fotoManager = mock()
        savedStateHandle = SavedStateHandle(mapOf("vorgangId" to 1L))
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private suspend fun erstelleViewModel(): DemontageViewModel {
        whenever(repository.schrittAnlegen(1L)).thenReturn(testSchritt())
        return DemontageViewModel(repository, fotoManager, savedStateHandle)
    }

    @Nested
    inner class `US-003_1 Bauteil-Foto aufnehmen` {

        @Test
        fun `preview oeffnet sich beim Flow-Start mit Schrittnummer`() = runTest {
            // Given: ViewModel wird erstellt mit vorgangId
            val schritt = testSchritt(id = 10L, nummer = 1)
            whenever(repository.schrittAnlegen(1L)).thenReturn(schritt)

            // When: init block laeuft
            viewModel = DemontageViewModel(repository, fotoManager, savedStateHandle)
            advanceUntilIdle()

            // Then: flowState = PREVIEW_BAUTEIL, schrittNummer = 1, neuer Schritt in DB
            val state = viewModel.uiState.value
            assertEquals(DemontageFlowState.PREVIEW_BAUTEIL, state.flowState)
            assertEquals(PreviewZustand.INITIAL, state.previewZustand)
            assertEquals(1, state.schrittNummer)
            assertEquals(10L, state.schrittId)
            assertEquals(1L, state.vorgangId)
            verify(repository).schrittAnlegen(1L)
        }

        @Test
        fun `foto aufnehmen oeffnet System-Kamera Intent`() = runTest {
            // Given: flowState = PREVIEW_BAUTEIL
            val tempFile = File("/tmp/bauteil_123.jpg")
            whenever(fotoManager.erstelleTempDatei(any())).thenReturn(tempFile)
            viewModel = erstelleViewModel()
            advanceUntilIdle()

            // When: onFotoAufnehmenGetippt()
            viewModel.onFotoAufnehmenGetippt()

            // Then: kameraIntentAktiv = true, tempFotoPfad gesetzt
            val state = viewModel.uiState.value
            assertTrue(state.kameraIntentAktiv)
            assertEquals(tempFile.absolutePath, state.tempFotoPfad)
        }

        @Test
        fun `foto aufgenommen wechselt zu Vorschau-Zustand`() = runTest {
            // Given: Kamera-Intent wurde gestartet
            val tempFile = File("/tmp/bauteil_123.jpg")
            whenever(fotoManager.erstelleTempDatei(any())).thenReturn(tempFile)
            viewModel = erstelleViewModel()
            advanceUntilIdle()
            viewModel.onFotoAufnehmenGetippt()

            // When: Kamera liefert Foto zurueck
            viewModel.onFotoAufgenommen()

            // Then: previewZustand = VORSCHAU, kameraIntentAktiv = false
            val state = viewModel.uiState.value
            assertEquals(PreviewZustand.VORSCHAU, state.previewZustand)
            assertFalse(state.kameraIntentAktiv)
            assertNotNull(state.tempFotoPfad)
        }

        @Test
        fun `wiederholen loescht temp-Foto und oeffnet Kamera erneut`() = runTest {
            // Given: previewZustand = VORSCHAU, tempFotoPfad gesetzt
            val tempFile = File("/tmp/bauteil_123.jpg")
            whenever(fotoManager.erstelleTempDatei(any())).thenReturn(tempFile)
            viewModel = erstelleViewModel()
            advanceUntilIdle()
            viewModel.onFotoAufnehmenGetippt()
            viewModel.onFotoAufgenommen()

            // When: onFotoWiederholen()
            viewModel.onFotoWiederholen()

            // Then: previewZustand = INITIAL, tempFotoPfad = null, FotoManager.loescheTempFoto aufgerufen
            val state = viewModel.uiState.value
            assertEquals(PreviewZustand.INITIAL, state.previewZustand)
            assertNull(state.tempFotoPfad)
            assertFalse(state.kameraIntentAktiv)
            verify(fotoManager).loescheTempFoto(tempFile.absolutePath)
        }

        @Test
        fun `bestaetigen verschiebt Foto und wechselt zu Arbeitsphase`() = runTest {
            // Given: previewZustand = VORSCHAU
            val tempFile = File("/tmp/bauteil_123.jpg")
            val permanentPfad = "/photos/bauteil_10.jpg"
            whenever(fotoManager.erstelleTempDatei(any())).thenReturn(tempFile)
            whenever(fotoManager.bestaetigeFoto(any(), any())).thenReturn(permanentPfad)
            viewModel = erstelleViewModel()
            advanceUntilIdle()
            viewModel.onFotoAufnehmenGetippt()
            viewModel.onFotoAufgenommen()

            // When: onFotoBestaetigt()
            viewModel.onFotoBestaetigt()
            advanceUntilIdle()

            // Then: flowState = ARBEITSPHASE, bauteilFotoPfad gesetzt
            val state = viewModel.uiState.value
            assertEquals(DemontageFlowState.ARBEITSPHASE, state.flowState)
            assertEquals(permanentPfad, state.bauteilFotoPfad)
            assertNull(state.tempFotoPfad)
            verify(fotoManager).bestaetigeFoto(tempFile.absolutePath, "bauteil_10")
            verify(repository).bauteilFotoBestaetigen(10L, permanentPfad)
        }

        @Test
        fun `keine Kamera-App zeigt Hinweis-Dialog`() = runTest {
            // Given: flowState = PREVIEW_BAUTEIL
            viewModel = erstelleViewModel()
            advanceUntilIdle()

            // When: onKeineKameraApp()
            viewModel.onKeineKameraApp()

            // Then: keineKameraApp = true
            assertTrue(viewModel.uiState.value.keineKameraApp)
        }

        @Test
        fun `kamera abgebrochen kehrt zu Initial-Zustand zurueck`() = runTest {
            // Given: kameraIntentAktiv = true
            val tempFile = File("/tmp/bauteil_123.jpg")
            whenever(fotoManager.erstelleTempDatei(any())).thenReturn(tempFile)
            viewModel = erstelleViewModel()
            advanceUntilIdle()
            viewModel.onFotoAufnehmenGetippt()
            assertTrue(viewModel.uiState.value.kameraIntentAktiv)

            // When: onKameraAbgebrochen()
            viewModel.onKameraAbgebrochen()

            // Then: previewZustand = INITIAL, kameraIntentAktiv = false
            val state = viewModel.uiState.value
            assertEquals(PreviewZustand.INITIAL, state.previewZustand)
            assertFalse(state.kameraIntentAktiv)
        }
    }

    @Nested
    inner class `US-003_2 AK1 Arbeitsphase-Screen` {

        private suspend fun setupArbeitsphase(): DemontageViewModel {
            val tempFile = File("/tmp/bauteil_123.jpg")
            val permanentPfad = "/photos/bauteil_10.jpg"
            whenever(fotoManager.erstelleTempDatei(any())).thenReturn(tempFile)
            whenever(fotoManager.bestaetigeFoto(any(), any())).thenReturn(permanentPfad)
            val vm = erstelleViewModel()
            return vm
        }

        @Test
        fun `arbeitsphase zeigt Schrittnummer und Bauteil-Foto`() = runTest {
            // Given: flowState = ARBEITSPHASE
            viewModel = setupArbeitsphase()
            advanceUntilIdle()
            viewModel.onFotoAufnehmenGetippt()
            viewModel.onFotoAufgenommen()
            viewModel.onFotoBestaetigt()
            advanceUntilIdle()

            // Then: schrittNummer > 0, bauteilFotoPfad != null
            val state = viewModel.uiState.value
            assertEquals(DemontageFlowState.ARBEITSPHASE, state.flowState)
            assertTrue(state.schrittNummer > 0)
            assertNotNull(state.bauteilFotoPfad)
        }

        @Test
        fun `ausgebaut-tap wechselt zu Dialog`() = runTest {
            // Given: flowState = ARBEITSPHASE
            viewModel = setupArbeitsphase()
            advanceUntilIdle()
            viewModel.onFotoAufnehmenGetippt()
            viewModel.onFotoAufgenommen()
            viewModel.onFotoBestaetigt()
            advanceUntilIdle()
            assertEquals(DemontageFlowState.ARBEITSPHASE, viewModel.uiState.value.flowState)

            // When: onAusgebautGetippt()
            viewModel.onAusgebautGetippt()

            // Then: flowState = DIALOG
            assertEquals(DemontageFlowState.DIALOG, viewModel.uiState.value.flowState)
        }
    }

    private suspend fun setupBisDialog(): DemontageViewModel {
        val tempFile = File("/tmp/bauteil_123.jpg")
        val permanentPfad = "/photos/bauteil_10.jpg"
        whenever(fotoManager.erstelleTempDatei(any())).thenReturn(tempFile)
        whenever(fotoManager.bestaetigeFoto(any(), any())).thenReturn(permanentPfad)
        val vm = erstelleViewModel()
        return vm
    }

    private suspend fun navigiereBisDialog(vm: DemontageViewModel) {
        vm.onFotoAufnehmenGetippt()
        vm.onFotoAufgenommen()
        vm.onFotoBestaetigt()
    }

    @Nested
    inner class `US-003_2 Dialog-Aktionen` {

        @Test
        fun `ablageort-fotografieren setzt typ AUSGEBAUT und wechselt zu Preview-Ablageort`() = runTest {
            // Given: flowState = DIALOG
            viewModel = setupBisDialog()
            advanceUntilIdle()
            navigiereBisDialog(viewModel)
            advanceUntilIdle()
            viewModel.onAusgebautGetippt()
            assertEquals(DemontageFlowState.DIALOG, viewModel.uiState.value.flowState)

            // When: onAblageortFotografieren()
            viewModel.onAblageortFotografieren()
            advanceUntilIdle()

            // Then: typ = AUSGEBAUT in DB, flowState = PREVIEW_ABLAGEORT
            val state = viewModel.uiState.value
            assertEquals(DemontageFlowState.PREVIEW_ABLAGEORT, state.flowState)
            assertEquals(PreviewZustand.INITIAL, state.previewZustand)
            verify(repository).schrittTypSetzen(10L, SchrittTyp.AUSGEBAUT)
        }

        @Test
        fun `weiter-ohne-ablageort setzt typ AM_FAHRZEUG und schliesst Schritt ab`() = runTest {
            // Given: flowState = DIALOG
            val naechsterSchritt = testSchritt(id = 20L, nummer = 2)
            whenever(repository.schrittAnlegen(1L))
                .thenReturn(testSchritt())
                .thenReturn(naechsterSchritt)
            viewModel = DemontageViewModel(repository, fotoManager, savedStateHandle)
            advanceUntilIdle()
            val tempFile = File("/tmp/bauteil_123.jpg")
            val permanentPfad = "/photos/bauteil_10.jpg"
            whenever(fotoManager.erstelleTempDatei(any())).thenReturn(tempFile)
            whenever(fotoManager.bestaetigeFoto(any(), any())).thenReturn(permanentPfad)
            viewModel.onFotoAufnehmenGetippt()
            viewModel.onFotoAufgenommen()
            viewModel.onFotoBestaetigt()
            advanceUntilIdle()
            viewModel.onAusgebautGetippt()

            // When: onWeiterOhneAblageort()
            viewModel.onWeiterOhneAblageort()
            advanceUntilIdle()

            // Then: typ = AM_FAHRZEUG + abgeschlossen, neuer Schritt gestartet
            verify(repository).schrittAbschliessen(10L, SchrittTyp.AM_FAHRZEUG)
            val state = viewModel.uiState.value
            assertEquals(DemontageFlowState.PREVIEW_BAUTEIL, state.flowState)
            assertEquals(2, state.schrittNummer)
            assertEquals(20L, state.schrittId)
        }

        @Test
        fun `beenden setzt typ AM_FAHRZEUG und navigiert zur Uebersicht`() = runTest {
            // Given: flowState = DIALOG
            viewModel = setupBisDialog()
            advanceUntilIdle()
            navigiereBisDialog(viewModel)
            advanceUntilIdle()
            viewModel.onAusgebautGetippt()

            // When: onBeenden()
            viewModel.onBeenden()
            advanceUntilIdle()

            // Then: typ = AM_FAHRZEUG + abgeschlossen, flowBeendet = true
            verify(repository).schrittAbschliessen(10L, SchrittTyp.AM_FAHRZEUG)
            assertTrue(viewModel.uiState.value.flowBeendet)
        }
    }

    @Nested
    inner class `US-003_3 Ablageort-Foto` {

        @Test
        fun `ablageort-modus zeigt korrekten State`() = runTest {
            // Given: Mechaniker hat "Ablageort fotografieren" gewaehlt
            viewModel = setupBisDialog()
            advanceUntilIdle()
            navigiereBisDialog(viewModel)
            advanceUntilIdle()
            viewModel.onAusgebautGetippt()
            viewModel.onAblageortFotografieren()
            advanceUntilIdle()

            // Then: PREVIEW_ABLAGEORT mit INITIAL, Schrittnummer bleibt gleich
            val state = viewModel.uiState.value
            assertEquals(DemontageFlowState.PREVIEW_ABLAGEORT, state.flowState)
            assertEquals(PreviewZustand.INITIAL, state.previewZustand)
            assertEquals(1, state.schrittNummer)
            assertNull(state.tempFotoPfad)
        }

        @Test
        fun `ablageort-foto bestaetigen schliesst Schritt ab und startet neuen`() = runTest {
            // Given: Ablageort-Foto im Vorschau-Zustand
            val naechsterSchritt = testSchritt(id = 20L, nummer = 2)
            whenever(repository.schrittAnlegen(1L))
                .thenReturn(testSchritt())
                .thenReturn(naechsterSchritt)
            viewModel = DemontageViewModel(repository, fotoManager, savedStateHandle)
            advanceUntilIdle()

            val tempFile = File("/tmp/bauteil_123.jpg")
            val permanentBauteil = "/photos/bauteil_10.jpg"
            val tempAblageort = File("/tmp/ablageort_456.jpg")
            val permanentAblageort = "/photos/ablageort_10.jpg"
            whenever(fotoManager.erstelleTempDatei(eq("bauteil"))).thenReturn(tempFile)
            whenever(fotoManager.erstelleTempDatei(eq("ablageort"))).thenReturn(tempAblageort)
            whenever(fotoManager.bestaetigeFoto(eq(tempFile.absolutePath), any()))
                .thenReturn(permanentBauteil)
            whenever(fotoManager.bestaetigeFoto(eq(tempAblageort.absolutePath), any()))
                .thenReturn(permanentAblageort)

            // Bauteil-Foto aufnehmen und bestaetigen
            viewModel.onFotoAufnehmenGetippt()
            viewModel.onFotoAufgenommen()
            viewModel.onFotoBestaetigt()
            advanceUntilIdle()

            // Dialog -> Ablageort fotografieren
            viewModel.onAusgebautGetippt()
            viewModel.onAblageortFotografieren()
            advanceUntilIdle()

            // Ablageort-Foto aufnehmen
            viewModel.onAblageortFotoAufnehmen()
            viewModel.onAblageortFotoAufgenommen()

            // When: Ablageort-Foto bestaetigen
            viewModel.onAblageortFotoBestaetigt()
            advanceUntilIdle()

            // Then: Schritt abgeschlossen, neuer Schritt gestartet
            verify(repository).ablageortFotoBestaetigen(10L, permanentAblageort)
            val state = viewModel.uiState.value
            assertEquals(DemontageFlowState.PREVIEW_BAUTEIL, state.flowState)
            assertEquals(2, state.schrittNummer)
            assertEquals(20L, state.schrittId)
        }

        @Test
        fun `ablageort-foto wiederholen loescht temp und bleibt in Ablageort-Modus`() = runTest {
            // Given: Ablageort-Foto im Vorschau-Zustand
            viewModel = setupBisDialog()
            advanceUntilIdle()
            navigiereBisDialog(viewModel)
            advanceUntilIdle()
            viewModel.onAusgebautGetippt()
            viewModel.onAblageortFotografieren()
            advanceUntilIdle()

            val tempAblageort = File("/tmp/ablageort_456.jpg")
            whenever(fotoManager.erstelleTempDatei(eq("ablageort"))).thenReturn(tempAblageort)
            viewModel.onAblageortFotoAufnehmen()
            viewModel.onAblageortFotoAufgenommen()
            assertEquals(PreviewZustand.VORSCHAU, viewModel.uiState.value.previewZustand)

            // When: onAblageortFotoWiederholen()
            viewModel.onAblageortFotoWiederholen()

            // Then: INITIAL, tempFotoPfad = null, bleibt in PREVIEW_ABLAGEORT
            val state = viewModel.uiState.value
            assertEquals(DemontageFlowState.PREVIEW_ABLAGEORT, state.flowState)
            assertEquals(PreviewZustand.INITIAL, state.previewZustand)
            assertNull(state.tempFotoPfad)
            verify(fotoManager).loescheTempFoto(tempAblageort.absolutePath)
        }
    }

    @Nested
    inner class `US-003_4 Schrittnummer-Tracking` {

        @Test
        fun `neuer vorgang startet mit schritt 1`() = runTest {
            // Given: kein unabgeschlossener Schritt, holeNaechsteSchrittNummer liefert 1
            val schritt = testSchritt(id = 10L, nummer = 1)
            whenever(repository.schrittAnlegen(1L)).thenReturn(schritt)

            // When: ViewModel init
            viewModel = DemontageViewModel(repository, fotoManager, savedStateHandle)
            advanceUntilIdle()

            // Then: schrittNummer = 1
            assertEquals(1, viewModel.uiState.value.schrittNummer)
        }

        @Test
        fun `schrittnummer nach abschluss inkrementiert`() = runTest {
            // Given: Schritt 3 abgeschlossen via "Weiter ohne Ablageort"
            val schritt3 = testSchritt(id = 30L, nummer = 3)
            val schritt4 = testSchritt(id = 40L, nummer = 4)
            whenever(repository.schrittAnlegen(1L))
                .thenReturn(schritt3)
                .thenReturn(schritt4)
            val tempFile = File("/tmp/bauteil_123.jpg")
            whenever(fotoManager.erstelleTempDatei(any())).thenReturn(tempFile)
            whenever(fotoManager.bestaetigeFoto(any(), any())).thenReturn("/photos/bauteil.jpg")

            viewModel = DemontageViewModel(repository, fotoManager, savedStateHandle)
            advanceUntilIdle()
            assertEquals(3, viewModel.uiState.value.schrittNummer)

            // Navigate to dialog
            viewModel.onFotoAufnehmenGetippt()
            viewModel.onFotoAufgenommen()
            viewModel.onFotoBestaetigt()
            advanceUntilIdle()
            viewModel.onAusgebautGetippt()

            // When: neuer Schritt angelegt via "Weiter ohne Ablageort"
            viewModel.onWeiterOhneAblageort()
            advanceUntilIdle()

            // Then: schrittNummer = 4 im UiState
            assertEquals(4, viewModel.uiState.value.schrittNummer)
        }

        @Test
        fun `inkrementierung auch ohne ablageort`() = runTest {
            // Given: Schritt mit "Weiter ohne Ablageort" beendet
            val schritt1 = testSchritt(id = 10L, nummer = 1)
            val schritt2 = testSchritt(id = 20L, nummer = 2)
            whenever(repository.schrittAnlegen(1L))
                .thenReturn(schritt1)
                .thenReturn(schritt2)
            val tempFile = File("/tmp/bauteil_123.jpg")
            whenever(fotoManager.erstelleTempDatei(any())).thenReturn(tempFile)
            whenever(fotoManager.bestaetigeFoto(any(), any())).thenReturn("/photos/bauteil.jpg")

            viewModel = DemontageViewModel(repository, fotoManager, savedStateHandle)
            advanceUntilIdle()

            viewModel.onFotoAufnehmenGetippt()
            viewModel.onFotoAufgenommen()
            viewModel.onFotoBestaetigt()
            advanceUntilIdle()
            viewModel.onAusgebautGetippt()

            // When: "Weiter ohne Ablageort" (kein Ablageort-Foto)
            viewModel.onWeiterOhneAblageort()
            advanceUntilIdle()

            // Then: Nummer inkrementiert
            assertEquals(2, viewModel.uiState.value.schrittNummer)
        }

        @Test
        fun `inkrementierung bei ablageort-foto bestaetigt`() = runTest {
            // Given: Ablageort-Foto bestaetigt
            val schritt1 = testSchritt(id = 10L, nummer = 1)
            val schritt2 = testSchritt(id = 20L, nummer = 2)
            whenever(repository.schrittAnlegen(1L))
                .thenReturn(schritt1)
                .thenReturn(schritt2)
            val tempBauteil = File("/tmp/bauteil_123.jpg")
            val tempAblageort = File("/tmp/ablageort_456.jpg")
            whenever(fotoManager.erstelleTempDatei(eq("bauteil"))).thenReturn(tempBauteil)
            whenever(fotoManager.erstelleTempDatei(eq("ablageort"))).thenReturn(tempAblageort)
            whenever(fotoManager.bestaetigeFoto(eq(tempBauteil.absolutePath), any()))
                .thenReturn("/photos/bauteil_10.jpg")
            whenever(fotoManager.bestaetigeFoto(eq(tempAblageort.absolutePath), any()))
                .thenReturn("/photos/ablageort_10.jpg")

            viewModel = DemontageViewModel(repository, fotoManager, savedStateHandle)
            advanceUntilIdle()

            // Bauteil-Foto -> Arbeitsphase -> Dialog -> Ablageort
            viewModel.onFotoAufnehmenGetippt()
            viewModel.onFotoAufgenommen()
            viewModel.onFotoBestaetigt()
            advanceUntilIdle()
            viewModel.onAusgebautGetippt()
            viewModel.onAblageortFotografieren()
            advanceUntilIdle()
            viewModel.onAblageortFotoAufnehmen()
            viewModel.onAblageortFotoAufgenommen()

            // When: Ablageort-Foto bestaetigt
            viewModel.onAblageortFotoBestaetigt()
            advanceUntilIdle()

            // Then: Nummer inkrementiert
            assertEquals(2, viewModel.uiState.value.schrittNummer)
        }

        @Test
        fun `beenden inkrementiert nicht`() = runTest {
            // Given: flowState = DIALOG
            val schritt = testSchritt(id = 10L, nummer = 1)
            whenever(repository.schrittAnlegen(1L)).thenReturn(schritt)
            val tempFile = File("/tmp/bauteil_123.jpg")
            whenever(fotoManager.erstelleTempDatei(any())).thenReturn(tempFile)
            whenever(fotoManager.bestaetigeFoto(any(), any())).thenReturn("/photos/bauteil.jpg")

            viewModel = DemontageViewModel(repository, fotoManager, savedStateHandle)
            advanceUntilIdle()

            viewModel.onFotoAufnehmenGetippt()
            viewModel.onFotoAufgenommen()
            viewModel.onFotoBestaetigt()
            advanceUntilIdle()
            viewModel.onAusgebautGetippt()

            // When: onBeenden()
            viewModel.onBeenden()
            advanceUntilIdle()

            // Then: kein neuer Schritt, flowBeendet = true
            assertTrue(viewModel.uiState.value.flowBeendet)
            assertEquals(1, viewModel.uiState.value.schrittNummer)
        }
    }

    @Nested
    inner class `US-003_4 AK6 Fortsetzung nach Unterbrechung` {

        @Test
        fun `unterbrochener schritt bei arbeitsphase zeigt arbeitsphase erneut`() = runTest {
            // Given: findUnabgeschlossenenSchritt liefert Schritt mit bauteilFotoPfad != null, typ = null
            val unterbrochenerSchritt = Schritt(
                id = 50L,
                reparaturvorgangId = 1L,
                schrittNummer = 5,
                bauteilFotoPfad = "/photos/bauteil_50.jpg",
                typ = null,
                gestartetAm = Instant.now()
            )
            whenever(repository.findUnabgeschlossenenSchritt(1L)).thenReturn(unterbrochenerSchritt)

            // When: ViewModel init
            viewModel = DemontageViewModel(repository, fotoManager, savedStateHandle)
            advanceUntilIdle()

            // Then: flowState = ARBEITSPHASE, schrittId = schritt.id, bauteilFotoPfad gesetzt
            val state = viewModel.uiState.value
            assertEquals(DemontageFlowState.ARBEITSPHASE, state.flowState)
            assertEquals(50L, state.schrittId)
            assertEquals(5, state.schrittNummer)
            assertEquals("/photos/bauteil_50.jpg", state.bauteilFotoPfad)
        }

        @Test
        fun `unterbrochener schritt ohne foto zeigt preview erneut`() = runTest {
            // Given: findUnabgeschlossenenSchritt liefert Schritt mit bauteilFotoPfad = null
            val unterbrochenerSchritt = Schritt(
                id = 50L,
                reparaturvorgangId = 1L,
                schrittNummer = 5,
                bauteilFotoPfad = null,
                typ = null,
                gestartetAm = Instant.now()
            )
            whenever(repository.findUnabgeschlossenenSchritt(1L)).thenReturn(unterbrochenerSchritt)

            // When: ViewModel init
            viewModel = DemontageViewModel(repository, fotoManager, savedStateHandle)
            advanceUntilIdle()

            // Then: flowState = PREVIEW_BAUTEIL, schrittNummer = schritt.schrittNummer
            val state = viewModel.uiState.value
            assertEquals(DemontageFlowState.PREVIEW_BAUTEIL, state.flowState)
            assertEquals(5, state.schrittNummer)
            assertEquals(50L, state.schrittId)
        }

        @Test
        fun `unterbrochener schritt mit typ AUSGEBAUT ohne ablageort zeigt arbeitsphase`() = runTest {
            // Given: findUnabgeschlossenenSchritt liefert Schritt mit typ=AUSGEBAUT, ablageortFotoPfad=null
            val unterbrochenerSchritt = Schritt(
                id = 50L,
                reparaturvorgangId = 1L,
                schrittNummer = 5,
                bauteilFotoPfad = "/photos/bauteil_50.jpg",
                typ = SchrittTyp.AUSGEBAUT,
                ablageortFotoPfad = null,
                gestartetAm = Instant.now()
            )
            whenever(repository.findUnabgeschlossenenSchritt(1L)).thenReturn(unterbrochenerSchritt)

            // When: ViewModel init
            viewModel = DemontageViewModel(repository, fotoManager, savedStateHandle)
            advanceUntilIdle()

            // Then: flowState = ARBEITSPHASE
            val state = viewModel.uiState.value
            assertEquals(DemontageFlowState.ARBEITSPHASE, state.flowState)
            assertEquals(50L, state.schrittId)
            assertEquals(5, state.schrittNummer)
        }
    }

    @Nested
    inner class `US-003_5 Demontage beenden und fortsetzen` {

        @Test
        fun `fortsetzung nach beenden startet mit korrekter nummer`() = runTest {
            // Given: Vorgang hat 5 abgeschlossene Schritte, findUnabgeschlossenenSchritt = null
            whenever(repository.findUnabgeschlossenenSchritt(1L)).thenReturn(null)
            val neuerSchritt = testSchritt(id = 60L, nummer = 6)
            whenever(repository.schrittAnlegen(1L)).thenReturn(neuerSchritt)

            // When: ViewModel init -> neuer Schritt angelegt
            viewModel = DemontageViewModel(repository, fotoManager, savedStateHandle)
            advanceUntilIdle()

            // Then: schrittNummer = 6
            assertEquals(6, viewModel.uiState.value.schrittNummer)
            assertEquals(60L, viewModel.uiState.value.schrittId)
        }
    }

    @Nested
    inner class `US-003_5 AK1 Beenden markiert Schritt` {

        @Test
        fun `beenden markiert aktuellen Schritt als abgeschlossen`() = runTest {
            // Given: Dialog nach "Ausgebaut" wird angezeigt
            whenever(repository.findUnabgeschlossenenSchritt(1L)).thenReturn(null)
            viewModel = setupBisDialog()
            advanceUntilIdle()
            navigiereBisDialog(viewModel)
            advanceUntilIdle()
            viewModel.onAusgebautGetippt()
            assertEquals(DemontageFlowState.DIALOG, viewModel.uiState.value.flowState)

            // When: Mechaniker waehlt "Beenden"
            viewModel.onBeenden()
            advanceUntilIdle()

            // Then: Aktueller Schritt als abgeschlossen markiert (typ + abgeschlossenAm)
            verify(repository).schrittAbschliessen(10L, SchrittTyp.AM_FAHRZEUG)
            assertTrue(viewModel.uiState.value.flowBeendet)
        }
    }
}
