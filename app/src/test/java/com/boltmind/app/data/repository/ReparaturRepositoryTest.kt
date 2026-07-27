package com.boltmind.app.data.repository

import app.cash.turbine.test
import com.boltmind.app.data.local.ReparaturvorgangDao
import com.boltmind.app.data.local.SchrittDao
import com.boltmind.app.data.local.SchrittFotoDao
import com.boltmind.app.data.model.Reparaturvorgang
import com.boltmind.app.data.model.ReparaturvorgangMitAnzahl
import com.boltmind.app.data.model.Schritt
import com.boltmind.app.data.model.SchrittFoto
import com.boltmind.app.data.model.SchrittMitFotos
import com.boltmind.app.data.model.VorgangStatus
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.mockito.ArgumentMatchers
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.lang.reflect.Modifier
import java.time.Instant

/**
 * Tests fuer [ReparaturRepository] -- den einzigen Schreibweg auf Vorgaenge,
 * Schritte und Fotos.
 *
 * Die DAOs sind gemockt, die Transaktion ist ein [DirekterLauf] und die Uhr eine
 * feste Funktion. Damit ist jeder Zeitstempel im Test deterministisch und
 * vergleichbar.
 *
 * Schwerpunkt ist die Invariante aus `docs/specs/governance.md`, Abschnitt
 * "Invariante `aktualisiertAm`": jede datenveraendernde Operation zieht
 * `Reparaturvorgang.aktualisiertAm` nach. Ein Verstoss ist im Betrieb unsichtbar
 * -- die Uebersicht sortiert nur falsch und das Archiv zeigt ein falsches
 * Abschlussdatum -- und faellt deshalb ohne Test niemandem auf.
 */
class ReparaturRepositoryTest {

    private lateinit var vorgangDao: ReparaturvorgangDao
    private lateinit var schrittDao: SchrittDao
    private lateinit var fotoDao: SchrittFotoDao
    private lateinit var repository: ReparaturRepository

    /** Die Uhr des Repositories. Veraenderbar, damit Tests die Zeit steuern koennen. */
    private var uhrzeit: Instant = JETZT

    @BeforeEach
    fun setUp() {
        neuAufsetzen()
    }

    /**
     * Frische Mocks plus die Stubs, ohne die keine schreibende Methode durchlaeuft.
     * Wird auch zwischen den dynamischen Faellen des Trichter-Tests aufgerufen --
     * die teilen sich sonst eine Test-Instanz und damit die Mocks.
     */
    private fun neuAufsetzen() {
        vorgangDao = mock()
        schrittDao = mock()
        fotoDao = mock()
        uhrzeit = JETZT
        repository = ReparaturRepository(vorgangDao, schrittDao, fotoDao, DirekterLauf()) { uhrzeit }

        runBlocking {
            whenever(schrittDao.findeVorgangId(SCHRITT_ID)).thenReturn(VORGANG_ID)
            whenever(schrittDao.holeNaechsteSchrittNummer(VORGANG_ID)).thenReturn(NAECHSTE_NUMMER)
            whenever(schrittDao.einfuegen(beliebigerSchritt())).thenReturn(NEUE_SCHRITT_ID)
            whenever(fotoDao.findById(FOTO_ID)).thenReturn(BESTEHENDES_FOTO)
            whenever(fotoDao.findeVorgangId(FOTO_ID)).thenReturn(VORGANG_ID)
            whenever(fotoDao.naechsteReihenfolge(SCHRITT_ID)).thenReturn(NAECHSTE_REIHENFOLGE)
            whenever(fotoDao.einfuegen(beliebigesFoto())).thenReturn(NEUE_FOTO_ID)
        }
    }

    // ------------------------------------------------------------------
    // Der Trichter
    // ------------------------------------------------------------------

