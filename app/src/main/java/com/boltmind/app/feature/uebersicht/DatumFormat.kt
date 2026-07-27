package com.boltmind.app.feature.uebersicht

import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.max
import kotlin.math.roundToLong

/**
 * Datums- und Dauerformat der Uebersichtsliste.
 *
 * Bewusst **ohne** Android-Abhaengigkeit und ohne `strings.xml`: die Formatierung
 * gehoert ins ViewModel und muss auf der JVM testbar sein. Die Wortlaute stehen
 * deshalb hier und nicht in den Ressourcen.
 *
 * Vier Stufen (Delta-Analyse K-09, ersetzt die dreistufige Regel aus
 * `docs/specs/F-001-uebersicht/uebersicht.md`):
 *
 * | Alter | Ausgabe |
 * |---|---|
 * | juenger als 60 Sekunden | `Gerade eben` |
 * | heute | `Heute, 08:12` |
 * | gestern | `Gestern, 15:40` |
 * | aelter | `01.05.2024` |
 *
 * Verglichen werden **Kalendertage in der lokalen Zeitzone**, nicht 24-Stunden-Abstaende.
 *
 * Im Archiv haengt [datumMitDauer] die gemessene Gesamtdauer an. Quelle ist
 * ausschliesslich `zeit_messung` (F-005) ueber
 * [com.boltmind.app.data.model.ReparaturvorgangMitAnzahl.dauerMillis]; liegt keine
 * Messung vor, entfaellt der Zusatz ersatzlos.
 */
object DatumFormat {

    private const val GERADE_EBEN = "Gerade eben"
    private const val HEUTE = "Heute"
    private const val GESTERN = "Gestern"

    /** `x.datum + ' · ' + fmtKurz(...)` -- Prototyp Z. 573. */
    private const val TRENNER = " · "
    private const val UHRZEIT_TRENNER = ", "
    private const val STUNDE = " h "
    private const val MINUTE = " min"

    /** Schwelle fuer "Gerade eben". Der Prototyp nennt keine; K-09 legt 60 s fest. */
    const val FRISCH_SEKUNDEN: Long = 60

    private val UHRZEIT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val KALENDER: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    /** Vier-Stufen-Datum eines Zeitpunkts, gemessen gegen [jetzt] in [zone]. */
    fun datum(zeitpunkt: Instant, jetzt: Instant, zone: ZoneId): String {
        if (Duration.between(zeitpunkt, jetzt).seconds < FRISCH_SEKUNDEN) return GERADE_EBEN

        val zeit = zeitpunkt.atZone(zone)
        val heute = jetzt.atZone(zone).toLocalDate()
        return when (zeit.toLocalDate()) {
            heute -> HEUTE + UHRZEIT_TRENNER + UHRZEIT.format(zeit)
            heute.minusDays(1) -> GESTERN + UHRZEIT_TRENNER + UHRZEIT.format(zeit)
            else -> KALENDER.format(zeit)
        }
    }

    /**
     * Datum mit angehaengter Gesamtdauer. Ist [dauerMillis] nicht positiv -- also
     * solange F-005 keine Messungen liefert -- bleibt es beim reinen Datum.
     */
    fun datumMitDauer(
        zeitpunkt: Instant,
        jetzt: Instant,
        zone: ZoneId,
        dauerMillis: Long
    ): String {
        val text = datum(zeitpunkt, jetzt, zone)
        return if (dauerMillis <= 0) text else text + TRENNER + dauer(dauerMillis)
    }

    /**
     * Kurzform der gemessenen Zeit, wie `fmtKurz` im Prototyp (Z. 483):
     * ab einer Stunde `1 h 26 min`, darunter auf volle Minuten gerundet `43 min`,
     * mindestens aber `1 min`.
     */
    fun dauer(millis: Long): String {
        val sekunden = millis / 1000
        val stunden = sekunden / 3600
        if (stunden > 0) {
            val minuten = (sekunden % 3600) / 60
            return "$stunden$STUNDE${zweistellig(minuten)}$MINUTE"
        }
        return "${max(1L, (sekunden / 60.0).roundToLong())}$MINUTE"
    }

    private fun zweistellig(wert: Long): String = wert.toString().padStart(2, '0')
}
