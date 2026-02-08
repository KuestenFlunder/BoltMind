package com.boltmind.app.feature.demontage

data class DemontageUiState(
    val phase: DemontagePhase = DemontagePhase.KAMERA,
    val fahrzeug: String = "",
    val beschreibung: String = "",
    val aktuellerSchritt: Int = 1,
    val vorgeschlageneAblageortNummer: Int = 1,
    val ablageortNummer: String = "1",
    val istAblageortAenderungAktiv: Boolean = false,
    val letzteFotoPath: String? = null,
    val aktuellesFotoPath: String? = null,
    val anzahlAblageorte: Int = 0,
    val fehler: String? = null,
    val isLoading: Boolean = true,
    val zeigAlleBesetztDialog: Boolean = false
)

enum class DemontagePhase {
    KAMERA,
    ABLAGEORT_BESTAETIGUNG
}
