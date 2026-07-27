package com.boltmind.app.feature.abschluss

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boltmind.app.data.repository.ReparaturRepository
import com.boltmind.app.feature.browser.Zeitformat
import com.boltmind.app.service.zeiterfassung.ReferenzTyp
import com.boltmind.app.service.zeiterfassung.ZeiterfassungService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AbschlussUiState(
    val auftragsnummer: String = "",
    val beschreibung: String? = null,
    val anzahlTeile: Int = 0,
    val gemesseneZeit: String = "",
    val archiviert: Boolean = false
)

/**
 * Der Abschluss-Screen der Montage.
 *
 * Archiviert wird erst auf ausdrueckliche Bestaetigung -- der Bildschirm nimmt den
 * Vorgang nicht von selbst aus der aktiven Liste.
 */
class AbschlussViewModel(
    zustand: SavedStateHandle,
    private val repository: ReparaturRepository,
    private val zeiterfassung: ZeiterfassungService
) : ViewModel() {

    private val vorgangId: Long = checkNotNull(zustand["vorgangId"])

    private val _uiState = MutableStateFlow(AbschlussUiState())
    val uiState: StateFlow<AbschlussUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val vorgang = repository.findVorgangById(vorgangId)
            val schrittIds = repository.holeSchrittIds(vorgangId)
            val sekunden = schrittIds.sumOf { id ->
                zeiterfassung.gesamtdauer(id, ReferenzTyp.DEMONTAGE_SCHRITT).seconds +
                    zeiterfassung.gesamtdauer(id, ReferenzTyp.MONTAGE_SCHRITT).seconds
            }
            _uiState.update {
                it.copy(
                    auftragsnummer = vorgang?.auftragsnummer.orEmpty(),
                    beschreibung = vorgang?.beschreibung,
                    anzahlTeile = schrittIds.size,
                    gemesseneZeit = Zeitformat.lang(sekunden)
                )
            }
        }
    }

    fun onArchivieren() {
        viewModelScope.launch {
            zeiterfassung.stoppeAlleOffenen()
            repository.archiviereVorgang(vorgangId)
            _uiState.update { it.copy(archiviert = true) }
        }
    }

    fun onNavigationAbgeschlossen() = _uiState.update { it.copy(archiviert = false) }
}
