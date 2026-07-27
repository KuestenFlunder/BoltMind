package com.boltmind.app.ui

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.boltmind.app.ui.components.BoltText
import com.boltmind.app.ui.components.GlasAktion
import com.boltmind.app.ui.theme.BoltMindDimensions
import com.boltmind.app.ui.theme.BoltMindTheme
import com.boltmind.app.ui.theme.BoltTextWeiss
import com.boltmind.app.ui.theme.BoltTypo
import com.boltmind.app.ui.theme.GlasRezepte
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Belegt, dass die 56dp-Mindesthoehe aus `governance.md` wirklich greift.
 *
 * Der Test existiert, weil das Projekt diesen Fehler schon einmal hatte: der
 * frueher vorhandene `BoltMindButton` schrieb `modifier.heightIn(min = ...)` --
 * also das Minimum **hinter** den von aussen uebergebenen Modifier. Size-Modifier
 * wirken ueber Constraints von aussen nach innen, ein aeusseres `.height(40.dp)`
 * gewann damit und die Mindesthoehe war wirkungslos. Der Fehler war nur durch
 * Lesen zu finden, nie durch Ausprobieren.
 *
 * Ausfuehren: ./gradlew connectedDebugAndroidTest  (Emulator oder Geraet noetig)
 */
@RunWith(AndroidJUnit4::class)
class MindesthoeheTest {

    @get:Rule
    val regel = createComposeRule()

    private companion object {
        const val MARKE = "aktion"
    }

    private fun zeigeAktion(aeusserer: Modifier) {
        regel.setContent {
            BoltMindTheme {
                GlasAktion(
                    rezept = GlasRezepte.orangeFlach,
                    hoehe = BoltMindDimensions.touchTargetMin,
                    eckRadius = BoltMindDimensions.radiusStandard,
                    modifier = aeusserer.testTag(MARKE),
                    onKlick = {}
                ) {
                    BoltText("LOS GEHT'S", BoltTypo.aktionSheet, BoltTextWeiss)
                }
            }
        }
    }

    @Test
    fun einAeusseresHoehenmassDruecktDieAktionNichtUnterDasMinimum() {
        // Given/When: der Aufrufer versucht, die Aktion auf 40dp zu zwingen
        zeigeAktion(Modifier.height(40.dp))

        // Then: die Governance gewinnt
        regel.onNodeWithTag(MARKE)
            .assertHeightIsAtLeast(BoltMindDimensions.touchTargetMin)
    }

    @Test
    fun ohneAeusseresMassMisstDieAktionGenauDasMinimum() {
        // Given/When: der Normalfall
        zeigeAktion(Modifier)

        // Then: keine unerwartete Aufblaehung
        regel.onNodeWithTag(MARKE)
            .assertHeightIsEqualTo(BoltMindDimensions.touchTargetMin)
    }

    @Test
    fun einAeusseresPaddingSchrumpftDieGlasflaecheNicht() {
        // Given/When: NeuerVorgangScreen gibt "NEU KNIPSEN" ein Padding mit
        zeigeAktion(Modifier.padding(10.dp))

        // Then: die Glasflaeche selbst behaelt ihre 56dp, das Padding kommt oben
        // drauf. Wuerde man das Minimum blosss nach vorne sortieren, laege das
        // Padding INNERHALB des Minimums und von den 56dp blieben 36 uebrig --
        // eine Regression, die genau so aussieht wie ein Fix.
        regel.onNodeWithTag(MARKE)
            .assertHeightIsEqualTo(BoltMindDimensions.touchTargetMin)
    }
}
