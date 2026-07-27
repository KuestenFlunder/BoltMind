package com.boltmind.app.feature

import com.boltmind.app.feature.browser.Zeitformat
import com.boltmind.app.feature.uebersicht.DatumFormat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Locale

/**
 * Tests der beiden reinen Formatierer:
 *
 * - [Zeitformat] (`feature/browser/Zeitformat.kt`) — Timer, Summe, Archivdauer
 * - [DatumFormat] (`feature/uebersicht/DatumFormat.kt`) — Vier-Stufen-Datum nach
 *   `docs/specs/design-system.md`, Abschnitt "Bewusste Abweichungen", K-09
 *
 * Beide sind Android-frei und deshalb als gewoehnliche JVM-Tests pruefbar.
 * Alle Zeitpunkte sind feste [Instant]s in einer festen Zone — kein Test darf von
 * der Systemzeit oder der Systemzone abhaengen.
 */
class FormatierungTest {

    companion object {
        /** Sommerzeit-Zone (+02:00 im Juli, +01:00 im Januar) — deckt den Zonenversatz mit ab. */
        private val BERLIN: ZoneId = ZoneId.of("Europe/Berlin")

        private var vorherigeLocale: Locale = Locale.getDefault()

        /**
         * [Zeitformat] formatiert ueber `String.format`, das die **Default-Locale** benutzt.
         * Damit die Erwartungen nicht von der Maschine abhaengen, wird die Locale hier
         * festgenagelt und danach wiederhergestellt.
         */
        @JvmStatic
        @BeforeAll
        fun localeFestnageln() {
            vorherigeLocale = Locale.getDefault()
            Locale.setDefault(Locale.GERMANY)
        }

        @JvmStatic
        @AfterAll
        fun localeZuruecksetzen() {
            Locale.setDefault(vorherigeLocale)
        }

        /** `"2026-07-27T08:12"` → der zugehoerige Instant in [BERLIN]. */
        private fun berlin(lokal: String): Instant =
            LocalDateTime.parse(lokal).atZone(BERLIN).toInstant()
    }

    // ------------------------------------------------------------------ Zeitformat

    @Nested
    @DisplayName("Zeitformat.mmss — der laufende Timer")
    inner class ZeitformatMmss {

        @Test
        fun `null Sekunden erscheinen als 00 zu 00`() {
            assertEquals("00:00", Zeitformat.mmss(0))
        }

        @Test
        fun `unter einer Minute bleibt die Minutenstelle bei 00`() {
            assertEquals("00:01", Zeitformat.mmss(1))
            assertEquals("00:59", Zeitformat.mmss(59))
        }

        @Test
        fun `exakt eine Minute springt auf 01 zu 00`() {
            assertEquals("01:00", Zeitformat.mmss(60))
        }

        @Test
        fun `ab einer Stunde zaehlen die Minuten weiter statt umzubrechen`() {
            // Zusage im Doc-Kommentar: mm:ss kennt keine Stundenstelle.
            assertEquals("59:59", Zeitformat.mmss(3599))
            assertEquals("60:00", Zeitformat.mmss(3600))
            assertEquals("61:01", Zeitformat.mmss(3661))
        }

        @Test
        fun `mehr als 24 Stunden brechen ebenfalls nicht um`() {
            // 25 h 1 min 1 s — ein Vorgang, der ueber Nacht offen blieb.
            assertEquals("1501:01", Zeitformat.mmss(90_061))
        }

        @Test
        fun `negative Eingabe wird auf 00 zu 00 geklemmt`() {
            // Eine Uhr, die rueckwaerts gestellt wurde, darf kein "-1:-1" zeigen.
            assertEquals("00:00", Zeitformat.mmss(-1))
            assertEquals("00:00", Zeitformat.mmss(-90_061))
        }
    }

    @Nested
    @DisplayName("Zeitformat.lang — die ausgeschriebene Summe")
    inner class ZeitformatLang {

        @Test
        fun `unter einer Stunde erscheinen Minuten und Sekunden`() {
            assertEquals("0 min 00 s", Zeitformat.lang(0))
            assertEquals("0 min 59 s", Zeitformat.lang(59))
            assertEquals("1 min 00 s", Zeitformat.lang(60))
        }

        @Test
        fun `die letzte Sekunde vor der Stunde bleibt in Minuten`() {
            assertEquals("59 min 59 s", Zeitformat.lang(3599))
        }

        @Test
        fun `ab einer Stunde entfallen die Sekunden`() {
            assertEquals("1 h 00 min", Zeitformat.lang(3600))
            assertEquals("1 h 00 min", Zeitformat.lang(3659))
            assertEquals("1 h 01 min", Zeitformat.lang(3660))
        }

        @Test
        fun `mehr als 24 Stunden werden als Stundenzahl weitergezaehlt`() {
            // Keine Tages-Einheit — 25 h bleiben 25 h.
            assertEquals("25 h 01 min", Zeitformat.lang(90_061))
        }

        @Test
        fun `negative Eingabe wird auf null geklemmt`() {
            assertEquals("0 min 00 s", Zeitformat.lang(-5))
        }
    }

