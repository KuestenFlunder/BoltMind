package com.boltmind.app.feature.neuervorgang

import app.cash.turbine.test
import com.boltmind.app.data.local.ReparaturvorgangDao
import com.boltmind.app.data.local.SchrittDao
import com.boltmind.app.data.model.Reparaturvorgang
import com.boltmind.app.data.model.ReparaturvorgangMitSchrittanzahl
import com.boltmind.app.data.model.Schritt
import com.boltmind.app.data.model.VorgangStatus
import com.boltmind.app.data.repository.ReparaturRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NeuerVorgangViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeReparaturRepository
    private lateinit var viewModel: NeuerVorgangViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeReparaturRepository()
        viewModel = NeuerVorgangViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `sollte leere Felder als Fehler erkennen`() = runTest {
        viewModel.onStartenGeklickt()

        val state = viewModel.uiState.value
        assertEquals(4, state.fehler.size)
        assertTrue(state.fehler.containsKey(Feld.FAHRZEUG))
        assertTrue(state.fehler.containsKey(Feld.AUFTRAGSNUMMER))
        assertTrue(state.fehler.containsKey(Feld.BESCHREIBUNG))
        assertTrue(state.fehler.containsKey(Feld.ANZAHL_ABLAGEORTE))
    }

    @Test
    fun `sollte nur Whitespace Felder als Fehler erkennen`() = runTest {
        viewModel.onFahrzeugGeaendert("   ")
        viewModel.onAuftragsnummerGeaendert("  ")
        viewModel.onBeschreibungGeaendert(" ")
        viewModel.onAnzahlAblageorteGeaendert("  ")

        viewModel.onStartenGeklickt()

        val state = viewModel.uiState.value
        assertEquals(4, state.fehler.size)
    }

    @Test
    fun `sollte ungueltige Anzahl Ablageorte ablehnen bei Null`() = runTest {
        viewModel.onFahrzeugGeaendert("BMW 320d")
        viewModel.onAuftragsnummerGeaendert("#2024-001")
        viewModel.onBeschreibungGeaendert("Bremsen")
        viewModel.onAnzahlAblageorteGeaendert("0")

        viewModel.onStartenGeklickt()

        val state = viewModel.uiState.value
        assertEquals(1, state.fehler.size)
        assertTrue(state.fehler.containsKey(Feld.ANZAHL_ABLAGEORTE))
        assertEquals(
            NeuerVorgangViewModel.FEHLER_ANZAHL_UNGUELTIG,
            state.fehler[Feld.ANZAHL_ABLAGEORTE]
        )
    }

    @Test
    fun `sollte ungueltige Anzahl Ablageorte ablehnen bei negativer Zahl`() = runTest {
        viewModel.onFahrzeugGeaendert("BMW 320d")
        viewModel.onAuftragsnummerGeaendert("#2024-001")
        viewModel.onBeschreibungGeaendert("Bremsen")
        viewModel.onAnzahlAblageorteGeaendert("-1")

        viewModel.onStartenGeklickt()

        val state = viewModel.uiState.value
        assertEquals(1, state.fehler.size)
        assertTrue(state.fehler.containsKey(Feld.ANZAHL_ABLAGEORTE))
        assertEquals(
            NeuerVorgangViewModel.FEHLER_ANZAHL_UNGUELTIG,
            state.fehler[Feld.ANZAHL_ABLAGEORTE]
        )
    }

    @Test
    fun `sollte ungueltige Anzahl Ablageorte ablehnen bei Text`() = runTest {
        viewModel.onFahrzeugGeaendert("BMW 320d")
        viewModel.onAuftragsnummerGeaendert("#2024-001")
        viewModel.onBeschreibungGeaendert("Bremsen")
        viewModel.onAnzahlAblageorteGeaendert("abc")

        viewModel.onStartenGeklickt()

        val state = viewModel.uiState.value
        assertEquals(1, state.fehler.size)
        assertTrue(state.fehler.containsKey(Feld.ANZAHL_ABLAGEORTE))
        assertEquals(
            NeuerVorgangViewModel.FEHLER_ANZAHL_UNGUELTIG,
            state.fehler[Feld.ANZAHL_ABLAGEORTE]
        )
    }

    @Test
    fun `sollte gueltige Eingaben akzeptieren und keine Fehler anzeigen`() = runTest {
        viewModel.onFahrzeugGeaendert("BMW 320d")
        viewModel.onAuftragsnummerGeaendert("#2024-001")
        viewModel.onBeschreibungGeaendert("Bremsen wechseln")
        viewModel.onAnzahlAblageorteGeaendert("5")

        viewModel.navigationEvent.test {
            viewModel.onStartenGeklickt()
            testDispatcher.scheduler.advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is NavigationEvent.ZuDemontage)
        }

        val state = viewModel.uiState.value
        assertTrue(state.fehler.isEmpty())
    }

    @Test
    fun `sollte Reparaturvorgang mit korrekten Werten speichern`() = runTest {
        viewModel.onFahrzeugGeaendert("BMW 320d Blau")
        viewModel.onAuftragsnummerGeaendert("#2024-0815")
        viewModel.onBeschreibungGeaendert("Bremsen vorne wechseln")
        viewModel.onAnzahlAblageorteGeaendert("10")

        viewModel.navigationEvent.test {
            viewModel.onStartenGeklickt()
            testDispatcher.scheduler.advanceUntilIdle()
            awaitItem()
        }

        val gespeicherterVorgang = fakeRepository.gespeicherterVorgang
        assertNotNull(gespeicherterVorgang)
        assertEquals("BMW 320d Blau", gespeicherterVorgang?.fahrzeug)
        assertEquals("#2024-0815", gespeicherterVorgang?.auftragsnummer)
        assertEquals("Bremsen vorne wechseln", gespeicherterVorgang?.beschreibung)
        assertEquals(10, gespeicherterVorgang?.anzahlAblageorte)
        assertEquals(VorgangStatus.OFFEN, gespeicherterVorgang?.status)
    }

    @Test
    fun `sollte Navigation Event mit neuer VorgangId senden nach Save`() = runTest {
        fakeRepository.naechsteId = 42L

        viewModel.onFahrzeugGeaendert("BMW 320d")
        viewModel.onAuftragsnummerGeaendert("#2024-001")
        viewModel.onBeschreibungGeaendert("Bremsen")
        viewModel.onAnzahlAblageorteGeaendert("5")

        viewModel.navigationEvent.test {
            viewModel.onStartenGeklickt()
            testDispatcher.scheduler.advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is NavigationEvent.ZuDemontage)
            assertEquals(42L, (event as NavigationEvent.ZuDemontage).vorgangId)
        }
    }

    @Test
    fun `sollte Zurueck Event senden bei Abbrechen`() = runTest {
        viewModel.navigationEvent.test {
            viewModel.onAbbrechenGeklickt()
            testDispatcher.scheduler.advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is NavigationEvent.Zurueck)
        }
    }

    @Test
    fun `sollte Fehler loeschen wenn Feld geaendert wird`() = runTest {
        viewModel.onStartenGeklickt()
        assertTrue(viewModel.uiState.value.fehler.containsKey(Feld.FAHRZEUG))

        viewModel.onFahrzeugGeaendert("BMW")
        assertFalse(viewModel.uiState.value.fehler.containsKey(Feld.FAHRZEUG))
    }

    @Test
    fun `sollte speichert auf true setzen waehrend Speichervorgang`() = runTest {
        viewModel.onFahrzeugGeaendert("BMW 320d")
        viewModel.onAuftragsnummerGeaendert("#2024-001")
        viewModel.onBeschreibungGeaendert("Bremsen")
        viewModel.onAnzahlAblageorteGeaendert("5")

        viewModel.onStartenGeklickt()

        assertTrue(viewModel.uiState.value.speichert)
    }
}

