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
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
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
 * Ein Screen, drei Betriebsarten -- und die Unterschiede sitzen fast alle im
 * Startpunkt, in der Anzeigereihenfolge und darin, welche Aktionen ueberhaupt
 * erscheinen duerfen. Der teuerste Fehler waere, den betrachteten mit dem
 * offenen Schritt zu verwechseln: dann verbrennt ein Fehltipp beim Nachschlagen
 * eine Schrittnummer, die schon auf einem Ablageort-Etikett klebt
 * (docs/specs/design-system.md, K-07).
 *
 * Der Konstruktor startet einen Ticker mit `delay(1_000)`, der nie endet.
 * Deshalb wird hier ausschliesslich mit [runCurrent] gearbeitet --
 * `advanceUntilIdle` wuerde die virtuelle Zeit endlos weiterdrehen.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BrowserViewModelTest {

    private val dispatcher = StandardTestDispatcher()

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
            runTest {
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
        }

        @Test
        fun `landet auf dem letzten Schritt, wenn keiner mehr offen ist`() {
            runTest {
                // Given: alle Schritte sind abgeschlossen (nach "Beenden" wieder geoeffnet)
                val browser = browserFuer(
                    BrowserModus.DEMONTAGE,
                    listOf(schritt(1), schritt(2), schritt(3))
                )

                // When: der Browser oeffnet
                val zustand = browser.uiState.value

                // Then: der letzte Schritt, und es gilt nicht als offener Schritt
                assertEquals(3, zustand.aktiverSchritt?.schritt?.schrittNummer)
                assertNull(zustand.offenerIndex)
                assertFalse(zustand.betrachtetOffenenSchritt)
            }
        }

        @Test
        fun `bleibt bei einem Vorgang ohne Schritte auf Index 0`() {
            runTest {
                // Given: ein frisch angelegter Vorgang ohne Schritte
                val browser = browserFuer(BrowserModus.DEMONTAGE, emptyList())

                // When: der Browser oeffnet
                val zustand = browser.uiState.value

                // Then: kein Absturz, kein aktiver Schritt
                assertEquals(0, zustand.aktiverIndex)
                assertNull(zustand.aktiverSchritt)
                assertFalse(zustand.laedt)
            }
        }
    }

    @Nested
    inner class `US-004_1 Montage-Reihenfolge und Einstiegspunkt` {

        @Test
        fun `dreht die Demontage-Reihenfolge um`() {
            runTest {
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
        }

        @Test
        fun `setzt beim ersten noch nicht eingebauten Schritt der Montage-Reihenfolge auf`() {
            runTest {
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
        }

        @Test
        fun `zeigt den hoechsten Schritt, wenn schon alles eingebaut ist`() {
            runTest {
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
        }

        @Test
        fun `schreibt beim Oeffnen nichts in die Datenbank`() {
            runTest {
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
    }

    @Nested
    inner class `US-001_5 Archiv-Ansicht` {

        @Test
        fun `beginnt beim ersten Schritt und laesst die Reihenfolge unveraendert`() {
            runTest {
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
    }

    // ------------------------------------------------------------------
    // Der teuerste Fall: betrachteter vs. offener Schritt (K-07)
    // ------------------------------------------------------------------

    @Nested
    inner class `K-07 Betrachteter und offener Schritt` {

        @Test
        fun `meldet beim Nachschlagen eines alten Schritts, dass nicht der offene betrachtet wird`() {
            runTest {
                // Given: Schritt 3 ist offen und wird betrachtet
                val browser = browserFuer(
                    BrowserModus.DEMONTAGE,
                    listOf(schritt(1), schritt(2), schritt(3, offen = true))
                )
                assertTrue(browser.uiState.value.betrachtetOffenenSchritt)

                // When: der Mechaniker springt zum Nachschlagen auf Schritt 1
                browser.onSchrittGewaehlt(0)
                runCurrent()

                // Then: "Naechstes Teil" und "Feierabend" duerfen nicht mehr erscheinen,
                // der offene Schritt bleibt aber bekannt
                val zustand = browser.uiState.value
                assertFalse(zustand.betrachtetOffenenSchritt)
                assertEquals(2, zustand.offenerIndex)
                assertEquals(1, zustand.aktiverSchritt?.schritt?.schrittNummer)
            }
        }

        @Test
        fun `springt vom nachgeschlagenen Schritt zurueck zum offenen`() {
            runTest {
                // Given: der Mechaniker schlaegt Schritt 1 nach, offen ist Schritt 3
                val browser = browserFuer(
                    BrowserModus.DEMONTAGE,
                    listOf(schritt(1), schritt(2), schritt(3, offen = true))
                )
                browser.onSchrittGewaehlt(0)
                runCurrent()

                // When: "Zurueck zu Schritt 3"
                browser.onZumOffenenSchritt()
                runCurrent()

                // Then: wieder am offenen Schritt
                val zustand = browser.uiState.value
                assertEquals(3, zustand.aktiverSchritt?.schritt?.schrittNummer)
                assertTrue(zustand.betrachtetOffenenSchritt)
            }
        }

        @Test
        fun `schliesst beim naechsten Teil den offenen Schritt ab, nicht den betrachteten`() {
            runTest {
                // Given: Schritt 3 ist offen, betrachtet wird Schritt 1
                val browser = browserFuer(
                    BrowserModus.DEMONTAGE,
                    listOf(schritt(1), schritt(2), schritt(3, offen = true))
                )
                browser.onSchrittGewaehlt(0)
                runCurrent()

                // When: "Naechstes Teil"
                browser.onNaechstesTeil()
                runCurrent()

                // Then: der offene Schritt 3 wird weitergereicht, nicht der betrachtete
                val offenerSchritt = schritt(3, offen = true).schritt
                verifyBlocking(fotoSteuerung) { naechstesTeil(VORGANG_ID, offenerSchritt) }
            }
        }

        @Test
        fun `reicht ohne offenen Schritt kein Ziel weiter`() {
            runTest {
                // Given: alle Schritte sind abgeschlossen
                val browser = browserFuer(BrowserModus.DEMONTAGE, listOf(schritt(1), schritt(2)))

                // When: die Demontage wird beendet
                browser.onFeierabendBestaetigt()
                runCurrent()

                // Then: es gibt keinen Schritt zum Abschliessen oder Verwerfen
                verifyBlocking(fotoSteuerung) { beenden(null) }
                assertTrue(browser.uiState.value.verlassen)
            }
        }

        @Test
        fun `haelt den offenen Schritt auch dann fest, wenn er nicht der letzte der Liste ist`() {
            runTest {
                // Given: nach einem Verwerfen steht der offene Schritt in der Mitte
                val browser = browserFuer(
                    BrowserModus.DEMONTAGE,
                    listOf(schritt(1), schritt(2, offen = true), schritt(3))
                )

                // When: der Mechaniker blaettert ans Ende
                browser.onNaechsterSchritt()
                runCurrent()

                // Then: Index 2 wird betrachtet, offen bleibt Index 1
                val zustand = browser.uiState.value
                assertEquals(3, zustand.aktiverSchritt?.schritt?.schrittNummer)
                assertEquals(1, zustand.offenerIndex)
                assertFalse(zustand.betrachtetOffenenSchritt)
            }
        }
    }

    // ------------------------------------------------------------------
    // Schrittnummer
    // ------------------------------------------------------------------

    @Nested
    inner class `US-003_4 Naechste Schrittnummer` {

        @Test
        fun `ist die hoechste vergebene Nummer plus eins, egal welcher Schritt sichtbar ist`() {
            runTest {
                // Given: Schritt 3 wurde verworfen, vergeben sind 1, 2 und 4
                val browser = browserFuer(
                    BrowserModus.DEMONTAGE,
                    listOf(schritt(1), schritt(2), schritt(4, offen = true))
                )

                // When: der Mechaniker schlaegt Schritt 1 nach
                browser.onSchrittGewaehlt(0)
                runCurrent()

                // Then: die naechste Nummer bleibt 5 -- Nummern werden nie wiederverwendet
                assertEquals(5, browser.uiState.value.naechsteSchrittNummer)
            }
        }

        @Test
        fun `ist 1, solange der Vorgang keinen Schritt hat`() {
            runTest {
                // Given: ein Vorgang ohne Schritte
                val browser = browserFuer(BrowserModus.DEMONTAGE, emptyList())

                // When/Then: der erste Schritt bekaeme die 1
                assertEquals(1, browser.uiState.value.naechsteSchrittNummer)
            }
        }
    }

    // ------------------------------------------------------------------
    // Label
    // ------------------------------------------------------------------

    @Nested
    inner class `US-003_3 Foto-Label umschalten` {

        @Test
        fun `kippt genau das angetippte Label und laesst die anderen stehen`() {
            runTest {
                // Given: ein Foto, das nur "Bauteil" traegt
                val browser = browserFuer(BrowserModus.DEMONTAGE, listOf(schritt(1, offen = true)))
                val foto = foto(id = 7, bauteil = true)

                // When: "Uebersicht" wird angetippt
                browser.onLabelUmgeschaltet(foto, LabelArt.UEBERSICHT)
                runCurrent()

                // Then: Uebersicht kommt dazu, Bauteil bleibt
                verifyBlocking(repository) {
                    setzeLabel(
                        fotoId = 7,
                        istBauteil = true,
                        istUebersicht = true,
                        istAblageort = false
                    )
                }
            }
        }

        @Test
        fun `nimmt ein gesetztes Label wieder zurueck`() {
            runTest {
                // Given: ein Foto mit "Uebersicht" und "Ablageort"
                val browser = browserFuer(BrowserModus.DEMONTAGE, listOf(schritt(1, offen = true)))
                val foto = foto(id = 8, bauteil = false, uebersicht = true, ablageort = true)

                // When: "Ablageort" wird erneut angetippt
                browser.onLabelUmgeschaltet(foto, LabelArt.ABLAGEORT)
                runCurrent()

                // Then: nur Ablageort faellt weg
                verifyBlocking(repository) {
                    setzeLabel(
                        fotoId = 8,
                        istBauteil = false,
                        istUebersicht = true,
                        istAblageort = false
                    )
                }
            }
        }

        @Test
        fun `laesst alle drei Label abwaehlen`() {
            runTest {
                // Given: ein frisches Foto mit dem Default-Label "Bauteil"
                val browser = browserFuer(BrowserModus.DEMONTAGE, listOf(schritt(1, offen = true)))
                val foto = foto(id = 9, bauteil = true)

                // When: "Bauteil" wird abgewaehlt
                browser.onLabelUmgeschaltet(foto, LabelArt.BAUTEIL)
                runCurrent()

                // Then: kein Label ist gesetzt -- ein gueltiger Zustand
                verifyBlocking(repository) {
                    setzeLabel(
                        fotoId = 9,
                        istBauteil = false,
                        istUebersicht = false,
                        istAblageort = false
                    )
                }
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
            runTest {
                // Given: die Montage steht auf Schritt 3
                val browser = browserFuer(
                    BrowserModus.MONTAGE,
                    listOf(schritt(1), schritt(2), schritt(3))
                )

                // When: "Eingebaut"
                browser.onEingebaut()
                runCurrent()

                // Then: Haken gesetzt, Messung des Schritts gestoppt -- mit dem
                // Montage-Referenztyp, nicht dem der Demontage (K-04)
                verifyBlocking(repository) { setzeEingebaut(SCHRITT_ID_3, true) }
                verifyBlocking(zeiterfassung) {
                    stoppeFallsLaeuft(SCHRITT_ID_3, ReferenzTyp.MONTAGE_SCHRITT)
                }
            }
        }

        @Test
        fun `springt zum naechsten nicht eingebauten Schritt und ueberspringt erledigte`() {
            runTest {
                // Given: Montage-Reihenfolge 3, 2, 1 -- Schritt 2 ist schon eingebaut
                val browser = browserFuer(
                    BrowserModus.MONTAGE,
                    listOf(schritt(1), schritt(2, eingebaut = true), schritt(3))
                )
                assertEquals(3, browser.uiState.value.aktiverSchritt?.schritt?.schrittNummer)

                // When: Schritt 3 wird abgehakt
                browser.onEingebaut()
                runCurrent()

                // Then: weiter zu Schritt 1, Schritt 2 wird uebersprungen
                val zustand = browser.uiState.value
                assertEquals(1, zustand.aktiverSchritt?.schritt?.schrittNummer)
                assertFalse(zustand.fertig)
            }
        }

        @Test
        fun `meldet fertig statt weiterzuspringen, wenn es das letzte Teil war`() {
            runTest {
                // Given: nur noch Schritt 3 ist offen
                val browser = browserFuer(
                    BrowserModus.MONTAGE,
                    listOf(
                        schritt(1, eingebaut = true),
                        schritt(2, eingebaut = true),
                        schritt(3)
                    )
                )

                // When: das letzte Teil wird abgehakt
                browser.onEingebaut()
                runCurrent()

                // Then: Abschluss-Screen statt Sprung -- der Schritt bleibt stehen
                val zustand = browser.uiState.value
                assertTrue(zustand.fertig)
                assertEquals(3, zustand.aktiverSchritt?.schritt?.schrittNummer)
            }
        }

        @Test
        fun `nimmt das Haekchen erst nach Bestaetigung zurueck und schliesst das Sheet`() {
            runTest {
                // Given: der Warndialog steht ueber Schritt 3
                val browser = browserFuer(
                    BrowserModus.MONTAGE,
                    listOf(schritt(1), schritt(2), schritt(3, eingebaut = true))
                )
                browser.zeigeSheet(SheetZustand("Haekchen weg?", "Sicher?", emptyList()))
                assertNotNull(browser.uiState.value.sheet)

                // When: der Mechaniker bestaetigt
                browser.onHaekchenZuruecknehmenBestaetigt()
                runCurrent()

                // Then: Haken weg, Sheet zu
                verifyBlocking(repository) { setzeEingebaut(SCHRITT_ID_3, false) }
                assertNull(browser.uiState.value.sheet)
            }
        }
    }

    // ------------------------------------------------------------------
    // Kamera (K-02)
    // ------------------------------------------------------------------

    @Nested
    inner class `K-02 Kamera-Abbruch vernichtet kein Foto` {

        @Test
        fun `raeumt nur die leere Zieldatei weg und laesst das vorhandene Foto stehen`() {
            runTest {
                // Given: "Wiederholen" wurde fuer ein vorhandenes Foto gestartet
                val browser = browserFuer(BrowserModus.DEMONTAGE, listOf(schritt(1, offen = true)))
                browser.aufnahmeAngemeldet(NEUER_PFAD, ersetztFotoId = 42)

                // When: die System-Kamera bricht ab
                browser.onKameraAbgebrochen()
                runCurrent()

                // Then: nur die Huelle verschwindet, das alte Foto bleibt unberuehrt
                verifyBlocking(fotoSteuerung) { verwerfeDatei(NEUER_PFAD) }
                verifyBlocking(fotoSteuerung, never()) { fotoErsetzen(any(), any()) }
                verifyBlocking(fotoSteuerung, never()) { fotoUebernehmen(any(), any()) }
            }
        }

        @Test
        fun `ignoriert eine nachtraegliche Erfolgsmeldung nach dem Abbruch`() {
            runTest {
                // Given: die Aufnahme wurde bereits abgebrochen
                val browser = browserFuer(BrowserModus.DEMONTAGE, listOf(schritt(1, offen = true)))
                browser.aufnahmeAngemeldet(NEUER_PFAD, ersetztFotoId = 42)
                browser.onKameraAbgebrochen()
                runCurrent()

                // When: doch noch eine Erfolgsmeldung eintrudelt
                browser.onFotoAufgenommen()
                runCurrent()

                // Then: nichts wird ersetzt oder angehaengt
                assertNull(browser.offeneAufnahme)
                verifyBlocking(fotoSteuerung, never()) { fotoErsetzen(any(), any()) }
                verifyBlocking(fotoSteuerung, never()) { fotoUebernehmen(any(), any()) }
            }
        }

        @Test
        fun `tut nichts, wenn gar keine Aufnahme angemeldet war`() {
            runTest {
                // Given: keine laufende Aufnahme (z.B. kein Kamera-Programm auf dem Geraet)
                val browser = browserFuer(BrowserModus.DEMONTAGE, listOf(schritt(1, offen = true)))

                // When: der Abbruch gemeldet wird
                browser.onKameraAbgebrochen()
                runCurrent()

                // Then: keine Datei wird geloescht
                verifyNoInteractions(fotoSteuerung)
            }
        }

        @Test
        fun `haengt ein bestaetigtes Foto an den betrachteten Schritt`() {
            runTest {
                // Given: "Weiteres Foto" am offenen Schritt 2
                val browser = browserFuer(
                    BrowserModus.DEMONTAGE,
                    listOf(schritt(1), schritt(2, offen = true))
                )
                browser.aufnahmeAngemeldet(NEUER_PFAD, ersetztFotoId = null)

                // When: die System-Kamera bestaetigt
                browser.onFotoAufgenommen()
                runCurrent()

                // Then: das Foto haengt am betrachteten Schritt, nichts wird ersetzt
                verifyBlocking(fotoSteuerung) { fotoUebernehmen(SCHRITT_ID_2, NEUER_PFAD) }
                verifyBlocking(fotoSteuerung, never()) { fotoErsetzen(any(), any()) }
                assertNull(browser.offeneAufnahme)
            }
        }

        @Test
        fun `ersetzt das alte Foto erst nach bestaetigter Neuaufnahme`() {
            runTest {
                // Given: "Wiederholen" fuer Foto 42
                val browser = browserFuer(BrowserModus.DEMONTAGE, listOf(schritt(1, offen = true)))
                browser.aufnahmeAngemeldet(NEUER_PFAD, ersetztFotoId = 42)
                verifyBlocking(fotoSteuerung, never()) { fotoErsetzen(any(), any()) }

                // When: die System-Kamera bestaetigt
                browser.onFotoAufgenommen()
                runCurrent()

                // Then: jetzt erst wird ersetzt, und nichts angehaengt
                verifyBlocking(fotoSteuerung) { fotoErsetzen(42, NEUER_PFAD) }
                verifyBlocking(fotoSteuerung, never()) { fotoUebernehmen(any(), any()) }
            }
        }
    }

    // ------------------------------------------------------------------
    // Indizes, wenn sich die Daten unter der Ansicht aendern
    // ------------------------------------------------------------------

    @Nested
    inner class `US-006_4 Indizes bleiben im gueltigen Bereich` {

        @Test
        fun `klemmt den Foto-Index, wenn die Fotoliste schrumpft`() {
            runTest {
                // Given: das dritte von drei Fotos ist sichtbar
                val browser = browserFuer(
                    BrowserModus.DEMONTAGE,
                    listOf(schritt(1, offen = true, fotos = listOf(foto(1), foto(2), foto(3))))
                )
                browser.onFotoGewaehlt(2)
                runCurrent()

                // When: zwei Fotos verschwinden (Ersetzen, Aufraeumen)
                schritteFlow.value = listOf(schritt(1, offen = true, fotos = listOf(foto(1))))
                runCurrent()

                // Then: der Browser zeigt das verbliebene Foto statt ins Leere zu greifen
                val zustand = browser.uiState.value.alsBrowserZustand()
                assertEquals(0, zustand.fotoIndex)
                assertEquals(1L, zustand.sichtbaresFoto?.id)
            }
        }

        @Test
        fun `klemmt den Schritt-Index, wenn ein Schritt verworfen wird`() {
            runTest {
                // Given: der dritte von drei Schritten wird betrachtet
                val browser = browserFuer(
                    BrowserModus.DEMONTAGE,
                    listOf(schritt(1), schritt(2), schritt(3, offen = true))
                )
                assertEquals(2, browser.uiState.value.aktiverIndex)

                // When: die Schritte 2 und 3 verschwinden
                schritteFlow.value = listOf(schritt(1))
                runCurrent()

                // Then: der Index rutscht auf den letzten gueltigen
                val zustand = browser.uiState.value
                assertEquals(0, zustand.aktiverIndex)
                assertEquals(1, zustand.aktiverSchritt?.schritt?.schrittNummer)
            }
        }

        @Test
        fun `beginnt nach einem Schrittwechsel wieder beim ersten Foto`() {
            runTest {
                // Given: im ersten Schritt ist das zweite Foto sichtbar
                val browser = browserFuer(
                    BrowserModus.ARCHIV,
                    listOf(
                        schritt(1, fotos = listOf(foto(1), foto(2))),
                        schritt(2, fotos = listOf(foto(3), foto(4)))
                    )
                )
                browser.onFotoGewaehlt(1)
                runCurrent()

                // When: der Mechaniker springt auf Schritt 2
                browser.onSchrittGewaehlt(1)
                runCurrent()

                // Then: das Karussell steht wieder vorn
                assertEquals(0, browser.uiState.value.aktivesFoto)
                assertEquals(3L, browser.uiState.value.alsBrowserZustand().sichtbaresFoto?.id)
            }
        }

        @Test
        fun `blaettert nicht ueber die Enden hinaus`() {
            runTest {
                // Given: zwei Schritte, Start beim ersten
                val browser = browserFuer(BrowserModus.ARCHIV, listOf(schritt(1), schritt(2)))

                // When: zweimal zurueck, dann dreimal weiter
                browser.onVorherigerSchritt()
                runCurrent()
                assertEquals(0, browser.uiState.value.aktiverIndex)
                repeat(3) {
                    browser.onNaechsterSchritt()
                    runCurrent()
                }

                // Then: am letzten Schritt stehengeblieben, kein Index ausserhalb der Liste
                assertEquals(1, browser.uiState.value.aktiverIndex)
                assertEquals(2, browser.uiState.value.aktiverSchritt?.schritt?.schrittNummer)
            }
        }
    }

    // ------------------------------------------------------------------
    // "Am Fahrzeug"
    // ------------------------------------------------------------------

    @Nested
    inner class `US-004_1 Hinweis Am Fahrzeug` {

        @Test
        fun `meldet am Fahrzeug geblieben, wenn der Schritt kein Ablageort-Foto hat`() {
            runTest {
                // Given: der Montage-Schritt hat nur ein Bauteil-Foto
                val browser = browserFuer(
                    BrowserModus.MONTAGE,
                    listOf(schritt(1, fotos = listOf(foto(1, bauteil = true))))
                )

                // When/Then: der Hinweis "Am Fahrzeug" gilt
                assertTrue(browser.uiState.value.amFahrzeugGeblieben)
            }
        }

        @Test
        fun `meldet nichts, sobald ein Ablageort-Foto existiert`() {
            runTest {
                // Given: der Schritt hat ein Foto vom Ablageort
                val browser = browserFuer(
                    BrowserModus.MONTAGE,
                    listOf(
                        schritt(
                            1,
                            fotos = listOf(foto(1, bauteil = true), foto(2, ablageort = true))
                        )
                    )
                )

                // When/Then: kein Hinweis -- das Teil liegt irgendwo
                assertFalse(browser.uiState.value.amFahrzeugGeblieben)
            }
        }

        @Test
        fun `zeigt den Hinweis nicht in der Demontage`() {
            runTest {
                // Given: derselbe Schritt ohne Ablageort-Foto, aber im Demontage-Modus
                val browser = browserFuer(
                    BrowserModus.DEMONTAGE,
                    listOf(schritt(1, offen = true, fotos = listOf(foto(1, bauteil = true))))
                )

                // When/Then: "Am Fahrzeug" ist ein Montage-Hinweis
                assertFalse(browser.uiState.value.amFahrzeugGeblieben)
            }
        }
    }

    // ------------------------------------------------------------------
    // Testdaten und Hilfen
    // ------------------------------------------------------------------

    /**
     * Baut das ViewModel und laesst die init-Coroutine genau einmal laufen.
     *
     * Bewusst [runCurrent] statt `advanceUntilIdle`: der Ticker des ViewModels
     * plant sich endlos neu ein und wuerde die virtuelle Zeit nie leerlaufen
     * lassen.
     */
    private fun TestScope.browserFuer(
        modus: BrowserModus,
        schritte: List<SchrittMitFotos>
    ): BrowserViewModel {
        schritteFlow.value = schritte
        val browser = BrowserViewModel(
            zustand = SavedStateHandle(
                mapOf("vorgangId" to VORGANG_ID, "modus" to modus.name)
            ),
            repository = repository,
            zeiterfassung = zeiterfassung,
            fotos = fotoSteuerung
        )
        runCurrent()
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

        /** [schritt] vergibt die ID als Nummer mal zehn. */
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
