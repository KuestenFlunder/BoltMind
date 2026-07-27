package com.boltmind.app.service.zeiterfassung

import app.cash.turbine.test
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.time.Duration
import java.time.Instant

/**
 * Tests fuer den Timer-Service.
 *
 * Der DAO ist gemockt, die Uhr ist eine feste Funktion ueber [jetzt]. Damit sind
 * Zeitpunkte exakt pruefbar, ohne auf Wanduhrzeit zu warten.
 *
 * Der Mock bildet die Semantik der Room-Query nach: `stoppen()` liefert die Anzahl
 * geaenderter Zeilen, und die Query trifft wegen `WHERE ... AND gestopptAm IS NULL`
 * keine Zeile mehr, wenn die Messung schon gestoppt ist. Genau darauf stuetzt sich
 * die Fehlererkennung in US-005.6.
 *
 * Spec: docs/specs/F-005-zeiterfassung/service.md (US-005.1 bis US-005.6)
 * Design-Entscheidung K-03 (docs/specs/design-system.md): der Timer ist von Hand
 * pausierbar -- dafuer stehen `umschalten()` und `stoppeFallsLaeuft()`.
 */
class ZeiterfassungServiceTest {

    private companion object {
        val T0: Instant = Instant.parse("2026-07-27T08:00:00Z")
        const val REF_ID = 42L
        val TYP = ReferenzTyp.DEMONTAGE_SCHRITT
    }

    private val dao: ZeitMessungDao = mock()

    /** Die "Wanduhr" des Service. Tests schieben sie von Hand vor. */
    private var jetzt: Instant = T0

    private val service = ZeiterfassungService(dao, uhr = { jetzt })

    private fun messung(
        id: Long = 1L,
        referenzId: Long = REF_ID,
        referenzTyp: String = TYP,
        gestartetAm: Instant = T0,
        gestopptAm: Instant? = null
    ) = ZeitMessung(
        id = id,
        referenzId = referenzId,
        referenzTyp = referenzTyp,
        gestartetAm = gestartetAm,
        gestopptAm = gestopptAm
    )

    // ------------------------------------------------------------------------

    @Nested
    inner class `US-005_1 Eine Zeitmessung starten` {

        @Test
        fun `legt eine offene Messung mit dem Zeitpunkt der Uhr an und liefert deren id`() = runTest {
            // Given: keine Messung fuer Referenz 42, Room vergibt die id 77
            whenever(dao.einfuegen(any())).thenReturn(77L)
            jetzt = T0

            // When: der Consumer startet eine Messung
            val id = service.starten(REF_ID, TYP)

            // Then: genau eine Zeile mit gestartetAm = Zeitpunkt des Aufrufs, gestopptAm = null
            val captor = argumentCaptor<ZeitMessung>()
            verify(dao, times(1)).einfuegen(captor.capture())
            val eingefuegt = captor.firstValue
            assertEquals(REF_ID, eingefuegt.referenzId)
            assertEquals(TYP, eingefuegt.referenzTyp)
            assertEquals(T0, eingefuegt.gestartetAm)
            assertNull(eingefuegt.gestopptAm)
            // id bleibt 0, damit Room sie vergibt -- der Service erfindet keine
            assertEquals(0L, eingefuegt.id)
            // And: Rueckgabewert ist die vom DAO vergebene id
            assertEquals(77L, id)
        }

        @Test
        fun `speichert den referenzTyp unveraendert und validiert ihn nicht`() = runTest {
            // Given: ein Typ, den der Service nicht kennt (keine Whitelist erlaubter Typen)
            val fremderTyp = "PRUEFSTAND_LAUF"

            // When
            service.starten(REF_ID, fremderTyp)

            // Then: der String steht unveraendert in der Zeile, keine Exception
            val captor = argumentCaptor<ZeitMessung>()
            verify(dao).einfuegen(captor.capture())
            assertEquals(fremderTyp, captor.firstValue.referenzTyp)
        }

        @Test
        fun `legt eine zweite offene Messung fuer dieselbe Referenz an ohne zu pruefen`() = runTest {
            // Given: fuer 42/DEMONTAGE_SCHRITT laeuft bereits eine Messung
            whenever(dao.findeOffene(REF_ID, TYP)).thenReturn(messung(id = 1L))

            // When: der Consumer startet erneut fuer dieselbe Kombination
            service.starten(REF_ID, TYP)

            // Then: zweite offene Zeile, keine Exception -- "hoechstens eine offene Messung"
            // ist Consumer-Verantwortung, der Service erzwingt sie nicht ...
            val captor = argumentCaptor<ZeitMessung>()
            verify(dao).einfuegen(captor.capture())
            assertNull(captor.firstValue.gestopptAm)
            // ... und fragt dafuer auch nicht vorher nach einer offenen Messung
            verify(dao, never()).findeOffene(any(), any())
            verify(dao, never()).stoppen(any(), any())
        }
    }

