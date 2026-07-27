package com.boltmind.app.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ============================================================================
// Glas
//
// Das Design setzt `backdrop-filter: blur(14px)` an 53 Stellen -- das ist der
// Kern seines Erscheinungsbilds. Compose kennt keinen Hintergrund-Filter, also
// wird er hier nachgebaut:
//
//   1. [GlasBuehne] zeichnet den Hintergrund einmal in eine GraphicsLayer und
//      legt davon **eine** unscharfe Kopie an -- einmal pro Frame, nicht einmal
//      pro Glasflaeche.
//   2. Jede Glasflaeche stanzt sich aus dieser Kopie ihren Ausschnitt, versetzt
//      um ihre eigene Position.
//
// Ohne umgebende [GlasBuehne] entfaellt nur die Unschaerfe; Fuellung, Rand und
// Glanz bleiben. Previews und Tests funktionieren dadurch ohne Sonderbehandlung.
//
// Referenz: docs/specs/design-system.md, Abschnitt "Glas-Rezepte"
// ============================================================================

/** Traegt die unscharfe Kopie des Hintergrunds und dessen Ursprung im Fenster. */
@Stable
class GlasQuelle internal constructor(
    internal val unscharf: GraphicsLayer
) {
    internal var ursprung: Offset by mutableStateOf(Offset.Zero)
}

val LocalGlasQuelle = compositionLocalOf<GlasQuelle?> { null }

/**
 * Rahmen fuer einen Screen mit Glasflaechen.
 *
 * [hintergrund] wird scharf gezeichnet und zusaetzlich unscharf zwischengelegt.
 * Alles in [inhalt] kann ueber [glas] darauf zugreifen.
 */
@Composable
fun GlasBuehne(
    modifier: Modifier = Modifier,
    blur: Dp = BoltMindDimensions.glasBlur,
    hintergrund: @Composable BoxScope.() -> Unit,
    inhalt: @Composable BoxScope.() -> Unit
) {
    val scharf = rememberGraphicsLayer()
    val unscharf = rememberGraphicsLayer()
    val quelle = remember(unscharf) { GlasQuelle(unscharf) }
    val blurPx = with(LocalDensity.current) { blur.toPx() }

    Box(
        modifier
            .fillMaxSize()
            .onGloballyPositioned { quelle.ursprung = it.positionInRoot() }
    ) {
        Box(
            Modifier
                .matchParentSize()
                .drawWithContent {
                    scharf.record { this@drawWithContent.drawContent() }
                    drawLayer(scharf)
                    unscharf.renderEffect = BlurEffect(blurPx, blurPx, TileMode.Clamp)
                    unscharf.record { drawLayer(scharf) }
                },
            content = hintergrund
        )
        CompositionLocalProvider(LocalGlasQuelle provides quelle) {
            Box(Modifier.matchParentSize(), content = inhalt)
        }
    }
}

/** Ein farbiges Leuchten hinter einer Glasflaeche. Entspricht einem CSS-Box-Shadow. */
@Immutable
data class Leuchten(
    val farbe: Color,
    val radius: Dp,
    val versatzY: Dp = 0.dp,
    val ausbreitung: Dp = 0.dp
)

/**
 * Ein Glas-Rezept. Die Namen folgen der Extraktion aus dem Prototyp
 * (GN = neutral, GO = orange, GG = gruen, GD = dunkel).
 */
@Immutable
data class GlasRezept(
    val fuellung: Brush,
    val randFarbe: Color,
    val randBreite: Dp = BoltMindDimensions.rahmenDuenn,
    /** `inset 0 1px 0` -- eine ein dp hohe Lichtkante an der Oberkante. */
    val innenGlanz: Color? = null,
    val leuchten: List<Leuchten> = emptyList(),
    val unschaerfe: Boolean = true
) {
    companion object {
        fun einfarbig(
            fuellung: Color,
            randFarbe: Color,
            randBreite: Dp = BoltMindDimensions.rahmenDuenn,
            innenGlanz: Color? = null,
            leuchten: List<Leuchten> = emptyList(),
            unschaerfe: Boolean = true
        ) = GlasRezept(
            fuellung = androidx.compose.ui.graphics.SolidColor(fuellung),
            randFarbe = randFarbe,
            randBreite = randBreite,
            innenGlanz = innenGlanz,
            leuchten = leuchten,
            unschaerfe = unschaerfe
        )
    }
}

