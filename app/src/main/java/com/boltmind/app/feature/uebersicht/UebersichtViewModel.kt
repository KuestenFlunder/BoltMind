package com.boltmind.app.feature.uebersicht

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boltmind.app.data.model.Schritt
import com.boltmind.app.data.repository.ReparaturRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class UebersichtViewModel(
    private val repository: ReparaturRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UebersichtUiState())
    val uiState: StateFlow<UebersichtUiState> = _uiState.asStateFlow()

    init {
        ladeOffeneVorgaenge()
        ladeArchivierteVorgaenge()
    }

    private fun ladeOffeneVorgaenge() {
        viewModelScope.launch {
            repository.beobachteOffeneVorgaengeMitAnzahl().collect { vorgaengeMitAnzahl ->
                val items = vorgaengeMitAnzahl.map { item ->
                    VorgangUiItem(
                        id = item.vorgang.id,
                        fahrzeugFotoPfad = item.vorgang.fahrzeugFotoPfad,
                        auftragsnummer = item.vorgang.auftragsnummer,
                        anzahlSchritte = item.schrittAnzahl,
                        erstelltAm = formatiereDatum(item.vorgang.erstelltAm),
                    )
                }
                _uiState.update { it.copy(vorgaenge = items, isLoading = false) }
            }
        }
    }

    private fun ladeArchivierteVorgaenge() {
        viewModelScope.launch {
            repository.beobachteArchivierteVorgaengeMitAnzahl().collect { vorgaengeMitAnzahl ->
                val archivItems = vorgaengeMitAnzahl.map { item ->
                    val schritte = repository.holeSchritte(item.vorgang.id)
                    ArchivVorgangUiItem(
                        id = item.vorgang.id,
                        fahrzeugFotoPfad = item.vorgang.fahrzeugFotoPfad,
                        auftragsnummer = item.vorgang.auftragsnummer,
                        anzahlSchritte = item.schrittAnzahl,
                        gesamtdauer = formatiereGesamtdauer(schritte),
                        abschlussDatum = formatiereDatum(item.vorgang.aktualisiertAm),
                    )
                }
                _uiState.update { it.copy(archivierteVorgaenge = archivItems) }
            }
        }
    }

    fun onTabGewaehlt(tabIndex: Int) {
        _uiState.update { it.copy(selectedTab = tabIndex) }
    }

    fun onVorgangGetippt(vorgangId: Long) {
        val vorgangUiItem = _uiState.value.vorgaenge.find { it.id == vorgangId } ?: return
        if (vorgangUiItem.anzahlSchritte == 0) {
            _uiState.update {
                it.copy(navigationsZiel = NavigationsZiel.Demontage(vorgangId))
            }
        } else {
            viewModelScope.launch {
                val vorgang = repository.findVorgangById(vorgangId)
                if (vorgang != null) {
                    _uiState.update {
                        it.copy(
                            auswahlDialog = AuswahlDialogState(
                                vorgangId = vorgang.id,
                                auftragsnummer = vorgang.auftragsnummer,
                                fahrzeugFotoPfad = vorgang.fahrzeugFotoPfad,
                            ),
                        )
                    }
                }
            }
        }
    }

    fun onWeiterDemontierenGewaehlt() {
        val dialog = _uiState.value.auswahlDialog ?: return
        _uiState.update {
            it.copy(
                navigationsZiel = NavigationsZiel.Demontage(dialog.vorgangId),
                auswahlDialog = null,
            )
        }
    }

    fun onMontageStartenGewaehlt() {
        val dialog = _uiState.value.auswahlDialog ?: return
        _uiState.update {
            it.copy(
                navigationsZiel = NavigationsZiel.Montage(dialog.vorgangId),
                auswahlDialog = null,
            )
        }
    }

    fun onNavigationAbgeschlossen() {
        _uiState.update { it.copy(navigationsZiel = null) }
    }

    fun onDialogVerworfen() {
        _uiState.update { it.copy(auswahlDialog = null) }
    }

    fun onLoeschenAngefragt(vorgangId: Long) {
        val vorgangUiItem = _uiState.value.vorgaenge.find { it.id == vorgangId } ?: return
        _uiState.update {
            it.copy(
                loeschenDialog = LoeschenDialogState(
                    vorgangId = vorgangUiItem.id,
                    auftragsnummer = vorgangUiItem.auftragsnummer,
                ),
            )
        }
    }

    fun onLoeschenBestaetigt() {
        val dialog = _uiState.value.loeschenDialog ?: return
        viewModelScope.launch {
            repository.loescheVorgang(dialog.vorgangId)
            _uiState.update { it.copy(loeschenDialog = null) }
        }
    }

    fun onLoeschenAbgebrochen() {
        _uiState.update { it.copy(loeschenDialog = null) }
    }

    internal fun formatiereDatum(instant: Instant): DatumAnzeige {
        val datum = instant.atZone(ZoneId.systemDefault()).toLocalDate()
        val heute = LocalDate.now()
        return when {
            datum == heute -> DatumAnzeige.Heute
            datum == heute.minusDays(1) -> DatumAnzeige.Gestern
            else -> DatumAnzeige.Formatiert(datum.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))
        }
    }

    internal fun formatiereGesamtdauer(schritte: List<Schritt>): DauerAnzeige {
        val gesamtSekunden = schritte.sumOf { schritt ->
            val ende = schritt.abgeschlossenAm ?: return@sumOf 0L
            Duration.between(schritt.gestartetAm, ende).seconds
        }
        val minuten = (gesamtSekunden / 60).toInt()
        val stunden = minuten / 60
        val restMinuten = minuten % 60
        return when {
            minuten < 1 -> DauerAnzeige.WenigerAlsEineMinute
            stunden < 1 -> DauerAnzeige.Minuten(minuten)
            else -> DauerAnzeige.StundenMinuten(stunden, restMinuten)
        }
    }
}
