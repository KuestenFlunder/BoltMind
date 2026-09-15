package com.boltmind.app.ui

import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.boltmind.app.R
import com.boltmind.app.feature.neuervorgang.NeuerVorgangScreen
import com.boltmind.app.feature.neuervorgang.NeuerVorgangUiState
import com.boltmind.app.feature.uebersicht.UebersichtScreen
import com.boltmind.app.feature.uebersicht.UebersichtSheet
import com.boltmind.app.feature.uebersicht.UebersichtUiState
import com.boltmind.app.feature.uebersicht.VorgangKarte
import com.boltmind.app.ui.theme.BoltMindTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * `governance.md`, Abschnitt "Fehlende Dateien", gilt **projektweit** und nicht
 * nur im Schritt-Browser: fehlt eine Foto-Datei, erscheint ein Platzhalter.
 *
 * [FotoPlatzhalterTest] belegt das fuer F-006. Hier stehen die drei Stellen
 * ausserhalb: die Vorgangskarte, das Auswahl-Sheet und das Anlage-Formular.
 *
 * Der letzte Test ist der wichtigere: ein Vorgang **ohne** Fahrzeugfoto ist etwas
 * anderes als einer, dessen Foto verschwunden ist. Wuerden beide gleich aussehen,
 * haette der Platzhalter seine Aussage verloren.
 *
 * Keiner der drei Screens haelt eine Endlos-Animation (die stecken nur im
 * Browser), deshalb genuegt hier die Auto-Synchronisierung.
 *
 * Ausfuehren: ./gradlew connectedDebugAndroidTest
 */
@RunWith(AndroidJUnit4::class)
class PlatzhalterAusserhalbDesBrowsersTest {

    @get:Rule
    val regel = createComposeRule()

    private val kontext = InstrumentationRegistry.getInstrumentation().targetContext

    private val fehlendBeschreibung: String
        get() = kontext.getString(R.string.foto_fehlt_beschreibung)

    /** Ein Pfad, der in der Datenbank steht, aber auf keine Datei mehr zeigt. */
    private val verschwunden: String
        get() = File(kontext.filesDir, "photos/verschwunden.jpg").absolutePath

    private fun karte(fotoPfad: String?) = VorgangKarte(
        id = 1,
        auftragsnummer = "4711",
        beschreibung = "Bremssattel hinten links",
        fahrzeugFotoPfad = fotoPfad,
        schrittAnzahl = 3,
        datumText = "Heute, 09:12"
    )

    private fun zeigeUebersicht(zustand: UebersichtUiState) {
        regel.setContent {
            BoltMindTheme {
                UebersichtScreen(
                    uiState = zustand,
                    onTabGewaehlt = {},
                    onVorgangGeoeffnet = {},
                    onLoeschenAngefragt = {},
                    onWeiterDemontieren = {},
                    onMontageStarten = {},
                    onLoeschenBestaetigt = {},
                    onSheetGeschlossen = {},
                    onNeuerVorgang = {}
                )
            }
        }
    }

    private fun platzhalterVorhanden(): Boolean = regel.onAllNodes(
        hasContentDescription(fehlendBeschreibung),
        useUnmergedTree = true
    ).fetchSemanticsNodes().isNotEmpty()

    @Test
    fun dieVorgangskarteZeigtDenPlatzhalterBeiVerschwundenerDatei() {
        // Given: ein Vorgang, dessen fahrzeugFotoPfad gesetzt ist, dessen Datei
        // aber fehlt -- etwa nach einem Backup ohne Bilderordner
        // When: die Uebersicht zeigt ihn
        zeigeUebersicht(UebersichtUiState(offene = listOf(karte(verschwunden))))

        // Then: der Platzhalter erscheint. Coil laedt asynchron.
        regel.waitUntil(10_000) { platzhalterVorhanden() }
    }

    @Test
    fun einVorgangOhneFahrzeugfotoZeigtKeinenPlatzhalter() {
        // Given/When: ein Vorgang ohne Fahrzeugfoto -- der null-Fall
        zeigeUebersicht(UebersichtUiState(offene = listOf(karte(null))))
        regel.waitForIdle()

        // Then: keine Fehlermeldung. "Nie fotografiert" ist kein Defekt und darf
        // nicht aussehen wie einer.
        require(!platzhalterVorhanden()) {
            "Ein Vorgang ohne Fahrzeugfoto darf nicht als verlorene Datei erscheinen."
        }
    }

    @Test
    fun dasAuswahlSheetZeigtDenPlatzhalterBeiVerschwundenerDatei() {
        // Given: dasselbe Foto fehlt, und der Mechaniker tippt den Vorgang an
        val karte = karte(verschwunden)

        // When: das Auswahl-Sheet steht offen (US-001.2)
        zeigeUebersicht(
            UebersichtUiState(
                offene = listOf(karte),
                sheet = UebersichtSheet.Auswahl(karte)
            )
        )

        // Then: auch das Vorschaubild im Sheet zeigt den Platzhalter
        regel.waitUntil(10_000) { platzhalterVorhanden() }
    }

    @Test
    fun dasAnlageFormularZeigtDenPlatzhalterBeiVerschwundenerDatei() {
        // Given: die Aufnahme ist durch, die Datei aber nicht mehr da
        // When: das Formular zeigt das Fahrzeugfoto
        regel.setContent {
            BoltMindTheme {
                NeuerVorgangScreen(
                    uiState = NeuerVorgangUiState(fahrzeugFotoPfad = verschwunden),
                    onFotoAufgenommen = {},
                    onKameraAbgebrochen = {},
                    onBildWiederholen = {},
                    onAuftragsnummerGeaendert = {},
                    onBeschreibungGeaendert = {},
                    onStartenGetippt = {},
                    onZurueck = {}
                )
            }
        }

        // Then: der Platzhalter statt einer leeren Flaeche
        regel.waitUntil(10_000) { platzhalterVorhanden() }
    }
}
