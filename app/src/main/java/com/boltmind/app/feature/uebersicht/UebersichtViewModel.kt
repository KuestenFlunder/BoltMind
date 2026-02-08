package com.boltmind.app.feature.uebersicht

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boltmind.app.data.model.Reparaturvorgang
import com.boltmind.app.data.model.ReparaturvorgangMitSchrittanzahl
import com.boltmind.app.data.model.VorgangStatus
import com.boltmind.app.data.repository.ReparaturRepository
import com.boltmind.app.ui.navigation.BoltMindRoute
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UebersichtViewModel(
    private val repository: ReparaturRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UebersichtUiState())
    val uiState: StateFlow<UebersichtUiState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<String>()
    val navigationEvent: SharedFlow<String> = _navigationEvent.asSharedFlow()

    private var sammelJob: Job? = null

    init {
        ladeVorgaenge(VorgangStatus.OFFEN)
    }

    fun onTabGewechselt(status: VorgangStatus) {
        _uiState.update { it.copy(aktuellerTab = status) }
        ladeVorgaenge(status)
    }

    fun onVorgangGetippt(vorgang: ReparaturvorgangMitSchrittanzahl) {
        if (vorgang.schrittAnzahl == 0) {
            viewModelScope.launch {
                _navigationEvent.emit(
                    BoltMindRoute.demontageRoute(vorgang.vorgang.id)
                )
            }
        } else {
            _uiState.update { it.copy(auswahlDialog = vorgang) }
        }
    }

    fun onLoeschenAngefragt(vorgang: Reparaturvorgang) {
        _uiState.update { it.copy(loeschDialog = vorgang) }
    }

    fun onLoeschenBestaetigt() {
        val vorgang = _uiState.value.loeschDialog ?: return
        _uiState.update { it.copy(loeschDialog = null) }
        viewModelScope.launch {
            repository.vorgangLoeschen(vorgang)
        }
    }

    fun onLoeschenAbgebrochen() {
        _uiState.update { it.copy(loeschDialog = null) }
    }

    fun onAuswahlGetroffen(route: String) {
        _uiState.update { it.copy(auswahlDialog = null) }
        viewModelScope.launch {
            _navigationEvent.emit(route)
        }
    }

    fun onAuswahlAbgebrochen() {
        _uiState.update { it.copy(auswahlDialog = null) }
    }

    private fun ladeVorgaenge(status: VorgangStatus) {
        sammelJob?.cancel()
        sammelJob = viewModelScope.launch {
            repository.getAlleVorgaenge(status).collect { liste ->
                _uiState.update { it.copy(vorgaenge = liste) }
            }
        }
    }
}
