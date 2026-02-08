package com.boltmind.app.feature.neuervorgang

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boltmind.app.data.model.Reparaturvorgang
import com.boltmind.app.data.repository.ReparaturRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NeuerVorgangViewModel(
    private val repository: ReparaturRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NeuerVorgangUiState())
    val uiState: StateFlow<NeuerVorgangUiState> = _uiState.asStateFlow()

    fun onFotoAufgenommen(pfad: String) {
        _uiState.update {
            it.copy(
                schritt = NeuerVorgangSchritt.Formular,
                fotoPfad = pfad,
            )
        }
    }

    fun onBildWiederholen() {
        _uiState.update {
            it.copy(schritt = NeuerVorgangSchritt.Kamera)
        }
    }

    fun onKameraAbgebrochen() {
        _uiState.update {
            it.copy(schritt = NeuerVorgangSchritt.Formular)
        }
    }

    fun onAuftragsnummerGeaendert(value: String) {
        _uiState.update {
            it.copy(
                auftragsnummer = value,
                validierungsFehler = null,
            )
        }
    }

    fun onBeschreibungGeaendert(value: String) {
        _uiState.update { it.copy(beschreibung = value) }
    }

    fun onStartenGetippt() {
        val state = _uiState.value

        if (state.auftragsnummer.isBlank()) {
            _uiState.update { it.copy(validierungsFehler = "Auftragsnummer ist erforderlich") }
            return
        }

        _uiState.update { it.copy(isSpeichernd = true, validierungsFehler = null) }

        viewModelScope.launch {
            val vorgang = Reparaturvorgang(
                fahrzeugFotoPfad = state.fotoPfad,
                auftragsnummer = state.auftragsnummer.trim(),
                beschreibung = state.beschreibung.ifBlank { null },
            )
            val id = repository.erstelleVorgang(vorgang)
            _uiState.update {
                it.copy(isSpeichernd = false, erstellterVorgangId = id)
            }
        }
    }

    fun onNavigationAbgeschlossen() {
        _uiState.update { it.copy(erstellterVorgangId = null) }
    }
}
