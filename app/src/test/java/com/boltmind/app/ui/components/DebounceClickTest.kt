package com.boltmind.app.ui.components

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Tests fuer die Debounce-Click-Logik.
 *
 * Der DebounceClickHandler verhindert versehentliche Doppelklicks,
 * indem Clicks innerhalb eines konfigurierbaren Zeitfensters ignoriert werden.
 *
 * Traceability: Issue #47 - NFR Utilities fuer Demontage-UI
 */
class DebounceClickTest {

    @Nested
    inner class `Debounce Grundverhalten` {

        @Test
        fun `erster Click wird ausgefuehrt`() {
            // Given: ein neuer Handler
            val handler = DebounceClickHandler()
            var clickCount = 0

            // When: erster Click
            handler.onClick(currentTime = 1000L) { clickCount++ }

            // Then: Click wird ausgefuehrt
            assertEquals(1, clickCount)
        }

        @Test
        fun `Click innerhalb 300ms wird ignoriert`() {
            // Given: ein Handler mit bereits erfolgtem Click
            val handler = DebounceClickHandler()
            var clickCount = 0
            handler.onClick(currentTime = 1000L) { clickCount++ }

            // When: zweiter Click nach 200ms (innerhalb Debounce)
            handler.onClick(currentTime = 1200L) { clickCount++ }

            // Then: zweiter Click wird ignoriert
            assertEquals(1, clickCount)
        }

        @Test
        fun `Click nach 300ms wird ausgefuehrt`() {
            // Given: ein Handler mit bereits erfolgtem Click
            val handler = DebounceClickHandler()
            var clickCount = 0
            handler.onClick(currentTime = 1000L) { clickCount++ }

            // When: zweiter Click nach exakt 300ms
            handler.onClick(currentTime = 1300L) { clickCount++ }

            // Then: zweiter Click wird ausgefuehrt
            assertEquals(2, clickCount)
        }
    }

    @Nested
    inner class `Konfigurierbare Debounce-Zeit` {

        @Test
        fun `Custom Debounce-Zeit wird respektiert`() {
            // Given: ein Handler mit 500ms Debounce
            val handler = DebounceClickHandler(debounceMs = 500L)
            var clickCount = 0
            handler.onClick(currentTime = 1000L) { clickCount++ }

            // When: Click nach 400ms (innerhalb 500ms Debounce)
            handler.onClick(currentTime = 1400L) { clickCount++ }

            // Then: Click wird ignoriert
            assertEquals(1, clickCount)
        }

        @Test
        fun `Click nach Custom Debounce-Zeit wird ausgefuehrt`() {
            // Given: ein Handler mit 500ms Debounce
            val handler = DebounceClickHandler(debounceMs = 500L)
            var clickCount = 0
            handler.onClick(currentTime = 1000L) { clickCount++ }

            // When: Click nach 500ms
            handler.onClick(currentTime = 1500L) { clickCount++ }

            // Then: Click wird ausgefuehrt
            assertEquals(2, clickCount)
        }
    }

    @Nested
    inner class `Mehrfache Clicks` {

        @Test
        fun `mehrere schnelle Clicks hintereinander werden gefiltert`() {
            // Given: ein Handler
            val handler = DebounceClickHandler()
            var clickCount = 0

            // When: 5 Clicks im 50ms-Abstand
            handler.onClick(currentTime = 1000L) { clickCount++ }
            handler.onClick(currentTime = 1050L) { clickCount++ }
            handler.onClick(currentTime = 1100L) { clickCount++ }
            handler.onClick(currentTime = 1150L) { clickCount++ }
            handler.onClick(currentTime = 1200L) { clickCount++ }

            // Then: nur der erste Click wird ausgefuehrt
            assertEquals(1, clickCount)
        }

        @Test
        fun `Click nach Debounce-Fenster startet neues Fenster`() {
            // Given: ein Handler mit erstem Click
            val handler = DebounceClickHandler()
            var clickCount = 0
            handler.onClick(currentTime = 1000L) { clickCount++ }

            // When: Click nach Debounce, dann schneller Folge-Click
            handler.onClick(currentTime = 1300L) { clickCount++ }
            handler.onClick(currentTime = 1400L) { clickCount++ }

            // Then: Click 1 und 2 ausgefuehrt, Click 3 ignoriert
            assertEquals(2, clickCount)
        }
    }
}
