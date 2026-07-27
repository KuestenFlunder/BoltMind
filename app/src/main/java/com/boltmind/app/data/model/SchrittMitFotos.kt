package com.boltmind.app.data.model

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Ein [Schritt] mit allen zugehoerigen [SchrittFoto]s, nach `reihenfolge` sortiert.
 *
 * Das ist die Eingabestruktur des Schritt-Browsers (F-006). Ohne sie muesste jeder
 * Consumer die Fotos einzeln nachladen.
 *
 * Room sortiert `@Relation`-Listen nicht. Die Sortierung nach `reihenfolge`
 * uebernimmt [sortiert] bzw. die aufrufende Query-Schicht.
 *
 * Spec: docs/specs/F-006-schritt-browser/browser.md, Abschnitt "Technische Hinweise"
 */
data class SchrittMitFotos(
    @Embedded val schritt: Schritt,
    @Relation(parentColumn = "id", entityColumn = "schrittId")
    val fotos: List<SchrittFoto> = emptyList()
) {
    /** Kopie mit nach `reihenfolge` aufsteigend sortierten Fotos. */
    fun sortiert(): SchrittMitFotos =
        copy(fotos = fotos.sortedBy { it.reihenfolge })

    /**
     * Kategorie des Schritts fuer die Farbcodierung in der Thumbnail-Leiste.
     * Prioritaet: Ablageort vor Uebersicht vor Bauteil (Governance, "Foto-Label").
     */
    val kategorie: FotoKategorie
        get() = when {
            fotos.any { it.istAblageort } -> FotoKategorie.ABLAGEORT
            fotos.any { it.istUebersicht } -> FotoKategorie.UEBERSICHT
            fotos.any { it.istBauteil } -> FotoKategorie.BAUTEIL
            else -> FotoKategorie.OHNE
        }
}

/** Abgeleitete Kategorie eines Schritts, nur zur Darstellung. Nicht persistiert. */
enum class FotoKategorie {
    BAUTEIL,
    UEBERSICHT,
    ABLAGEORT,

    /** Der Schritt hat keine Fotos oder alle Label sind abgewaehlt. */
    OHNE
}
