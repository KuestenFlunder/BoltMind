package com.boltmind.app.feature.neuervorgang

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boltmind.app.data.foto.FotoManager
import com.boltmind.app.data.model.Reparaturvorgang
import com.boltmind.app.data.repository.ReparaturRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * F-002: Vorgang anlegen.
 *
 * Der Flow ist Foto-first -- die System-Kamera wird beim Betreten sofort
 * angefordert. Das Formular ueberlebt jeden Kamera-Abbruch, weil der Zustand hier
 * liegt und nicht im Screen.
 *
 * Die Governance-Regel zum Wiederholen ist hier verdrahtet: erst die neue Aufnahme
 * sichern, dann die alte Datei loeschen. Ein Kamera-Abbruch raeumt ausschliesslich
 * die fuer *diese* Aufnahme vorangelegte Zieldatei weg.
 */
class NeuerVorgangViewModel(
    private val repository: ReparaturRepository,
    private val fotoManager: FotoManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(NeuerVorgangUiState())
    val uiState: StateFlow<NeuerVorgangUiState> = _uiState.asStateFlow()

    private var auftragsZaehler = 0

    init {
        kameraAnfordern()
    }

    // ------------------------------------------------------------------
    // Kamera
    // ------------------------------------------------------------------

    /** "NEU KNIPSEN" -- das vorhandene Foto bleibt dabei unangetastet. */
    fun onBildWiederholen() = kameraAnfordern()

    private fun kameraAnfordern() {
        val ziel = fotoManager.erstelleZieldatei(FOTO_PRAEFIX)
        auftragsZaehler += 1
        _uiState.update {
            it.copy(kameraAuftrag = KameraAuftrag(auftragsZaehler, ziel.absolutePath))
        }
    }

    /**
     * Die System-Kamera hat bestaetigt. Erst jetzt faellt das alte Foto weg --
     * vorher waere ein Abbruch ein Datenverlust.
     */
    fun onFotoAufgenommen(pfad: String) {
        val uebernommen = fotoManager.uebernimmAufnahme(pfad)
        if (uebernommen == null) {
            onKameraAbgebrochen()
            return
        }
        val altesFoto = _uiState.value.fahrzeugFotoPfad
        if (altesFoto != null && altesFoto != uebernommen) {
            fotoManager.loescheFoto(altesFoto)
        }
        _uiState.update { it.copy(fahrzeugFotoPfad = uebernommen, kameraAuftrag = null) }
    }

    /**
     * Abbruch in der System-Kamera. Gab es noch kein Foto, ist der ganze
     * Anlage-Flow hinfaellig; sonst geht es mit dem bisherigen Foto weiter.
     */
    fun onKameraAbgebrochen() {
        val zustand = _uiState.value
        fotoManager.loescheFoto(zustand.kameraAuftrag?.zielPfad)
        _uiState.update {
            it.copy(
                kameraAuftrag = null,
                verlassen = it.fahrzeugFotoPfad == null
            )
        }
    }

    // ------------------------------------------------------------------
    // Formular
    // ------------------------------------------------------------------

    fun onAuftragsnummerGeaendert(wert: String) {
        _uiState.update { it.copy(auftragsnummer = wert, nummerFehlt = false) }
    }

    fun onBeschreibungGeaendert(wert: String) {
        _uiState.update { it.copy(beschreibung = wert) }
    }

    /**
     * "LOS GEHT'S": ohne Auftragsnummer nur die Fehlerzeile, sonst den Vorgang
     * anlegen und weiter in die Demontage.
     *
     * **Kein Schritt.** Den legt F-003 an, sobald der Browser keinen offenen
     * Schritt vorfindet -- und startet in derselben Bewegung die Kamera. Legte
     * F-002 den Schritt hier an, saehe der Browser einen offenen Schritt, hielte
     * das fuer eine Fortsetzung und zeigte dem Mechaniker eine leere Maske
     * (F-002 anlegen.md AK 2).
     */
    fun onStartenGetippt() {
        val zustand = _uiState.value
        if (zustand.gestarteterVorgangId != null) return

        val nummer = zustand.auftragsnummer.trim()
        if (nummer.isEmpty()) {
            _uiState.update { it.copy(nummerFehlt = true) }
            return
        }

        viewModelScope.launch {
            val vorgangId = repository.erstelleVorgang(
                Reparaturvorgang(
                    fahrzeugFotoPfad = zustand.fahrzeugFotoPfad,
                    auftragsnummer = nummer,
                    beschreibung = zustand.beschreibung.trim().ifEmpty { null }
                )
            )
            _uiState.update { it.copy(gestarteterVorgangId = vorgangId, nummerFehlt = false) }
        }
    }

    /**
     * Zurueck. Verwirft das Foto samt Datei und legt nichts an -- die Datei haette
     * ohne DB-Zeile ohnehin keine Referenz mehr.
     */
    fun onZurueck() {
        val zustand = _uiState.value
        fotoManager.loescheFoto(zustand.kameraAuftrag?.zielPfad)
        fotoManager.loescheFoto(zustand.fahrzeugFotoPfad)
        _uiState.update {
            it.copy(fahrzeugFotoPfad = null, kameraAuftrag = null, verlassen = true)
        }
    }

    private companion object {
        const val FOTO_PRAEFIX = "fahrzeug"
    }
}
