package com.boltmind.app.feature.neuervorgang

data class NeuerVorgangUiState(
    val fahrzeug: String = "",
    val auftragsnummer: String = "",
    val beschreibung: String = "",
    val anzahlAblageorte: String = "",
    val fehler: Map<Feld, String> = emptyMap(),
    val speichert: Boolean = false
)

enum class Feld {
    FAHRZEUG,
    AUFTRAGSNUMMER,
    BESCHREIBUNG,
    ANZAHL_ABLAGEORTE
}
