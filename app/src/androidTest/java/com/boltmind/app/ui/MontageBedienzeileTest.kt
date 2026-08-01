package com.boltmind.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.boltmind.app.R
import com.boltmind.app.data.model.Schritt
import com.boltmind.app.data.model.SchrittMitFotos
import com.boltmind.app.feature.browser.Bedienkreise
import com.boltmind.app.feature.browser.BrowserUiState
import com.boltmind.app.ui.navigation.BrowserModus
import com.boltmind.app.ui.theme.BoltMindDimensions
import com.boltmind.app.ui.theme.BoltMindTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

/**
 * Die Bedienzeile der Montage auf einem schmalen Geraet (#124).
 *
 * Ist jedes Teil eingebaut, stehen vier Rundkreise nebeneinander: RAUS (60),
 * ZURÜCK (86), ABSCHLUSS (86) und DRIN (124), dazu drei Abstaende von 8 -- in
 * Summe 380 dp plus 22 dp Endabstand. Auf einem 360-dp-Geraet passt das nicht.
 *
 * `Rundbutton` setzt `.size(...)`, nicht `requiredSize`. Eine `Row` misst ihre
 * Kinder der Reihe nach mit dem verbleibenden Platz, und der zuletzt gemessene,
 * groesste Kreis bekommt den Rest -- der Hauptknopf wurde zur Ellipse. Das
 * trifft ausgerechnet den wichtigsten Knopf des Screens, und Quality Goal 1 ist
 * die Bedienbarkeit unter Werkstatt-Bedingungen.
 *
 * Ausfuehren: ./gradlew connectedDebugAndroidTest
 */
@RunWith(AndroidJUnit4::class)
class MontageBedienzeileTest {

    @get:Rule
    val regel = createComposeRule()

    private fun text(id: Int): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(id)

    private fun montageZustand(alleDrin: Boolean) = BrowserUiState(
        laedt = false,
        modus = BrowserModus.MONTAGE,
        schritte = (1..3).map {
            SchrittMitFotos(
                schritt = Schritt(
                    id = it.toLong(),
                    reparaturvorgangId = 1,
                    schrittNummer = it,
                    eingebautBeiMontage = alleDrin,
                    gestartetAm = Instant.EPOCH
                )
            )
        },
        aktiverIndex = 1
    )

    private fun zeigeBei(breite: Int, alleDrin: Boolean) {
        regel.setContent {
            BoltMindTheme {
                Box(Modifier.width(breite.dp)) {
                    Bedienkreise(
                        uiState = montageZustand(alleDrin),
                        onNaechstesTeil = {},
                        onWeiteresFoto = {},
                        onWiederholen = {},
                        onZumOffenenSchritt = {},
                        onEingebaut = {},
                        onHaekchenAnfragen = {},
                        onZumAbschluss = {},
                        onVorherigerSchritt = {},
                        onNaechsterSchritt = {},
                        onFeierabendAnfragen = {}
                    )
                }
            }
        }
    }

    @Test
    fun aufEinemSchmalenGeraetBehaeltJederKreisSeineSollgroesse() {
        // Given: 360 dp -- das schmale Ende der gaengigen Geraete
        // When: jedes Teil ist eingebaut, der vierte Kreis steht in der Zeile
        zeigeBei(breite = 360, alleDrin = true)

        // Then: kein Kreis unterschreitet sein Mass aus Dimensions.kt
        regel.onNodeWithText(text(R.string.montage_raus))
            .assertWidthIsEqualTo(BoltMindDimensions.rundbuttonKlein)
        regel.onNodeWithText(text(R.string.browser_zurueck))
            .assertWidthIsEqualTo(BoltMindDimensions.rundbuttonMittel)
        regel.onNodeWithText(text(R.string.montage_zum_abschluss))
            .assertWidthIsEqualTo(BoltMindDimensions.rundbuttonMittel)
        regel.onNodeWithText(text(R.string.montage_drin))
            .assertWidthIsEqualTo(BoltMindDimensions.rundbuttonGross)
    }

    @Test
    fun aufEinemBreitenGeraetBleibtAllesWieBisher() {
        // Given: 411 dp -- die Breite des AVD boltmind36, auf dem nichts auffiel
        zeigeBei(breite = 411, alleDrin = true)

        // Then: dieselben Masse. Der Umbau darf den bisher heilen Fall nicht
        // veraendern.
        regel.onNodeWithText(text(R.string.montage_raus))
            .assertWidthIsEqualTo(BoltMindDimensions.rundbuttonKlein)
        regel.onNodeWithText(text(R.string.montage_drin))
            .assertWidthIsEqualTo(BoltMindDimensions.rundbuttonGross)
    }

    @Test
    fun ohneAbschlussKreisPasstDieZeileOhnehin() {
        // Given: noch nicht alles eingebaut -- drei Kreise, 300 dp
        zeigeBei(breite = 360, alleDrin = false)

        // Then: unveraendert
        regel.onNodeWithText(text(R.string.montage_raus))
            .assertWidthIsEqualTo(BoltMindDimensions.rundbuttonKlein)
        regel.onNodeWithText(text(R.string.montage_sitzt))
            .assertWidthIsEqualTo(BoltMindDimensions.rundbuttonGross)
    }
}
