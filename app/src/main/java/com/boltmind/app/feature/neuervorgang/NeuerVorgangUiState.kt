package com.boltmind.app.feature.neuervorgang

import androidx.compose.runtime.Immutable

/**
 * Ein offener Kamera-Auftrag.
 *
 * Das ViewModel legt die Zieldatei an und reicht sie hier hoch; der Screen haengt
 * seinen Launcher daran. [nummer] zaehlt mit, damit zwei aufeinanderfolgende
 * Auftraege auch dann unterscheidbar sind, wenn sie in dieselbe Millisekunde
 * fallen und deshalb denselben Dateinamen tragen wuerden.
 */
@Immutable
data class KameraAuftrag(
    val nummer: Int,
    val zielPfad: String
)

/**
 * Zustand des Anlage-Screens (F-002).
 *
 * Der Flow beginnt mit dem Foto: solange [fahrzeugFotoPfad] leer ist und ein
 * [kameraAuftrag] laeuft, wartet der Screen auf die System-Kamera.
 *
 * [verlassen] und [gestarteterVorgangId] sind Signale nach oben. Der Screen
 * navigiert nicht selbst -- die Route wertet sie aus.
 */
@Immutable
data class NeuerVorgangUiState(
    /** Pfad des aufgenommenen Fahrzeugfotos unter `photos/`. */
    val fahrzeugFotoPfad: String? = null,

    val auftragsnummer: String = "",

    /** Bildet `Reparaturvorgang.beschreibung` ab -- fakultativ. */
    val beschreibung: String = "",

    /** Fehlerzeile unter dem Nummernfeld. Erst nach einem Versuch, nicht beim Tippen. */
    val nummerFehlt: Boolean = false,

    /** Nicht null, solange eine Aufnahme aussteht. */
    val kameraAuftrag: KameraAuftrag? = null,

    /** Der Anlage-Flow ist verworfen -- zurueck zur Uebersicht, nichts angelegt. */
    val verlassen: Boolean = false,

    /** Der Vorgang steht in der Datenbank; Schritt 1 ist angelegt. */
    val gestarteterVorgangId: Long? = null
)
