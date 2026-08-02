package com.boltmind.app

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import java.io.File

/**
 * Erzwingt die LOC-Grenze aus `CLAUDE.md`, Abschnitt "Verbotene Patterns".
 *
 * Die Grenze stand dort bisher nur als Satz, und die daneben genannten Zahlen
 * waren ueber ein halbes Jahr alt -- sie nannten ein `DemontageViewModel`, das
 * es nicht mehr gibt, waehrend `BrowserViewModel` unbemerkt von 265 auf 368
 * Zeilen wuchs (#126). Eine Regel, deren Verletzung niemand merkt, ist keine.
 *
 * Gemessen wird in **Rohzeilen wie `wc -l`**, Kommentare und Leerzeilen
 * eingeschlossen. Das ist die Vorgabe aus `CLAUDE.md` und bewusst streng: sonst
 * liesse sich das Limit durch Zusammenschieben von Zeilen unterlaufen.
 */
class ViewModelGroesseTest {

    private companion object {
        /** Die Regel fuer ein ViewModel mit einer Betriebsart. */
        const val GRENZE = 200

        /**
         * Die begruendete Ausnahme fuer [com.boltmind.app.feature.browser.BrowserViewModel].
         *
         * Sie bedient Demontage, Montage und Archiv, weil der Entwurf dafuer
         * **einen** Screen mit drei Betriebsarten vorsieht; `CODING_RULES.md`
         * haelt diese Entscheidung ausdruecklich fest. Zwei Verantwortungen sind
         * bereits ausgelagert (`BrowserFotoSteuerung`, `BrowserZeitsteuerung`) --
         * danach bleiben 234 Code- und 366 Rohzeilen. Der Rest sind
         * Zustandsuebergaenge; sie weiter aufzuteilen hiesse, den Zustand auf
         * mehrere Halter zu verteilen, und genau das soll die Regel verhindern.
         *
         * Die Ausnahme ist gedeckelt, nicht offen: wer die Grenze reisst, legt
         * eine neue Verantwortung frei und baut dafuer einen Mitarbeiter.
         */
        const val GRENZE_BROWSER = 380

        val AUSNAHMEN = mapOf("BrowserViewModel.kt" to GRENZE_BROWSER)
    }

    /** Gradle setzt das Arbeitsverzeichnis auf das Modul, die IDE auf das Wurzelprojekt. */
    private val quellen: File = listOf(File("src/main/java"), File("app/src/main/java"))
        .firstOrNull { it.isDirectory }
        ?: error("Quellverzeichnis nicht gefunden -- Arbeitsverzeichnis unerwartet")

    private val viewModels: List<File>
        get() = quellen.walkTopDown()
            .filter { it.isFile && it.name.endsWith("ViewModel.kt") }
            .sortedBy { it.name }
            .toList()

    @Test
    fun `es gibt ueberhaupt ViewModels zu messen`() {
        // Ohne diese Zusicherung waere ein kaputter Pfad ein gruener Test.
        assertTrue(viewModels.size >= 4, "Nur ${viewModels.size} ViewModels gefunden")
    }

    @TestFactory
    fun `kein ViewModel reisst seine Grenze`(): List<DynamicTest> = viewModels.map { datei ->
        DynamicTest.dynamicTest(datei.name) {
            val zeilen = datei.readLines().size
            val grenze = AUSNAHMEN[datei.name] ?: GRENZE
            assertTrue(zeilen <= grenze) {
                "${datei.name} hat $zeilen Zeilen, erlaubt sind $grenze. " +
                    if (AUSNAHMEN.containsKey(datei.name)) {
                        "Die Ausnahme ist gedeckelt: eine weitere Verantwortung gehoert " +
                            "in einen eigenen Mitarbeiter, nicht in eine hoehere Zahl."
                    } else {
                        "CLAUDE.md, Abschnitt \"Verbotene Patterns\": bei Erweiterung aufteilen."
                    }
            }
        }
    }
}
