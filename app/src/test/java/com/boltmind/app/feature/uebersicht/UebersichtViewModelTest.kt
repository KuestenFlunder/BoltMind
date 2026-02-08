package com.boltmind.app.feature.uebersicht

import app.cash.turbine.test
import com.boltmind.app.data.model.Reparaturvorgang
import com.boltmind.app.data.model.ReparaturvorgangMitSchrittanzahl
import com.boltmind.app.data.model.VorgangStatus
import com.boltmind.app.ui.navigation.BoltMindRoute
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class UebersichtViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeReparaturRepository
    private lateinit var viewModel: UebersichtViewModel

    private val jetztInstant: Instant = Instant.parse("2026-02-08T10:00:00Z")

    private val offenerVorgangOhneSchritte = ReparaturvorgangMitSchrittanzahl(
        vorgang = Reparaturvorgang(
            id = 1L,
            fahrzeug = "BMW 320d",
            auftragsnummer = "#2024-0815",
            beschreibung = "Getriebe defekt",
            anzahlAblageorte = 5,
            status = VorgangStatus.OFFEN,
            erstelltAm = jetztInstant
        ),
        schrittAnzahl = 0
    )

    private val offenerVorgangMitSchritten = ReparaturvorgangMitSchrittanzahl(
        vorgang = Reparaturvorgang(
            id = 2L,
            fahrzeug = "VW Golf GTI",
            auftragsnummer = "#2024-0712",
            beschreibung = "Bremsen wechseln",
            anzahlAblageorte = 3,
            status = VorgangStatus.OFFEN,
            erstelltAm = jetztInstant
        ),
        schrittAnzahl = 8
    )

    private val archivierterVorgang = ReparaturvorgangMitSchrittanzahl(
        vorgang = Reparaturvorgang(
            id = 3L,
            fahrzeug = "Audi A4 Avant",
            auftragsnummer = "#2024-0601",
            beschreibung = "Turbolader tauschen",
            anzahlAblageorte = 8,
            status = VorgangStatus.ARCHIVIERT,
            erstelltAm = jetztInstant
        ),
        schrittAnzahl = 15
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeReparaturRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun erstelleViewModel(): UebersichtViewModel {
        return UebersichtViewModel(fakeRepository).also { viewModel = it }
    }

    @Test
    fun `sollte offene Vorgaenge als Default laden`() = runTest {
        fakeRepository.offeneVorgaengeFlow.value = listOf(
            offenerVorgangOhneSchritte,
            offenerVorgangMitSchritten
        )

        val vm = erstelleViewModel()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(VorgangStatus.OFFEN, state.aktuellerTab)
        assertEquals(2, state.vorgaenge.size)
        assertEquals("BMW 320d", state.vorgaenge[0].vorgang.fahrzeug)
        assertEquals("VW Golf GTI", state.vorgaenge[1].vorgang.fahrzeug)
    }

    @Test
    fun `sollte Tab wechseln und archivierte Vorgaenge laden`() = runTest {
        fakeRepository.offeneVorgaengeFlow.value = listOf(offenerVorgangMitSchritten)
        fakeRepository.archivierteVorgaengeFlow.value = listOf(archivierterVorgang)

        val vm = erstelleViewModel()
        advanceUntilIdle()

        assertEquals(VorgangStatus.OFFEN, vm.uiState.value.aktuellerTab)
        assertEquals(1, vm.uiState.value.vorgaenge.size)

        vm.onTabGewechselt(VorgangStatus.ARCHIVIERT)
        advanceUntilIdle()

        assertEquals(VorgangStatus.ARCHIVIERT, vm.uiState.value.aktuellerTab)
        assertEquals(1, vm.uiState.value.vorgaenge.size)
        assertEquals("Audi A4 Avant", vm.uiState.value.vorgaenge[0].vorgang.fahrzeug)
    }

    @Test
    fun `sollte direkt navigieren wenn Vorgang 0 Schritte hat`() = runTest {
        fakeRepository.offeneVorgaengeFlow.value = listOf(offenerVorgangOhneSchritte)

        val vm = erstelleViewModel()
        advanceUntilIdle()

        vm.navigationEvent.test {
            vm.onVorgangGetippt(offenerVorgangOhneSchritte)
            val route = awaitItem()
            assertEquals(BoltMindRoute.demontageRoute(1L), route)
        }

        assertNull(vm.uiState.value.auswahlDialog)
    }

    @Test
    fun `sollte Auswahl-Dialog zeigen wenn Vorgang Schritte hat`() = runTest {
        fakeRepository.offeneVorgaengeFlow.value = listOf(offenerVorgangMitSchritten)

        val vm = erstelleViewModel()
        advanceUntilIdle()

        vm.onVorgangGetippt(offenerVorgangMitSchritten)

        assertNotNull(vm.uiState.value.auswahlDialog)
        assertEquals(2L, vm.uiState.value.auswahlDialog?.vorgang?.id)
        assertEquals(8, vm.uiState.value.auswahlDialog?.schrittAnzahl)
    }

    @Test
    fun `sollte Auswahl-Dialog schliessen bei Abbruch`() = runTest {
        fakeRepository.offeneVorgaengeFlow.value = listOf(offenerVorgangMitSchritten)

        val vm = erstelleViewModel()
        advanceUntilIdle()

        vm.onVorgangGetippt(offenerVorgangMitSchritten)
        assertNotNull(vm.uiState.value.auswahlDialog)

        vm.onAuswahlAbgebrochen()
        assertNull(vm.uiState.value.auswahlDialog)
    }

    @Test
    fun `sollte nach Auswahl navigieren und Dialog schliessen`() = runTest {
        fakeRepository.offeneVorgaengeFlow.value = listOf(offenerVorgangMitSchritten)

        val vm = erstelleViewModel()
        advanceUntilIdle()

        vm.onVorgangGetippt(offenerVorgangMitSchritten)

        val expectedRoute = BoltMindRoute.montageRoute(2L)

        vm.navigationEvent.test {
            vm.onAuswahlGetroffen(expectedRoute)
            val route = awaitItem()
            assertEquals(expectedRoute, route)
        }

        assertNull(vm.uiState.value.auswahlDialog)
    }

    @Test
    fun `sollte Loesch-Dialog oeffnen wenn Loeschen angefragt`() = runTest {
        fakeRepository.offeneVorgaengeFlow.value = listOf(offenerVorgangOhneSchritte)

        val vm = erstelleViewModel()
        advanceUntilIdle()

        vm.onLoeschenAngefragt(offenerVorgangOhneSchritte.vorgang)

        assertNotNull(vm.uiState.value.loeschDialog)
        assertEquals(1L, vm.uiState.value.loeschDialog?.id)
    }

    @Test
    fun `sollte Loesch-Dialog schliessen bei Abbruch`() = runTest {
        fakeRepository.offeneVorgaengeFlow.value = listOf(offenerVorgangOhneSchritte)

        val vm = erstelleViewModel()
        advanceUntilIdle()

        vm.onLoeschenAngefragt(offenerVorgangOhneSchritte.vorgang)
        assertNotNull(vm.uiState.value.loeschDialog)

        vm.onLoeschenAbgebrochen()
        assertNull(vm.uiState.value.loeschDialog)
    }

    @Test
    fun `sollte Vorgang loeschen nach Bestaetigung`() = runTest {
        fakeRepository.offeneVorgaengeFlow.value = listOf(
            offenerVorgangOhneSchritte,
            offenerVorgangMitSchritten
        )

        val vm = erstelleViewModel()
        advanceUntilIdle()

        vm.onLoeschenAngefragt(offenerVorgangOhneSchritte.vorgang)
        vm.onLoeschenBestaetigt()
        advanceUntilIdle()

        assertNull(vm.uiState.value.loeschDialog)
        assertEquals(offenerVorgangOhneSchritte.vorgang, fakeRepository.zuletztGeloeschterVorgang)
    }

    @Test
    fun `sollte leere Liste anzeigen wenn keine Vorgaenge vorhanden`() = runTest {
        fakeRepository.offeneVorgaengeFlow.value = emptyList()

        val vm = erstelleViewModel()
        advanceUntilIdle()

        assertEquals(0, vm.uiState.value.vorgaenge.size)
    }
}
