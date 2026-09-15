package com.boltmind.app.ui.theme

import androidx.compose.ui.unit.dp
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import kotlin.reflect.full.memberProperties

/**
 * Wacht ueber die Uebersetzung von CSS-`box-shadow` in [Leuchten].
 *
 * Der Entwurf schreibt den Ring eines Rundbuttons als
 * `box-shadow: 0 0 0 1px rgba(...)`. Der dritte Wert ist die **Weichzeichnung**
 * (0), der vierte die **Ausbreitung** (1px). Extrahiert wurde er an vier Stellen
 * genau andersherum -- als Weichzeichnung 1dp bei Ausbreitung 0 (#120).
 *
 * Der Unterschied ist im Bild leicht zu uebersehen und schwer zu benennen:
 * statt eines sauberen 1dp-Rings entsteht eine nahezu harte Scheibe in
 * Buttongroesse, von der nur ein duenner Saum herausschaut. Genau deshalb steht
 * die Regel hier als Test und nicht nur als Kommentar.
 */
class GlasRezepteTest {

    private val alleRezepte: List<Pair<String, GlasRezept>> =
        GlasRezepte::class.memberProperties
            .filter { it.returnType.classifier == GlasRezept::class }
            .map { it.name to it.getter.call(GlasRezepte) as GlasRezept }
            .sortedBy { it.first }

    @Nested
    inner class `Ein Ring ist Ausbreitung, keine Weichzeichnung` {

        @Test
        fun `die vier Ring-Leuchten tragen Radius null und Ausbreitung eins`() {
            // Given: die vier Rezepte mit einem 1px-Ring aus dem Entwurf
            val mitRing = listOf(
                "orangeVoll" to GlasRezepte.orangeVoll,
                "orangeArchiv" to GlasRezepte.orangeArchiv,
                "orangeTimer" to GlasRezepte.orangeTimer,
                "gruenVoll" to GlasRezepte.gruenVoll
            )

            // When/Then: das erste Leuchten jedes Rezepts ist der Ring
            mitRing.forEach { (name, rezept) ->
                val ring = rezept.leuchten.first()
                assertEquals(0.dp, ring.radius, "$name: der Ring darf nicht weichzeichnen")
                assertEquals(1.dp, ring.ausbreitung, "$name: dem Ring fehlt die Ausbreitung")
            }
        }

        @TestFactory
        fun `kein Rezept traegt die vertauschte Fassung`(): List<DynamicTest> =
            alleRezepte.map { (name, rezept) ->
                DynamicTest.dynamicTest(name) {
                    // Ein Leuchten mit Radius 1dp und Ausbreitung 0 ist die
                    // Signatur der Fehl-Uebersetzung. Es gibt im Entwurf keinen
                    // Schatten, der so gemeint waere: 1px Weichzeichnung ohne
                    // Ausbreitung waere im Bild nicht von einem Rand zu
                    // unterscheiden -- dafuer gibt es randBreite.
                    val vertauscht = rezept.leuchten.filter {
                        it.radius == 1.dp && it.ausbreitung == 0.dp
                    }
                    assertEquals(
                        emptyList<Leuchten>(),
                        vertauscht,
                        "$name: `0 0 0 1px` gehoert als radius = 0.dp, ausbreitung = 1.dp " +
                            "uebersetzt, nicht umgekehrt"
                    )
                }
            }
    }
}