    // ------------------------------------------------------------------------

    @Nested
    inner class `US-005_2 Eine laufende Zeitmessung stoppen` {

        @Test
        fun `setzt gestopptAm auf den Zeitpunkt der Uhr und liefert die Dauer`() = runTest {
            // Given: Messung 7 laeuft seit 08:00, jetzt ist 08:12
            jetzt = T0.plus(Duration.ofMinutes(12))
            whenever(dao.stoppen(eq(7L), any())).thenReturn(1)
            whenever(dao.findById(7L)).thenReturn(messung(id = 7L, gestartetAm = T0, gestopptAm = jetzt))

            // When
            val dauer = service.stoppen(7L)

            // Then: der gespeicherte Stopp-Zeitpunkt ist der Zeitpunkt des Aufrufs
            val captor = argumentCaptor<Instant>()
            verify(dao).stoppen(eq(7L), captor.capture())
            assertEquals(jetzt, captor.firstValue)
            // And: der Rueckgabewert ist gestopptAm - gestartetAm
            assertEquals(Duration.ofMinutes(12), dauer)
        }

        @Test
        fun `liefert Duration ZERO wenn Start und Stopp im selben Moment liegen`() = runTest {
            // Given: gestartet und im selben Moment gestoppt
            jetzt = T0
            whenever(dao.stoppen(eq(7L), any())).thenReturn(1)
            whenever(dao.findById(7L)).thenReturn(messung(id = 7L, gestartetAm = T0, gestopptAm = T0))

            // When
            val dauer = service.stoppen(7L)

            // Then: nicht negativ, minimal ZERO
            assertEquals(Duration.ZERO, dauer)
            assertFalse(dauer.isNegative)
        }

        @Test
        fun `stoppt nur die angegebene Messung und laesst die zweite offen`() = runTest {
            // Given: 7 und 8 laufen gleichzeitig
            jetzt = T0.plus(Duration.ofMinutes(3))
            whenever(dao.stoppen(eq(7L), any())).thenReturn(1)
            whenever(dao.findById(7L)).thenReturn(messung(id = 7L, gestartetAm = T0, gestopptAm = jetzt))

            // When: nur 7 wird gestoppt
            service.stoppen(7L)

            // Then: 8 wird nicht angefasst
            verify(dao).stoppen(eq(7L), any())
            verify(dao, never()).stoppen(eq(8L), any())
        }

        @Test
        fun `legt beim Stoppen keine zweite Zeile an`() = runTest {
            // Given: eine laufende Messung
            jetzt = T0.plus(Duration.ofSeconds(30))
            whenever(dao.stoppen(eq(7L), any())).thenReturn(1)
            whenever(dao.findById(7L)).thenReturn(messung(id = 7L, gestartetAm = T0, gestopptAm = jetzt))

            // When
            service.stoppen(7L)

            // Then: Stopp ist ein Update, kein Insert
            verify(dao, never()).einfuegen(any())
        }
    }

    // ------------------------------------------------------------------------

