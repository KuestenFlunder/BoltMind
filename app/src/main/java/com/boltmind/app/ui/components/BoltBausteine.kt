package com.boltmind.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.boltmind.app.ui.theme.BoltMindDimensions
import com.boltmind.app.ui.theme.BoltTextWeiss
import com.boltmind.app.ui.theme.BoltTypo
import com.boltmind.app.ui.theme.GlasRezept
import com.boltmind.app.ui.theme.glas

/**
 * Der Klick der App.
 *
 * Drei Dinge zugleich: die 300ms-Sperre aus der Governance gegen Doppelauslesung
 * mit Handschuhen, die Stauchung auf 97 Prozent aus dem Entwurf statt eines
 * Material-Ripples, und ein sichtbar abgesenkter Zustand, wenn die Aktion
 * gerade nicht moeglich ist.
 */
@Composable
fun Modifier.boltKlick(
    aktiv: Boolean = true,
    stauchung: Float = 0.97f,
    debounceMs: Long = 300L,
    rolle: Role = Role.Button,
    onKlick: () -> Unit
): Modifier {
    val quelle = remember { MutableInteractionSource() }
    val gedrueckt by quelle.collectIsPressedAsState()
    val skalierung by animateFloatAsState(
        targetValue = if (gedrueckt && aktiv) stauchung else 1f,
        label = "klickStauchung"
    )
    var letzterKlick by remember { mutableLongStateOf(0L) }

    return this
        .scale(skalierung)
        .alpha(if (aktiv) 1f else 0.4f)
        .clickable(
            interactionSource = quelle,
            indication = null,
            enabled = aktiv,
            role = rolle
        ) {
            val jetzt = System.currentTimeMillis()
            if (jetzt - letzterKlick >= debounceMs) {
                letzterKlick = jetzt
                onKlick()
            }
        }
}

/**
 * Eine Glasflaeche mit Rezept.
 *
 * [eckRadius] bestimmt zugleich die Form und den Zuschnitt des Leuchtens.
 */
@Composable
fun GlasFlaeche(
    rezept: GlasRezept,
    eckRadius: Dp,
    modifier: Modifier = Modifier,
    ausrichtung: Alignment = Alignment.Center,
    inhalt: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier.glas(rezept, RoundedCornerShape(eckRadius), eckRadius),
        contentAlignment = ausrichtung,
        content = inhalt
    )
}

/**
 * Ein runder Glas-Button. Das ist die Hauptbedienform im Browser: gross, rund
 * und mit Handschuhen sicher zu treffen.
 */
@Composable
fun Rundbutton(
    rezept: GlasRezept,
    durchmesser: Dp,
    modifier: Modifier = Modifier,
    aktiv: Boolean = true,
    onKlick: () -> Unit,
    inhalt: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .size(durchmesser)
            .boltKlick(aktiv = aktiv, onKlick = onKlick)
            .glas(rezept, CircleShape, durchmesser / 2, rund = true),
        contentAlignment = Alignment.Center
    ) {
        Column2(inhalt)
    }
}

/** Zwei- oder dreizeiliger Inhalt eines Rundbuttons, zentriert. */
@Composable
private fun Column2(inhalt: @Composable () -> Unit) {
    androidx.compose.foundation.layout.Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) { inhalt() }
}

/**
 * Eine breite Glas-Aktion: "LOS GEHT'S", "AB INS ARCHIV", Tabs, FAB.
 */
@Composable
fun GlasAktion(
    rezept: GlasRezept,
    hoehe: Dp,
    eckRadius: Dp,
    modifier: Modifier = Modifier,
    aktiv: Boolean = true,
    stauchung: Float = 0.985f,
    onKlick: () -> Unit,
    inhalt: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier
            .sizeIn(minHeight = hoehe.coerceAtLeast(BoltMindDimensions.touchTargetMin))
            .boltKlick(aktiv = aktiv, stauchung = stauchung, onKlick = onKlick)
            .glas(rezept, RoundedCornerShape(eckRadius), eckRadius),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        content = inhalt
    )
}

/**
 * Beschriftung eines Rundbuttons -- eine grosse Zeile, darunter eine kleine.
 */
@Composable
fun RundbuttonText(
    oben: String,
    unten: String,
    stilOben: TextStyle = BoltTypo.rundbuttonZahl,
    stilUnten: TextStyle = BoltTypo.rundbuttonLabel,
    farbeOben: androidx.compose.ui.graphics.Color = BoltTextWeiss,
    farbeUnten: androidx.compose.ui.graphics.Color = BoltTextWeiss
) {
    BoltText(oben, stilOben, farbeOben)
    BoltText(unten, stilUnten, farbeUnten)
}

/** Text im Duktus der App: keine Umbrueche, exakte Stilvorgabe. */
@Composable
fun BoltText(
    text: String,
    stil: TextStyle,
    farbe: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    maxZeilen: Int = 1,
    ausrichtung: TextAlign? = null
) {
    androidx.compose.material3.Text(
        text = text,
        style = stil,
        color = farbe,
        maxLines = maxZeilen,
        textAlign = ausrichtung,
        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
        modifier = modifier
    )
}

/**
 * Ein Verlauf, der Inhalt am unteren Bildrand ausblendet. Der Entwurf legt ihn
 * unter FAB und Aktionskreise, damit Listeneintraege nicht dagegenstossen.
 */
@Composable
fun AusblendVerlauf(
    modifier: Modifier = Modifier,
    farben: List<androidx.compose.ui.graphics.Color>
) {
    Box(modifier.background(Brush.verticalGradient(farben)))
}

/** Eine reine Form ohne Glas -- Punkte, Streifen, Trennlinien. */
@Composable
fun Flaeche(
    farbe: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    form: Shape = RoundedCornerShape(0.dp)
) {
    Box(modifier.background(farbe, form))
}
