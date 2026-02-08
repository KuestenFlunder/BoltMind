package com.boltmind.app.feature.uebersicht

data class VorgangUiItem(
    val id: Long,
    val fahrzeugFotoPfad: String?,
    val auftragsnummer: String,
    val anzahlSchritte: Int,
    val erstelltAm: String,
)

sealed class NavigationsZiel {
    data class Demontage(val vorgangId: Long) : NavigationsZiel()
    data class Montage(val vorgangId: Long) : NavigationsZiel()
}

data class AuswahlDialogState(
    val vorgangId: Long,
    val auftragsnummer: String,
    val fahrzeugFotoPfad: String? = null,
)

data class LoeschenDialogState(
    val vorgangId: Long,
    val auftragsnummer: String,
)

data class UebersichtUiState(
    val vorgaenge: List<VorgangUiItem> = emptyList(),
    val isLoading: Boolean = false,
    val navigationsZiel: NavigationsZiel? = null,
    val auswahlDialog: AuswahlDialogState? = null,
    val loeschenDialog: LoeschenDialogState? = null,
)
