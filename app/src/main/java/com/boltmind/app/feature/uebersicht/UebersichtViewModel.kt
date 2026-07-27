package com.boltmind.app.feature.uebersicht

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boltmind.app.data.model.ReparaturvorgangMitAnzahl
import com.boltmind.app.data.model.VorgangStatus
import com.boltmind.app.data.repository.ReparaturRepository
import com.boltmind.app.ui.navigation.BrowserModus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId

/**
 * F-001 Uebersicht: zwei Tabs, zwei Listen, zwei Sheets, ein Navigationsziel.
 *
 * Sortierung und Schrittzahl liefert bereits das Repository
 * (`ORDER BY aktualisiertAm DESC`); hier wird nur noch formatiert.
 */
class UebersichtViewModel(
    private val repository: ReparaturRepository,
    private val uhr: () -> Instant = Instant::now,
    private val zone: ZoneId = ZoneId.systemDefault()
) : ViewModel() {

    private val tabAuswahl = MutableStateFlow(UebersichtTab.OFFEN)
    private val sheetZustand = MutableStateFlow<UebersichtSheet?>(null)
    private val navigationsZiel = MutableStateFlow<UebersichtZiel?>(null)

    val uiState: StateFlow<UebersichtUiState> = combine(
        repository.beobachteOffeneVorgaengeMitAnzahl(),
        repository.beobachteArchivierteVorgaengeMitAnzahl(),
        tabAuswahl,
        sheetZustand,
        navigationsZiel
    ) { offene, archivierte, tab, sheet, ziel ->
        UebersichtUiState(
            tab = tab,
            offene = offene.map { karte(it, archiviert = false) },
            archivierte = archivierte.map { karte(it, archiviert = true) },
            sheet = sheet,
            ziel = ziel
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_VERZOEGERUNG_MS), UebersichtUiState())

    // ------------------------------------------------------------------
    // Tab
    // ------------------------------------------------------------------

    fun onTabGewaehlt(tab: UebersichtTab) {
        tabAuswahl.value = tab
    }

    // ------------------------------------------------------------------
    // Karte antippen (US-001.2, US-001.5)
    // ------------------------------------------------------------------

    /**
     * Archiviert -> direkt in den Lesemodus. Offen ohne Schritt -> direkt in die
     * Demontage; "Montage starten" fuehrt dort garantiert in eine Sackgasse
     * (uebersicht.md US-001.2, Delta-Analyse K-10). Sonst das Auswahl-Sheet.
     */
    fun onVorgangGeoeffnet(karte: VorgangKarte) {
        viewModelScope.launch {
            val vorgang = repository.findVorgangById(karte.id) ?: return@launch
            when {
                vorgang.status == VorgangStatus.ARCHIVIERT ->
                    navigiere(karte.id, BrowserModus.ARCHIV)

                repository.zaehleSchritte(karte.id) == 0 ->
                    navigiere(karte.id, BrowserModus.DEMONTAGE)

                else -> sheetZustand.value = UebersichtSheet.Auswahl(karte)
            }
        }
    }

    fun onWeiterDemontieren(vorgangId: Long) = navigiere(vorgangId, BrowserModus.DEMONTAGE)

    fun onMontageStarten(vorgangId: Long) = navigiere(vorgangId, BrowserModus.MONTAGE)

    // ------------------------------------------------------------------
    // Neuanlage (US-001.3) und Loeschen (US-001.4)
    // ------------------------------------------------------------------

    fun onNeuerVorgang() {
        sheetZustand.value = null
        navigationsZiel.value = UebersichtZiel.NeuerVorgang
    }

    fun onLoeschenAngefragt(karte: VorgangKarte) {
        sheetZustand.value = UebersichtSheet.Loeschen(karte)
    }

    /** Kaskadiert ueber die Fremdschluessel auf Schritte und Fotos. */
    fun onLoeschenBestaetigt(vorgangId: Long) {
        sheetZustand.value = null
        viewModelScope.launch { repository.loescheVorgang(vorgangId) }
    }

    fun onSheetGeschlossen() {
        sheetZustand.value = null
    }

    fun onZielVerbraucht() {
        navigationsZiel.value = null
    }

    // ------------------------------------------------------------------

    private fun navigiere(vorgangId: Long, modus: BrowserModus) {
        sheetZustand.value = null
        navigationsZiel.value = UebersichtZiel.Browser(vorgangId, modus)
    }

    /**
     * Offen zeigt `erstelltAm` (der Auftrag ist daran wiedererkennbar), Archiv
     * zeigt `aktualisiertAm` -- den Zeitpunkt des Archivierens -- plus Dauer.
     * Sortiert wird in beiden Faellen nach `aktualisiertAm` (uebersicht.md,
     * "Bewusste Abweichung (Anzeige vs. Sortierung)").
     */
    private fun karte(eintrag: ReparaturvorgangMitAnzahl, archiviert: Boolean): VorgangKarte {
        val vorgang = eintrag.vorgang
        val jetzt = uhr()
        return VorgangKarte(
            id = vorgang.id,
            auftragsnummer = vorgang.auftragsnummer,
            beschreibung = vorgang.beschreibung?.takeIf { it.isNotBlank() },
            fahrzeugFotoPfad = vorgang.fahrzeugFotoPfad,
            schrittAnzahl = eintrag.schrittAnzahl,
            datumText = if (archiviert) {
                DatumFormat.datumMitDauer(vorgang.aktualisiertAm, jetzt, zone, eintrag.dauerMillis)
            } else {
                DatumFormat.datum(vorgang.erstelltAm, jetzt, zone)
            }
        )
    }

    private companion object {
        const val STOP_VERZOEGERUNG_MS = 5_000L
    }
}
