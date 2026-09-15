package com.boltmind.app.ui.schrittbrowser

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

/**
 * Das wandernde Fenster der Thumbnail-Leiste (US-006.1).
 *
 * Die Rechnung stand bis #119 inline in `ThumbnailLeiste.kt` und war nur ueber
 * einen UI-Test erreichbar -- fuer eine reine Indexrechnung ein unverhaeltnis-
 * maessiger Preis. Sie ist deshalb als reine Funktion herausgezogen.
 */
class ThumbnailFensterTest {

    @Nested
    inner class `US-006_1 Der aktive Schritt liegt moeglichst mittig` {

        @Test
        fun `mittendrin steht der aktive Schritt in der Mitte des Fensters`() {
            // Given: 20 Schritte, vier Kacheln passen, der zehnte ist aktiv
            val f = thumbnailFenster(anzahl = 20, aktiverIndex = 10, hoechstens = 4)
            // Then: das Fenster liegt um den aktiven herum
            assertEquals(4, f.sichtbar)
            assertTrue(10 in f.start until f.start + f.sichtbar)
            assertEquals(9, f.start)
        }

        @ParameterizedTest
        @ValueSource(ints = [0, 1, 5, 9, 18, 19])
        fun `der aktive Schritt liegt immer im Fenster`(aktiv: Int) {
            val f = thumbnailFenster(anzahl = 20, aktiverIndex = aktiv, hoechstens = 4)
            assertTrue(
                aktiv in f.start until f.start + f.sichtbar,
                "Index $aktiv liegt nicht im Fenster ${f.start}..${f.start + f.sichtbar - 1}"
            )
        }
    }

    @Nested
    inner class `US-006_1 Das Fenster laeuft an den Raendern nicht ueber` {

        @Test
        fun `am oberen Rand beginnt das Fenster bei null`() {
            val f = thumbnailFenster(anzahl = 20, aktiverIndex = 0, hoechstens = 4)
            assertEquals(0, f.start)
            assertEquals(0, f.ueberOben)
            assertEquals(16, f.ueberUnten)
        }

        @Test
        fun `am unteren Rand endet das Fenster beim letzten Schritt`() {
            val f = thumbnailFenster(anzahl = 20, aktiverIndex = 19, hoechstens = 4)
            assertEquals(16, f.start)
            assertEquals(16, f.ueberOben)
            assertEquals(0, f.ueberUnten)
        }

        @Test
        fun `weniger Schritte als Plaetze fuellen das Fenster nur teilweise`() {
            // Given: drei Schritte, vier Plaetze
            val f = thumbnailFenster(anzahl = 3, aktiverIndex = 1, hoechstens = 4)
            // Then: kein Ueberlauf in beide Richtungen
            assertEquals(0, f.start)
            assertEquals(3, f.sichtbar)
            assertEquals(0, f.ueberOben)
            assertEquals(0, f.ueberUnten)
        }

        @Test
        fun `genau ein Schritt ergibt genau eine Kachel`() {
            val f = thumbnailFenster(anzahl = 1, aktiverIndex = 0, hoechstens = 4)
            assertEquals(0, f.start)
            assertEquals(1, f.sichtbar)
            assertEquals(0, f.ueberOben)
            assertEquals(0, f.ueberUnten)
        }

        @Test
        fun `eine leere Liste ergibt ein leeres Fenster ohne Ueberlauf`() {
            val f = thumbnailFenster(anzahl = 0, aktiverIndex = 0, hoechstens = 4)
            assertEquals(0, f.sichtbar)
            assertEquals(0, f.ueberOben)
            assertEquals(0, f.ueberUnten)
        }

        @Test
        fun `ein Index ausserhalb der Liste sprengt das Fenster nicht`() {
            // Given: der Consumer zeigt auf einen Schritt, den es nicht gibt
            val f = thumbnailFenster(anzahl = 5, aktiverIndex = 99, hoechstens = 4)
            // Then: das Fenster bleibt innerhalb der Liste
            assertTrue(f.start >= 0)
            assertTrue(f.start + f.sichtbar <= 5)
        }
    }

    @Nested
    inner class `US-006_1 Die Ueberlaufzaehler nennen die richtige Zahl` {

        @Test
        fun `oben und unten zusammen ergeben die verdeckten Schritte`() {
            // Given: 20 Schritte, vier sichtbar
            val f = thumbnailFenster(anzahl = 20, aktiverIndex = 10, hoechstens = 4)
            // Then: kein Schritt faellt unter den Tisch und keiner wird doppelt gezaehlt
            assertEquals(20 - f.sichtbar, f.ueberOben + f.ueberUnten)
        }

        @Test
        fun `kein Zaehler wird negativ wenn das Fenster groesser ist als die Liste`() {
            val f = thumbnailFenster(anzahl = 2, aktiverIndex = 0, hoechstens = 4)
            assertTrue(f.ueberOben >= 0)
            assertTrue(f.ueberUnten >= 0)
        }
    }
}
