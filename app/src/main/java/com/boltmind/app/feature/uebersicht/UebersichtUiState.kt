package com.boltmind.app.feature.uebersicht

data class VorgangUiItem(
    val id: Long,
    val fahrzeugFotoPfad: String?,
    val auftragsnummer: String,
    val anzahlSchritte: Int,
    val erstelltAm: String,
)

data class UebersichtUiState(
    val vorgaenge: List<VorgangUiItem> = emptyList(),
    val isLoading: Boolean = false,
)
