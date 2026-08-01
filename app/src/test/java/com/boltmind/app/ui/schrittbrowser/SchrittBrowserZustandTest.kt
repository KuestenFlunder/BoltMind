package com.boltmind.app.ui.schrittbrowser

import com.boltmind.app.data.model.FotoKategorie
import com.boltmind.app.data.model.Schritt
import com.boltmind.app.data.model.SchrittFoto
import com.boltmind.app.data.model.SchrittMitFotos
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant

/**
 * Die zustandslose Logik des Schritt-Browsers (F-006).
 *
 * `browser.md`, Abschnitt "Tests", verlangt sie ausdruecklich: Kategorie-Ableitung
 * und Index-Grenzfaelle sind reine Rechnung ohne Android-Abhaengigkeit und laufen
 * damit auf der JVM. Genau deshalb ist ihr Fehlen lange nicht aufgefallen -- sie
 * kosten nichts, und niemand vermisst sie, bis eine Randbedingung kippt.
 *
 * Die Randpruefung fuer Vor/Zurueck steht hier bewusst **nicht**: sie gehoert
 * seit dem 2026-07-31 dem Consumer und wird in `BrowserUiStateTest` geprueft
 * (#127).
 */
class SchrittBrowserZustandTest {

    private fun foto(
        id: Long,
        reihenfolge: Int = 0,
        bauteil: Boolean = false,
        uebersicht: Boolean = false,
        ablageort: Boolean = false
    ) = SchrittFoto(
        id = id,
        schrittId = 1,
        pfad = "/photos/$id.jpg",
        reihenfolge = reihenfolge,
        istBauteil = bauteil,
        istUebersicht = uebersicht,
        istAblageort = ablageort,
        aufgenommenAm = Instant.EPOCH
    )

    private fun schritt(nummer: Int, fotos: List<SchrittFoto> = emptyList()) = SchrittMitFotos(
        schritt = Schritt(
            id = nummer.toLong(),
            reparaturvorgangId = 1,
            schrittNummer = nummer,
            gestartetAm = Instant.EPOCH
        ),
        fotos = fotos
    )

    @Nested
    inner class `US-006_1 Schritte wechseln` {

        @Test
        fun `aktiverSchritt ist bei leerer Liste null statt einer Ausnahme`() {
            // Given: kein einziger Schritt -- der Archiv-Fall aus browser.md
            val zustand = SchrittBrowserZustand(schritte = emptyList())
            // When/Then: der Browser klemmt, statt zu fallen
            assertNull(zustand.aktiverSchritt)
            assertTrue(zustand.fotos.isEmpty())
            assertFalse(zustand.hatFotos)
        }

        @Test
        fun `ein Index ausserhalb des Bereichs liefert keinen Schritt`() {
            // Given: drei Schritte, der Consumer zeigt auf den siebten
            val zustand = SchrittBrowserZustand(
                schritte = listOf(schritt(1), schritt(2), schritt(3)),
                aktiverIndex = 7
            )
            // Then: wie der Leer-Zustand, kein Absturz (browser.md, Index-Grenzfaelle)
            assertNull(zustand.aktiverSchritt)
        }
    }

    @Nested
    inner class `US-006_2 Fotos eines Schritts durchblaettern` {

        @Test
        fun `fotoIndex klemmt auf eine geschrumpfte Fotoliste`() {
            // Given: der Consumer stand auf Foto 3, danach wurden Fotos entfernt
            val zustand = SchrittBrowserZustand(
                schritte = listOf(schritt(1, listOf(foto(1), foto(2, 1)))),
                aktivesFoto = 3
            )
            // Then: der letzte vorhandene Index, nicht 3
            assertEquals(1, zustand.fotoIndex)
            assertEquals(2L, zustand.sichtbaresFoto?.id)
        }

        @Test
        fun `fotoIndex bleibt bei einem Schritt ohne Fotos auf null`() {
            // Given: ein frisch eroeffneter Schritt -- waehrend der Arbeit normal
            val zustand = SchrittBrowserZustand(schritte = listOf(schritt(1)), aktivesFoto = 2)
            // Then: definiertes Ergebnis statt einer Ausnahme
            assertEquals(0, zustand.fotoIndex)
            assertNull(zustand.sichtbaresFoto)
        }
    }

    @Nested
    inner class `US-006_3 Kategorie eines Schritts` {

        @Test
        fun `Ablageort schlaegt Uebersicht und Bauteil`() {
            // Given: ein Schritt, dessen Fotos alle drei Label tragen
            val s = schritt(1, listOf(foto(1, bauteil = true), foto(2, 1, uebersicht = true), foto(3, 2, ablageort = true)))
            // Then: Prioritaet Ablageort > Uebersicht > Bauteil (governance.md)
            assertEquals(FotoKategorie.ABLAGEORT, s.kategorie)
        }

        @Test
        fun `Uebersicht schlaegt Bauteil`() {
            val s = schritt(1, listOf(foto(1, bauteil = true), foto(2, 1, uebersicht = true)))
            assertEquals(FotoKategorie.UEBERSICHT, s.kategorie)
        }

        @Test
        fun `nur Bauteil ergibt Bauteil`() {
            assertEquals(FotoKategorie.BAUTEIL, schritt(1, listOf(foto(1, bauteil = true))).kategorie)
        }

        @Test
        fun `ein Foto ohne jedes Label ergibt OHNE`() {
            // Given: alle drei Label abgewaehlt -- laut SchrittFoto ein gueltiger Zustand
            val s = schritt(1, listOf(foto(1)))
            // Then: eine eigene Kategorie, kein Rueckfall auf Bauteil
            assertEquals(FotoKategorie.OHNE, s.kategorie)
        }

        @Test
        fun `ein Schritt ohne Fotos ergibt OHNE`() {
            assertEquals(FotoKategorie.OHNE, schritt(1).kategorie)
        }
    }

    @Nested
    inner class `US-006_4 Label nur in der Demontage aenderbar` {

        @Test
        fun `nur BEARBEITBAR laesst die Label aendern`() {
            // Given/When/Then: F-004 und das Archiv zeigen die Label, aendern sie nicht
            assertTrue(BrowserBetriebsart.BEARBEITBAR.labelAenderbar)
            assertFalse(BrowserBetriebsart.LESEND_MIT_AKTIONEN.labelAenderbar)
            assertFalse(BrowserBetriebsart.NUR_LESEN.labelAenderbar)
        }
    }
}