    @Nested
    inner class `Invariante aktualisiertAm -- der Trichter` {

        @TestFactory
        fun `jede schreibende Methode zieht aktualisiertAm nach`(): List<DynamicTest> =
            UEBER_DEN_TRICHTER.map { (name, aufruf) ->
                DynamicTest.dynamicTest(name) {
                    // Given: ein frisch aufgesetztes Repository
                    neuAufsetzen()
                    // When: die schreibende Methode laeuft
                    runTest { aufruf(repository) }
                    // Then: der Vorgang wurde genau einmal auf die Aktionszeit gestempelt
                    runTest { verify(vorgangDao).beruehre(VORGANG_ID, JETZT.toEpochMilli()) }
                }
            }

        @Test
        fun `die oeffentlichen Methoden des Repositories sind vollstaendig einsortiert`() {
            // Given: die Kategorien oben in dieser Datei
            val einsortiert = (NUR_LESEND + EIGENER_ZEITSTEMPEL + OHNE_ZEITSTEMPEL +
                UEBER_DEN_TRICHTER.map { it.first }).toSet()

            // When: die tatsaechliche Oberflaeche der Klasse abgefragt wird
            val vorhanden = ReparaturRepository::class.java.declaredMethods
                .filter { Modifier.isPublic(it.modifiers) && !it.isSynthetic && !it.isBridge }
                .map { it.name }
                .toSet()

            // Then: keine Methode ist unkategorisiert und keine Kategorie zeigt ins Leere
            assertEquals(
                emptySet<String>(),
                vorhanden - einsortiert,
                "Neue oeffentliche Methode am Repository. Schreibt sie Daten und nutzt " +
                    "beruehrend(), gehoert sie in UEBER_DEN_TRICHTER (Name -> Aufruf); " +
                    "setzt sie aktualisiertAm selbst, in EIGENER_ZEITSTEMPEL plus eigener " +
                    "Test; liest sie nur, in NUR_LESEND."
            )
            assertEquals(
                emptySet<String>(),
                einsortiert - vorhanden,
                "Eintrag ohne Methode -- umbenannt oder geloescht? Kategorie aufraeumen."
            )
        }

        @Test
        fun `Neuanlage stempelt aktualisiertAm auf die Aktionszeit`() = runTest {
            // Given: ein Vorgang mit einem alten Zeitstempel aus dem Aufrufer
            val roh = Reparaturvorgang(
                auftragsnummer = "A-1",
                erstelltAm = FRUEHER,
                aktualisiertAm = FRUEHER
            )
            whenever(vorgangDao.einfuegen(beliebigerVorgang())).thenReturn(VORGANG_ID)

            // When: der Vorgang angelegt wird
            val id = repository.erstelleVorgang(roh)

            // Then: gespeichert wird mit der Aktionszeit, die neue Id kommt zurueck
            assertEquals(VORGANG_ID, id)
            verify(vorgangDao).einfuegen(roh.copy(aktualisiertAm = JETZT))
        }

        @Test
        fun `Aktualisieren stempelt aktualisiertAm auf die Aktionszeit`() = runTest {
            // Given: ein geladener Vorgang mit altem Zeitstempel
            val bestand = Reparaturvorgang(
                id = VORGANG_ID,
                auftragsnummer = "A-1",
                erstelltAm = FRUEHER,
                aktualisiertAm = FRUEHER
            )

            // When: er veraendert zurueckgeschrieben wird
            repository.aktualisiereVorgang(bestand.copy(beschreibung = "Bremse hinten"))

            // Then: der alte Zeitstempel wird nicht mitgeschleppt
            verify(vorgangDao).aktualisieren(
                bestand.copy(beschreibung = "Bremse hinten", aktualisiertAm = JETZT)
            )
        }

        @Test
        fun `Archivieren stempelt aktualisiertAm auf die Aktionszeit`() = runTest {
            // Given: ein offener Vorgang
            // When: er archiviert wird
            repository.archiviereVorgang(VORGANG_ID)

            // Then: Status und Zeitstempel wandern in einem Zug -- das Archiv leitet
            // sein Abschlussdatum aus aktualisiertAm ab (governance.md)
            verify(vorgangDao).setzeStatus(
                VORGANG_ID,
                VorgangStatus.ARCHIVIERT,
                JETZT.toEpochMilli()
            )
        }

        @Test
        fun `beruehrt niemanden wenn der Vorgang zum Schritt nicht mehr auffindbar ist`() =
            runTest {
                // Given: der Schritt gehoert zu keinem Vorgang mehr (Vorgang geloescht)
                whenever(schrittDao.findeVorgangId(SCHRITT_ID)).thenReturn(null)

                // When: der Schritt trotzdem abgeschlossen wird
                repository.schrittAbschliessen(SCHRITT_ID)

                // Then: die eigentliche Aenderung laeuft, es wird aber nichts beruehrt
                // und vor allem nichts geworfen
                verify(schrittDao).setzeAbschluss(SCHRITT_ID, JETZT.toEpochMilli())
                verify(vorgangDao, never()).beruehre(VORGANG_ID, JETZT.toEpochMilli())
            }
    }

