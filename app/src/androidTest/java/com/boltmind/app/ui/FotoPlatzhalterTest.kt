package com.boltmind.app.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.boltmind.app.R
import com.boltmind.app.data.model.SchrittFoto
import com.boltmind.app.ui.schrittbrowser.FotoKarussell
import com.boltmind.app.ui.theme.BoltMindTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.time.Instant

/**
 * Belegt die Trennung der beiden Faelle aus F-006.
 *
 * `governance.md` verlangt bei einer **fehlenden Datei** ein Platzhalter-Bild
 * (US-006.8). Ein Schritt **ohne Fotos** (US-006.9) ist etwas voellig anderes --
 * waehrend der Arbeit der Normalfall. `schritt-ansicht.md` besteht ausdruecklich
 * auf der Trennung, und genau die wird hier gepruerft: die beiden Zustaende
 * duerfen nie denselben Text zeigen.
 *
 * Getestet wird `FotoKarussell` einzeln, nicht der ganze Browser. Der haelt zwei
 * Endlos-Animationen, gegen die Compose bis zum Timeout auf Ruhe wartet -- siehe
 * `docs/CODING_RULES.md`, Abschnitt "Falle: Endlos-Animationen".
 *
 * Ausfuehren: ./gradlew connectedDebugAndroidTest
 */
@RunWith(AndroidJUnit4::class)
class FotoPlatzhalterTest {

    @get:Rule
    val regel = createComposeRule()

    private val kontext = InstrumentationRegistry.getInstrumentation().targetContext

    private fun text(id: Int): String = kontext.getString(id)

    private fun foto(pfad: String) = SchrittFoto(
        id = 1,
        schrittId = 1,
        pfad = pfad,
        reihenfolge = 0,
        aufgenommenAm = Instant.EPOCH
    )

    private fun zeigeKarussell(fotos: List<SchrittFoto>) {
        regel.setContent {
            BoltMindTheme {
                FotoKarussell(
                    fotos = fotos,
                    aktuellesFoto = 0,
                    schrittNummer = 7,
                    modifier = Modifier.fillMaxSize(),
                    onFotoGewaehlt = {},
                    onFotoGetippt = {}
                )
            }
        }
    }

    @Test
    fun eineFehlendeDateiZeigtDenPlatzhalter() {
        // Given: eine Foto-Zeile, deren Datei es nicht gibt -- etwa nach einem
        // Backup, das die Datenbank mitnahm, aber nicht den Bilderordner
        val nirgends = File(kontext.filesDir, "photos/gibt_es_nicht.jpg").absolutePath

        // When: das Karussell zeigt sie
        zeigeKarussell(listOf(foto(nirgends)))

        // Then: der Platzhalter erscheint. Coil laedt asynchron, deshalb warten
        // statt sofort behaupten.
        regel.waitUntil(10_000) {
            regel.onAllNodes(
                hasContentDescription(text(R.string.browser_foto_fehlt_beschreibung)),
                useUnmergedTree = true
            ).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun eineLeereDateiGiltEbenfallsAlsFehlend() {
        // Given: eine 0-Byte-Huelle, wie FotoManager sie vor dem Kamerastart
        // anlegt. Bleibt sie nach einem Abbruch mitsamt DB-Zeile stehen, ist das
        // ein kaputtes Foto -- kein fehlendes und kein gueltiges.
        val huelle = File(kontext.cacheDir, "leer_${System.currentTimeMillis()}.jpg")
        huelle.parentFile?.mkdirs()
        huelle.createNewFile()

        // When: das Karussell zeigt sie
        zeigeKarussell(listOf(foto(huelle.absolutePath)))

        // Then: derselbe Platzhalter
        regel.waitUntil(10_000) {
            regel.onAllNodes(
                hasContentDescription(text(R.string.browser_foto_fehlt_beschreibung)),
                useUnmergedTree = true
            ).fetchSemanticsNodes().isNotEmpty()
        }
        huelle.delete()
    }

    @Test
    fun einSchrittOhneFotosZeigtEtwasAnderesAlsEineFehlendeDatei() {
        // Given/When: gar keine Fotos
        zeigeKarussell(emptyList())

        // Then: der Leer-Zustand, NICHT der Platzhalter. Wuerden beide Faelle
        // denselben Text zeigen, koennte der Mechaniker "noch nicht
        // fotografiert" nicht von "Foto verloren" unterscheiden.
        regel.onAllNodes(hasText(text(R.string.browser_keine_fotos)), useUnmergedTree = true)
            .fetchSemanticsNodes()
            .isNotEmpty()
            .let { require(it) { "Der Leer-Zustand fehlt." } }

        val platzhalterDa = regel.onAllNodes(
            hasContentDescription(text(R.string.browser_foto_fehlt_beschreibung)),
            useUnmergedTree = true
        ).fetchSemanticsNodes().isNotEmpty()
        require(!platzhalterDa) {
            "Ein Schritt ohne Fotos darf nicht als fehlende Datei erscheinen."
        }
    }
}