private class StubReparaturvorgangDao : ReparaturvorgangDao {
    override suspend fun insert(vorgang: Reparaturvorgang): Long = 0L
    override suspend fun delete(vorgang: Reparaturvorgang) {}
    override suspend fun getById(id: Long): Reparaturvorgang? = null
    override fun getAllByStatusMitAnzahl(
        status: VorgangStatus
    ): Flow<List<ReparaturvorgangMitSchrittanzahl>> = flowOf(emptyList())
}

private class StubSchrittDao : SchrittDao {
    override suspend fun insert(schritt: Schritt): Long = 0L
    override fun getAllByVorgangId(vorgangId: Long): Flow<List<Schritt>> = flowOf(emptyList())
    override suspend fun getAllByVorgangIdEinmalig(vorgangId: Long): List<Schritt> = emptyList()
    override fun getAnzahlByVorgangId(vorgangId: Long): Flow<Int> = flowOf(0)
    override suspend fun getLetzerByVorgangId(vorgangId: Long): Schritt? = null
    override suspend fun getAnzahlByVorgangIdEinmalig(vorgangId: Long): Int = 0
}

class FakeReparaturRepository : ReparaturRepository(
    reparaturvorgangDao = StubReparaturvorgangDao(),
    schrittDao = StubSchrittDao()
) {
    var gespeicherterVorgang: Reparaturvorgang? = null
    var naechsteId: Long = 1L

    override suspend fun vorgangAnlegen(vorgang: Reparaturvorgang): Long {
        gespeicherterVorgang = vorgang
        return naechsteId
    }
}
