package com.boltmind.app.feature.neuervorgang

import com.boltmind.app.data.foto.FotoManager
import com.boltmind.app.data.repository.ReparaturRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyBlocking
import java.io.File

/**
 * Tests fuer den Anlage-Flow (F-002).
 *
 * Hier steht bewusst eine **Negativ**-Aussage im Mittelpunkt: "LOS GEHT'S" legt
 * keinen Schritt an. Das sieht nach einer Nichtigkeit aus, ist aber die Naht
 * zwischen F-002 und F-003. Legte F-002 Schritt 1 selbst an, faende der Browser
 * einen offenen Schritt vor, hielte das fuer eine Fortsetzung und zeigte dem
 * Mechaniker statt der Kamera eine leere Maske -- genau der Fehler, den
 * `anlegen.md` AK 2 seit dem 2026-08-01 ausschliesst.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class NeuerVorgangViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private lateinit var repository: ReparaturRepository
    private lateinit var fotoManager: FotoManager

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repository = mock {
            onBlocking { erstelleVorgang(any()) } doReturn VORGANG_ID
        }
        fotoManager = mock {
            on { erstelleZieldatei(any()) } doReturn File(ZIEL_PFAD)
        }
    }

    @AfterEach
    fun tearDown() = Dispatchers.resetMain()

    @Nested
    inner class `US-002_2 AK 2 Los geht's uebergibt an die Demontage` {

        @Test
        fun `legt den Vorgang an, aber keinen Schritt`() = runTest(dispatcher) {
            // Given: ein ausgefuelltes Formular
            val anlage = anlegen()
            anlage.onAuftragsnummerGeaendert("2026-0815")

            // When: "LOS GEHT'S"
            anlage.onStartenGetippt()
            testScheduler.runCurrent()

            // Then: der Vorgang steht, der Schritt gehoert F-003
            verifyBlocking(repository) { erstelleVorgang(any()) }
            verifyBlocking(repository, never()) { schrittAnlegen(any()) }
            assertEquals(VORGANG_ID, anlage.uiState.value.gestarteterVorgangId)
        }

        @Test
        fun `legt ohne Auftragsnummer weder Vorgang noch Schritt an`() = runTest(dispatcher) {
            // Given: das Nummernfeld ist leer
            val anlage = anlegen()

            // When: "LOS GEHT'S"
            anlage.onStartenGetippt()
            testScheduler.runCurrent()

            // Then: nur die Fehlerzeile, sonst nichts (AK 4)
            assertTrue(anlage.uiState.value.nummerFehlt)
            assertNull(anlage.uiState.value.gestarteterVorgangId)
            verifyBlocking(repository, never()) { erstelleVorgang(any()) }
            verifyBlocking(repository, never()) { schrittAnlegen(any()) }
        }

        @Test
        fun `legt bei einem zweiten Tap keinen zweiten Vorgang an`() = runTest(dispatcher) {
            // Given: der Vorgang ist bereits gestartet
            val anlage = anlegen()
            anlage.onAuftragsnummerGeaendert("2026-0815")
            anlage.onStartenGetippt()
            testScheduler.runCurrent()

            // When: der Mechaniker tippt mit Handschuhen ein zweites Mal
            anlage.onStartenGetippt()
            testScheduler.runCurrent()

            // Then: es bleibt bei einem Vorgang (AK 3)
            verifyBlocking(repository) { erstelleVorgang(any()) }
            verify(repository, never()).aktualisiereVorgang(any())
        }
    }

    private fun anlegen() = NeuerVorgangViewModel(repository, fotoManager)

    private companion object {
        const val VORGANG_ID = 7L
        const val ZIEL_PFAD = "/photos/fahrzeug.jpg"
    }
}
