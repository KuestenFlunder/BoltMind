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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
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
    private val zeiterfassung: ZeiterfassungService,
    private val fotos: BrowserFotoSteuerung
) : ViewModel() {

    private val vorgangId: Long = checkNotNull(zustand["vorgangId"])
    private val modus: BrowserModus = BrowserModus.ausName(zustand["modus"])

    private val _uiState = MutableStateFlow(BrowserUiState(modus = modus))
    val uiState: StateFlow<BrowserUiState> = _uiState.asStateFlow()

    private var ticker: Job? = null

    private val referenzTyp: String
        get() = if (modus == BrowserModus.MONTAGE) ReferenzTyp.MONTAGE_SCHRITT
        else ReferenzTyp.DEMONTAGE_SCHRITT

    /**
     * Der Schritt, auf den die Ansicht springen soll, sobald er in der Liste
     * auftaucht. Gesetzt beim Schritt-Start, weil der neue Schritt zu diesem
     * Zeitpunkt schon eine Id hat, aber noch nicht in der beobachteten Liste
     * steht.
     */
    private var folgeSchrittId: Long? = null

    private var auftragsZaehler = 0

    init {
        viewModelScope.launch {
            val vorgang = repository.findVorgangById(vorgangId)
            repository.beobachteSchritteMitFotos(vorgangId).collect { alle ->
                val sortiert = if (modus == BrowserModus.MONTAGE) alle.reversed() else alle
                var starteSchritt = false
                _uiState.update { s ->
                    val ersteLadung = s.laedt
                    // Die Demontage laesst keinen Vorgang ohne offenen Schritt
                    // zurueck: gibt es keinen, beginnt hier einer -- beim frisch
                    // aus F-002 uebergebenen Vorgang genauso wie beim
                    // Wiedereinstieg nach dem Feierabend.
                    starteSchritt = ersteLadung && modus == BrowserModus.DEMONTAGE &&
                        sortiert.none { it.schritt.abgeschlossenAm == null }
                    val index = when {
                        ersteLadung -> startIndex(sortiert)
                        else -> folgeIndex(sortiert) ?: s.aktiverIndex
                    }
                    val neu = s.copy(
                        laedt = false,
                        vorgang = vorgang,
                        schritte = sortiert,
                        aktiverIndex = index.coerceIn(0, (sortiert.size - 1).coerceAtLeast(0)),
                        eingebauteAnzahl = sortiert.count { it.schritt.eingebautBeiMontage }
                    )
                    // Steigt der Mechaniker in eine bereits vollstaendige Montage
                    // wieder ein, fuehrt der Weg direkt zum Abschluss -- es gibt
                    // keinen offenen Schritt mehr, ueber den er dorthin kaeme
                    // (montage.md, US-004.5). Nur beim ersten Laden: nach der
                    // Rueckkehr per Back uebernimmt der Knopf "Zum Abschluss",
                    // sonst liesse sich der Screen nie verlassen.
                    if (ersteLadung && neu.alleEingebaut) neu.copy(fertig = true) else neu
                }
                if (starteSchritt) schrittStarten()
                zeitAktualisieren()
            }
        }
        starteTicker()
    }

    /**
     * Wohin die Ansicht springt, sobald der eben angelegte Schritt in der Liste
     * ankommt. Danach ist der Sprung erledigt und die Ansicht folgt wieder dem
     * Mechaniker -- ein Foto, das spaeter eintrifft, zieht sie nicht erneut mit.
     */
    private fun folgeIndex(schritte: List<SchrittMitFotos>): Int? {
        val ziel = folgeSchrittId ?: return null
        val index = schritte.indexOfFirst { it.schritt.id == ziel }
        if (index < 0) return null
        folgeSchrittId = null
        return index
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

    /**
     * Zurueck zum Abschluss-Screen, nachdem der Mechaniker ihn per Back
     * verlassen hat. Navigiert nur -- archiviert wird ausschliesslich dort.
     */
    fun onZumAbschluss() = _uiState.update { it.copy(fertig = true) }

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

    /**
     * Zaehlt die Anzeige im Sekundentakt hoch -- aber nur, solange wirklich
     * gemessen wird.
     *
     * Eine unbedingte Endlosschleife waere zweifach falsch: sie weckt das
     * ViewModel auch dann jede Sekunde, wenn der Timer steht, und sie laesst
     * jeden Test mit virtueller Zeit ins Leere laufen, weil immer noch eine
     * Verzoegerung eingeplant ist.
     */
    private fun starteTicker() {
        ticker?.cancel()
        ticker = viewModelScope.launch {
            _uiState
                .map { it.timerLaeuft }
                .distinctUntilChanged()
                .collectLatest { laeuft ->
                    while (laeuft) {
                        delay(1_000)
                        zeitAktualisieren()
                    }
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

    // --- Kamera und Schritte (Demontage) ---------------------------------------

    /** Ein weiteres Foto am betrachteten Schritt. */
    fun onWeiteresFoto() {
        val schrittId = _uiState.value.aktiverSchritt?.schritt?.id ?: return
        kameraAnfordern(schrittId)
    }

    /**
     * "Wiederholen" am sichtbaren Foto. Geloescht wird nichts -- gemerkt wird nur,
     * welches Foto die Aufnahme ersetzen soll (Governance, "Kamera").
     */
    fun onWiederholen() {
        val zustand = _uiState.value
        val schrittId = zustand.aktiverSchritt?.schritt?.id ?: return
        val foto = zustand.aktiverSchritt?.fotos?.getOrNull(zustand.aktivesFoto) ?: return
        kameraAnfordern(schrittId, ersetztFotoId = foto.id)
    }

    fun onNaechstesTeil() = schrittStarten()

    /**
     * Beginnt einen Schritt -- der gemeinsame Weg von "Naechstes Teil" und vom
     * Einstieg ohne offenen Schritt.
     *
     * Die Reihenfolge ist die Regel: erst existiert der Schritt und ist der
     * betrachtete, **dann** faehrt die Kamera an (workflow.md, "Reihenfolge beim
     * Schritt-Start"). Startete die Kamera parallel zur Anlage, kaeme sie in eine
     * Ansicht zurueck, die noch auf dem eben abgeschlossenen Schritt steht -- das
     * Foto landete dort, und der grosse Kreis fiele auf "ZURUECK ZU" zurueck.
     */
    private fun schrittStarten() {
        val zustand = _uiState.value
        val offen = zustand.offenerIndex?.let { zustand.schritte.getOrNull(it)?.schritt }
        viewModelScope.launch {
            val neu = fotos.naechstesTeil(vorgangId, offen)
            folgeSchrittId = neu.id
            kameraAnfordern(neu.id, eroeffnetSchritt = true)
        }
    }

    private fun kameraAnfordern(
        zielSchrittId: Long,
        ersetztFotoId: Long? = null,
        eroeffnetSchritt: Boolean = false
    ) {
        auftragsZaehler++
        val auftrag = KameraAuftrag(
            nummer = auftragsZaehler,
            zielPfad = fotos.neueZieldatei(),
            zielSchrittId = zielSchrittId,
            ersetztFotoId = ersetztFotoId,
            eroeffnetSchritt = eroeffnetSchritt
        )
        _uiState.update { it.copy(kameraAuftrag = auftrag) }
    }

    fun onFotoAufgenommen() {
        val auftrag = _uiState.value.kameraAuftrag ?: return
        _uiState.update { it.copy(kameraAuftrag = null) }
        viewModelScope.launch {
            if (auftrag.ersetztFotoId != null) {
                fotos.fotoErsetzen(auftrag.ersetztFotoId, auftrag.zielPfad)
            } else {
                fotos.fotoUebernehmen(auftrag.zielSchrittId, auftrag.zielPfad)
            }
        }
    }

    /**
     * Kamera abgebrochen. Ein vorhandenes Foto bleibt in jedem Fall unangetastet;
     * hat diese Runde den Schritt eroeffnet und blieb er leer, wird der
     * Schritt-Start zurueckgenommen.
     */
    fun onKameraAbgebrochen() {
        val auftrag = _uiState.value.kameraAuftrag ?: return
        _uiState.update { it.copy(kameraAuftrag = null) }
        viewModelScope.launch {
            fotos.verwerfeDatei(auftrag.zielPfad)
            if (auftrag.eroeffnetSchritt) fotos.schrittStartZuruecknehmen(auftrag.zielSchrittId)
        }
    }

    /** Springt vom nachgeschlagenen Schritt zurueck zum offenen. */
    fun onZumOffenenSchritt() {
        _uiState.value.offenerIndex?.let(::onSchrittGewaehlt)
    }

    fun onFeierabendBestaetigt() {
        val offen = _uiState.value.offenerIndex
            ?.let { _uiState.value.schritte.getOrNull(it)?.schritt }
        viewModelScope.launch {
            fotos.beenden(offen)
            zeiterfassung.stoppeAlleOffenen()
            _uiState.update { it.copy(sheet = null, verlassen = true) }
        }
    }

    fun onKameraFehltBestaetigt() = _uiState.update { it.copy(kameraFehlt = false) }

    fun onNavigationAbgeschlossen() = _uiState.update { it.copy(verlassen = false, fertig = false) }

    override fun onCleared() {
        ticker?.cancel()
        super.onCleared()
    }
}
