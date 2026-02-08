package com.boltmind.app.feature.uebersicht

sealed class DatumAnzeige {
    data object Heute : DatumAnzeige()
    data object Gestern : DatumAnzeige()
    data class Formatiert(val text: String) : DatumAnzeige()
}

sealed class DauerAnzeige {
    data object WenigerAlsEineMinute : DauerAnzeige()
    data class Minuten(val minuten: Int) : DauerAnzeige()
    data class StundenMinuten(val stunden: Int, val minuten: Int) : DauerAnzeige()
}

data class VorgangUiItem(
    val id: Long,
    val fahrzeugFotoPfad: String?,
    val auftragsnummer: String,
    val anzahlSchritte: Int,
    val erstelltAm: DatumAnzeige,
)

data class ArchivVorgangUiItem(
    val id: Long,
    val fahrzeugFotoPfad: String?,
    val auftragsnummer: String,
    val anzahlSchritte: Int,
    val gesamtdauer: DauerAnzeige,
    val abschlussDatum: DatumAnzeige,
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
