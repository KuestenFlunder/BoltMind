package com.boltmind.app.feature.browser

import androidx.lifecycle.SavedStateHandle
import com.boltmind.app.data.model.Reparaturvorgang
import com.boltmind.app.data.model.Schritt
import com.boltmind.app.data.model.SchrittFoto
import com.boltmind.app.data.model.SchrittMitFotos
import com.boltmind.app.data.repository.ReparaturRepository
import com.boltmind.app.service.zeiterfassung.ReferenzTyp
import com.boltmind.app.service.zeiterfassung.ZeiterfassungService
import com.boltmind.app.ui.navigation.BrowserModus
import com.boltmind.app.ui.schrittbrowser.LabelArt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.verifyNoInteractions
import java.time.Duration
import java.time.Instant

/**
 * Tests fuer den Consumer des Schritt-Browsers.
 *
 * Ein Screen, drei Betriebsarten -- die Unterschiede sitzen im Startpunkt, in
 * der Anzeigereihenfolge und darin, welche Aktionen ueberhaupt erscheinen
 * duerfen. Der teuerste Fehler waere, den betrachteten mit dem offenen Schritt
 * zu verwechseln: dann verbrennt ein Fehltipp beim Nachschlagen eine
 * Schrittnummer, die schon auf einem Ablageort-Etikett klebt
 * (docs/specs/design-system.md, K-07).
 *
 * **Kein `runTest` hier, mit Absicht.** Der Konstruktor startet einen Ticker
 * mit `delay(1_000)`, der sich endlos neu einplant. `runTest` laesst den
 * Scheduler am Ende jedes Tests leerlaufen und wuerde daran ewig drehen.
 * Stattdessen wird der [TestCoroutineScheduler] direkt bedient: [abarbeiten]
 * fuehrt genau die Coroutinen aus, die zur aktuellen virtuellen Zeit anstehen.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BrowserViewModelTest {

    private val scheduler = TestCoroutineScheduler()
    private val dispatcher = StandardTestDispatcher(scheduler)

    private val schritteFlow = MutableStateFlow<List<SchrittMitFotos>>(emptyList())

    private lateinit var repository: ReparaturRepository
    private lateinit var zeiterfassung: ZeiterfassungService
    private lateinit var fotoSteuerung: BrowserFotoSteuerung

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        schritteFlow.value = emptyList()
        repository = mock {
            on { beobachteSchritteMitFotos(VORGANG_ID) } doReturn schritteFlow
            onBlocking { findVorgangById(VORGANG_ID) } doReturn VORGANG
            onBlocking { holeSchrittIds(VORGANG_ID) } doReturn emptyList()
        }
        zeiterfassung = mock {
            onBlocking { gesamtdauer(any(), any()) } doReturn Duration.ZERO
            onBlocking { laeuft(any(), any()) } doReturn false
        }
        fotoSteuerung = mock()
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ------------------------------------------------------------------
    // Startpunkt
    // ------------------------------------------------------------------

    @Nested
    inner class `US-003_6 Wiedereinstieg in die Demontage` {

        @Test
        fun `setzt auf dem offenen Schritt auf, nicht auf dem ersten oder letzten`() {
            // Given: Schritt 2 ist offen, 1 und 3 sind abgeschlossen
            val browser = browserFuer(
                BrowserModus.DEMONTAGE,
                listOf(schritt(1), schritt(2, offen = true), schritt(3))
            )

            // When: der Browser oeffnet
            val zustand = browser.uiState.value

            // Then: der offene Schritt wird gezeigt
            assertEquals(2, zustand.aktiverSchritt?.schritt?.schrittNummer)
            assertTrue(zustand.betrachtetOffenenSchritt)
        }

        @Test
        fun `landet auf dem letzten Schritt, wenn keiner mehr offen ist`() {
            // Given: alle Schritte sind abgeschlossen (nach "Beenden" wieder geoeffnet)
            val browser = browserFuer(
                BrowserModus.DEMONTAGE,
                listOf(schritt(1), schritt(2), schritt(3))
            )

            // When: der Browser oeffnet
            val zustand = browser.uiState.value

            // Then: der letzte Schritt -- und er gilt nicht als offener Schritt
            assertEquals(3, zustand.aktiverSchritt?.schritt?.schrittNummer)
            assertNull(zustand.offenerIndex)
            assertFalse(zustand.betrachtetOffenenSchritt)
        }

        @Test
        fun `bleibt bei einem Vorgang ohne Schritte auf Index 0`() {
            // Given: ein frisch angelegter Vorgang ohne Schritte
            val browser = browserFuer(BrowserModus.DEMONTAGE, emptyList())

            // When: der Browser oeffnet
            val zustand = browser.uiState.value

            // Then: kein Absturz, kein aktiver Schritt, aber fertig geladen
            assertEquals(0, zustand.aktiverIndex)
            assertNull(zustand.aktiverSchritt)
            assertFalse(zustand.laedt)
        }
    }

    @Nested
    inner class `US-004_1 Montage-Reihenfolge und Einstiegspunkt` {

        @Test
        fun `dreht die Demontage-Reihenfolge um`() {
            // Given: die Schritte 1 bis 3 in Demontage-Reihenfolge
            val browser = browserFuer(
                BrowserModus.MONTAGE,
                listOf(schritt(1), schritt(2), schritt(3))
            )

            // When: der Montage-Flow oeffnet
            val nummern = browser.uiState.value.schritte.map { it.schritt.schrittNummer }

            // Then: das zuletzt ausgebaute Teil kommt zuerst
            assertEquals(listOf(3, 2, 1), nummern)
        }

        @Test
        fun `setzt beim ersten noch nicht eingebauten Schritt der Montage-Reihenfolge auf`() {
            // Given: Schritt 3 ist bereits eingebaut, 1 und 2 nicht
            val browser = browserFuer(
                BrowserModus.MONTAGE,
                listOf(schritt(1), schritt(2), schritt(3, eingebaut = true))
            )

            // When: der Montage-Flow oeffnet
            val zustand = browser.uiState.value

            // Then: Schritt 2 -- nicht der mit der hoechsten Demontage-Nummer
            assertEquals(2, zustand.aktiverSchritt?.schritt?.schrittNummer)
            assertEquals(1, zustand.eingebauteAnzahl)
        }

        @Test
        fun `zeigt den hoechsten Schritt, wenn schon alles eingebaut ist`() {
            // Given: alle Schritte sind abgehakt, der Vorgang ist aber noch offen
            val browser = browserFuer(
                BrowserModus.MONTAGE,
                listOf(schritt(1, eingebaut = true), schritt(2, eingebaut = true))
            )

            // When: der Montage-Flow oeffnet
            val zustand = browser.uiState.value

            // Then: Anfang der Montage-Reihenfolge, Fortschritt vollstaendig
            assertEquals(2, zustand.aktiverSchritt?.schritt?.schrittNummer)
            assertEquals(2, zustand.eingebauteAnzahl)
        }

        @Test
        fun `schreibt beim Oeffnen nichts in die Datenbank`() {
            // Given/When: der Wiedereinstieg in eine begonnene Montage
            browserFuer(
                BrowserModus.MONTAGE,
                listOf(schritt(1), schritt(2, eingebaut = true))
            )

            // Then: reiner Lesevorgang
            verifyBlocking(repository, never()) { setzeEingebaut(any(), any()) }
            verifyBlocking(repository, never()) { schrittAnlegen(any()) }
            verifyBlocking(repository, never()) { schrittAbschliessen(any()) }
        }
    }

    @Nested
    inner class `US-001_5 Archiv-Ansicht` {

        @Test
        fun `beginnt beim ersten Schritt und laesst die Reihenfolge unveraendert`() {
            // Given: ein archivierter Vorgang mit drei Schritten
            val browser = browserFuer(
                BrowserModus.ARCHIV,
                listOf(schritt(1), schritt(2), schritt(3))
            )

            // When: die Nur-Lese-Ansicht oeffnet
            val zustand = browser.uiState.value

            // Then: von vorne, in Demontage-Reihenfolge
            assertEquals(1, zustand.aktiverSchritt?.schritt?.schrittNummer)
            assertEquals(listOf(1, 2, 3), zustand.schritte.map { it.schritt.schrittNummer })
        }
    }

    // ------------------------------------------------------------------
    // Der teuerste Fall: betrachteter vs. offener Schritt (K-07)
    // ------------------------------------------------------------------

    @Nested
    inner class `K-07 Betrachteter und offener Schritt` {

        @Test
        fun `meldet beim Nachschlagen eines alten Schritts, dass nicht der offene betrachtet wird`() {
            // Given: Schritt 3 ist offen und wird betrachtet
            val browser = browserFuer(
                BrowserModus.DEMONTAGE,
                listOf(schritt(1), schritt(2), schritt(3, offen = true))
            )
            assertTrue(browser.uiState.value.betrachtetOffenenSchritt)

            // When: der Mechaniker springt zum Nachschlagen auf Schritt 1
            browser.onSchrittGewaehlt(0)
            abarbeiten()

            // Then: "Naechstes Teil" und "Feierabend" duerfen nicht mehr erscheinen,
            // der offene Schritt bleibt aber bekannt
            val zustand = browser.uiState.value
            assertFalse(zustand.betrachtetOffenenSchritt)
            assertEquals(2, zustand.offenerIndex)
            assertEquals(1, zustand.aktiverSchritt?.schritt?.schrittNummer)
        }

        @Test
        fun `springt vom nachgeschlagenen Schritt zurueck zum offenen`() {
            // Given: der Mechaniker schlaegt Schritt 1 nach, offen ist Schritt 3
            val browser = browserFuer(
                BrowserModus.DEMONTAGE,
                listOf(schritt(1), schritt(2), schritt(3, offen = true))
            )
            browser.onSchrittGewaehlt(0)
            abarbeiten()

            // When: "Zurueck zu Schritt 3"
            browser.onZumOffenenSchritt()
            abarbeiten()

            // Then: wieder am offenen Schritt
            val zustand = browser.uiState.value
            assertEquals(3, zustand.aktiverSchritt?.schritt?.schrittNummer)
            assertTrue(zustand.betrachtetOffenenSchritt)
        }

        @Test
        fun `schliesst beim naechsten Teil den offenen Schritt ab, nicht den betrachteten`() {
            // Given: Schritt 3 ist offen, betrachtet wird Schritt 1
            val browser = browserFuer(
                BrowserModus.DEMONTAGE,
                listOf(schritt(1), schritt(2), schritt(3, offen = true))
            )
            browser.onSchrittGewaehlt(0)
            abarbeiten()

            // When: "Naechstes Teil"
            browser.onNaechstesTeil()
            abarbeiten()

            // Then: der offene Schritt 3 wird weitergereicht, nicht der betrachtete
            val offenerSchritt = schritt(3, offen = true).schritt
            verifyBlocking(fotoSteuerung) { naechstesTeil(VORGANG_ID, offenerSchritt) }
        }

        @Test
        fun `reicht beim Beenden ohne offenen Schritt kein Ziel weiter`() {
            // Given: alle Schritte sind abgeschlossen
            val browser = browserFuer(BrowserModus.DEMONTAGE, listOf(schritt(1), schritt(2)))

            // When: Feierabend
            browser.onFeierabendBestaetigt()
            abarbeiten()

            // Then: es gibt keinen Schritt zum Abschliessen oder Verwerfen
            verifyBlocking(fotoSteuerung) { beenden(null) }
            verifyBlocking(zeiterfassung) { stoppeAlleOffenen() }
            assertTrue(browser.uiState.value.verlassen)
        }

        @Test
        fun `haelt den offenen Schritt fest, auch wenn er nicht der letzte der Liste ist`() {
            // Given: nach einem Verwerfen steht der offene Schritt in der Mitte
            val browser = browserFuer(
                BrowserModus.DEMONTAGE,
                listOf(schritt(1), schritt(2, offen = true), schritt(3))
            )

            // When: der Mechaniker blaettert ans Ende
            browser.onNaechsterSchritt()
            abarbeiten()

            // Then: betrachtet wird Schritt 3, offen bleibt Index 1
            val zustand = browser.uiState.value
            assertEquals(3, zustand.aktiverSchritt?.schritt?.schrittNummer)
            assertEquals(1, zustand.offenerIndex)
            assertFalse(zustand.betrachtetOffenenSchritt)
        }
    }

    // ------------------------------------------------------------------
    // Schrittnummer
    // ------------------------------------------------------------------

    @Nested
    inner class `US-003_4 Naechste Schrittnummer` {

        @Test
        fun `ist die hoechste vergebene Nummer plus eins, egal welcher Schritt sichtbar ist`() {
            // Given: Schritt 3 wurde verworfen, vergeben sind 1, 2 und 4
            val browser = browserFuer(
                BrowserModus.DEMONTAGE,
                listOf(schritt(1), schritt(2), schritt(4, offen = true))
            )

            // When: der Mechaniker schlaegt Schritt 1 nach
            browser.onSchrittGewaehlt(0)
            abarbeiten()

            // Then: die naechste Nummer bleibt 5 -- Nummern werden nie wiederverwendet
            assertEquals(5, browser.uiState.value.naechsteSchrittNummer)
        }

        @Test
        fun `ist 1, solange der Vorgang keinen Schritt hat`() {
            // Given: ein Vorgang ohne Schritte
            val browser = browserFuer(BrowserModus.DEMONTAGE, emptyList())

            // When/Then: der erste Schritt bekaeme die 1
            assertEquals(1, browser.uiState.value.naechsteSchrittNummer)
        }
    }

    // ------------------------------------------------------------------
    // Label
    // ------------------------------------------------------------------

    @Nested
    inner class `US-003_3 Foto-Label umschalten` {

        @Test
        fun `kippt genau das angetippte Label und laesst die anderen stehen`() {
            // Given: ein Foto, das nur "Bauteil" traegt
            val browser = browserFuer(BrowserModus.DEMONTAGE, listOf(schritt(1, offen = true)))

            // When: "Uebersicht" wird angetippt
            browser.onLabelUmgeschaltet(foto(id = 7, bauteil = true), LabelArt.UEBERSICHT)
            abarbeiten()

            // Then: Uebersicht kommt dazu, Bauteil bleibt
            verifyBlocking(repository) {
                setzeLabel(fotoId = 7, istBauteil = true, istUebersicht = true, istAblageort = false)
            }
        }

        @Test
        fun `nimmt ein gesetztes Label wieder zurueck`() {
            // Given: ein Foto mit "Uebersicht" und "Ablageort"
            val browser = browserFuer(BrowserModus.DEMONTAGE, listOf(schritt(1, offen = true)))
            val foto = foto(id = 8, bauteil = false, uebersicht = true, ablageort = true)

            // When: "Ablageort" wird erneut angetippt
            browser.onLabelUmgeschaltet(foto, LabelArt.ABLAGEORT)
            abarbeiten()

            // Then: nur Ablageort faellt weg
            verifyBlocking(repository) {
                setzeLabel(fotoId = 8, istBauteil = false, istUebersicht = true, istAblageort = false)
            }
        }

        @Test
        fun `laesst alle drei Label abwaehlen`() {
            // Given: ein frisches Foto mit dem Default-Label "Bauteil"
            val browser = browserFuer(BrowserModus.DEMONTAGE, listOf(schritt(1, offen = true)))

            // When: "Bauteil" wird abgewaehlt
            browser.onLabelUmgeschaltet(foto(id = 9, bauteil = true), LabelArt.BAUTEIL)
            abarbeiten()

            // Then: kein Label ist gesetzt -- ein gueltiger Zustand
            verifyBlocking(repository) {
                setzeLabel(fotoId = 9, istBauteil = false, istUebersicht = false, istAblageort = false)
            }
        }
    }

    // ------------------------------------------------------------------
    // Montage-Haken
    // ------------------------------------------------------------------

    @Nested
    inner class `US-004_2 Schritt als eingebaut markieren` {

        @Test
        fun `setzt das Haekchen und stoppt die Montage-Messung dieses Schritts`() {
            // Given: die Montage steht auf Schritt 3
            val browser = browserFuer(
                BrowserModus.MONTAGE,
                listOf(schritt(1), schritt(2), schritt(3))
            )

            // When: "Eingebaut"
            browser.onEingebaut()
            abarbeiten()

            // Then: Haken gesetzt und die Messung mit dem Montage-Referenztyp
            // gestoppt, nicht mit dem der Demontage (K-04)
            verifyBlocking(repository) { setzeEingebaut(SCHRITT_ID_3, true) }
            verifyBlocking(zeiterfassung) {
                stoppeFallsLaeuft(SCHRITT_ID_3, ReferenzTyp.MONTAGE_SCHRITT)
            }
        }

        @Test
        fun `springt zum naechsten nicht eingebauten Schritt und ueberspringt erledigte`() {
            // Given: Montage-Reihenfolge 3, 2, 1 -- Schritt 2 ist schon eingebaut
            val browser = browserFuer(
                BrowserModus.MONTAGE,
                listOf(schritt(1), schritt(2, eingebaut = true), schritt(3))
            )
            assertEquals(3, browser.uiState.value.aktiverSchritt?.schritt?.schrittNummer)

            // When: Schritt 3 wird abgehakt
            browser.onEingebaut()
            abarbeiten()

            // Then: weiter zu Schritt 1, Schritt 2 wird uebersprungen
            val zustand = browser.uiState.value
            assertEquals(1, zustand.aktiverSchritt?.schritt?.schrittNummer)
            assertFalse(zustand.fertig)
        }

        @Test
        fun `meldet fertig statt weiterzuspringen, wenn es das letzte Teil war`() {
            // Given: nur noch Schritt 3 ist nicht eingebaut
            val browser = browserFuer(
                BrowserModus.MONTAGE,
                listOf(schritt(1, eingebaut = true), schritt(2, eingebaut = true), schritt(3))
            )

            // When: das letzte Teil wird abgehakt
            browser.onEingebaut()
            abarbeiten()

            // Then: Abschluss statt Sprung -- der Schritt bleibt stehen
            val zustand = browser.uiState.value
            assertTrue(zustand.fertig)
            assertEquals(3, zustand.aktiverSchritt?.schritt?.schrittNummer)
        }

        @Test
        fun `nimmt das Haekchen erst nach Bestaetigung zurueck und schliesst das Sheet`() {
            // Given: der Mechaniker blaettert zum bereits abgehakten Schritt 3 zurueck
            val browser = browserFuer(
                BrowserModus.MONTAGE,
                listOf(schritt(1), schritt(2), schritt(3, eingebaut = true))
            )
            browser.onSchrittGewaehlt(0)
            abarbeiten()
            assertEquals(3, browser.uiState.value.aktiverSchritt?.schritt?.schrittNummer)

            // und der Warndialog steht
            browser.zeigeSheet(SheetZustand("Haekchen weg?", "Sicher?", emptyList()))
            assertNotNull(browser.uiState.value.sheet)
            verifyBlocking(repository, never()) { setzeEingebaut(any(), any()) }

            // When: der Mechaniker bestaetigt
            browser.onHaekchenZuruecknehmenBestaetigt()
            abarbeiten()

            // Then: Haken weg, Sheet zu
            verifyBlocking(repository) { setzeEingebaut(SCHRITT_ID_3, false) }
            assertNull(browser.uiState.value.sheet)
        }
    }

    // ------------------------------------------------------------------
    // Der Abschluss darf nie unerreichbar werden
    // ------------------------------------------------------------------

    /**
     * Ist jedes Teil abgehakt, gibt es keinen offenen Schritt mehr, dessen
     * Abhaken den Abschluss-Screen ausloesen koennte. Ohne die beiden Wege hier
     * -- Sprung beim Wiedereinstieg und ein eigener Knopf nach der Rueckkehr --
     * laesst sich der Vorgang nie archivieren und bleibt fuer immer in der
     * offenen Liste stehen.
     *
     * Spec: docs/specs/F-004-montage/montage.md, US-004.5
     */
    @Nested
    inner class `US-004_5 Abschluss erreichbar halten` {

        @Test
        fun `springt beim Wiedereinstieg sofort zum Abschluss, wenn schon alles drin ist`() {
            // Given/When: der Mechaniker hat den Abschluss-Screen ohne "AB INS
            // ARCHIV" verlassen und waehlt spaeter erneut "MONTAGE STARTEN"
            val browser = browserFuer(
                BrowserModus.MONTAGE,
                listOf(schritt(1, eingebaut = true), schritt(2, eingebaut = true))
            )

            // Then: direkt der Abschluss-Screen, ohne Schreibvorgang
            assertTrue(browser.uiState.value.fertig)
            verifyBlocking(repository, never()) { setzeEingebaut(any(), any()) }
        }

        @Test
        fun `bleibt still, solange noch ein Teil fehlt`() {
            // Given/When: ein Teil ist noch nicht eingebaut
            val browser = browserFuer(
                BrowserModus.MONTAGE,
                listOf(schritt(1, eingebaut = true), schritt(2))
            )

            // Then: kein Sprung -- es gibt noch Arbeit
            assertFalse(browser.uiState.value.fertig)
            assertFalse(browser.uiState.value.alleEingebaut)
        }

        @Test
        fun `haelt einen Vorgang ohne Schritte nicht faelschlich fuer fertig`() {
            // Given/When: ein Vorgang, in dem nie demontiert wurde
            val browser = browserFuer(BrowserModus.MONTAGE, emptyList())

            // Then: eine leere Liste ist nicht "alles eingebaut" -- sonst
            // schickt der Wiedereinstieg den Mechaniker auf einen Abschluss-
            // Screen ueber null Teile
            assertFalse(browser.uiState.value.fertig)
            assertFalse(browser.uiState.value.alleEingebaut)
        }

        @Test
        fun `bietet nach der Rueckkehr vom Abschluss einen Weg dorthin zurueck`() {
            // Given: der Sprung beim Wiedereinstieg ist quittiert -- der
            // Mechaniker steht mit Back wieder in der Schritt-Ansicht
            val browser = browserFuer(
                BrowserModus.MONTAGE,
                listOf(schritt(1, eingebaut = true), schritt(2, eingebaut = true))
            )
            browser.onNavigationAbgeschlossen()
            assertFalse(browser.uiState.value.fertig)

            // Then: der Knopf "Zum Abschluss" steht bereit
            assertTrue(browser.uiState.value.alleEingebaut)

            // When: er wird getippt
            browser.onZumAbschluss()
            abarbeiten()

            // Then: wieder zum Abschluss-Screen, ohne etwas zu schreiben
            assertTrue(browser.uiState.value.fertig)
            verifyBlocking(repository, never()) { setzeEingebaut(any(), any()) }
        }

        @Test
        fun `schickt nach der Rueckkehr nicht bei jeder Datenmeldung erneut zum Abschluss`() {
            // Given: der Mechaniker hat den Abschluss-Screen mit Back verlassen
            val browser = browserFuer(
                BrowserModus.MONTAGE,
                listOf(schritt(1, eingebaut = true), schritt(2, eingebaut = true))
            )
            browser.onNavigationAbgeschlossen()
            assertFalse(browser.uiState.value.fertig)

            // When: der Flow meldet die weiterhin vollstaendige Liste erneut.
            // Room stoesst ihn bei jeder Aenderung an den Schritt-Tabellen an --
            // was sich dabei aendert, ist gleichgueltig, hier ein Foto
            schritteFlow.value = listOf(
                schritt(1, eingebaut = true, fotos = listOf(foto(1))),
                schritt(2, eingebaut = true)
            )
            abarbeiten()

            // Then: er bleibt in der Schritt-Ansicht. Griffe der Sprung nicht nur
            // beim ersten Laden, waere der Abschluss-Screen eine Falle: Back
            // wuerde ihn verlassen und die naechste Meldung ihn sofort
            // zurueckwerfen
            assertFalse(browser.uiState.value.fertig)
            assertTrue(browser.uiState.value.alleEingebaut)
        }

        @Test
        fun `nimmt den Knopf weg, sobald ein Haekchen zurueckgenommen wurde`() {
            // Given: alles ist abgehakt und der Knopf steht
            val browser = browserFuer(
                BrowserModus.MONTAGE,
                listOf(schritt(1, eingebaut = true), schritt(2, eingebaut = true))
            )
            browser.onNavigationAbgeschlossen()
            assertTrue(browser.uiState.value.alleEingebaut)

            // When: der Mechaniker nimmt eine Markierung zurueck
            schritteFlow.value = listOf(schritt(1, eingebaut = true), schritt(2))
            abarbeiten()

            // Then: wieder ein offenes Teil, der Knopf verschwindet
            assertFalse(browser.uiState.value.alleEingebaut)
        }

        @Test
        fun `kennt den Knopf nur in der Montage`() {
            // Given/When: dieselbe Datenlage in Demontage und Archiv
            val demontage = browserFuer(
                BrowserModus.DEMONTAGE,
                listOf(schritt(1, eingebaut = true), schritt(2, eingebaut = true))
            )
            val archiv = browserFuer(
                BrowserModus.ARCHIV,
                listOf(schritt(1, eingebaut = true), schritt(2, eingebaut = true))
            )

            // Then: "Zum Abschluss" ist eine Montage-Aktion
            assertFalse(demontage.uiState.value.alleEingebaut)
            assertFalse(demontage.uiState.value.fertig)
            assertFalse(archiv.uiState.value.alleEingebaut)
            assertFalse(archiv.uiState.value.fertig)
        }
    }

    // ------------------------------------------------------------------
    // Kamera (K-02)
    // ------------------------------------------------------------------

    @Nested
    inner class `K-02 Kamera-Abbruch vernichtet kein Foto` {

        @Test
        fun `raeumt nur die leere Zieldatei weg und laesst das vorhandene Foto stehen`() {
            // Given: "Wiederholen" wurde fuer das vorhandene Foto 42 gestartet
            val browser = browserFuer(BrowserModus.DEMONTAGE, listOf(schritt(1, offen = true)))
            browser.aufnahmeAngemeldet(NEUER_PFAD, ersetztFotoId = 42)

            // When: die System-Kamera bricht ab
            browser.onKameraAbgebrochen()
            abarbeiten()

            // Then: nur die leere Huelle verschwindet, das alte Foto bleibt unberuehrt
            verifyBlocking(fotoSteuerung) { verwerfeDatei(NEUER_PFAD) }
            verifyBlocking(fotoSteuerung, never()) { fotoErsetzen(any(), any()) }
            verifyBlocking(fotoSteuerung, never()) { fotoUebernehmen(any(), any()) }
        }

        @Test
        fun `ignoriert eine nachtraegliche Erfolgsmeldung nach dem Abbruch`() {
            // Given: die Aufnahme wurde bereits abgebrochen
            val browser = browserFuer(BrowserModus.DEMONTAGE, listOf(schritt(1, offen = true)))
            browser.aufnahmeAngemeldet(NEUER_PFAD, ersetztFotoId = 42)
            browser.onKameraAbgebrochen()
            abarbeiten()

            // When: doch noch eine Erfolgsmeldung eintrudelt
            browser.onFotoAufgenommen()
            abarbeiten()

            // Then: nichts wird ersetzt oder angehaengt
            assertNull(browser.offeneAufnahme)
            verifyBlocking(fotoSteuerung, never()) { fotoErsetzen(any(), any()) }
            verifyBlocking(fotoSteuerung, never()) { fotoUebernehmen(any(), any()) }
        }

        @Test
        fun `tut nichts, wenn gar keine Aufnahme angemeldet war`() {
            // Given: keine laufende Aufnahme (z.B. kein Kamera-Programm auf dem Geraet)
            val browser = browserFuer(BrowserModus.DEMONTAGE, listOf(schritt(1, offen = true)))

            // When: der Abbruch gemeldet wird
            browser.onKameraAbgebrochen()
            abarbeiten()

            // Then: keine Datei wird geloescht
            verifyNoInteractions(fotoSteuerung)
        }

        @Test
        fun `haengt ein bestaetigtes Foto an den betrachteten Schritt`() {
            // Given: "Weiteres Foto" am offenen Schritt 2
            val browser = browserFuer(
                BrowserModus.DEMONTAGE,
                listOf(schritt(1), schritt(2, offen = true))
            )
            browser.aufnahmeAngemeldet(NEUER_PFAD, ersetztFotoId = null)

            // When: die System-Kamera bestaetigt
            browser.onFotoAufgenommen()
            abarbeiten()

            // Then: das Foto haengt am betrachteten Schritt, nichts wird ersetzt
            verifyBlocking(fotoSteuerung) { fotoUebernehmen(SCHRITT_ID_2, NEUER_PFAD) }
            verifyBlocking(fotoSteuerung, never()) { fotoErsetzen(any(), any()) }
            assertNull(browser.offeneAufnahme)
        }

        @Test
        fun `ersetzt das alte Foto erst nach bestaetigter Neuaufnahme`() {
            // Given: "Wiederholen" fuer Foto 42 ist angemeldet
            val browser = browserFuer(BrowserModus.DEMONTAGE, listOf(schritt(1, offen = true)))
            browser.aufnahmeAngemeldet(NEUER_PFAD, ersetztFotoId = 42)
            verifyBlocking(fotoSteuerung, never()) { fotoErsetzen(any(), any()) }

            // When: die System-Kamera bestaetigt
            browser.onFotoAufgenommen()
            abarbeiten()

            // Then: jetzt erst wird ersetzt, und nichts angehaengt
            verifyBlocking(fotoSteuerung) { fotoErsetzen(42, NEUER_PFAD) }
            verifyBlocking(fotoSteuerung, never()) { fotoUebernehmen(any(), any()) }
        }
    }

    // ------------------------------------------------------------------
    // Indizes, wenn sich die Daten unter der Ansicht aendern
    // ------------------------------------------------------------------

    @Nested
    inner class `US-006_4 Indizes bleiben im gueltigen Bereich` {

        @Test
        fun `klemmt den Foto-Index, wenn die Fotoliste schrumpft`() {
            // Given: das dritte von drei Fotos ist sichtbar
            val browser = browserFuer(
                BrowserModus.DEMONTAGE,
                listOf(schritt(1, offen = true, fotos = listOf(foto(1), foto(2), foto(3))))
            )
            browser.onFotoGewaehlt(2)
            abarbeiten()

            // When: zwei Fotos verschwinden
            schritteFlow.value = listOf(schritt(1, offen = true, fotos = listOf(foto(1))))
            abarbeiten()

            // Then: der Browser zeigt das verbliebene Foto statt ins Leere zu greifen
            val zustand = browser.uiState.value.alsBrowserZustand()
            assertEquals(0, zustand.fotoIndex)
            assertEquals(1L, zustand.sichtbaresFoto?.id)
        }

        @Test
        fun `klemmt den Schritt-Index, wenn Schritte verschwinden`() {
            // Given: der dritte von drei Schritten wird betrachtet
            val browser = browserFuer(
                BrowserModus.DEMONTAGE,
                listOf(schritt(1), schritt(2), schritt(3, offen = true))
            )
            assertEquals(2, browser.uiState.value.aktiverIndex)

            // When: die Schritte 2 und 3 verschwinden (verworfen oder geloescht)
            schritteFlow.value = listOf(schritt(1))
            abarbeiten()

            // Then: der Index rutscht auf den letzten gueltigen
            val zustand = browser.uiState.value
            assertEquals(0, zustand.aktiverIndex)
            assertEquals(1, zustand.aktiverSchritt?.schritt?.schrittNummer)
        }

        @Test
        fun `haelt den betrachteten Schritt fest, wenn ein Foto dazukommt`() {
            // Given: der Mechaniker schlaegt Schritt 1 nach, offen ist Schritt 3
            val browser = browserFuer(
                BrowserModus.DEMONTAGE,
                listOf(schritt(1), schritt(2), schritt(3, offen = true))
            )
            browser.onSchrittGewaehlt(0)
            abarbeiten()

            // When: ein neues Foto am offenen Schritt landet und der Flow neu liefert
            schritteFlow.value = listOf(
                schritt(1),
                schritt(2),
                schritt(3, offen = true, fotos = listOf(foto(1)))
            )
            abarbeiten()

            // Then: die Ansicht springt nicht zurueck auf den offenen Schritt
            assertEquals(0, browser.uiState.value.aktiverIndex)
        }

        @Test
        fun `beginnt nach einem Schrittwechsel wieder beim ersten Foto`() {
            // Given: im ersten Schritt ist das zweite Foto sichtbar
            val browser = browserFuer(
                BrowserModus.ARCHIV,
                listOf(
                    schritt(1, fotos = listOf(foto(1), foto(2))),
                    schritt(2, fotos = listOf(foto(3), foto(4)))
                )
            )
            browser.onFotoGewaehlt(1)
            abarbeiten()

            // When: der Mechaniker springt auf Schritt 2
            browser.onSchrittGewaehlt(1)
            abarbeiten()

            // Then: das Karussell steht wieder vorn
            assertEquals(0, browser.uiState.value.aktivesFoto)
            assertEquals(3L, browser.uiState.value.alsBrowserZustand().sichtbaresFoto?.id)
        }

        @Test
        fun `blaettert nicht ueber die Enden hinaus`() {
            // Given: zwei Schritte, Start beim ersten
            val browser = browserFuer(BrowserModus.ARCHIV, listOf(schritt(1), schritt(2)))

            // When: einmal zurueck, dann dreimal weiter
            browser.onVorherigerSchritt()
            abarbeiten()
            assertEquals(0, browser.uiState.value.aktiverIndex)
            repeat(3) {
                browser.onNaechsterSchritt()
                abarbeiten()
            }

            // Then: am letzten Schritt stehengeblieben, kein Index ausserhalb der Liste
            assertEquals(1, browser.uiState.value.aktiverIndex)
            assertEquals(2, browser.uiState.value.aktiverSchritt?.schritt?.schrittNummer)
        }
    }

    // ------------------------------------------------------------------
    // "Am Fahrzeug"
    // ------------------------------------------------------------------

    @Nested
    inner class `US-004_1 Hinweis Am Fahrzeug` {

        @Test
        fun `meldet am Fahrzeug geblieben, wenn der Schritt kein Ablageort-Foto hat`() {
            // Given: der Montage-Schritt hat nur ein Bauteil-Foto
            val browser = browserFuer(
                BrowserModus.MONTAGE,
                listOf(schritt(1, fotos = listOf(foto(1, bauteil = true))))
            )

            // When/Then: der Hinweis "Am Fahrzeug" gilt
            assertTrue(browser.uiState.value.amFahrzeugGeblieben)
        }

        @Test
        fun `meldet nichts, sobald ein Ablageort-Foto existiert`() {
            // Given: der Schritt hat ein Foto vom Ablageort
            val browser = browserFuer(
                BrowserModus.MONTAGE,
                listOf(schritt(1, fotos = listOf(foto(1, bauteil = true), foto(2, ablageort = true))))
            )

            // When/Then: kein Hinweis -- das Teil liegt irgendwo
            assertFalse(browser.uiState.value.amFahrzeugGeblieben)
        }

        @Test
        fun `zeigt den Hinweis nicht in der Demontage`() {
            // Given: derselbe Schritt ohne Ablageort-Foto, aber im Demontage-Modus
            val browser = browserFuer(
                BrowserModus.DEMONTAGE,
                listOf(schritt(1, offen = true, fotos = listOf(foto(1, bauteil = true))))
            )

            // When/Then: "Am Fahrzeug" ist ein Montage-Hinweis
            assertFalse(browser.uiState.value.amFahrzeugGeblieben)
        }
    }

    // ------------------------------------------------------------------
    // Testdaten und Hilfen
    // ------------------------------------------------------------------

    /**
     * Fuehrt die Coroutinen aus, die zur aktuellen virtuellen Zeit anstehen.
     *
     * Bewusst kein `advanceUntilIdle`: der Ticker des ViewModels plant sich
     * jede Sekunde neu ein, der Scheduler wuerde nie leerlaufen.
     */
    private fun abarbeiten() = scheduler.runCurrent()

    private fun browserFuer(
        modus: BrowserModus,
        schritte: List<SchrittMitFotos>
    ): BrowserViewModel {
        schritteFlow.value = schritte
        val browser = BrowserViewModel(
            zustand = SavedStateHandle(mapOf("vorgangId" to VORGANG_ID, "modus" to modus.name)),
            repository = repository,
            zeiterfassung = zeiterfassung,
            fotos = fotoSteuerung
        )
        abarbeiten()
        return browser
    }

    private fun schritt(
        nummer: Int,
        offen: Boolean = false,
        eingebaut: Boolean = false,
        fotos: List<SchrittFoto> = emptyList()
    ) = SchrittMitFotos(
        schritt = Schritt(
            id = nummer.toLong() * 10,
            reparaturvorgangId = VORGANG_ID,
            schrittNummer = nummer,
            eingebautBeiMontage = eingebaut,
            gestartetAm = T0,
            abgeschlossenAm = if (offen) null else T0.plusSeconds(60)
        ),
        fotos = fotos
    )

    private fun foto(
        id: Long,
        bauteil: Boolean = true,
        uebersicht: Boolean = false,
        ablageort: Boolean = false
    ) = SchrittFoto(
        id = id,
        schrittId = 10,
        pfad = "/photos/$id.jpg",
        reihenfolge = (id - 1).toInt(),
        istBauteil = bauteil,
        istUebersicht = uebersicht,
        istAblageort = ablageort,
        aufgenommenAm = T0
    )

    private companion object {
        const val VORGANG_ID = 4L
        const val NEUER_PFAD = "/photos/schritt_neu.jpg"

        /** [schritt] vergibt die ID als Schrittnummer mal zehn. */
        const val SCHRITT_ID_2 = 20L
        const val SCHRITT_ID_3 = 30L

        val T0: Instant = Instant.parse("2026-07-27T08:00:00Z")

        val VORGANG = Reparaturvorgang(
            id = VORGANG_ID,
            auftragsnummer = "A-4711",
            erstelltAm = T0,
            aktualisiertAm = T0
        )
    }
}