    // ------------------------------------------------------------------
    // Fotos
    // ------------------------------------------------------------------

    @Nested
    inner class `Foto anhaengen` {

        @Test
        fun `haengt das Foto an die naechste freie Position mit Default-Label Bauteil`() =
            runTest {
                // Given: der Schritt hat bereits Fotos, naechste freie Position ist 3
                // When: ein Foto angehaengt wird
                repository.fotoAnhaengen(SCHRITT_ID, "/photos/neu.jpg")

                // Then: Position aus dem DAO, Aufnahmezeit aus der Uhr, Label = Bauteil
                verify(fotoDao).einfuegen(
                    SchrittFoto(
                        schrittId = SCHRITT_ID,
                        pfad = "/photos/neu.jpg",
                        reihenfolge = NAECHSTE_REIHENFOLGE,
                        istBauteil = true,
                        istUebersicht = false,
                        istAblageort = false,
                        aufgenommenAm = JETZT
                    )
                )
            }

        @Test
        fun `liefert das Foto mit der vergebenen Id zurueck`() = runTest {
            // Given: die DB vergibt beim Einfuegen eine Id
            // When: ein Foto angehaengt wird
            val foto = repository.fotoAnhaengen(SCHRITT_ID, "/photos/neu.jpg")

            // Then: der Aufrufer bekommt sie zurueck -- ohne sie kann er das Foto
            // weder labeln noch ersetzen
            assertEquals(NEUE_FOTO_ID, foto.id)
            assertEquals("/photos/neu.jpg", foto.pfad)
            assertEquals(NAECHSTE_REIHENFOLGE, foto.reihenfolge)
        }

        @Test
        fun `beginnt beim ersten Foto eines Schritts bei Position 0`() = runTest {
            // Given: ein Schritt ohne Fotos
            whenever(fotoDao.naechsteReihenfolge(SCHRITT_ID)).thenReturn(0)

            // When: das erste Foto angehaengt wird
            val foto = repository.fotoAnhaengen(SCHRITT_ID, "/photos/erstes.jpg")

            // Then: Position 0 -- die Reihenfolge ist 0-basiert und lueckenlos
            assertEquals(0, foto.reihenfolge)
        }
    }

    @Nested
    inner class `Foto loeschen` {

        @Test
        fun `loescht das Foto und schliesst die Luecke dahinter`() = runTest {
            // Given: das Foto steht an Position 2 von Schritt 42
            // When: es geloescht wird
            repository.fotoLoeschen(FOTO_ID)

            // Then: geloescht wird die Zeile, danach ruecken die Fotos hinter
            // Position 2 auf -- sonst reisst die 0-basierte Reihenfolge
            verify(fotoDao).loeschen(FOTO_ID)
            verify(fotoDao).rueckeNach(SCHRITT_ID, BESTEHENDES_FOTO.reihenfolge)
        }

        @Test
        fun `laesst ein unbekanntes Foto unberuehrt und wirft nicht`() = runTest {
            // Given: zu der Id gibt es keine Zeile mehr (Doppel-Tap auf "Loeschen")
            whenever(fotoDao.findById(FOTO_ID)).thenReturn(null)

            // When: trotzdem geloescht wird
            repository.fotoLoeschen(FOTO_ID)

            // Then: kein Loeschen, kein Aufruecken fremder Fotos, kein Zeitstempel
            verify(fotoDao, never()).loeschen(FOTO_ID)
            verify(fotoDao, never()).rueckeNach(SCHRITT_ID, BESTEHENDES_FOTO.reihenfolge)
            verify(vorgangDao, never()).beruehre(VORGANG_ID, JETZT.toEpochMilli())
        }
    }

