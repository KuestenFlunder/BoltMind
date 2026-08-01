package com.boltmind.app.ui

import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.boltmind.app.R
import com.boltmind.app.feature.splash.SplashScreen
import com.boltmind.app.ui.theme.BoltMindTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Der Tipp auf den Splash (US-007.1).
 *
 * **Warum eigene Klasse statt eines Tests in [StartSmokeTest]?** Der frueher
 * dort stehende `splashLaesstSichUeberspringen` gab sich 3000 ms Zeit, waehrend
 * der Splash nach 2400 ms von selbst weiterschaltet. Er konnte "getippt" und
 * "von allein abgelaufen" nicht unterscheiden -- ging das Ueberspringen kaputt,
 * blieb er gruen (#125).
 *
 * Hier laeuft die Uhr deshalb von Hand: `mainClock.autoAdvance = false` stellt
 * auch die virtuelle Zeit des `delay(SPLASH_DAUER_MS)` still. Der Tipp ist damit
 * die einzige moegliche Ursache fuer die Meldung, und der Test faellt, wenn er
 * wirkungslos wird.
 *
 * Getestet wird [SplashScreen] einzeln, nicht ueber die Activity: nur so ist die
 * Meldung zaehlbar.
 *
 * Ausfuehren: ./gradlew connectedDebugAndroidTest
 */
@RunWith(AndroidJUnit4::class)
class SplashUeberspringenTest {

    @get:Rule
    val regel = createComposeRule()

    private val ueberspringen: String
        get() = InstrumentationRegistry.getInstrumentation().targetContext
            .getString(R.string.splash_ueberspringen)

    private var meldungen = 0

    private fun zeigeSplash() {
        regel.mainClock.autoAdvance = false
        regel.setContent {
            BoltMindTheme { SplashScreen(onFertig = { meldungen++ }) }
        }
        // Ein paar Frames, damit der Screen steht -- weit unter den 2400 ms.
        regel.mainClock.advanceTimeBy(100)
    }

    @Test
    fun derTippBeendetDenSplashVorAblaufDerZeit() {
        // Given: der Splash steht, die Uhr ist bei 100 ms
        zeigeSplash()
        assertEquals("Ohne Tipp darf noch nichts gemeldet sein.", 0, meldungen)

        // When: auf die Flaeche getippt wird
        regel.onNodeWithContentDescription(ueberspringen).performClick()
        regel.mainClock.advanceTimeBy(32)

        // Then: gemeldet, und zwar lange vor den 2400 ms des Selbstablaufs.
        // Bleibt der Tipp wirkungslos, steht hier 0.
        assertEquals("Der Tipp hat den Splash nicht beendet.", 1, meldungen)
        assertTrue(
            "Die Meldung kam erst bei ${regel.mainClock.currentTime} ms -- das koennte " +
                "auch der Selbstablauf gewesen sein.",
            regel.mainClock.currentTime < 2400
        )
    }

    @Test
    fun ohneTippMeldetErstDerAblauf() {
        // Given: der Splash steht, niemand tippt
        zeigeSplash()
        assertEquals(0, meldungen)

        // When: die Zeit laeuft bis kurz vor Schluss
        regel.mainClock.advanceTimeBy(2200)
        assertEquals("Vor Ablauf darf nichts gemeldet sein.", 0, meldungen)

        // Then: nach 2400 ms uebergibt er von selbst (US-007.1)
        regel.mainClock.advanceTimeBy(400)
        assertEquals(1, meldungen)
    }

    @Test
    fun einZweiterTippMeldetNichtErneut() {
        // Given: bereits uebersprungen
        zeigeSplash()
        regel.onNodeWithContentDescription(ueberspringen).performClick()
        regel.mainClock.advanceTimeBy(32)

        // When: der Ablauf holt den Countdown ein und jemand tippt nochmal
        regel.onAllNodes(hasContentDescription(ueberspringen)).fetchSemanticsNodes()
            .takeIf { it.isNotEmpty() }
            ?.let { regel.onNodeWithContentDescription(ueberspringen).performClick() }
        regel.mainClock.advanceTimeBy(3000)

        // Then: genau eine Meldung. Der Screen sichert das ueber `gemeldet` zu.
        assertEquals("Der Splash darf sich nur einmal fertig melden.", 1, meldungen)
    }
}
