package com.boltmind.app.feature.uebersicht

data class VorgangUiItem(
    val id: Long,
    val fahrzeugFotoPfad: String?,
    val auftragsnummer: String,
    val anzahlSchritte: Int,
    val erstelltAm: String,
)

data class ArchivVorgangUiItem(
    val id: Long,
    val fahrzeugFotoPfad: String?,
    val auftragsnummer: String,
    val anzahlSchritte: Int,
    val gesamtdauer: String,
    val abschlussDatum: String,
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
    val archivierteVorgaenge: List<ArchivVorgangUiItem> = emptyList(),
    val selectedTab: Int = 0,
    val isLoading: Boolean = false,
    val navigationsZiel: NavigationsZiel? = null,
    val auswahlDialog: AuswahlDialogState? = null,
    val loeschenDialog: LoeschenDialogState? = null,
)
