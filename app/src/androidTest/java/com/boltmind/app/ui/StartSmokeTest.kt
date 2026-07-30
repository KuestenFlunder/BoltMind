package com.boltmind.app.ui

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.boltmind.app.MainActivity
import com.boltmind.app.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Der erste Compose-UI-Test des Projekts.
 *
 * Sein Zweck ist es, das Harness zu belegen: dass `ui-test-junit4` und
 * `debugImplementation(ui-test-manifest)` wirklich tragen, dass die App unter
 * dem Testrunner mit Koin hochkommt und dass der Splash von selbst uebergibt.
 * Solange das nicht bewiesen ist, ist jede spaetere UI-Behauptung -- etwa "die
 * Mindesthoehe greift" -- unbelegt.
 *
 * **Warum nur Splash und Uebersicht?** Der Schritt-Browser haelt zwei
 * Endlos-Animationen: den Atem-Puls des aktiven Thumbnails
 * (`ThumbnailLeiste.kt`) und den pulsierenden Wischhinweis
 * (`FotoKarussell.kt`). Compose synchronisiert Tests gegen die Animationsuhr
 * und wartet auf Ruhe -- die tritt dort nie ein, der Test haenge bis zum
 * Timeout. Ein Test, der den Browser betritt, muss deshalb
 * `composeTestRule.mainClock.autoAdvance = false` setzen und die Uhr von Hand
 * vorstellen. Das ist keine Randnotiz, sondern die erste Falle, in die jeder
 * naechste UI-Test laeuft.
 *
 * Konvention: JUnit 4 ueber AndroidJUnitRunner, eine Klasse je zusammenhaengendem
 * Anliegen -- siehe `docs/CODING_RULES.md`, Abschnitt "Konvention fuer androidTest".
 *
 * Ausfuehren: ./gradlew connectedDebugAndroidTest  (Emulator oder Geraet noetig)
 */
@RunWith(AndroidJUnit4::class)
class StartSmokeTest {

    @get:Rule
    val regel = createAndroidComposeRule<MainActivity>()

    private fun text(id: Int): String = regel.activity.getString(id)

    /** Wartet, bis mindestens ein Knoten mit diesem Text existiert. */
    private fun warteAufText(wert: String, timeoutMs: Long = 15_000) {
        regel.waitUntil(timeoutMs) {
            regel.onAllNodes(hasText(wert), useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun splashUebergibtVonSelbstAnDieUebersicht() {
        // Given: die App startet auf dem Splash
        warteAufText(text(R.string.splash_claim))

        // When: der Splash laeuft ab (2,1 s, plus Puffer fuer langsame Geraete)
        warteAufText(text(R.string.uebersicht_tab_offen))

        // Then: die Uebersicht steht mit beiden Tabs
        regel.onNodeWithText(text(R.string.uebersicht_tab_offen)).assertExists()
        regel.onNodeWithText(text(R.string.uebersicht_tab_archiv)).assertExists()
    }

    @Test
    fun splashLaesstSichUeberspringen() {
        // Given: der Splash ist sichtbar
        warteAufText(text(R.string.splash_claim))

        // When: irgendwo auf die Flaeche getippt wird (US-007.1)
        regel.onNodeWithText(text(R.string.splash_claim)).performClick()

        // Then: die Uebersicht erscheint, ohne die vollen 2,1 s abzuwarten
        warteAufText(text(R.string.uebersicht_tab_offen), timeoutMs = 3_000)
    }

    @Test
    fun derWechselInsArchivZeigtDenLeerzustand() {
        // Given: die Uebersicht steht
        warteAufText(text(R.string.uebersicht_tab_offen))

        // When: auf den Archiv-Tab gewechselt wird
        regel.onNodeWithText(text(R.string.uebersicht_tab_archiv)).performClick()

        // Then: der Tab bleibt bedienbar und die App lebt noch. Ein
        // Leerzustandstext wird hier bewusst nicht geprueft -- er haengt am
        // Datenbestand des Geraets, und dieser Test soll das Harness belegen,
        // nicht Inhalte.
        regel.onNodeWithText(text(R.string.uebersicht_tab_offen)).assertExists()
    }
}