    @Nested
    inner class `Foto ersetzen -- Wiederholen` {

        @Test
        fun `das neue Foto uebernimmt Position und alle drei Label des alten`() = runTest {
            // Given: ein Foto an Position 5 mit den Labeln Uebersicht + Ablageort
            val altes = BESTEHENDES_FOTO.copy(
                reihenfolge = 5,
                istBauteil = false,
                istUebersicht = true,
                istAblageort = true
            )
            whenever(fotoDao.findById(FOTO_ID)).thenReturn(altes)

            // When: es durch eine Neuaufnahme ersetzt wird
            repository.fotoErsetzen(FOTO_ID, "/photos/neu.jpg")

            // Then: Position und Label wandern mit, nur Pfad und Zeitpunkt sind neu
            // (governance.md, "Wiederholen": das neue Foto uebernimmt die reihenfolge)
            verify(fotoDao).einfuegen(
                SchrittFoto(
                    schrittId = SCHRITT_ID,
                    pfad = "/photos/neu.jpg",
                    reihenfolge = 5,
                    istBauteil = false,
                    istUebersicht = true,
                    istAblageort = true,
                    aufgenommenAm = JETZT
                )
            )
        }

        @Test
        fun `sichert das neue Foto bevor das alte geloescht wird`() = runTest {
            // Given: ein bestehendes Foto
            // When: es ersetzt wird
            repository.fotoErsetzen(FOTO_ID, "/photos/neu.jpg")

            // Then: erst einfuegen, dann loeschen. Umgekehrt waere das alte Foto bei
            // einem Abbruch dazwischen verloren (Quality Goal Zuverlaessigkeit)
            val reihenfolge = inOrder(fotoDao)
            reihenfolge.verify(fotoDao).einfuegen(beliebigesFoto())
            reihenfolge.verify(fotoDao).loeschen(FOTO_ID)
        }

        @Test
        fun `liefert das neue Foto mit der vergebenen Id zurueck`() = runTest {
            // Given: die DB vergibt beim Einfuegen eine Id
            // When: ersetzt wird
            val neues = repository.fotoErsetzen(FOTO_ID, "/photos/neu.jpg")

            // Then: der Aufrufer arbeitet ab jetzt mit der neuen Zeile weiter
            assertEquals(NEUE_FOTO_ID, neues?.id)
            assertEquals("/photos/neu.jpg", neues?.pfad)
        }

        @Test
        fun `tut bei unbekanntem Foto nichts und liefert null`() = runTest {
            // Given: zu der Id gibt es keine Zeile mehr
            whenever(fotoDao.findById(FOTO_ID)).thenReturn(null)

            // When: ersetzt werden soll
            val neues = repository.fotoErsetzen(FOTO_ID, "/photos/neu.jpg")

            // Then: kein Datensatz entsteht, keiner verschwindet
            assertNull(neues)
            verify(fotoDao, never()).einfuegen(beliebigesFoto())
            verify(fotoDao, never()).loeschen(FOTO_ID)
        }
    }

    // ------------------------------------------------------------------
    // Schritte
    // ------------------------------------------------------------------

    @Nested
    inner class `Schritt anlegen` {

        @Test
        fun `nimmt die naechste Schrittnummer und startet offen`() = runTest {
            // Given: der Vorgang hat Schritte bis Nummer 7, naechste ist 8
            // When: ein Schritt angelegt wird
            val schritt = repository.schrittAnlegen(VORGANG_ID)

            // Then: Nummer aus dem DAO, Startzeit aus der Uhr, noch nicht abgeschlossen.
            // Die Nummer wird nie wiederverwendet -- sie klebt physisch am Ablageort
            verify(schrittDao).einfuegen(
                Schritt(
                    reparaturvorgangId = VORGANG_ID,
                    schrittNummer = NAECHSTE_NUMMER,
                    gestartetAm = JETZT
                )
            )
            assertEquals(NEUE_SCHRITT_ID, schritt.id)
            assertEquals(NAECHSTE_NUMMER, schritt.schrittNummer)
            assertNull(schritt.abgeschlossenAm)
        }

        @Test
        fun `stempelt die Startzeit zum Zeitpunkt des Anlegens`() = runTest {
            // Given: die Uhr ist weitergelaufen, seit das Repository gebaut wurde
            uhrzeit = SPAETER

            // When: ein Schritt angelegt wird
            val schritt = repository.schrittAnlegen(VORGANG_ID)

            // Then: gestartetAm ist die Aktionszeit, nicht die Bauzeit des Repositories
            assertEquals(SPAETER, schritt.gestartetAm)
            verify(vorgangDao).beruehre(VORGANG_ID, SPAETER.toEpochMilli())
        }
    }

    // ------------------------------------------------------------------
    // Lesewege
    // ------------------------------------------------------------------

