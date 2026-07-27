package com.boltmind.app.data.model

import androidx.room.Embedded

/**
 * Projektion fuer die Uebersichtsliste: ein Vorgang mit der Anzahl seiner Schritte
 * und der aufsummierten gemessenen Zeit.
 *
 * [dauerMillis] kommt aus `zeit_messung` (F-005) und **nicht** aus den Workflow-
 * Timestamps des Schritts -- `gestartetAm`/`abgeschlossenAm` messen keine Arbeitszeit
 * (Governance: keine Dual-Purpose-Felder).
 */
data class ReparaturvorgangMitAnzahl(
    @Embedded val vorgang: Reparaturvorgang,
    val schrittAnzahl: Int,
    val dauerMillis: Long = 0L
)
