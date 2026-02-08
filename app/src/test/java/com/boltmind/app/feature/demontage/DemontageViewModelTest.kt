package com.boltmind.app.feature.demontage

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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class DemontageViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeDemontageRepository

    private val testVorgang = Reparaturvorgang(
        id = 1L,
        fahrzeug = "BMW 320d",
        auftragsnummer = "#2024-001",
        beschreibung = "Bremsen vorne wechseln",
        anzahlAblageorte = 5,
        status = VorgangStatus.OFFEN,
        erstelltAm = Instant.parse("2024-01-15T10:00:00Z")
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeDemontageRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun erstelleViewModel(vorgangId: Long = 1L): DemontageViewModel {
        return DemontageViewModel(fakeRepository, vorgangId)
    }

    // ==========================================================================
    // Basis-Tests (bestehend, angepasst)
    // ==========================================================================

    @Test
    fun `sollte Vorgang laden und initialen State setzen`() = runTest {
        fakeRepository.vorgangToReturn = testVorgang
        fakeRepository.schrittAnzahl = 0

        val viewModel = erstelleViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("BMW 320d", state.fahrzeug)
        assertEquals("Bremsen vorne wechseln", state.beschreibung)
        assertEquals(1, state.aktuellerSchritt)
        assertEquals(5, state.anzahlAblageorte)
        assertEquals(1, state.vorgeschlageneAblageortNummer)
        assertEquals("1", state.ablageortNummer)
        assertNull(state.letzteFotoPath)
        assertFalse(state.isLoading)
    }

    @Test
    fun `sollte Navigation senden wenn Vorgang nicht gefunden`() = runTest {
        fakeRepository.vorgangToReturn = null

        val viewModel = erstelleViewModel()

        viewModel.navigationEvent.test {
            testDispatcher.scheduler.advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is DemontageNavigationEvent.ZurueckZurUebersicht)
        }
    }

    @Test
    fun `sollte Phase zu ABLAGEORT_BESTAETIGUNG wechseln nach Foto`() = runTest {
        fakeRepository.vorgangToReturn = testVorgang

        val viewModel = erstelleViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onFotoAufgenommen("/fotos/schritt1.jpg")

        val state = viewModel.uiState.value
        assertEquals(DemontagePhase.ABLAGEORT_BESTAETIGUNG, state.phase)
        assertEquals("/fotos/schritt1.jpg", state.aktuellesFotoPath)
        assertNull(state.fehler)
    }

    @Test
    fun `sollte Schritt in DB speichern bei Ablageort Bestaetigung`() = runTest {
        fakeRepository.vorgangToReturn = testVorgang

        val viewModel = erstelleViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onFotoAufgenommen("/fotos/schritt1.jpg")
        viewModel.onAblageortBestaetigt()
        testDispatcher.scheduler.advanceUntilIdle()

        val gespeicherterSchritt = fakeRepository.gespeicherterSchritt
        assertNotNull(gespeicherterSchritt)
        assertEquals(1L, gespeicherterSchritt?.reparaturvorgangId)
        assertEquals("/fotos/schritt1.jpg", gespeicherterSchritt?.fotoPath)
        assertEquals("1", gespeicherterSchritt?.ablageortNummer)
        assertEquals(1, gespeicherterSchritt?.reihenfolge)
    }

    @Test
    fun `sollte nach Bestaetigung zurueck zu KAMERA Phase wechseln`() = runTest {
        fakeRepository.vorgangToReturn = testVorgang

        val viewModel = erstelleViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onFotoAufgenommen("/fotos/schritt1.jpg")
        viewModel.onAblageortBestaetigt()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(DemontagePhase.KAMERA, state.phase)
        assertNull(state.aktuellesFotoPath)
        assertEquals("/fotos/schritt1.jpg", state.letzteFotoPath)
    }

    @Test
    fun `sollte Schrittzaehler hochzaehlen nach Bestaetigung`() = runTest {
        fakeRepository.vorgangToReturn = testVorgang
        fakeRepository.schrittAnzahl = 0

        val viewModel = erstelleViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.aktuellerSchritt)

        viewModel.onFotoAufgenommen("/fotos/schritt1.jpg")
        viewModel.onAblageortBestaetigt()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.aktuellerSchritt)
    }

    @Test
    fun `sollte naechste Ablageort Nummer automatisch vorschlagen`() = runTest {
        fakeRepository.vorgangToReturn = testVorgang.copy(anzahlAblageorte = 5)
        fakeRepository.schrittAnzahl = 0

        val viewModel = erstelleViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.vorgeschlageneAblageortNummer)

        viewModel.onFotoAufgenommen("/fotos/schritt1.jpg")
        viewModel.onAblageortBestaetigt()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.vorgeschlageneAblageortNummer)
        assertEquals("2", viewModel.uiState.value.ablageortNummer)
    }

    @Test
    fun `sollte Ablageort Aenderung aktivieren`() = runTest {
        fakeRepository.vorgangToReturn = testVorgang

        val viewModel = erstelleViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onFotoAufgenommen("/fotos/schritt1.jpg")
        viewModel.onAblageortAendern()

        val state = viewModel.uiState.value
        assertTrue(state.istAblageortAenderungAktiv)
        assertEquals("", state.ablageortNummer)
    }

    @Test
    fun `sollte benutzerdefinierte Ablageort Nummer uebernehmen`() = runTest {
        fakeRepository.vorgangToReturn = testVorgang

        val viewModel = erstelleViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onFotoAufgenommen("/fotos/schritt1.jpg")
        viewModel.onAblageortAendern()
        viewModel.onAblageortNummerGeaendert("7")

        assertEquals("7", viewModel.uiState.value.ablageortNummer)

        viewModel.onAblageortBestaetigt()
        testDispatcher.scheduler.advanceUntilIdle()

        val gespeicherterSchritt = fakeRepository.gespeicherterSchritt
        assertNotNull(gespeicherterSchritt)
        assertEquals("7", gespeicherterSchritt?.ablageortNummer)
    }

    @Test
    fun `sollte Navigation Event senden bei Demontage Beenden`() = runTest {
        fakeRepository.vorgangToReturn = testVorgang

        val viewModel = erstelleViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.navigationEvent.test {
            viewModel.onDemontageBeenden()
            testDispatcher.scheduler.advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is DemontageNavigationEvent.ZurueckZurUebersicht)
        }
    }

    @Test
    fun `sollte Fehler setzen bei Foto Fehler`() = runTest {
        fakeRepository.vorgangToReturn = testVorgang

        val viewModel = erstelleViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onFotoFehler("Kamera nicht verfuegbar")

        assertEquals("Kamera nicht verfuegbar", viewModel.uiState.value.fehler)
    }

    // ==========================================================================
    // NEUE Tests: Belegte Ablageorte überspringen
    // ==========================================================================

    @Test
    fun `sollte belegte Ablageorte beim Vorschlag ueberspringen`() = runTest {
        // Ablageort 3 ist schon belegt (von früherer Session)
        fakeRepository.vorgangToReturn = testVorgang.copy(anzahlAblageorte = 5)
        fakeRepository.schrittAnzahl = 2
        fakeRepository.belegteAblageorte = setOf(1, 3)

        val viewModel = erstelleViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Sequenz-Counter startet bei 3 (nächster nach 2 Schritten)
        // Ablageort 3 ist belegt → Vorschlag muss 4 sein (nächste freie)
        val state = viewModel.uiState.value
        assertEquals(4, state.vorgeschlageneAblageortNummer)
        assertEquals("4", state.ablageortNummer)
    }

    @Test
    fun `sollte bei manueller Wahl Sequenz nicht brechen und danach zurueckspringen`() = runTest {
        fakeRepository.vorgangToReturn = testVorgang.copy(anzahlAblageorte = 5)
        fakeRepository.schrittAnzahl = 0

        val viewModel = erstelleViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Schritt 1: Vorschlag 1 → bestätigt
        assertEquals(1, viewModel.uiState.value.vorgeschlageneAblageortNummer)
        viewModel.onFotoAufgenommen("/fotos/schritt1.jpg")
        viewModel.onAblageortBestaetigt()
        testDispatcher.scheduler.advanceUntilIdle()

        // Schritt 2: Vorschlag 2 → User wählt manuell 5
        assertEquals(2, viewModel.uiState.value.vorgeschlageneAblageortNummer)
        viewModel.onFotoAufgenommen("/fotos/schritt2.jpg")
        viewModel.onAblageortAendern()
        viewModel.onAblageortNummerGeaendert("5")
        viewModel.onAblageortBestaetigt()
        testDispatcher.scheduler.advanceUntilIdle()

        // Schritt 3: Vorschlag muss 3 sein (Sequenz springt zurück, nicht 6)
        assertEquals(3, viewModel.uiState.value.vorgeschlageneAblageortNummer)
    }

    @Test
    fun `sollte belegte Nummer in Sequenz ueberspringen nach manueller Wahl`() = runTest {
        fakeRepository.vorgangToReturn = testVorgang.copy(anzahlAblageorte = 5)
        fakeRepository.schrittAnzahl = 0

        val viewModel = erstelleViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Schritt 1: bestätige 1
        viewModel.onFotoAufgenommen("/fotos/s1.jpg")
        viewModel.onAblageortBestaetigt()
        testDispatcher.scheduler.advanceUntilIdle()

        // Schritt 2: manuell 5
        viewModel.onFotoAufgenommen("/fotos/s2.jpg")
        viewModel.onAblageortAendern()
        viewModel.onAblageortNummerGeaendert("5")
        viewModel.onAblageortBestaetigt()
        testDispatcher.scheduler.advanceUntilIdle()

        // Schritt 3: bestätige 3
        assertEquals(3, viewModel.uiState.value.vorgeschlageneAblageortNummer)
        viewModel.onFotoAufgenommen("/fotos/s3.jpg")
        viewModel.onAblageortBestaetigt()
        testDispatcher.scheduler.advanceUntilIdle()

        // Schritt 4: bestätige 4
        assertEquals(4, viewModel.uiState.value.vorgeschlageneAblageortNummer)
        viewModel.onFotoAufgenommen("/fotos/s4.jpg")
        viewModel.onAblageortBestaetigt()
        testDispatcher.scheduler.advanceUntilIdle()

        // Schritt 5: Counter wäre 5, aber 5 ist belegt → nächste freie = 2
        assertEquals(2, viewModel.uiState.value.vorgeschlageneAblageortNummer)
    }

    // ==========================================================================
    // NEUE Tests: Alle Ablageorte belegt → Dialog
    // ==========================================================================

    @Test
    fun `sollte Dialog zeigen wenn alle Ablageorte belegt`() = runTest {
        fakeRepository.vorgangToReturn = testVorgang.copy(anzahlAblageorte = 3)
        fakeRepository.schrittAnzahl = 0

        val viewModel = erstelleViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // 3 Schritte bestätigen → alle 3 Ablageorte belegt
        repeat(3) { i ->
            viewModel.onFotoAufgenommen("/fotos/s${i + 1}.jpg")
            viewModel.onAblageortBestaetigt()
            testDispatcher.scheduler.advanceUntilIdle()
        }

        // Alle belegt → Dialog muss angezeigt werden
        assertTrue(viewModel.uiState.value.zeigAlleBesetztDialog)
    }

    @Test
    fun `sollte bei Dialog Beenden zurueck zur Uebersicht navigieren`() = runTest {
        fakeRepository.vorgangToReturn = testVorgang.copy(anzahlAblageorte = 2)
        fakeRepository.schrittAnzahl = 0

        val viewModel = erstelleViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // 2 Schritte bestätigen
        repeat(2) { i ->
            viewModel.onFotoAufgenommen("/fotos/s${i + 1}.jpg")
            viewModel.onAblageortBestaetigt()
            testDispatcher.scheduler.advanceUntilIdle()
        }

        assertTrue(viewModel.uiState.value.zeigAlleBesetztDialog)

        // Beenden wählen → Navigation zurück
        viewModel.navigationEvent.test {
            viewModel.onAlleBesetztBeenden()
            testDispatcher.scheduler.advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is DemontageNavigationEvent.ZurueckZurUebersicht)
        }
    }

    @Test
    fun `sollte bei Dialog Erweitern ueber max hinaus weitermachen`() = runTest {
        fakeRepository.vorgangToReturn = testVorgang.copy(anzahlAblageorte = 2)
        fakeRepository.schrittAnzahl = 0

        val viewModel = erstelleViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // 2 Schritte bestätigen → alle belegt
        repeat(2) { i ->
            viewModel.onFotoAufgenommen("/fotos/s${i + 1}.jpg")
            viewModel.onAblageortBestaetigt()
            testDispatcher.scheduler.advanceUntilIdle()
        }

        assertTrue(viewModel.uiState.value.zeigAlleBesetztDialog)

        // +1 Erweitern wählen → weiter mit Nummer 3 (über max hinaus)
        viewModel.onAlleBesetztErweitern()

        assertFalse(viewModel.uiState.value.zeigAlleBesetztDialog)
        assertEquals(DemontagePhase.KAMERA, viewModel.uiState.value.phase)
        assertEquals(3, viewModel.uiState.value.vorgeschlageneAblageortNummer)
        assertEquals("3", viewModel.uiState.value.ablageortNummer)
    }

    // ==========================================================================
    // NEUE Tests: naechsteFreieNummer Algorithmus
    // ==========================================================================

    @Test
    fun `sollte naechsteFreieNummer korrekt berechnen ohne belegte`() {
        val result = DemontageViewModel.naechsteFreieNummer(1, emptySet(), 5)
        assertEquals(1, result)
    }

    @Test
    fun `sollte naechsteFreieNummer belegte ueberspringen`() {
        // Start bei 3, Ablageort 3 belegt → 4
        val result = DemontageViewModel.naechsteFreieNummer(3, setOf(1, 3), 5)
        assertEquals(4, result)
    }

    @Test
    fun `sollte naechsteFreieNummer mit wrap belegte ueberspringen`() {
        // Start bei 5, 5 belegt, 1 belegt → 2
        val result = DemontageViewModel.naechsteFreieNummer(5, setOf(1, 3, 4, 5), 5)
        assertEquals(2, result)
    }

    @Test
    fun `sollte naechsteFreieNummer null liefern wenn alle belegt`() {
        val result = DemontageViewModel.naechsteFreieNummer(1, setOf(1, 2, 3, 4, 5), 5)
        assertNull(result)
    }

    @Test
    fun `sollte naechsteFreieNummer mit einzelnem freien Platz finden`() {
        // Nur Platz 4 ist frei
        val result = DemontageViewModel.naechsteFreieNummer(1, setOf(1, 2, 3, 5), 5)
        assertEquals(4, result)
    }

    // ==========================================================================
    // Fortsetzen nach App-Neustart (belegte aus DB geladen)
    // ==========================================================================

    @Test
    fun `sollte beim Laden belegte Ablageorte aus bestehenden Schritten ermitteln`() = runTest {
        fakeRepository.vorgangToReturn = testVorgang.copy(anzahlAblageorte = 5)
        fakeRepository.schrittAnzahl = 3
        fakeRepository.belegteAblageorte = setOf(1, 2, 3)

        val viewModel = erstelleViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Counter startet bei 4 (nächster nach 3 Schritten), 4 ist frei
        assertEquals(4, viewModel.uiState.value.vorgeschlageneAblageortNummer)
    }
}

// ==========================================================================
// Test-Infrastruktur
// ==========================================================================

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

private class FakeDemontageRepository : ReparaturRepository(
    reparaturvorgangDao = StubReparaturvorgangDao(),
    schrittDao = StubSchrittDao()
) {
    var vorgangToReturn: Reparaturvorgang? = null
    var letzterSchritt: Schritt? = null
    var schrittAnzahl: Int = 0
    var gespeicherterSchritt: Schritt? = null
    var belegteAblageorte: Set<Int> = emptySet()

    private val gespeicherteSchritte = mutableListOf<Schritt>()

    override suspend fun getVorgangById(id: Long): Reparaturvorgang? = vorgangToReturn

    override suspend fun getLetzerSchritt(vorgangId: Long): Schritt? = letzterSchritt

    override suspend fun getSchrittAnzahlEinmalig(vorgangId: Long): Int = schrittAnzahl

    override suspend fun schrittAnlegen(schritt: Schritt): Long {
        gespeicherterSchritt = schritt
        gespeicherteSchritte.add(schritt)
        return schritt.id
    }

    override suspend fun getBelegteAblageortNummern(vorgangId: Long): Set<Int> = belegteAblageorte
}