/**
 * Stanzt den unscharfen Hintergrund in die Flaeche. Ohne [GlasBuehne] wird nur
 * beschnitten -- die Flaeche bleibt dann einfach getoent statt unscharf.
 */
@Composable
private fun Modifier.hintergrundUnschaerfe(shape: Shape): Modifier {
    val quelle = LocalGlasQuelle.current ?: return this.clip(shape)
    var eigenePosition by remember { mutableStateOf(Offset.Zero) }
    return this
        .onGloballyPositioned { eigenePosition = it.positionInRoot() }
        .clip(shape)
        .drawBehind {
            val versatz = quelle.ursprung - eigenePosition
            translate(versatz.x, versatz.y) {
                drawLayer(quelle.unscharf)
            }
        }
}

/** Zeichnet die Leuchten hinter der Flaeche. Nutzt `setShadowLayer`, damit das
 *  Leuchten ueber die Grenzen der Flaeche hinausreicht. */
private fun DrawScope.zeichneLeuchten(
    leuchten: List<Leuchten>,
    eckRadius: Float,
    rund: Boolean
) {
    if (leuchten.isEmpty()) return
    drawIntoCanvas { canvas ->
        leuchten.forEach { l ->
            val paint = Paint()
            val fw = paint.asFrameworkPaint()
            fw.color = android.graphics.Color.TRANSPARENT
            fw.setShadowLayer(
                l.radius.toPx().coerceAtLeast(0.1f),
                0f,
                l.versatzY.toPx(),
                l.farbe.toArgb()
            )
            val a = l.ausbreitung.toPx()
            if (rund) {
                canvas.drawCircle(
                    androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f),
                    size.minDimension / 2f + a,
                    paint
                )
            } else {
                canvas.drawRoundRect(
                    -a, -a, size.width + a, size.height + a,
                    eckRadius, eckRadius, paint
                )
            }
            fw.clearShadowLayer()
        }
    }
}

/**
 * Wendet ein Glas-Rezept an: Leuchten, Hintergrund-Unschaerfe, Fuellung, Rand
 * und Lichtkante -- in dieser Reihenfolge.
 *
 * [eckRadius] wird nur fuer die Leuchten gebraucht; die Flaeche selbst nutzt
 * [shape].
 */
@Composable
fun Modifier.glas(
    rezept: GlasRezept,
    shape: Shape,
    eckRadius: Dp = 0.dp,
    rund: Boolean = false
): Modifier {
    val radiusPx = with(LocalDensity.current) { eckRadius.toPx() }
    var m = this
    if (rezept.leuchten.isNotEmpty()) {
        m = m.drawBehind { zeichneLeuchten(rezept.leuchten, radiusPx, rund) }
    }
    if (rezept.unschaerfe) {
        m = m.hintergrundUnschaerfe(shape)
    } else {
        m = m.clip(shape)
    }
    m = m.background(rezept.fuellung, shape)
    if (rezept.randBreite > 0.dp) {
        m = m.border(rezept.randBreite, rezept.randFarbe, shape)
    }
    rezept.innenGlanz?.let { glanz ->
        m = m.drawBehind {
            // inset 0 1px 0: eine dp hohe Lichtkante direkt unter der Oberkante
            val h = 1.dp.toPx()
            drawRect(
                color = glanz,
                topLeft = androidx.compose.ui.geometry.Offset(0f, 0f),
                size = androidx.compose.ui.geometry.Size(size.width, h)
            )
        }
    }
    return m
}

/** Bequemer Aufruf mit abgerundetem Rechteck. */
@Composable
fun Modifier.glas(rezept: GlasRezept, eckRadius: Dp): Modifier =
    glas(rezept, RoundedCornerShape(eckRadius), eckRadius, rund = false)
