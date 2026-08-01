package com.boltmind.app.feature.browser

import com.boltmind.app.data.model.Schritt
import com.boltmind.app.data.model.SchrittMitFotos
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant

/**
 * Die Randpruefung fuer Vor/Zurueck.
 *
 * Sie steht hier und nicht in F-006: Vor/Zurueck gehoert seit dem 2026-07-31 dem
 * Consumer, und mit dem Bedienelement gehoert ihm auch die Frage, wann es
 * inaktiv ist (#127). Die frueher doppelt gefuehrten Flags im Browser-Zustand
 * sind entfallen.
 */
class BrowserUiStateTest {

    private fun schritte(anzahl: Int) = (1..anzahl).map {
        SchrittMitFotos(
            schritt = Schritt(
                id = it.toLong(),
                reparaturvorgangId = 1,
                schrittNummer = it,
                gestartetAm = Instant.EPOCH
            )
        )
    }

    @Nested
    inner class `US-006_1 Vor und Zurueck an den Raendern` {

        @Test
        fun `am ersten Schritt ist Zurueck inaktiv und Weiter aktiv`() {
            val zustand = BrowserUiState(schritte = schritte(5), aktiverIndex = 0)
            assertTrue(zustand.istErsterSchritt)
            assertFalse(zustand.istLetzterSchritt)
        }

        @Test
        fun `am letzten Schritt ist Weiter inaktiv und Zurueck aktiv`() {
            val zustand = BrowserUiState(schritte = schritte(5), aktiverIndex = 4)
            assertFalse(zustand.istErsterSchritt)
            assertTrue(zustand.istLetzterSchritt)
        }

        @Test
        fun `mittendrin sind beide aktiv`() {
            val zustand = BrowserUiState(schritte = schritte(5), aktiverIndex = 2)
            assertFalse(zustand.istErsterSchritt)
            assertFalse(zustand.istLetzterSchritt)
        }

        @Test
        fun `bei genau einem Schritt sind beide Richtungen inaktiv`() {
            // Given: ein Vorgang mit einem einzigen Schritt -- nach F-002 der
            // Normalfall unmittelbar nach der Anlage
            val zustand = BrowserUiState(schritte = schritte(1), aktiverIndex = 0)
            // Then: es gibt weder ein Davor noch ein Danach
            assertTrue(zustand.istErsterSchritt)
            assertTrue(zustand.istLetzterSchritt)
        }

        @Test
        fun `bei leerer Liste sind beide Richtungen inaktiv`() {
            // Given: der Archiv-Fall eines Vorgangs ohne Schritte
            val zustand = BrowserUiState(schritte = emptyList(), aktiverIndex = 0)
            // Then: kein Bedienelement fuehrt irgendwohin. lastIndex ist -1,
            // deshalb ist istLetzterSchritt hier ebenfalls wahr.
            assertTrue(zustand.istErsterSchritt)
            assertTrue(zustand.istLetzterSchritt)
        }
    }
}
