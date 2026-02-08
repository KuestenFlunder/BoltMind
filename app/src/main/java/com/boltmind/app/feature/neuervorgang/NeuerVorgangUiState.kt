package com.boltmind.app.feature.neuervorgang

sealed class NeuerVorgangSchritt {
    data object Kamera : NeuerVorgangSchritt()
    data object Formular : NeuerVorgangSchritt()
}

data class NeuerVorgangUiState(
    val schritt: NeuerVorgangSchritt = NeuerVorgangSchritt.Kamera,
    val fotoPfad: String? = null,
    val auftragsnummer: String = "",
    val beschreibung: String = "",
    val validierungsFehler: String? = null,
    val isSpeichernd: Boolean = false,
    val erstellterVorgangId: Long? = null,
)
