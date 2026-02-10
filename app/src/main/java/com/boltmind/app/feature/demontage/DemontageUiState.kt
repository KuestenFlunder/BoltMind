package com.boltmind.app.feature.demontage

import android.net.Uri

/**
 * Haupt-States des Demontage-Flows (= aktive View).
 */
enum class DemontageFlowState {
    PREVIEW_BAUTEIL,
    ARBEITSPHASE,
    DIALOG,
    PREVIEW_ABLAGEORT
}

/**
 * Sub-Zustand der Preview-View (Bauteil oder Ablageort).
 */
enum class PreviewZustand {
    INITIAL,
    VORSCHAU
}

/**
 * UI-State fuer den gesamten Demontage-Flow.
 */
data class DemontageUiState(
    val flowState: DemontageFlowState = DemontageFlowState.PREVIEW_BAUTEIL,
    val previewZustand: PreviewZustand = PreviewZustand.INITIAL,
    val schrittNummer: Int = 1,
    val schrittId: Long = 0,
    val vorgangId: Long = 0,
    val bauteilFotoPfad: String? = null,
    val ablageortFotoPfad: String? = null,
    val kameraIntentAktiv: Boolean = false,
    val fotoUri: Uri? = null,
    val keineKameraApp: Boolean = false,
    val flowBeendet: Boolean = false
)
