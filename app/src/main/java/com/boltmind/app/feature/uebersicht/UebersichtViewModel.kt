package com.boltmind.app.feature.uebersicht

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boltmind.app.data.repository.ReparaturRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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
    }

    private fun ladeOffeneVorgaenge() {
        viewModelScope.launch {
            repository.beobachteOffeneVorgaenge().collect { vorgaenge ->
                val items = vorgaenge.map { vorgang ->
                    val anzahl = repository.zaehleSchritte(vorgang.id)
                    VorgangUiItem(
                        id = vorgang.id,
                        fahrzeugFotoPfad = vorgang.fahrzeugFotoPfad,
                        auftragsnummer = vorgang.auftragsnummer,
                        anzahlSchritte = anzahl,
                        erstelltAm = formatiereDatum(vorgang.erstelltAm),
                    )
                }
                _uiState.value = UebersichtUiState(vorgaenge = items, isLoading = false)
            }
        }
    }

    internal fun formatiereDatum(instant: Instant): String {
        val datum = instant.atZone(ZoneId.systemDefault()).toLocalDate()
        val heute = LocalDate.now()
        return when {
            datum == heute -> "Heute"
            datum == heute.minusDays(1) -> "Gestern"
            else -> datum.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        }
    }
}
