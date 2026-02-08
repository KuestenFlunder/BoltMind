package com.boltmind.app.feature.uebersicht

import com.boltmind.app.data.model.Reparaturvorgang
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
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
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

    private fun erstelleViewModel(): UebersichtViewModel =
        UebersichtViewModel(repository)

    @Nested
    inner class `US-001_1 Offene Vorgaenge anzeigen` {

        @Test
        fun `zeigt alle offenen Vorgaenge sortiert nach letzter Bearbeitung`() = runTest {
            // Given: offene Vorgaenge existieren (DAO liefert bereits sortiert)
            val gestern = Instant.now().minusSeconds(86400)
            val heute = Instant.now()
            val vorgaenge = listOf(
                testVorgang(id = 1L, auftragsnummer = "NEU", erstelltAm = heute),
                testVorgang(id = 2L, auftragsnummer = "ALT", erstelltAm = gestern),
            )
            whenever(repository.beobachteOffeneVorgaenge()).thenReturn(flowOf(vorgaenge))
            whenever(repository.zaehleSchritte(1L)).thenReturn(5)
            whenever(repository.zaehleSchritte(2L)).thenReturn(3)

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
            whenever(repository.beobachteOffeneVorgaenge()).thenReturn(flowOf(emptyList()))

            // When: ViewModel wird initialisiert
            viewModel = erstelleViewModel()
            advanceUntilIdle()

            // Then: leere Liste
            assertTrue(viewModel.uiState.value.vorgaenge.isEmpty())
        }

        @Test
        fun `zeigt Schrittanzahl pro Vorgang an`() = runTest {
            // Given: Vorgang mit 12 Schritten
            val vorgang = testVorgang(id = 1L, auftragsnummer = "TEST")
            whenever(repository.beobachteOffeneVorgaenge()).thenReturn(flowOf(listOf(vorgang)))
            whenever(repository.zaehleSchritte(1L)).thenReturn(12)

            // When: ViewModel wird initialisiert
            viewModel = erstelleViewModel()
            advanceUntilIdle()

            // Then: Schrittanzahl korrekt
            assertEquals(12, viewModel.uiState.value.vorgaenge[0].anzahlSchritte)
        }

        @Test
        fun `formatiert heutiges Datum als Heute`() = runTest {
            // Given: Vorgang von heute
            val vorgang = testVorgang(id = 1L, erstelltAm = Instant.now())
            whenever(repository.beobachteOffeneVorgaenge()).thenReturn(flowOf(listOf(vorgang)))
            whenever(repository.zaehleSchritte(1L)).thenReturn(0)

            // When
            viewModel = erstelleViewModel()
            advanceUntilIdle()

            // Then
            assertEquals("Heute", viewModel.uiState.value.vorgaenge[0].erstelltAm)
        }

        @Test
        fun `formatiert gestriges Datum als Gestern`() = runTest {
            // Given: Vorgang von gestern
            val gestern = LocalDate.now().minusDays(1)
                .atStartOfDay(ZoneId.systemDefault()).toInstant()
            val vorgang = testVorgang(id = 1L, erstelltAm = gestern)
            whenever(repository.beobachteOffeneVorgaenge()).thenReturn(flowOf(listOf(vorgang)))
            whenever(repository.zaehleSchritte(1L)).thenReturn(0)

            // When
            viewModel = erstelleViewModel()
            advanceUntilIdle()

            // Then
            assertEquals("Gestern", viewModel.uiState.value.vorgaenge[0].erstelltAm)
        }

        @Test
        fun `formatiert aelteres Datum als dd MM yyyy`() = runTest {
            // Given: Vorgang von vor 10 Tagen
            val vorZehnTagen = LocalDate.now().minusDays(10)
                .atStartOfDay(ZoneId.systemDefault()).toInstant()
            val vorgang = testVorgang(id = 1L, erstelltAm = vorZehnTagen)
            whenever(repository.beobachteOffeneVorgaenge()).thenReturn(flowOf(listOf(vorgang)))
            whenever(repository.zaehleSchritte(1L)).thenReturn(0)

            // When
            viewModel = erstelleViewModel()
            advanceUntilIdle()

            // Then: dd.MM.yyyy Format
            val erwartetesDatum = LocalDate.now().minusDays(10)
                .format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy"))
            assertEquals(erwartetesDatum, viewModel.uiState.value.vorgaenge[0].erstelltAm)
        }

        @Test
        fun `zeigt Fahrzeugfoto-Pfad pro Vorgang`() = runTest {
            // Given: Vorgang mit Foto
            val vorgang = testVorgang(id = 1L)
            whenever(repository.beobachteOffeneVorgaenge()).thenReturn(flowOf(listOf(vorgang)))
            whenever(repository.zaehleSchritte(1L)).thenReturn(0)

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
            whenever(repository.beobachteOffeneVorgaenge()).thenReturn(flowOf(emptyList()))

            // When: ViewModel wird erstellt
            viewModel = erstelleViewModel()

            // Then: initialer State ist korrekt (vor collect)
            val initialState = viewModel.uiState.value
            assertTrue(initialState.vorgaenge.isEmpty())
            assertEquals(false, initialState.isLoading)
        }
    }
}