    @Nested
    inner class `US-005_3 Die Dauer einer Messung abfragen` {

        @Test
        fun `liefert die Dauer einer gestoppten Messung`() = runTest {
            // Given: Messung 7 ist gestoppt, 25 Minuten nach dem Start
            whenever(dao.findById(7L))
                .thenReturn(messung(id = 7L, gestartetAm = T0, gestopptAm = T0.plus(Duration.ofMinutes(25))))

            // When / Then
            assertEquals(Duration.ofMinutes(25), service.dauer(7L))
        }

        @Test
        fun `liefert null solange die Messung laeuft`() = runTest {
            // Given: Messung 8 laeuft (gestopptAm = null), die Uhr ist weit weiter
            jetzt = T0.plus(Duration.ofHours(2))
            whenever(dao.findById(8L)).thenReturn(messung(id = 8L, gestartetAm = T0, gestopptAm = null))

            // When / Then: keine bisher verstrichene Zeit, keine Exception
            assertNull(service.dauer(8L))
        }

        @Test
        fun `liefert null bei unbekannter id ohne Exception`() = runTest {
            // Given: zu 999 existiert kein Datensatz
            whenever(dao.findById(999L)).thenReturn(null)

            // When / Then: lesende Aufrufe scheitern leise, schreibende laut (US-005.6)
            assertNull(service.dauer(999L))
        }

        @Test
        fun `liefert bei mehrfachem Aufruf denselben Wert und schreibt nichts`() = runTest {
            // Given: eine gestoppte Messung
            whenever(dao.findById(7L))
                .thenReturn(messung(id = 7L, gestartetAm = T0, gestopptAm = T0.plus(Duration.ofMinutes(9))))

            // When: dreimal abgefragt, die Uhr laeuft zwischendurch weiter
            val erste = service.dauer(7L)
            jetzt = T0.plus(Duration.ofHours(1))
            val zweite = service.dauer(7L)
            val dritte = service.dauer(7L)

            // Then: identischer Wert, frei von Seiteneffekten
            assertEquals(erste, zweite)
            assertEquals(erste, dritte)
            verify(dao, never()).stoppen(any(), any())
            verify(dao, never()).einfuegen(any())
        }

        @Test
        fun `liefert denselben Wert den stoppen zurueckgegeben hat`() = runTest {
            // Given: eine laufende Messung wird nach 90 Sekunden gestoppt
            jetzt = T0.plus(Duration.ofSeconds(90))
            whenever(dao.stoppen(eq(7L), any())).thenReturn(1)
            whenever(dao.findById(7L)).thenReturn(messung(id = 7L, gestartetAm = T0, gestopptAm = jetzt))

            // When
            val ausStoppen = service.stoppen(7L)
            val ausDauer = service.dauer(7L)

            // Then: beide Pfade rechnen gleich
            assertEquals(Duration.ofSeconds(90), ausStoppen)
            assertEquals(ausStoppen, ausDauer)
        }
    }

    // ------------------------------------------------------------------------