    @Nested
    inner class `Schritte mit Fotos` {

        @Test
        fun `gibt die Fotos nach Reihenfolge sortiert heraus`() = runTest {
            // Given: Room sortiert @Relation-Listen nicht -- der DAO liefert sie
            // in beliebiger Reihenfolge
            whenever(schrittDao.beobachteSchritteMitFotos(VORGANG_ID)).thenReturn(
                flowOf(listOf(schrittMitFotos(schrittNummer = 1, positionen = listOf(2, 0, 1))))
            )

            // When: der Schritt-Browser die Schritte beobachtet
            repository.beobachteSchritteMitFotos(VORGANG_ID).test {
                val fotos = awaitItem().single().fotos

                // Then: aufsteigend nach reihenfolge -- das Karussell blaettert sonst
                // in Zufallsreihenfolge
                assertEquals(listOf(0, 1, 2), fotos.map { it.reihenfolge })
                assertEquals(
                    listOf("/photos/1-0.jpg", "/photos/1-1.jpg", "/photos/1-2.jpg"),
                    fotos.map { it.pfad }
                )
                awaitComplete()
            }
        }

        @Test
        fun `laesst die Reihenfolge der Schritte unangetastet`() = runTest {
            // Given: der DAO liefert die Schritte bereits nach Schrittnummer sortiert
            whenever(schrittDao.beobachteSchritteMitFotos(VORGANG_ID)).thenReturn(
                flowOf(
                    listOf(
                        schrittMitFotos(schrittNummer = 1, positionen = listOf(1, 0)),
                        schrittMitFotos(schrittNummer = 2, positionen = listOf(0))
                    )
                )
            )

            // When: beobachtet wird
            repository.beobachteSchritteMitFotos(VORGANG_ID).test {
                // Then: das Sortieren der Fotos ruehrt die Schrittfolge nicht an
                assertEquals(listOf(1, 2), awaitItem().map { it.schritt.schrittNummer })
                awaitComplete()
            }
        }

        @Test
        fun `sortiert die Fotos auch auf dem Einmal-Leseweg`() = runTest {
            // Given: derselbe unsortierte DAO-Stand, einmalig abgefragt
            whenever(schrittDao.holeSchritteMitFotos(VORGANG_ID)).thenReturn(
                listOf(schrittMitFotos(schrittNummer = 1, positionen = listOf(2, 0, 1)))
            )

            // When: einmalig geladen wird
            val schritte = repository.holeSchritteMitFotos(VORGANG_ID)

            // Then: gleiche Zusage wie beim Flow -- sonst haengt die Sortierung davon
            // ab, welchen Weg der Consumer waehlt
            assertEquals(listOf(0, 1, 2), schritte.single().fotos.map { it.reihenfolge })
        }

        @Test
        fun `kommt mit einem Schritt ohne Fotos zurecht`() = runTest {
            // Given: ein frisch angelegter Schritt, die Kamera wurde abgebrochen
            whenever(schrittDao.beobachteSchritteMitFotos(VORGANG_ID)).thenReturn(
                flowOf(listOf(schrittMitFotos(schrittNummer = 1, positionen = emptyList())))
            )

            // When: beobachtet wird
            repository.beobachteSchritteMitFotos(VORGANG_ID).test {
                // Then: leere Fotoliste statt Fehler
                assertTrue(awaitItem().single().fotos.isEmpty())
                awaitComplete()
            }
        }
    }