    @Nested
    @DisplayName("Zeitformat.kurz — die Dauer auf der Archivkarte")
    inner class ZeitformatKurz {

        @Test
        fun `zeigt nie 0 min — eine erfasste Arbeit ist mindestens eine Minute wert`() {
            assertEquals("1 min", Zeitformat.kurz(0))
            assertEquals("1 min", Zeitformat.kurz(1))
            assertEquals("1 min", Zeitformat.kurz(29))
        }

        @Test
        fun `rundet kaufmaennisch auf volle Minuten`() {
            assertEquals("1 min", Zeitformat.kurz(30))
            assertEquals("1 min", Zeitformat.kurz(89))
            assertEquals("2 min", Zeitformat.kurz(90))
        }

        @Test
        fun `negative Eingabe ergibt ebenfalls 1 min statt eines Minuszeichens`() {
            assertEquals("1 min", Zeitformat.kurz(-600))
        }

        @Test
        fun `ab einer Stunde erscheinen Stunden und zweistellige Minuten`() {
            assertEquals("1 h 00 min", Zeitformat.kurz(3600))
            assertEquals("1 h 01 min", Zeitformat.kurz(3661))
            assertEquals("2 h 30 min", Zeitformat.kurz(9000))
        }

        @Test
        fun `kurz vor der vollen Stunde rundet auf 60 min statt auf 1 h 00 min`() {
            // Festgehaltenes Verhalten der Rundung: die Stundenschwelle wird auf den
            // ungerundeten Sekunden geprueft, gerundet wird erst danach.
            assertEquals("60 min", Zeitformat.kurz(3599))
        }

        @Test
        fun `mehr als 24 Stunden bleiben in Stunden`() {
            assertEquals("25 h 01 min", Zeitformat.kurz(90_061))
        }
    }

    @Nested
    @DisplayName("Zeitformat.kurz und DatumFormat.dauer sind dieselbe Regel")
    inner class KurzformDoppelt {

        @Test
        fun `beide Kurzform-Implementierungen liefern fuer dieselbe Zeit dasselbe`() {
            // Die Regel "fmtKurz" existiert doppelt (einmal auf Sekunden, einmal auf Millis).
            // Dieser Test faengt ein Auseinanderlaufen der beiden Kopien ab.
            listOf(0L, 1L, 29L, 30L, 89L, 90L, 3599L, 3600L, 3661L, 9000L, 90_061L).forEach { s ->
                assertEquals(
                    Zeitformat.kurz(s),
                    DatumFormat.dauer(s * 1000),
                    "Kurzform weicht bei $s Sekunden ab"
                )
            }
        }
    }

    // ----------------------------------------------------------------- DatumFormat

