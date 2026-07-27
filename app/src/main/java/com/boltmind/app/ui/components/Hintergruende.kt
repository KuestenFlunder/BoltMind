package com.boltmind.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import com.boltmind.app.ui.theme.BoltHintergrund
import com.boltmind.app.ui.theme.BoltMeshBlau
import com.boltmind.app.ui.theme.BoltMeshDunkel90
import com.boltmind.app.ui.theme.BoltMeshDunkel96
import com.boltmind.app.ui.theme.BoltMeshGelb
import com.boltmind.app.ui.theme.BoltMeshGruen
import com.boltmind.app.ui.theme.BoltMeshOrange
import com.boltmind.app.ui.theme.BoltMeshPink
import com.boltmind.app.ui.theme.BoltMeshSchleier10
import com.boltmind.app.ui.theme.BoltMeshSchleier14
import com.boltmind.app.ui.theme.BoltMeshSchleier30
import com.boltmind.app.ui.theme.BoltMeshSchleier40
import com.boltmind.app.ui.theme.BoltMeshSchleier68
import com.boltmind.app.ui.theme.BoltMeshSchleier88

/**
 * Eine Farbwolke des Mesh-Verlaufs.
 *
 * CSS beschreibt sie als `radial-gradient(rx ry at cx cy, farbe, transparent stopp)`.
 * Alle Werte sind Anteile der Flaeche, nicht Pixel -- dadurch waechst der
 * Hintergrund mit dem Geraet mit, statt auf 372x806 festzukleben.
 */
private data class Wolke(
    val farbe: Color,
    val rx: Float,
    val ry: Float,
    val cx: Float,
    val cy: Float,
    val stopp: Float
)

/**
 * Die sieben Wolken der Uebersicht und des Anlage-Screens, in der Reihenfolge
 * des Entwurfs. Die beiden dunklen am Ende druecken die Ecken zurueck, damit die
 * Kopfzeile und der FAB lesbar bleiben.
 */
private val MeshWolken = listOf(
    Wolke(BoltMeshGruen, 0.70f, 0.46f, 0.08f, 0.26f, 0.66f),
    Wolke(BoltMeshBlau, 0.66f, 0.44f, 0.04f, 0.54f, 0.66f),
    Wolke(BoltMeshPink, 0.64f, 0.42f, 0.74f, 0.30f, 0.64f),
    Wolke(BoltMeshOrange, 0.78f, 0.50f, 0.34f, 0.74f, 0.64f),
    Wolke(BoltMeshGelb, 0.52f, 0.34f, 0.22f, 0.88f, 0.62f),
    Wolke(BoltMeshDunkel96, 0.74f, 0.48f, 0.92f, 0.88f, 0.62f),
    Wolke(BoltMeshDunkel90, 0.60f, 0.40f, 0.60f, 0.04f, 0.66f)
)

/**
 * Der abdunkelnde Schleier ueber dem Mesh. Ohne ihn ist heller Text auf den
 * Farbwolken nicht mehr lesbar.
 */
private val SchleierStopps = arrayOf(
    0.00f to BoltMeshSchleier88,
    0.20f to BoltMeshSchleier68,
    0.40f to BoltMeshSchleier40,
    0.62f to BoltMeshSchleier14,
    0.82f to BoltMeshSchleier10,
    1.00f to BoltMeshSchleier30
)

private fun DrawScope.zeichneWolke(w: Wolke) {
    val rx = w.rx * size.width
    val ry = w.ry * size.height
    val mitte = Offset(w.cx * size.width, w.cy * size.height)
    // Compose kennt nur kreisrunde Radialverlaeufe. Der CSS-Verlauf ist eine
    // Ellipse, also wird beim Zeichnen in Y gestaucht.
    withTransform({ scale(1f, ry / rx, pivot = mitte) }) {
        drawCircle(
            brush = Brush.radialGradient(
                colorStops = arrayOf(0f to w.farbe, w.stopp to Color.Transparent),
                center = mitte,
                radius = rx
            ),
            radius = rx,
            center = mitte
        )
    }
}

/**
 * Der Mesh-Hintergrund der Uebersicht und des Anlage-Screens: sieben Farbwolken,
 * darueber der abdunkelnde Schleier fuer die Lesbarkeit.
 *
 * Ohne Stahltextur. Der Prototyp legt sie zwar auch innerhalb der App ueber den
 * Verlauf (Deckkraft .30 bzw. .28 im Overlay-Modus), der auffaellige Stahl liegt
 * dort aber im Praesentationsrahmen um das Telefon-Mockup und gehoert gar nicht
 * zur App. Produktentscheidung vom 2026-07-27: der Verlauf traegt allein.
 */
@Composable
fun BoltMeshHintergrund(modifier: Modifier = Modifier) {
    Canvas(modifier.fillMaxSize()) {
        drawRect(BoltHintergrund)
        MeshWolken.forEach { zeichneWolke(it) }
        drawRect(Brush.verticalGradient(colorStops = SchleierStopps))
    }
}

/** Ruhige Grundflaeche fuer Splash und Abschluss. */
@Composable
fun BoltRuhigerHintergrund(modifier: Modifier = Modifier) {
    Canvas(modifier.fillMaxSize()) { drawRect(BoltHintergrund) }
}

/** Reine Grundflaeche -- Browser, Kamera und Vollbild liegen auf Schwarz. */
@Composable
fun BoltSchwarzHintergrund(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize()) { drawRect(Color.Black) }
    }
}