    @Nested
    inner class `Vorgangslisten` {

        @Test
        fun `offene und archivierte Liste fragen ihren eigenen Status ab`() = runTest {
            // Given: beide Status liefern unterscheidbare Vorgaenge
            whenever(vorgangDao.beobachteNachStatus(VorgangStatus.OFFEN))
                .thenReturn(flowOf(listOf(vorgang("OFFEN-1", VorgangStatus.OFFEN))))
            whenever(vorgangDao.beobachteNachStatus(VorgangStatus.ARCHIVIERT))
                .thenReturn(flowOf(listOf(vorgang("ARCHIV-1", VorgangStatus.ARCHIVIERT))))

            // When / Then: jede Liste zeigt ihren eigenen Topf -- ein vertauschter
            // Status wuerde archivierte Vorgaenge in die Arbeitsliste holen
            repository.beobachteOffeneVorgaenge().test {
                assertEquals(listOf("OFFEN-1"), awaitItem().map { it.auftragsnummer })
                awaitComplete()
            }
            repository.beobachteArchivierteVorgaenge().test {
                assertEquals(listOf("ARCHIV-1"), awaitItem().map { it.auftragsnummer })
                awaitComplete()
            }
        }

        @Test
        fun `Listen mit Anzahl reichen Status und Jetzt-Zeitpunkt an die Abfrage durch`() =
            runTest {
                // Given: die Projektion summiert laufende Zeitmessungen gegen "jetzt"
                whenever(
                    vorgangDao.beobachteNachStatusMitAnzahl(
                        VorgangStatus.OFFEN,
                        JETZT.toEpochMilli()
                    )
                ).thenReturn(flowOf(listOf(mitAnzahl("OFFEN-1", VorgangStatus.OFFEN, 3))))
                whenever(
                    vorgangDao.beobachteNachStatusMitAnzahl(
                        VorgangStatus.ARCHIVIERT,
                        JETZT.toEpochMilli()
                    )
                ).thenReturn(flowOf(listOf(mitAnzahl("ARCHIV-1", VorgangStatus.ARCHIVIERT, 9))))

                // When / Then: beide Listen kommen mit ihrer eigenen Schrittzahl
                repository.beobachteOffeneVorgaengeMitAnzahl().test {
                    assertEquals(3, awaitItem().single().schrittAnzahl)
                    awaitComplete()
                }
                repository.beobachteArchivierteVorgaengeMitAnzahl().test {
                    assertEquals(9, awaitItem().single().schrittAnzahl)
                    awaitComplete()
                }
            }
    }

    // ------------------------------------------------------------------
    // Testdaten und Matcher-Helfer
    // ------------------------------------------------------------------

    private fun schrittMitFotos(schrittNummer: Int, positionen: List<Int>) = SchrittMitFotos(
        schritt = Schritt(
            id = schrittNummer.toLong(),
            reparaturvorgangId = VORGANG_ID,
            schrittNummer = schrittNummer,
            gestartetAm = FRUEHER
        ),
        fotos = positionen.map { position ->
            SchrittFoto(
                id = schrittNummer * 10L + position,
                schrittId = schrittNummer.toLong(),
                pfad = "/photos/$schrittNummer-$position.jpg",
                reihenfolge = position,
                aufgenommenAm = FRUEHER
            )
        }
    )

    private fun vorgang(auftragsnummer: String, status: VorgangStatus) = Reparaturvorgang(
        id = auftragsnummer.hashCode().toLong(),
        auftragsnummer = auftragsnummer,
        status = status,
        erstelltAm = FRUEHER,
        aktualisiertAm = FRUEHER
    )

    private fun mitAnzahl(auftragsnummer: String, status: VorgangStatus, anzahl: Int) =
        ReparaturvorgangMitAnzahl(vorgang(auftragsnummer, status), schrittAnzahl = anzahl)

