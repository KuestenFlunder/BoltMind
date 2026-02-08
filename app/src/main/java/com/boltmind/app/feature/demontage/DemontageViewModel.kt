package com.boltmind.app.feature.demontage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boltmind.app.data.model.Schritt
import com.boltmind.app.data.repository.ReparaturRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant

sealed interface DemontageNavigationEvent {
    data object ZurueckZurUebersicht : DemontageNavigationEvent
}

class DemontageViewModel(
    private val repository: ReparaturRepository,
    private val vorgangId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(DemontageUiState())
    val uiState: StateFlow<DemontageUiState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<DemontageNavigationEvent>()
    val navigationEvent: SharedFlow<DemontageNavigationEvent> = _navigationEvent.asSharedFlow()

    private var schrittStartzeit: Instant? = null
    private var sequenzCounter: Int = 1
    private val belegteNummern: MutableSet<Int> = mutableSetOf()

    init {
        ladeVorgang()
    }

    private fun ladeVorgang() {
        viewModelScope.launch {
            val vorgang = repository.getVorgangById(vorgangId)
            if (vorgang == null) {
                _navigationEvent.emit(DemontageNavigationEvent.ZurueckZurUebersicht)
                return@launch
            }

            val anzahlSchritte = repository.getSchrittAnzahlEinmalig(vorgangId)
            val letzterSchritt = repository.getLetzerSchritt(vorgangId)
            belegteNummern.addAll(repository.getBelegteAblageortNummern(vorgangId))

            val naechsterSchritt = anzahlSchritte + 1
            sequenzCounter = anzahlSchritte + 1

            val naechsteFreie = naechsteFreieNummer(
                sequenzCounter, belegteNummern, vorgang.anzahlAblageorte
            ) ?: sequenzCounter

            schrittStartzeit = Instant.now()

            _uiState.update {
                it.copy(
                    fahrzeug = vorgang.fahrzeug,
                    beschreibung = vorgang.beschreibung,
                    aktuellerSchritt = naechsterSchritt,
                    vorgeschlageneAblageortNummer = naechsteFreie,
                    ablageortNummer = naechsteFreie.toString(),
                    anzahlAblageorte = vorgang.anzahlAblageorte,
                    letzteFotoPath = letzterSchritt?.fotoPath,
                    isLoading = false
                )
            }
        }
    }

    fun onFotoAufgenommen(fotoPath: String) {
        _uiState.update {
            it.copy(
                phase = DemontagePhase.ABLAGEORT_BESTAETIGUNG,
                aktuellesFotoPath = fotoPath,
                fehler = null
            )
        }
    }

    fun onFotoFehler(nachricht: String) {
        _uiState.update { it.copy(fehler = nachricht) }
    }

    fun onAblageortBestaetigt() {
        val state = _uiState.value
        val ablageort = state.ablageortNummer.trim()
        if (ablageort.isBlank()) return

        viewModelScope.launch {
            val schritt = Schritt(
                reparaturvorgangId = vorgangId,
                fotoPath = state.aktuellesFotoPath ?: return@launch,
                ablageortNummer = ablageort,
                reihenfolge = state.aktuellerSchritt,
                startedAt = schrittStartzeit,
                completedAt = Instant.now()
            )
            repository.schrittAnlegen(schritt)

            ablageort.toIntOrNull()?.let { belegteNummern.add(it) }
            sequenzCounter = (sequenzCounter % state.anzahlAblageorte) + 1

            val naechsteFreie = naechsteFreieNummer(
                sequenzCounter, belegteNummern, state.anzahlAblageorte
            )

            schrittStartzeit = Instant.now()

            if (naechsteFreie == null) {
                _uiState.update {
                    it.copy(
                        phase = DemontagePhase.KAMERA,
                        aktuellerSchritt = it.aktuellerSchritt + 1,
                        istAblageortAenderungAktiv = false,
                        letzteFotoPath = it.aktuellesFotoPath,
                        aktuellesFotoPath = null,
                        fehler = null,
                        zeigAlleBesetztDialog = true
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        phase = DemontagePhase.KAMERA,
                        aktuellerSchritt = it.aktuellerSchritt + 1,
                        vorgeschlageneAblageortNummer = naechsteFreie,
                        ablageortNummer = naechsteFreie.toString(),
                        istAblageortAenderungAktiv = false,
                        letzteFotoPath = it.aktuellesFotoPath,
                        aktuellesFotoPath = null,
                        fehler = null
                    )
                }
            }
        }
    }

    fun onAblageortAendern() {
        _uiState.update {
            it.copy(
                istAblageortAenderungAktiv = true,
                ablageortNummer = ""
            )
        }
    }

    fun onAblageortNummerGeaendert(nummer: String) {
        _uiState.update { it.copy(ablageortNummer = nummer) }
    }

    fun onDemontageBeenden() {
        viewModelScope.launch {
            _navigationEvent.emit(DemontageNavigationEvent.ZurueckZurUebersicht)
        }
    }

    fun onAlleBesetztBeenden() {
        viewModelScope.launch {
            _navigationEvent.emit(DemontageNavigationEvent.ZurueckZurUebersicht)
        }
    }

    fun onAlleBesetztErweitern() {
        val naechsteNummer = _uiState.value.anzahlAblageorte + belegteNummern.count { it > _uiState.value.anzahlAblageorte } + 1
        _uiState.update {
            it.copy(
                zeigAlleBesetztDialog = false,
                phase = DemontagePhase.KAMERA,
                vorgeschlageneAblageortNummer = naechsteNummer,
                ablageortNummer = naechsteNummer.toString()
            )
        }
    }

    companion object {
        fun naechsteFreieNummer(
            startBei: Int,
            belegte: Set<Int>,
            max: Int
        ): Int? {
            for (i in 0 until max) {
                val kandidat = ((startBei - 1 + i) % max) + 1
                if (kandidat !in belegte) return kandidat
            }
            return null
        }
    }
}