    @Nested
    @DisplayName("K-09 Stufe 1 — juenger als 60 Sekunden: Gerade eben")
    inner class DatumGeradeEben {

        @Test
        fun `59 Sekunden alt ist Gerade eben`() {
            val jetzt = berlin("2026-07-27T08:12:00")
            val zeitpunkt = berlin("2026-07-27T08:11:01")
            assertEquals("Gerade eben", DatumFormat.datum(zeitpunkt, jetzt, BERLIN))
        }

        @Test
        fun `exakt 60 Sekunden faellt bereits auf die Uhrzeit`() {
            // FRISCH_SEKUNDEN ist eine Untergrenze mit "<", nicht "<=".
            val jetzt = berlin("2026-07-27T08:12:00")
            val zeitpunkt = berlin("2026-07-27T08:11:00")
            assertEquals("Heute, 08:11", DatumFormat.datum(zeitpunkt, jetzt, BERLIN))
        }

        @Test
        fun `61 Sekunden alt ist nicht mehr Gerade eben`() {
            val jetzt = berlin("2026-07-27T08:12:00")
            val zeitpunkt = berlin("2026-07-27T08:10:59")
            assertEquals("Heute, 08:10", DatumFormat.datum(zeitpunkt, jetzt, BERLIN))
        }

        @Test
        fun `Gerade eben schlaegt die Tagesgrenze kurz nach Mitternacht`() {
            // 40 Sekunden alt, aber kalendarisch schon "gestern" — die Frische gewinnt,
            // sonst stuende an einer eben angefassten Karte "Gestern, 23:59".
            val jetzt = berlin("2026-07-27T00:00:10")
            val zeitpunkt = berlin("2026-07-26T23:59:30")
            assertEquals("Gerade eben", DatumFormat.datum(zeitpunkt, jetzt, BERLIN))
        }

        @Test
        fun `ein Zeitpunkt in der Zukunft gilt als Gerade eben`() {
            // Festgehaltenes Verhalten bei rueckwaerts gestellter Uhr: negative Differenz
            // ist ebenfalls kleiner als 60 s. Kein Datum aus der Zukunft auf der Karte.
            val jetzt = berlin("2026-07-27T08:12:00")
            val zeitpunkt = berlin("2026-07-27T09:00:00")
            assertEquals("Gerade eben", DatumFormat.datum(zeitpunkt, jetzt, BERLIN))
        }
    }

    @Nested
    @DisplayName("K-09 Stufe 2 — heute: Heute, hh:mm")
    inner class DatumHeute {

        @Test
        fun `zeigt Heute mit zweistelliger Uhrzeit`() {
            val jetzt = berlin("2026-07-27T17:00:00")
            val zeitpunkt = berlin("2026-07-27T08:12:00")
            assertEquals("Heute, 08:12", DatumFormat.datum(zeitpunkt, jetzt, BERLIN))
        }

        @Test
        fun `kurz nach Mitternacht heisst es Heute und nicht Gestern`() {
            val jetzt = berlin("2026-07-27T06:30:00")
            val zeitpunkt = berlin("2026-07-27T00:01:00")
            assertEquals("Heute, 00:01", DatumFormat.datum(zeitpunkt, jetzt, BERLIN))
        }

        @Test
        fun `22 Stunden alt ist heute — verglichen werden Kalendertage keine 24 Stunden`() {
            val jetzt = berlin("2026-07-27T23:00:00")
            val zeitpunkt = berlin("2026-07-27T00:30:00")
            assertEquals("Heute, 00:30", DatumFormat.datum(zeitpunkt, jetzt, BERLIN))
        }

        @Test
        fun `die uebergebene Zone entscheidet ueber den Kalendertag`() {
            // Derselbe Instant: in Berlin (+02:00) faellt er auf den 27., in UTC auf den 26.
            val zeitpunkt = Instant.parse("2026-07-26T22:30:00Z")
            val jetzt = Instant.parse("2026-07-27T06:00:00Z")

            assertEquals("Heute, 00:30", DatumFormat.datum(zeitpunkt, jetzt, BERLIN))
            assertEquals("Gestern, 22:30", DatumFormat.datum(zeitpunkt, jetzt, ZoneId.of("UTC")))
        }
    }

    @Nested
    @DisplayName("K-09 Stufe 3 — gestern: Gestern, hh:mm")
    inner class DatumGestern {

        @Test
        fun `zeigt Gestern mit Uhrzeit`() {
            val jetzt = berlin("2026-07-27T08:00:00")
            val zeitpunkt = berlin("2026-07-26T15:40:00")
            assertEquals("Gestern, 15:40", DatumFormat.datum(zeitpunkt, jetzt, BERLIN))
        }

        @Test
        fun `zwei Stunden alt ueber Mitternacht ist gestern`() {
            val jetzt = berlin("2026-07-27T01:00:00")
            val zeitpunkt = berlin("2026-07-26T23:00:00")
            assertEquals("Gestern, 23:00", DatumFormat.datum(zeitpunkt, jetzt, BERLIN))
        }

        @Test
        fun `Silvester ist am Neujahrsmorgen Gestern`() {
            val jetzt = berlin("2026-01-01T09:00:00")
            val zeitpunkt = berlin("2025-12-31T22:00:00")
            assertEquals("Gestern, 22:00", DatumFormat.datum(zeitpunkt, jetzt, BERLIN))
        }
    }

