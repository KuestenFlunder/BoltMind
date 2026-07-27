package com.boltmind.app.feature.browser

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boltmind.app.data.model.SchrittFoto
import com.boltmind.app.data.model.SchrittMitFotos
import com.boltmind.app.data.repository.ReparaturRepository
import com.boltmind.app.service.zeiterfassung.ReferenzTyp
import com.boltmind.app.service.zeiterfassung.ZeiterfassungService
import com.boltmind.app.ui.navigation.BrowserModus
import com.boltmind.app.ui.schrittbrowser.LabelArt
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Der Consumer des Schritt-Browsers. Bedient F-003 (Demontage), F-004 (Montage)
 * und die Nur-Lese-Ansicht des Archivs aus F-001 -- im Entwurf ist das ein
 * einziger Screen mit drei Betriebsarten.
 */
class BrowserViewModel(
    zustand: SavedStateHandle,
    private val repository: ReparaturRepository,
    private val zeiterfassung: ZeiterfassungService
) : ViewModel() {

    private val vorgangId: Long = checkNotNull(zustand["vorgangId"])
    private val modus: BrowserModus = BrowserModus.ausName(zustand["modus"])

    private val _uiState = MutableStateFlow(BrowserUiState(modus = modus))
    val uiState: StateFlow<BrowserUiState> = _uiState.asStateFlow()

    private var ticker: Job? = null

    private val referenzTyp: String
        get() = if (modus == BrowserModus.MONTAGE) ReferenzTyp.MONTAGE_SCHRITT
        else ReferenzTyp.DEMONTAGE_SCHRITT

    init {
        viewModelScope.launch {
            val vorgang = repository.findVorgangById(vorgangId)
            repository.beobachteSchritteMitFotos(vorgangId).collect { alle ->
                val sortiert = if (modus == BrowserModus.MONTAGE) alle.reversed() else alle
                _uiState.update { s ->
                    val index = if (s.laedt) startIndex(sortiert) else s.aktiverIndex
                    s.copy(
                        laedt = false,
                        vorgang = vorgang,
                        schritte = sortiert,
                        aktiverIndex = index.coerceIn(0, (sortiert.size - 1).coerceAtLeast(0)),
                        eingebauteAnzahl = sortiert.count { it.schritt.eingebautBeiMontage }
                    )
                }
                zeitAktualisieren()
            }
        }
        starteTicker()
    }

    /**
     * Wo der Browser aufsetzt: die Demontage beim offenen Schritt, die Montage
     * beim ersten noch nicht eingebauten Teil, das Archiv am Anfang.
     */
    private fun startIndex(schritte: List<SchrittMitFotos>): Int = when (modus) {
        BrowserModus.DEMONTAGE ->
            schritte.indexOfLast { it.schritt.abgeschlossenAm == null }
                .takeIf { it >= 0 } ?: schritte.lastIndex.coerceAtLeast(0)
        BrowserModus.MONTAGE ->
            schritte.indexOfFirst { !it.schritt.eingebautBeiMontage }
                .takeIf { it >= 0 } ?: 0
        BrowserModus.ARCHIV -> 0
    }

    // --- Navigation im Browser ------------------------------------------------

    fun onSchrittGewaehlt(index: Int) {
        _uiState.update {
            it.copy(aktiverIndex = index, aktivesFoto = 0, zeigeWischHinweis = false)
        }
        viewModelScope.launch { zeitAktualisieren() }
    }

    fun onVorherigerSchritt() = onSchrittGewaehlt((_uiState.value.aktiverIndex - 1).coerceAtLeast(0))

    fun onNaechsterSchritt() {
        val s = _uiState.value
        onSchrittGewaehlt((s.aktiverIndex + 1).coerceAtMost(s.schritte.lastIndex.coerceAtLeast(0)))
    }

    fun onFotoGewaehlt(index: Int) =
        _uiState.update { it.copy(aktivesFoto = index, zeigeWischHinweis = false) }

    fun onVollbildOeffnen() = _uiState.update { it.copy(vollbild = true) }

    fun onVollbildSchliessen() = _uiState.update { it.copy(vollbild = false) }

    // --- Label ----------------------------------------------------------------

    fun onLabelUmgeschaltet(foto: SchrittFoto, art: LabelArt) {
        viewModelScope.launch {
            repository.setzeLabel(
                fotoId = foto.id,
                istBauteil = if (art == LabelArt.BAUTEIL) !foto.istBauteil else foto.istBauteil,
                istUebersicht = if (art == LabelArt.UEBERSICHT) !foto.istUebersicht else foto.istUebersicht,
                istAblageort = if (art == LabelArt.ABLAGEORT) !foto.istAblageort else foto.istAblageort
            )
        }
    }

    // --- Montage --------------------------------------------------------------

    fun onEingebaut() {
        val schritt = _uiState.value.aktiverSchritt?.schritt ?: return
        viewModelScope.launch {
            repository.setzeEingebaut(schritt.id, true)
            zeiterfassung.stoppeFallsLaeuft(schritt.id, referenzTyp)
            val naechster = _uiState.value.schritte
                .indexOfFirst { !it.schritt.eingebautBeiMontage && it.schritt.id != schritt.id }
            if (naechster < 0) _uiState.update { it.copy(fertig = true) }
            else onSchrittGewaehlt(naechster)
        }
    }

    fun onHaekchenZuruecknehmenBestaetigt() {
        val schritt = _uiState.value.aktiverSchritt?.schritt ?: return
        viewModelScope.launch { repository.setzeEingebaut(schritt.id, false) }
        onSheetGeschlossen()
    }

    // --- Sheets ---------------------------------------------------------------

    fun onSheetGeschlossen() = _uiState.update { it.copy(sheet = null) }

    fun zeigeSheet(sheet: SheetZustand) = _uiState.update { it.copy(sheet = sheet) }

    fun onVerlassenBestaetigt() {
        viewModelScope.launch {
            zeiterfassung.stoppeAlleOffenen()
            _uiState.update { it.copy(sheet = null, verlassen = true) }
        }
    }

    // --- Zeiterfassung ---------------------------------------------------------

    fun onTimerUmgeschaltet() {
        val schritt = _uiState.value.aktiverSchritt?.schritt ?: return
        viewModelScope.launch {
            zeiterfassung.umschalten(schritt.id, referenzTyp)
            zeitAktualisieren()
        }
    }

    private fun starteTicker() {
        ticker?.cancel()
        ticker = viewModelScope.launch {
            while (true) {
                delay(1_000)
                if (_uiState.value.timerLaeuft) zeitAktualisieren()
            }
        }
    }

    private suspend fun zeitAktualisieren() {
        val s = _uiState.value
        val schritt = s.aktiverSchritt?.schritt
        val schrittSek = schritt
            ?.let { zeiterfassung.gesamtdauer(it.id, referenzTyp).seconds } ?: 0L
        val laeuft = schritt?.let { zeiterfassung.laeuft(it.id, referenzTyp) } ?: false
        val gesamt = repository.holeSchrittIds(vorgangId).sumOf { id ->
            zeiterfassung.gesamtdauer(id, ReferenzTyp.DEMONTAGE_SCHRITT).seconds +
                zeiterfassung.gesamtdauer(id, ReferenzTyp.MONTAGE_SCHRITT).seconds
        }
        _uiState.update {
            it.copy(schrittSekunden = schrittSek, timerLaeuft = laeuft, gesamtSekunden = gesamt)
        }
    }

    fun onKameraFehltBestaetigt() = _uiState.update { it.copy(kameraFehlt = false) }

    fun onNavigationAbgeschlossen() = _uiState.update { it.copy(verlassen = false, fertig = false) }

    override fun onCleared() {
        ticker?.cancel()
        super.onCleared()
    }
}