    private companion object {

        const val VORGANG_ID = 7L
        const val SCHRITT_ID = 42L
        const val FOTO_ID = 99L
        const val NEUE_SCHRITT_ID = 43L
        const val NEUE_FOTO_ID = 100L
        const val NAECHSTE_NUMMER = 8
        const val NAECHSTE_REIHENFOLGE = 3

        val FRUEHER: Instant = Instant.parse("2026-07-20T08:00:00Z")
        val JETZT: Instant = Instant.parse("2026-07-26T10:15:30Z")
        val SPAETER: Instant = Instant.parse("2026-07-26T11:00:00Z")

        val BESTEHENDES_FOTO = SchrittFoto(
            id = FOTO_ID,
            schrittId = SCHRITT_ID,
            pfad = "/photos/alt.jpg",
            reihenfolge = 2,
            aufgenommenAm = FRUEHER
        )

        /**
         * Alle schreibenden Methoden, die ihren Zeitstempel ueber den Trichter
         * `beruehrend()` ziehen: Anzeigename -> Aufruf am Repository.
         *
         * **So erweitert man diese Tabelle:** Kommt eine neue oeffentliche Methode
         * ans Repository, schlaegt zuerst
         * `die oeffentlichen Methoden des Repositories sind vollstaendig einsortiert`
         * fehl. Danach die Methode genau einer Kategorie zuordnen:
         *
         * - sie veraendert Daten und laeuft ueber `beruehrend()` -> hier eintragen,
         *   der Guard-Test prueft sie ab sofort automatisch mit;
         * - sie setzt `aktualisiertAm` selbst -> [EIGENER_ZEITSTEMPEL] plus einen
         *   eigenen Test, der den Zeitstempel prueft;
         * - sie veraendert Daten ohne Zeitstempel -> [OHNE_ZEITSTEMPEL], und der
         *   Grund gehoert als Kommentar dazu;
         * - sie liest nur -> [NUR_LESEND].
         */
        val UEBER_DEN_TRICHTER: List<Pair<String, suspend (ReparaturRepository) -> Unit>> =
            listOf(
                eintrag("schrittAnlegen") { it.schrittAnlegen(VORGANG_ID) },
                eintrag("schrittAbschliessen") { it.schrittAbschliessen(SCHRITT_ID) },
                eintrag("schrittVerwerfen") { it.schrittVerwerfen(SCHRITT_ID) },
                eintrag("setzeEingebaut") { it.setzeEingebaut(SCHRITT_ID, eingebaut = true) },
                eintrag("fotoAnhaengen") { it.fotoAnhaengen(SCHRITT_ID, "/photos/neu.jpg") },
                eintrag("fotoLoeschen") { it.fotoLoeschen(FOTO_ID) },
                eintrag("setzeLabel") {
                    it.setzeLabel(
                        FOTO_ID,
                        istBauteil = false,
                        istUebersicht = true,
                        istAblageort = false
                    )
                },
                eintrag("fotoErsetzen") { it.fotoErsetzen(FOTO_ID, "/photos/neu.jpg") }
            )

        /** Ein Tabelleneintrag: Methodenname und der zugehoerige Aufruf. */
        private fun eintrag(
            name: String,
            aufruf: suspend (ReparaturRepository) -> Unit
        ): Pair<String, suspend (ReparaturRepository) -> Unit> = name to aufruf

        /** Schreibend, aber mit eigenem Zeitstempel statt `beruehre()`. Je ein Test oben. */
        val EIGENER_ZEITSTEMPEL = listOf(
            "erstelleVorgang",
            "aktualisiereVorgang",
            "archiviereVorgang"
        )

        /**
         * Schreibend ohne Zeitstempel -- zulaessig nur, wenn die Zeile danach weg ist.
         * `loescheVorgang` entfernt genau den Vorgang, dessen `aktualisiertAm` man
         * sonst setzen wuerde.
         */
        val OHNE_ZEITSTEMPEL = listOf("loescheVorgang")

        /** Reine Lesewege. */
        val NUR_LESEND = listOf(
            "beobachteOffeneVorgaenge",
            "beobachteArchivierteVorgaenge",
            "beobachteOffeneVorgaengeMitAnzahl",
            "beobachteArchivierteVorgaengeMitAnzahl",
            "findVorgangById",
            "zaehleSchritte",
            "zaehleEingebaute",
            "beobachteSchritte",
            "beobachteSchritteMitFotos",
            "holeSchritteMitFotos",
            "holeSchritte",
            "findSchritt",
            "findUnabgeschlossenenSchritt",
            "holeSchrittIds",
            "beobachteFotos",
            "holeFotos",
            "findFoto",
            "holeAlleFotoPfade",
            "holeAlleFahrzeugFotoPfade"
        )

        /**
         * Mockito-Matcher fuer ein beliebiges [SchrittFoto].
         *
         * `ArgumentMatchers.any(Class)` liefert `null`; Kotlin lehnt das an einem
         * Nicht-Null-Parameter ab, und `org.mockito.kotlin.any()` weicht dann auf
         * Reflection-Instanziierung aus, die an `Instant` scheitert. Der Matcher ist
         * nach dem Aufruf aber bereits registriert -- der zurueckgegebene Wert wird
         * von Mockito nicht angesehen.
         */
        fun beliebigesFoto(): SchrittFoto {
            ArgumentMatchers.any(SchrittFoto::class.java)
            return BESTEHENDES_FOTO
        }

        /** Wie [beliebigesFoto], fuer [Schritt]. */
        fun beliebigerSchritt(): Schritt {
            ArgumentMatchers.any(Schritt::class.java)
            return Schritt(
                reparaturvorgangId = VORGANG_ID,
                schrittNummer = 1,
                gestartetAm = FRUEHER
            )
        }

        /** Wie [beliebigesFoto], fuer [Reparaturvorgang]. */
        fun beliebigerVorgang(): Reparaturvorgang {
            ArgumentMatchers.any(Reparaturvorgang::class.java)
            return Reparaturvorgang(auftragsnummer = "?", erstelltAm = FRUEHER, aktualisiertAm = FRUEHER)
        }
    }
}