    @Nested
    inner class `US-005_4 Alle Messungen zu einer Referenz abfragen` {

        @Test
        fun `emittiert alle Messungen der Referenz einschliesslich der offenen`() = runTest {
            // Given: eine gestoppte und eine offene Messung
            val gestoppt = messung(id = 1L, gestopptAm = T0.plus(Duration.ofMinutes(5)))
            val offen = messung(id = 2L, gestartetAm = T0.plus(Duration.ofMinutes(6)))
            whenever(dao.findByReferenz(REF_ID, TYP)).thenReturn(flowOf(listOf(gestoppt, offen)))

            // When / Then: der Service filtert offene Messungen nicht heraus
            service.messungenFuer(REF_ID, TYP).test {
                assertEquals(listOf(gestoppt, offen), awaitItem())
                awaitComplete()
            }
        }

        @Test
        fun `trennt dieselbe referenzId nach referenzTyp`() = runTest {
            // Given: Referenz 42 hat Messungen in beiden Typen
            val demontage = messung(id = 1L, referenzTyp = ReferenzTyp.DEMONTAGE_SCHRITT)
            val montage = messung(id = 2L, referenzTyp = ReferenzTyp.MONTAGE_SCHRITT)
            whenever(dao.findByReferenz(REF_ID, ReferenzTyp.DEMONTAGE_SCHRITT))
                .thenReturn(flowOf(listOf(demontage)))
            whenever(dao.findByReferenz(REF_ID, ReferenzTyp.MONTAGE_SCHRITT))
                .thenReturn(flowOf(listOf(montage)))

            // When / Then: die referenzId allein ist nicht eindeutig
            service.messungenFuer(REF_ID, ReferenzTyp.MONTAGE_SCHRITT).test {
                assertEquals(listOf(montage), awaitItem())
                awaitComplete()
            }
        }

        @Test
        fun `emittiert eine leere Liste wenn keine Messung existiert`() = runTest {
            // Given: keine Messung zu dieser Referenz
            whenever(dao.findByReferenz(REF_ID, TYP)).thenReturn(flowOf(emptyList()))

            // When / Then: leere Liste, nicht null und keine Exception
            service.messungenFuer(REF_ID, TYP).test {
                val ersteEmission = awaitItem()
                assertNotNull(ersteEmission)
                assertTrue(ersteEmission.isEmpty())
                awaitComplete()
            }
        }

        @Test
        fun `emittiert erneut wenn eine Messung dazukommt`() = runTest {
            // Given: ein Consumer sammelt den Flow
            val quelle = MutableStateFlow(listOf(messung(id = 1L)))
            whenever(dao.findByReferenz(REF_ID, TYP)).thenReturn(quelle)

            service.messungenFuer(REF_ID, TYP).test {
                assertEquals(1, awaitItem().size)

                // When: danach wird eine weitere Messung gestartet
                quelle.value = listOf(messung(id = 1L), messung(id = 2L))

                // Then: der Flow ist live durchgereicht, kein eingefrorener Snapshot
                assertEquals(2, awaitItem().size)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    // ------------------------------------------------------------------------

    @Nested
    inner class `US-005_5 Eine offene Messung uebersteht die App-Unterbrechung` {

        @Test
        fun `raeumt beim Anlegen des Service nichts auf`() {
            // Given: beim App-Start existieren offene Messungen
            val frischerDao: ZeitMessungDao = mock()

            // When: der Service wird initialisiert
            ZeiterfassungService(frischerDao, uhr = { T0 })

            // Then: er stoppt nichts, loescht nichts, liest nichts -- kein Cleanup
            verifyNoInteractions(frischerDao)
        }

        @Test
        fun `rechnet die Zeit mit in der die App nicht lief`() = runTest {
            // Given: Messung lief beim Beenden der App um 08:00, App-Neustart um 11:30
            jetzt = T0.plus(Duration.ofMinutes(210))
            whenever(dao.stoppen(eq(7L), any())).thenReturn(1)
            whenever(dao.findById(7L)).thenReturn(messung(id = 7L, gestartetAm = T0, gestopptAm = jetzt))

            // When: der Consumer stoppt sie nach dem Neustart
            val dauer = service.stoppen(7L)

            // Then: der Timer laeuft konzeptionell durch, die Pause zaehlt mit
            assertEquals(Duration.ofMinutes(210), dauer)
        }
    }

    // ------------------------------------------------------------------------

    @Nested
    inner class `US-005_6 Doppeltes Stoppen als Fehler erkennen` {

        @Test
        fun `wirft IllegalStateException wenn die Messung bereits gestoppt ist`() = runTest {
            // Given: Messung 7 ist gestoppt -- das Update trifft keine Zeile mehr
            val gestopptUm = T0.plus(Duration.ofMinutes(4))
            whenever(dao.stoppen(eq(7L), any())).thenReturn(0)
            whenever(dao.findById(7L)).thenReturn(messung(id = 7L, gestartetAm = T0, gestopptAm = gestopptUm))
            jetzt = T0.plus(Duration.ofMinutes(30))

            // When
            val fehler = assertThrows<IllegalStateException> { service.stoppen(7L) }

            // Then: die Meldung benennt den Fall "bereits gestoppt" unterscheidbar
            val meldung = fehler.message.orEmpty()
            assertTrue(meldung.contains("bereits"), "Meldung war: $meldung")
            assertFalse(meldung.contains("unbekannt"), "Meldung war: $meldung")
            assertTrue(meldung.contains(gestopptUm.toString()), "Meldung war: $meldung")
        }

        @Test
        fun `wirft IllegalStateException mit anderer Meldung bei unbekannter id`() = runTest {
            // Given: zu 999 existiert kein Datensatz
            whenever(dao.stoppen(eq(999L), any())).thenReturn(0)
            whenever(dao.findById(999L)).thenReturn(null)

            // When
            val fehler = assertThrows<IllegalStateException> { service.stoppen(999L) }

            // Then: unterscheidbare Meldung, und es wurde keine Zeile angelegt
            val meldung = fehler.message.orEmpty()
            assertTrue(meldung.contains("unbekannt"), "Meldung war: $meldung")
            assertFalse(meldung.contains("bereits"), "Meldung war: $meldung")
            verify(dao, never()).einfuegen(any())
        }

        @Test
        fun `stuetzt die Fehlererkennung auf die Anzahl geaenderter Zeilen statt auf einen Vorher-Lesevorgang`() =
            runTest {
                // Given: eine laufende Messung, der Stopp gelingt
                jetzt = T0.plus(Duration.ofMinutes(2))
                whenever(dao.stoppen(eq(7L), any())).thenReturn(1)
                whenever(dao.findById(7L)).thenReturn(messung(id = 7L, gestartetAm = T0, gestopptAm = jetzt))

                // When
                service.stoppen(7L)

                // Then: erst das Update, dann genau ein Lesevorgang. Ein Lesen VOR dem
                // Update waere eine Race Condition (zwei Consumer stoppen gleichzeitig,
                // beide sehen "laeuft noch", beide schreiben).
                val reihenfolge = inOrder(dao)
                reihenfolge.verify(dao).stoppen(eq(7L), any())
                reihenfolge.verify(dao).findById(7L)
                verify(dao, times(1)).findById(7L)
            }

        @Test
        fun `laesst den bereits gespeicherten Stopp-Zeitpunkt unveraendert`() = runTest {
            // Given: Messung 7 wurde um 08:04 gestoppt
            val gestopptUm = T0.plus(Duration.ofMinutes(4))
            whenever(dao.stoppen(eq(7L), any())).thenReturn(0)
            whenever(dao.findById(7L)).thenReturn(messung(id = 7L, gestartetAm = T0, gestopptAm = gestopptUm))
            jetzt = T0.plus(Duration.ofHours(3))

            // When: ein zweiter Stopp scheitert
            assertThrows<IllegalStateException> { service.stoppen(7L) }

            // Then: dauer() liefert weiterhin die urspruengliche Duration -- der
            // fehlgeschlagene Aufruf hat die alte Dauer nicht ueberschrieben
            assertEquals(Duration.ofMinutes(4), service.dauer(7L))
        }
    }

    // ------------------------------------------------------------------------

    @Nested
    inner class `Pause-Schalter umschalten (K-03)` {

        @Test
        fun `startet eine Messung und meldet laeuft wenn keine offen ist`() = runTest {
            // Given: fuer die Referenz laeuft nichts
            whenever(dao.findeOffene(REF_ID, TYP)).thenReturn(null)
            jetzt = T0.plus(Duration.ofMinutes(1))

            // When: der Mechaniker tippt den Play-Schalter
            val laeuftJetzt = service.umschalten(REF_ID, TYP)

            // Then: neue offene Messung ab jetzt, Rueckgabe true
            assertTrue(laeuftJetzt)
            val captor = argumentCaptor<ZeitMessung>()
            verify(dao).einfuegen(captor.capture())
            assertEquals(jetzt, captor.firstValue.gestartetAm)
            assertNull(captor.firstValue.gestopptAm)
        }

        @Test
        fun `stoppt die offene Messung und meldet steht`() = runTest {
            // Given: Messung 5 laeuft seit 08:00, jetzt ist 08:07
            jetzt = T0.plus(Duration.ofMinutes(7))
            whenever(dao.findeOffene(REF_ID, TYP)).thenReturn(messung(id = 5L, gestartetAm = T0))
            whenever(dao.stoppen(eq(5L), any())).thenReturn(1)
            whenever(dao.findById(5L)).thenReturn(messung(id = 5L, gestartetAm = T0, gestopptAm = jetzt))

            // When: der Mechaniker tippt den Pause-Schalter
            val laeuftJetzt = service.umschalten(REF_ID, TYP)

            // Then: die offene Messung ist gestoppt, Rueckgabe false
            assertFalse(laeuftJetzt)
            verify(dao).stoppen(eq(5L), any())
            // And: Pause startet keine Folgemessung -- sonst liefe der Timer weiter
            verify(dao, never()).einfuegen(any())
        }

        @Test
        fun `fortsetzen nach Pause legt eine zweite Messung an statt die alte zu oeffnen`() = runTest {
            // Given: eine bereits gestoppte Messung, aktuell laeuft nichts
            whenever(dao.findeOffene(REF_ID, TYP)).thenReturn(null)
            jetzt = T0.plus(Duration.ofMinutes(15))

            // When: der Mechaniker setzt fort
            val laeuftJetzt = service.umschalten(REF_ID, TYP)

            // Then: neues Start/Stopp-Paar, kein Zuruecksetzen von gestopptAm
            assertTrue(laeuftJetzt)
            verify(dao).einfuegen(any())
            verify(dao, never()).stoppen(any(), any())
        }
    }

    // ------------------------------------------------------------------------

    @Nested
    inner class `stoppeFallsLaeuft beim Verlassen eines Schritts` {

        @Test
        fun `liefert null und wirft nicht wenn keine Messung laeuft`() = runTest {
            // Given: fuer die Referenz laeuft nichts (Normalfall beim Verlassen)
            whenever(dao.findeOffene(REF_ID, TYP)).thenReturn(null)

            // When
            val dauer = service.stoppeFallsLaeuft(REF_ID, TYP)

            // Then: kein Fehler, kein Schreibzugriff
            assertNull(dauer)
            verify(dao, never()).stoppen(any(), any())
        }

        @Test
        fun `stoppt die laufende Messung und liefert ihre Dauer`() = runTest {
            // Given: Messung 5 laeuft seit 08:00, jetzt ist 08:20
            jetzt = T0.plus(Duration.ofMinutes(20))
            whenever(dao.findeOffene(REF_ID, TYP)).thenReturn(messung(id = 5L, gestartetAm = T0))
            whenever(dao.stoppen(eq(5L), any())).thenReturn(1)
            whenever(dao.findById(5L)).thenReturn(messung(id = 5L, gestartetAm = T0, gestopptAm = jetzt))

            // When
            val dauer = service.stoppeFallsLaeuft(REF_ID, TYP)

            // Then
            assertEquals(Duration.ofMinutes(20), dauer)
            verify(dao).stoppen(eq(5L), any())
        }

        @Test
        fun `schweigt wo stoppen fuer dieselbe Lage laut scheitert`() = runTest {
            // Given: nichts laeuft, und die id 999 kennt niemand
            whenever(dao.findeOffene(REF_ID, TYP)).thenReturn(null)
            whenever(dao.stoppen(eq(999L), any())).thenReturn(0)
            whenever(dao.findById(999L)).thenReturn(null)

            // When / Then: der gezielte Stopp ist ein Programmierfehler ...
            assertThrows<IllegalStateException> { service.stoppen(999L) }
            // ... der ungezielte beim Verlassen des Schritts ist es nicht
            assertNull(service.stoppeFallsLaeuft(REF_ID, TYP))
        }
    }

    // ------------------------------------------------------------------------

    @Nested
    inner class `gesamtdauer einer Referenz` {

        @Test
        fun `liefert die Summe mehrerer Start-Stopp-Paare als Duration`() = runTest {
            // Given: drei abgeschlossene Paare, zusammen 30:45 Minuten
            val summe = Duration.ofMinutes(30).plusSeconds(45).toMillis()
            whenever(dao.summeMillis(REF_ID, TYP, T0.toEpochMilli())).thenReturn(summe)
            jetzt = T0

            // When / Then
            assertEquals(Duration.ofSeconds(1845), service.gesamtdauer(REF_ID, TYP))
        }

        @Test
        fun `liefert Duration ZERO wenn keine Messung existiert`() = runTest {
            // Given: keine Messung -- die Query liefert 0, nicht null
            whenever(dao.summeMillis(REF_ID, TYP, T0.toEpochMilli())).thenReturn(0L)
            jetzt = T0

            // When / Then: ZERO statt null oder Exception
            assertEquals(Duration.ZERO, service.gesamtdauer(REF_ID, TYP))
        }

        @Test
        fun `rechnet eine laufende Messung bis zum aktuellen Uhrzeitpunkt`() = runTest {
            // Given: eine seit 08:00 laufende Messung. Die Query bekommt "jetzt"
            // uebergeben und rechnet die offene Zeile bis dahin.
            val nachFuenf = T0.plus(Duration.ofMinutes(5))
            val nachZwanzig = T0.plus(Duration.ofMinutes(20))
            whenever(dao.summeMillis(REF_ID, TYP, nachFuenf.toEpochMilli()))
                .thenReturn(Duration.ofMinutes(5).toMillis())
            whenever(dao.summeMillis(REF_ID, TYP, nachZwanzig.toEpochMilli()))
                .thenReturn(Duration.ofMinutes(20).toMillis())

            // When: zweimal abgefragt, dazwischen laeuft die Uhr weiter
            jetzt = nachFuenf
            val frueh = service.gesamtdauer(REF_ID, TYP)
            jetzt = nachZwanzig
            val spaet = service.gesamtdauer(REF_ID, TYP)

            // Then: die Uhr wird je Aufruf neu gelesen, die Summe waechst mit
            assertEquals(Duration.ofMinutes(5), frueh)
            assertEquals(Duration.ofMinutes(20), spaet)
        }
    }

    // ------------------------------------------------------------------------

    @Nested
    inner class `stoppeAlleOffenen beim Verlassen eines Vorgangs` {

        @Test
        fun `stoppt jede offene Messung`() = runTest {
            // Given: zwei offene Messungen ueber verschiedene Referenzen
            jetzt = T0.plus(Duration.ofMinutes(10))
            val offen1 = messung(id = 1L, referenzId = 42L, gestartetAm = T0)
            val offen2 = messung(id = 2L, referenzId = 43L, gestartetAm = T0)
            whenever(dao.holeAlleOffenen()).thenReturn(listOf(offen1, offen2))
            whenever(dao.stoppen(eq(1L), any())).thenReturn(1)
            whenever(dao.stoppen(eq(2L), any())).thenReturn(1)
            whenever(dao.findById(1L)).thenReturn(offen1.copy(gestopptAm = jetzt))
            whenever(dao.findById(2L)).thenReturn(offen2.copy(gestopptAm = jetzt))

            // When
            service.stoppeAlleOffenen()

            // Then: beide gestoppt, jeweils mit dem Zeitpunkt der Uhr
            verify(dao).stoppen(1L, jetzt)
            verify(dao).stoppen(2L, jetzt)
        }

        @Test
        fun `stoppt die uebrigen auch wenn eine Messung dabei scheitert`() = runTest {
            // Given: Messung 1 wurde zwischenzeitlich schon gestoppt -> stoppen() wirft
            jetzt = T0.plus(Duration.ofMinutes(10))
            val schon = messung(id = 1L, gestartetAm = T0)
            val offen = messung(id = 2L, referenzId = 43L, gestartetAm = T0)
            whenever(dao.holeAlleOffenen()).thenReturn(listOf(schon, offen))
            whenever(dao.stoppen(eq(1L), any())).thenReturn(0)
            whenever(dao.findById(1L)).thenReturn(schon.copy(gestopptAm = T0.plus(Duration.ofMinutes(1))))
            whenever(dao.stoppen(eq(2L), any())).thenReturn(1)
            whenever(dao.findById(2L)).thenReturn(offen.copy(gestopptAm = jetzt))

            // When: der Aufruf darf nicht durchschlagen
            service.stoppeAlleOffenen()

            // Then: die zweite Messung wurde trotzdem gestoppt
            verify(dao).stoppen(2L, jetzt)
        }

        @Test
        fun `tut nichts wenn keine Messung offen ist`() = runTest {
            // Given: alles gestoppt
            whenever(dao.holeAlleOffenen()).thenReturn(emptyList())

            // When
            service.stoppeAlleOffenen()

            // Then: kein Schreibzugriff
            verify(dao, never()).stoppen(any(), any())
        }
    }
}
