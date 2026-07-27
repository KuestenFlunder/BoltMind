package com.boltmind.app.ui.theme

import androidx.compose.ui.unit.dp
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Wacht ueber die Masse, die `docs/specs/governance.md` verbindlich festlegt.
 *
 * Diese Zahlen sind keine Design-Vorliebe, sondern eine Anforderung aus dem
 * ersten Quality Goal: bedienbar mit Handschuhen und oeligen Haenden. Ein
 * spaeterer Tweak auf 48dp soll den Build brechen und nicht stillschweigend
 * durchgehen -- der Verstoss faellt sonst erst in der Werkstatt auf.
 *
 * **Was dieser Test NICHT leistet:** Er belegt die Token-Werte, nicht ihre
 * Verwendung. Ein literales `.height(40.dp)` an einer Aufrufstelle passiert ihn
 * ungeruehrt. Diese Luecke schliesst nur `MindesthoeheTest` in `androidTest`.
 */
@DisplayName("Governance-Masse")
class DimensionenTest {

    @Nested
    @DisplayName("Touch-Targets")
    inner class TouchTargets {

        @Test
        fun `primaere Aktionen sind mindestens 56dp hoch`() {
            assertEquals(
                56.dp,
                BoltMindDimensions.touchTargetMin,
                "governance.md, Abschnitt \"Bedienbarkeit\": 56dp ist das Minimum, " +
                    "nicht ein Richtwert. Wer das senken will, aendert zuerst die Spec."
            )
        }

        @Test
        fun `benachbarte Touch-Targets stehen mindestens 8dp auseinander`() {
            assertEquals(
                8.dp,
                BoltMindDimensions.touchAbstandMin,
                "governance.md, Abschnitt \"Bedienbarkeit\": ohne Abstand trifft ein " +
                    "Handschuh zwei Knoepfe gleichzeitig."
            )
        }
    }

    @Nested
    @DisplayName("Rundbuttons erfuellen das Minimum aus sich heraus")
    inner class Rundbuttons {

        /**
         * `Rundbutton` setzt `.size(durchmesser)` -- eine feste Groesse, kein
         * Minimum. Die Regel wird dort also nicht erzwungen, sondern durch die
         * Wahl der Token eingehalten. Genau deshalb gehoeren die Token hierher:
         * ein zu kleiner Wert waere sonst unbemerkt.
         */
        @Test
        fun `kein Rundbutton-Durchmesser liegt unter dem Minimum`() {
            val durchmesser = mapOf(
                "rundbuttonKlein" to BoltMindDimensions.rundbuttonKlein,
                "rundbuttonMittel" to BoltMindDimensions.rundbuttonMittel,
                "rundbuttonGross" to BoltMindDimensions.rundbuttonGross,
                "rundbuttonArchivZurueck" to BoltMindDimensions.rundbuttonArchivZurueck,
                "rundbuttonArchivWeiter" to BoltMindDimensions.rundbuttonArchivWeiter
            )
            durchmesser.forEach { (name, wert) ->
                assertTrue(
                    wert >= BoltMindDimensions.touchTargetMin,
                    "$name ist $wert und damit kleiner als das Governance-Minimum " +
                        "${BoltMindDimensions.touchTargetMin}."
                )
            }
        }
    }
}