    @Nested
    @DisplayName("K-09 Stufe 4 — aelter: TT.MM.JJJJ")
    inner class DatumAelter {

        @Test
        fun `vorgestern erscheint als Kalenderdatum ohne Uhrzeit`() {
            val jetzt = berlin("2026-07-27T08:00:00")
            val zeitpunkt = berlin("2026-07-25T16:20:00")
            assertEquals("25.07.2026", DatumFormat.datum(zeitpunkt, jetzt, BERLIN))
        }

        @Test
        fun `Tag und Monat werden zweistellig mit fuehrender Null gezeigt`() {
            // K-09: kein Monatsname, immer TT.MM.JJJJ — im Langzeitarchiv eindeutig.
            val jetzt = berlin("2026-07-27T08:00:00")
            val zeitpunkt = berlin("2024-05-01T07:05:00")
            assertEquals("01.05.2024", DatumFormat.datum(zeitpunkt, jetzt, BERLIN))
        }

        @Test
        fun `ueber den Jahreswechsel hinaus erscheint das alte Jahr`() {
            val jetzt = berlin("2026-01-01T09:00:00")
            val zeitpunkt = berlin("2025-12-30T18:45:00")
            assertEquals("30.12.2025", DatumFormat.datum(zeitpunkt, jetzt, BERLIN))
        }
    }

    @Nested
    @DisplayName("DatumFormat.dauer — Kurzform der gemessenen Zeit")
    inner class DatumFormatDauer {

        @Test
        fun `rundet auf volle Minuten und zeigt nie 0 min`() {
            assertEquals("1 min", DatumFormat.dauer(0))
            assertEquals("1 min", DatumFormat.dauer(999))
            assertEquals("1 min", DatumFormat.dauer(89_000))
            assertEquals("2 min", DatumFormat.dauer(90_000))
        }

        @Test
        fun `ab einer Stunde Stunden plus zweistellige Minuten`() {
            assertEquals("1 h 00 min", DatumFormat.dauer(3_600_000))
            assertEquals("1 h 26 min", DatumFormat.dauer(5_160_000))
            assertEquals("25 h 01 min", DatumFormat.dauer(90_061_000))
        }
    }

    @Nested
    @DisplayName("DatumFormat.datumMitDauer — die Archivkarte")
    inner class DatumMitDauer {

        @Test
        fun `haengt die gemessene Dauer mit Mittelpunkt an das Datum`() {
            val jetzt = berlin("2026-07-27T08:00:00")
            val zeitpunkt = berlin("2026-05-12T14:03:00")
            assertEquals(
                "12.05.2026 · 1 h 26 min",
                DatumFormat.datumMitDauer(zeitpunkt, jetzt, BERLIN, dauerMillis = 5_160_000)
            )
        }

        @Test
        fun `ohne Messung bleibt es beim reinen Datum`() {
            // Solange F-005 keine Messung liefert, entfaellt der Zusatz ersatzlos —
            // kein "· 0 min" und kein haengender Trenner.
            val jetzt = berlin("2026-07-27T08:00:00")
            val zeitpunkt = berlin("2026-05-12T14:03:00")
            assertEquals(
                "12.05.2026",
                DatumFormat.datumMitDauer(zeitpunkt, jetzt, BERLIN, dauerMillis = 0)
            )
        }

        @Test
        fun `negative Dauer wird wie keine Messung behandelt`() {
            val jetzt = berlin("2026-07-27T08:00:00")
            val zeitpunkt = berlin("2026-05-12T14:03:00")
            assertEquals(
                "12.05.2026",
                DatumFormat.datumMitDauer(zeitpunkt, jetzt, BERLIN, dauerMillis = -1)
            )
        }

        @Test
        fun `eine angefangene Minute erscheint als 1 min statt zu verschwinden`() {
            val jetzt = berlin("2026-07-27T08:00:00")
            val zeitpunkt = berlin("2026-05-12T14:03:00")
            assertEquals(
                "12.05.2026 · 1 min",
                DatumFormat.datumMitDauer(zeitpunkt, jetzt, BERLIN, dauerMillis = 1_000)
            )
        }

        @Test
        fun `die Dauer haengt auch an den Wortstufen`() {
            // Ein soeben archivierter Vorgang steht auf Stufe 1 — der Zusatz gilt trotzdem.
            val jetzt = berlin("2026-07-27T08:00:00")
            val zeitpunkt = berlin("2026-07-27T07:59:30")
            assertEquals(
                "Gerade eben · 5 min",
                DatumFormat.datumMitDauer(zeitpunkt, jetzt, BERLIN, dauerMillis = 300_000)
            )
        }
    }
}
