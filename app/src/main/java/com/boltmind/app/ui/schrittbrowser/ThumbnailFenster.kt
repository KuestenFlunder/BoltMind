package com.boltmind.app.ui.schrittbrowser

/**
 * Der sichtbare Ausschnitt der Thumbnail-Leiste.
 *
 * [ueberOben] und [ueberUnten] sind die Zahlen der beiden Ueberlaufzaehler.
 * Zusammen mit [sichtbar] ergeben sie immer genau die Gesamtzahl der Schritte --
 * kein Schritt faellt unter den Tisch, keiner wird doppelt gezaehlt.
 */
data class ThumbnailFenster(
    val start: Int,
    val sichtbar: Int,
    val ueberOben: Int,
    val ueberUnten: Int
)

/**
 * Schiebt das Fenster so, dass der aktive Schritt moeglichst mittig liegt, ohne
 * ueber die Liste hinauszulaufen (US-006.1).
 *
 * Bewusst eine reine Funktion ohne Compose: [ThumbnailLeiste] misst nur noch,
 * wie viele Kacheln in die Hoehe passen, und reicht das als [hoechstens] herein.
 * Die Rechnung selbst ist damit auf der JVM pruefbar (#119) -- vorher stand sie
 * inline im Composable und war nur ueber einen Geraetetest erreichbar.
 *
 * @param anzahl Zahl der Schritte insgesamt.
 * @param aktiverIndex Der betrachtete Schritt. Werte ausserhalb der Liste werden
 *   geklemmt; der Browser korrigiert den Zustand des Consumers nie.
 * @param hoechstens Wie viele Kacheln hoechstens gezeigt werden duerfen.
 */
fun thumbnailFenster(anzahl: Int, aktiverIndex: Int, hoechstens: Int): ThumbnailFenster {
    if (anzahl <= 0 || hoechstens <= 0) return ThumbnailFenster(0, 0, 0, 0)

    val sichtbar = minOf(hoechstens, anzahl)
    val mitte = aktiverIndex.coerceIn(0, anzahl - 1) - (sichtbar - 1) / 2
    val start = mitte.coerceIn(0, anzahl - sichtbar)

    return ThumbnailFenster(
        start = start,
        sichtbar = sichtbar,
        ueberOben = start,
        ueberUnten = anzahl - start - sichtbar
    )
}
