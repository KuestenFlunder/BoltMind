package com.boltmind.app.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.boltmind.app.ui.components.KameraAnbindung
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Belegt, dass ein Kamera-Auftrag genau einmal startet.
 *
 * Der Test existiert, weil das Projekt diesen Fehler schon einmal gebaut hat: als
 * die Kamera-Hoheit vom Tap in den ViewModel-Zustand wanderte, blieb der Start in
 * einem `LaunchedEffect` haengen, der an den Auftrag gebunden ist. Der Auftrag
 * liegt im ViewModel und ueberlebt eine Neuerstellung der Activity -- die
 * Komposition nicht. Dreht der Mechaniker das Telefon, waehrend er fotografiert,
 * laeuft der Effekt in der neuen Komposition erneut an und die Kamera oeffnet ein
 * zweites Mal.
 *
 * Durch Ausprobieren ist das kaum zu finden: es braucht einen Konfigurationswechsel
 * genau waehrend die fremde Kamera-App im Vordergrund steht.
 *
 * Ausfuehren: ./gradlew connectedDebugAndroidTest  (Emulator oder Geraet noetig)
 */
@RunWith(AndroidJUnit4::class)
class KameraAnbindungTest {

    @get:Rule
    val regel = createComposeRule()

    @Test
    fun startet_denselben_auftrag_nach_wiederherstellung_nicht_erneut() {
        // Given: ein offener Auftrag, der die Kamera einmal gestartet hat
        val wiederhersteller = StateRestorationTester(regel)
        var starts = 0
        wiederhersteller.setContent {
            KameraAnbindung(
                auftragsNummer = 1,
                zielPfad = PFAD,
                onKeineKameraApp = {},
                starte = { starts++ }
            )
        }
        regel.waitForIdle()
        assertEquals("Der Auftrag muss ueberhaupt erst einmal starten", 1, starts)

        // When: die Activity wird neu erstellt -- der Auftrag im ViewModel bleibt
        wiederhersteller.emulateSavedInstanceStateRestore()
        regel.waitForIdle()

        // Then: kein zweiter Start. Sonst stuende die Kamera doppelt auf dem Stapel
        assertEquals(1, starts)
    }

    @Test
    fun startet_einen_neuen_auftrag_nach_der_wiederherstellung_sehr_wohl() {
        // Given: ein gestarteter Auftrag, danach eine Wiederherstellung
        val wiederhersteller = StateRestorationTester(regel)
        var starts = 0
        var pfad by mutableStateOf(PFAD)
        wiederhersteller.setContent {
            KameraAnbindung(
                auftragsNummer = 1,
                zielPfad = pfad,
                onKeineKameraApp = {},
                starte = { starts++ }
            )
        }
        regel.waitForIdle()
        wiederhersteller.emulateSavedInstanceStateRestore()
        regel.waitForIdle()

        // When: der Mechaniker fordert eine weitere Aufnahme an. Nach einem
        // Prozesstod faengt der Zaehler im ViewModel wieder bei eins an, deshalb
        // unterscheidet nur der Zielpfad die beiden Auftraege
        pfad = ZWEITER_PFAD
        regel.waitForIdle()

        // Then: die Sperre darf die neue Aufnahme nicht verschlucken
        assertEquals(2, starts)
    }

    private companion object {
        const val PFAD = "/photos/schritt_1.jpg"
        const val ZWEITER_PFAD = "/photos/schritt_2.jpg"
    }
}
