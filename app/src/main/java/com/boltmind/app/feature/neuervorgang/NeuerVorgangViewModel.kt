package com.boltmind.app.feature.neuervorgang

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boltmind.app.data.model.Reparaturvorgang
import com.boltmind.app.data.model.VorgangStatus
import com.boltmind.app.data.repository.ReparaturRepository
import com.boltmind.app.ui.navigation.BoltMindRoute
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant

sealed interface NavigationEvent {
    data class ZuDemontage(val vorgangId: Long) : NavigationEvent
    data object Zurueck : NavigationEvent
}

class NeuerVorgangViewModel(
    private val repository: ReparaturRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NeuerVorgangUiState())
    val uiState: StateFlow<NeuerVorgangUiState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent: SharedFlow<NavigationEvent> = _navigationEvent.asSharedFlow()

    fun onFahrzeugGeaendert(value: String) {
        _uiState.update { it.copy(fahrzeug = value, fehler = it.fehler - Feld.FAHRZEUG) }
    }

    fun onAuftragsnummerGeaendert(value: String) {
        _uiState.update { it.copy(auftragsnummer = value, fehler = it.fehler - Feld.AUFTRAGSNUMMER) }
    }

    fun onBeschreibungGeaendert(value: String) {
        _uiState.update { it.copy(beschreibung = value, fehler = it.fehler - Feld.BESCHREIBUNG) }
    }

    fun onAnzahlAblageorteGeaendert(value: String) {
        _uiState.update { it.copy(anzahlAblageorte = value, fehler = it.fehler - Feld.ANZAHL_ABLAGEORTE) }
    }

    fun onStartenGeklickt() {
        val state = _uiState.value
        val fehler = validiere(state)
        if (fehler.isNotEmpty()) {
            _uiState.update { it.copy(fehler = fehler) }
            return
        }
        _uiState.update { it.copy(speichert = true) }
        viewModelScope.launch {
            val vorgang = Reparaturvorgang(
                fahrzeug = state.fahrzeug.trim(),
                auftragsnummer = state.auftragsnummer.trim(),
                beschreibung = state.beschreibung.trim(),
                anzahlAblageorte = state.anzahlAblageorte.trim().toInt(),
                status = VorgangStatus.OFFEN,
                erstelltAm = Instant.now()
            )
            val vorgangId = repository.vorgangAnlegen(vorgang)
            _navigationEvent.emit(NavigationEvent.ZuDemontage(vorgangId))
        }
    }

    fun onAbbrechenGeklickt() {
        viewModelScope.launch {
            _navigationEvent.emit(NavigationEvent.Zurueck)
        }
    }

    private fun validiere(state: NeuerVorgangUiState): Map<Feld, String> {
        val fehler = mutableMapOf<Feld, String>()
        if (state.fahrzeug.isBlank()) {
            fehler[Feld.FAHRZEUG] = FEHLER_FAHRZEUG
        }
        if (state.auftragsnummer.isBlank()) {
            fehler[Feld.AUFTRAGSNUMMER] = FEHLER_AUFTRAGSNUMMER
        }
        if (state.beschreibung.isBlank()) {
            fehler[Feld.BESCHREIBUNG] = FEHLER_BESCHREIBUNG
        }
        val anzahl = state.anzahlAblageorte.trim()
        if (anzahl.isBlank()) {
            fehler[Feld.ANZAHL_ABLAGEORTE] = FEHLER_ANZAHL_PFLICHT
        } else {
            val parsed = anzahl.toIntOrNull()
            if (parsed == null || parsed < 1) {
                fehler[Feld.ANZAHL_ABLAGEORTE] = FEHLER_ANZAHL_UNGUELTIG
            }
        }
        return fehler
    }

    companion object {
        const val FEHLER_FAHRZEUG = "fehler_fahrzeug_pflicht"
        const val FEHLER_AUFTRAGSNUMMER = "fehler_auftragsnummer_pflicht"
        const val FEHLER_BESCHREIBUNG = "fehler_beschreibung_pflicht"
        const val FEHLER_ANZAHL_PFLICHT = "fehler_anzahl_ablageorte_pflicht"
        const val FEHLER_ANZAHL_UNGUELTIG = "fehler_anzahl_ablageorte_ungueltig"
    }
}
