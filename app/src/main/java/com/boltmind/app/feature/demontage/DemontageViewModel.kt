package com.boltmind.app.feature.demontage

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boltmind.app.data.foto.FotoManager
import com.boltmind.app.data.model.Schritt
import com.boltmind.app.data.model.SchrittTyp
import com.boltmind.app.data.repository.ReparaturRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DemontageViewModel(
    private val repository: ReparaturRepository,
    private val fotoManager: FotoManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val vorgangId: Long = savedStateHandle.get<Long>("vorgangId")
        ?: error("vorgangId fehlt")

    private val _uiState = MutableStateFlow(DemontageUiState(vorgangId = vorgangId))
    val uiState: StateFlow<DemontageUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch { starteFlow() }
    }

    private suspend fun starteFlow() {
        val unabgeschlossen = repository.findUnabgeschlossenenSchritt(vorgangId)
        if (unabgeschlossen != null) {
            setzeFortsetzungsState(unabgeschlossen)
        } else {
            starteNeuenSchritt()
        }
    }

    private fun setzeFortsetzungsState(schritt: Schritt) {
        val flowState = when {
            schritt.bauteilFotoPfad == null -> DemontageFlowState.PREVIEW_BAUTEIL
            else -> DemontageFlowState.ARBEITSPHASE
        }
        if (flowState == DemontageFlowState.PREVIEW_BAUTEIL) {
            val tempDatei = fotoManager.erstelleTempDatei("bauteil")
            _uiState.update {
                it.copy(
                    flowState = flowState,
                    previewZustand = PreviewZustand.INITIAL,
                    schrittNummer = schritt.schrittNummer,
                    schrittId = schritt.id,
                    bauteilFotoPfad = null,
                    ablageortFotoPfad = null,
                    tempFotoPfad = tempDatei.absolutePath,
                    kameraIntentAktiv = true
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    flowState = flowState,
                    previewZustand = PreviewZustand.INITIAL,
                    schrittNummer = schritt.schrittNummer,
                    schrittId = schritt.id,
                    bauteilFotoPfad = schritt.bauteilFotoPfad,
                    ablageortFotoPfad = null,
                    tempFotoPfad = null,
                    kameraIntentAktiv = false
                )
            }
        }
    }

    private suspend fun starteNeuenSchritt() {
        val schritt = repository.schrittAnlegen(vorgangId)
        val tempDatei = fotoManager.erstelleTempDatei("bauteil")
        _uiState.update {
            it.copy(
                flowState = DemontageFlowState.PREVIEW_BAUTEIL,
                previewZustand = PreviewZustand.INITIAL,
                schrittNummer = schritt.schrittNummer,
                schrittId = schritt.id,
                bauteilFotoPfad = null,
                ablageortFotoPfad = null,
                tempFotoPfad = tempDatei.absolutePath,
                kameraIntentAktiv = true
            )
        }
    }

    private fun starteKamera(prefix: String) {
        val tempDatei = fotoManager.erstelleTempDatei(prefix)
        _uiState.update {
            it.copy(kameraIntentAktiv = true, tempFotoPfad = tempDatei.absolutePath)
        }
    }

    private fun setzeFotoVorschau() {
        _uiState.update {
            it.copy(previewZustand = PreviewZustand.VORSCHAU, kameraIntentAktiv = false)
        }
    }

    // --- Bauteil-Foto ---

    fun onFotoAufnehmenGetippt() = starteKamera("bauteil")
    fun onFotoAufgenommen() = setzeFotoVorschau()
    fun onFotoWiederholen() {
        _uiState.value.tempFotoPfad?.let { fotoManager.loescheTempFoto(it) }
        starteKamera("bauteil")
    }

    fun onFotoBestaetigt() {
        val state = _uiState.value
        val tempPfad = state.tempFotoPfad ?: return
        val schrittId = state.schrittId
        viewModelScope.launch {
            val permanentPfad = fotoManager.bestaetigeFoto(tempPfad, "bauteil_$schrittId")
            if (permanentPfad != null) {
                repository.bauteilFotoBestaetigen(schrittId, permanentPfad)
                _uiState.update {
                    it.copy(
                        flowState = DemontageFlowState.ARBEITSPHASE,
                        previewZustand = PreviewZustand.INITIAL,
                        bauteilFotoPfad = permanentPfad,
                        tempFotoPfad = null
                    )
                }
            }
        }
    }

    fun onKeineKameraApp() {
        _uiState.update { it.copy(keineKameraApp = true) }
    }

    fun onKameraAbgebrochen() {
        _uiState.update {
            it.copy(previewZustand = PreviewZustand.INITIAL, kameraIntentAktiv = false)
        }
    }

    fun onKeineKameraDialogBestaetigt() {
        _uiState.update { it.copy(keineKameraApp = false) }
    }

    // --- Arbeitsphase + Dialog ---

    fun onAusgebautGetippt() {
        _uiState.update { it.copy(flowState = DemontageFlowState.DIALOG) }
    }

    fun onAblageortFotografieren() {
        val schrittId = _uiState.value.schrittId
        viewModelScope.launch {
            repository.schrittTypSetzen(schrittId, SchrittTyp.AUSGEBAUT)
            val tempDatei = fotoManager.erstelleTempDatei("ablageort")
            _uiState.update {
                it.copy(
                    flowState = DemontageFlowState.PREVIEW_ABLAGEORT,
                    previewZustand = PreviewZustand.INITIAL,
                    tempFotoPfad = tempDatei.absolutePath,
                    kameraIntentAktiv = true
                )
            }
        }
    }

    fun onWeiterOhneAblageort() {
        val schrittId = _uiState.value.schrittId
        viewModelScope.launch {
            repository.schrittAbschliessen(schrittId, SchrittTyp.AM_FAHRZEUG)
            starteNeuenSchritt()
        }
    }

    fun onBeenden() {
        val schrittId = _uiState.value.schrittId
        viewModelScope.launch {
            repository.schrittAbschliessen(schrittId, SchrittTyp.AM_FAHRZEUG)
            _uiState.update { it.copy(flowBeendet = true) }
        }
    }

    // --- Ablageort-Foto ---

    fun onAblageortFotoAufnehmen() = starteKamera("ablageort")
    fun onAblageortFotoAufgenommen() = setzeFotoVorschau()
    fun onAblageortFotoWiederholen() {
        _uiState.value.tempFotoPfad?.let { fotoManager.loescheTempFoto(it) }
        starteKamera("ablageort")
    }

    fun onAblageortFotoBestaetigt() {
        val state = _uiState.value
        val tempPfad = state.tempFotoPfad ?: return
        val schrittId = state.schrittId
        viewModelScope.launch {
            val permanentPfad = fotoManager.bestaetigeFoto(tempPfad, "ablageort_$schrittId")
            if (permanentPfad != null) {
                repository.ablageortFotoBestaetigen(schrittId, permanentPfad)
                starteNeuenSchritt()
            }
        }
    }
}
