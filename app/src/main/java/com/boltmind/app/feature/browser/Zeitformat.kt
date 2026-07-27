package com.boltmind.app.feature.browser

/**
 * Die drei Zeitformate des Entwurfs. Reine Funktionen ohne Android-Abhaengigkeit,
 * damit sie als gewoehnliche Unit-Tests pruefbar sind.
 */
object Zeitformat {

    /** mm:ss -- der laufende Timer. Ab einer Stunde zaehlen die Minuten weiter. */
    fun mmss(sekunden: Long): String {
        val s = sekunden.coerceAtLeast(0)
        return "%02d:%02d".format(s / 60, s % 60)
    }

    /**
     * Ausgeschrieben -- die Summe in der Statuszeile und im Abschluss.
     * Unter einer Stunde mit Sekunden, darueber nur Stunden und Minuten.
     */
    fun lang(sekunden: Long): String {
        val s = sekunden.coerceAtLeast(0)
        val stunden = s / 3600
        val minuten = (s % 3600) / 60
        return if (stunden > 0) "$stunden h %02d min".format(minuten)
        else "$minuten min %02d s".format(s % 60)
    }

    /**
     * Knapp -- die Dauer auf der Archivkarte. Rundet auf Minuten und zeigt nie
     * "0 min", weil eine erfasste Arbeit immer mindestens eine Minute wert ist.
     */
    fun kurz(sekunden: Long): String {
        val s = sekunden.coerceAtLeast(0)
        val stunden = s / 3600
        return if (stunden > 0) "$stunden h %02d min".format((s % 3600) / 60)
        else "${maxOf(1L, Math.round(s / 60.0))} min"
    }
}
