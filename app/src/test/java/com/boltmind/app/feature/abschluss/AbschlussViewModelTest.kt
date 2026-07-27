package com.boltmind.app.feature.abschluss

import androidx.lifecycle.SavedStateHandle
import com.boltmind.app.data.model.Reparaturvorgang
import com.boltmind.app.data.repository.ReparaturRepository
import com.boltmind.app.service.zeiterfassung.ReferenzTyp
import com.boltmind.app.service.zeiterfassung.ZeiterfassungService
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
import java.time.Duration
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
@DisplayName("AbschlussViewModel")
class AbschlussViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var repository: ReparaturRepository
    private lateinit var zeiterfassung: ZeiterfassungService

    private companion object {
        const val VORGANG_ID = 7L
        val SCHRITT_IDS = listOf(11L, 12L, 13L)
    }

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repository = mock {
            onBlocking { findVorgangById(VORGANG_ID) } doReturn Reparaturvorgang(
                id = VORGANG_ID,
                auftragsnummer = "2026-0815",
                beschreibung = "Bremsen vorne wechseln",
                erstelltAm = Instant.EPOCH,
                aktualisiertAm = Instant.EPOCH
            )
            onBlocking { holeSchrittIds(VORGANG_ID) } doReturn SCHRITT_IDS
        }
        zeiterfassung = mock {
            onBlocking { gesamtdauer(any(), any()) } doReturn Duration.ofMinutes(10)
        }
    }

    @AfterEach
    fun tearDown() = Dispatchers.resetMain()

    private fun vm() = AbschlussViewModel(
        SavedStateHandle(mapOf("vorgangId" to VORGANG_ID)),
        repository,
        zeiterfassung
    )

    @Nested
    @DisplayName("Anzeige")
    inner class Anzeige {

        @Test
        fun `zeigt Auftragsnummer, Beschreibung und Teilezahl des Vorgangs`() = runTest {
            val viewModel = vm()
            advanceUntilIdle()

            val zustand = viewModel.uiState.value
            assertEquals("2026-0815", zustand.auftragsnummer)
            assertEquals("Bremsen vorne wechseln", zustand.beschreibung)
            assertEquals(3, zustand.anzahlTeile)
        }

        @Test
        fun `summiert Demontage- und Montagezeit ueber alle Schritte`() = runTest {
            // Drei Schritte, je 10 min Demontage und 10 min Montage -> 60 min
            val viewModel = vm()
            advanceUntilIdle()

            assertEquals("1 h 00 min", viewModel.uiState.value.gemesseneZeit)
        }
    }

    @Nested
    @DisplayName("Archivieren")
    inner class Archivieren {

        @Test
        fun `passiert nicht von allein`() = runTest {
            vm()
            advanceUntilIdle()

            // Der Abschluss-Screen darf den Vorgang nicht beim blossen Anzeigen aus
            // der aktiven Liste nehmen -- Archivieren braucht eine bewusste Geste.
            verify(repository, never()).archiviereVorgang(any())
        }

        @Test
        fun `setzt den Vorgang auf archiviert`() = runTest {
            val viewModel = vm()
            advanceUntilIdle()

            viewModel.onArchivieren()
            advanceUntilIdle()

            verify(repository).archiviereVorgang(VORGANG_ID)
            assertTrue(
                viewModel.uiState.value.archiviert,
                "Der Screen muss melden, dass er fertig ist, sonst navigiert niemand weiter."
            )
        }

        @Test
        fun `stoppt offene Zeitmessungen, bevor es archiviert`() = runTest {
            val viewModel = vm()
            advanceUntilIdle()

            viewModel.onArchivieren()
            advanceUntilIdle()

            // Reihenfolge zaehlt: eine Messung, die nach dem Archivieren noch laeuft,
            // wuerde die Gesamtdauer des Vorgangs weiter wachsen lassen.
            inOrder(zeiterfassung, repository) {
                verify(zeiterfassung).stoppeAlleOffenen()
                verify(repository).archiviereVorgang(VORGANG_ID)
            }
        }

        @Test
        fun `meldet den Navigationswunsch nur einmal`() = runTest {
            val viewModel = vm()
            advanceUntilIdle()

            viewModel.onArchivieren()
            advanceUntilIdle()
            assertTrue(viewModel.uiState.value.archiviert)

            viewModel.onNavigationAbgeschlossen()
            assertFalse(
                viewModel.uiState.value.archiviert,
                "Ohne Quittung wuerde die Navigation bei jeder Neuzusammensetzung erneut ausgeloest."
            )
        }
    }

    @Nested
    @DisplayName("Randfaelle")
    inner class Randfaelle {

        @Test
        fun `haelt einen Vorgang ohne Beschreibung aus`() = runTest {
            repository.stub {
                onBlocking { findVorgangById(VORGANG_ID) } doReturn Reparaturvorgang(
                    id = VORGANG_ID,
                    auftragsnummer = "2026-0001",
                    beschreibung = null,
                    erstelltAm = Instant.EPOCH,
                    aktualisiertAm = Instant.EPOCH
                )
            }
            val viewModel = vm()
            advanceUntilIdle()

            assertEquals(null, viewModel.uiState.value.beschreibung)
            assertEquals("2026-0001", viewModel.uiState.value.auftragsnummer)
        }

        @Test
        fun `haelt einen geloeschten Vorgang aus`() = runTest {
            repository.stub {
                onBlocking { findVorgangById(VORGANG_ID) } doReturn null
                onBlocking { holeSchrittIds(VORGANG_ID) } doReturn emptyList()
            }
            val viewModel = vm()
            advanceUntilIdle()

            assertEquals("", viewModel.uiState.value.auftragsnummer)
            assertEquals(0, viewModel.uiState.value.anzahlTeile)
        }
    }
}
