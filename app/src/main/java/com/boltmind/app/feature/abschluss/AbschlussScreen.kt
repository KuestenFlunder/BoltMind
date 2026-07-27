package com.boltmind.app.feature.abschluss

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.boltmind.app.R
import com.boltmind.app.ui.components.BoltText
import com.boltmind.app.ui.components.BoltTexturHintergrund
import com.boltmind.app.ui.components.boltKlick
import com.boltmind.app.ui.theme.BoltErfolgFlaeche
import com.boltmind.app.ui.theme.BoltGruen
import com.boltmind.app.ui.theme.BoltMindDimensions
import com.boltmind.app.ui.theme.BoltMindTheme
import com.boltmind.app.ui.theme.BoltTextPrimaer
import com.boltmind.app.ui.theme.BoltTextSchwach
import com.boltmind.app.ui.theme.BoltTextSchwaecher
import com.boltmind.app.ui.theme.BoltTextWeiss
import com.boltmind.app.ui.theme.BoltTrennlinie
import com.boltmind.app.ui.theme.BoltTypo
import com.boltmind.app.ui.theme.GlasRezepte
import com.boltmind.app.ui.theme.glas

/** Das Easing des Entwurfs: `cubic-bezier(.16, 1, .3, 1)`. */
private val Ausrollen = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)

/**
 * Der Abschluss-Screen der Montage.
 *
 * Er ist der **einzige** Weg ins Archiv. Die Zurueck-Geste fuehrt zum letzten
 * Schritt und archiviert ausdruecklich nicht -- Archivieren nimmt den Vorgang aus
 * der aktiven Liste und braucht mit Handschuhen eine bewusste Bestaetigung.
 */
@Composable
fun AbschlussScreen(
    auftragsnummer: String,
    beschreibung: String?,
    anzahlTeile: Int,
    gemesseneZeit: String,
    modifier: Modifier = Modifier,
    onArchivieren: () -> Unit
) {
    // Der Haken wird gestempelt: aus doppelter Groesse heruntergefahren.
    val stempel = remember { Animatable(0f) }
    val einblenden = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        stempel.animateTo(1f, tween(700, easing = Ausrollen))
    }
    LaunchedEffect(Unit) {
        einblenden.animateTo(1f, tween(500, delayMillis = 150, easing = Ausrollen))
    }

    Box(modifier.fillMaxSize()) {
        BoltTexturHintergrund()

        Column(
            Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(BoltMindDimensions.abstandXl, Alignment.CenterVertically)
        ) {
            Box(
                Modifier
                    .size(BoltMindDimensions.abschlussSiegel)
                    .scale(2.1f - 1.1f * stempel.value)
                    .alpha(stempel.value.coerceIn(0f, 1f))
                    .background(BoltErfolgFlaeche, CircleShape)
                    .border(BoltMindDimensions.rahmenAbschluss, BoltGruen, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                BoltText(
                    text = stringResource(R.string.zeichen_haken),
                    stil = BoltTypo.schrittZifferRiesig.copy(fontSize = BoltTypo.abschlussTitel.fontSize * 1.45f),
                    farbe = BoltGruen
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(BoltMindDimensions.abstandXl),
                modifier = Modifier.alpha(einblenden.value)
            ) {
                BoltText(
                    text = stringResource(R.string.abschluss_titel),
                    stil = BoltTypo.abschlussTitel,
                    farbe = BoltTextWeiss,
                    ausrichtung = TextAlign.Center
                )
                BoltText(
                    text = if (beschreibung.isNullOrBlank()) "#$auftragsnummer"
                    else "#$auftragsnummer · $beschreibung",
                    stil = BoltTypo.abschlussText,
                    farbe = BoltTextSchwach,
                    maxZeilen = 2,
                    ausrichtung = TextAlign.Center
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(26.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Kennzahl(anzahlTeile.toString(), stringResource(R.string.abschluss_teile), gross = true)
                    Box(
                        Modifier
                            .width(1.dp)
                            .fillMaxHeight(0.6f)
                            .background(BoltTrennlinie)
                    )
                    Kennzahl(gemesseneZeit, stringResource(R.string.abschluss_gemessen), gross = false)
                }
            }
        }

        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .safeDrawingPadding()
                .padding(start = 20.dp, end = 20.dp, bottom = 30.dp)
                .fillMaxWidth()
                .height(BoltMindDimensions.archivierenHoehe)
                .boltKlick(stauchung = 0.985f, onKlick = onArchivieren)
                .glas(
                    GlasRezepte.orangeArchiv,
                    RoundedCornerShape(BoltMindDimensions.radiusAktionGross),
                    BoltMindDimensions.radiusAktionGross
                ),
            contentAlignment = Alignment.Center
        ) {
            BoltText(
                text = stringResource(R.string.abschluss_archivieren),
                stil = BoltTypo.aktionGross,
                farbe = BoltTextWeiss
            )
        }
    }
}

@Composable
private fun Kennzahl(wert: String, beschriftung: String, gross: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(BoltMindDimensions.abstandXs)
    ) {
        BoltText(
            text = wert,
            stil = if (gross) BoltTypo.abschlussKennzahl else BoltTypo.abschlussKennzahlKlein,
            farbe = BoltTextPrimaer
        )
        BoltText(
            text = beschriftung,
            stil = BoltTypo.abschlussKennzahlLabel,
            farbe = BoltTextSchwaecher
        )
    }
}

@Preview(widthDp = 372, heightDp = 806, backgroundColor = 0xFF0B0C0D, showBackground = true)
@Composable
private fun AbschlussVorschau() {
    BoltMindTheme {
        AbschlussScreen(
            auftragsnummer = "2026-0815",
            beschreibung = "Bremsen vorne wechseln",
            anzahlTeile = 7,
            gemesseneZeit = "49 min 55 s",
            onArchivieren = {}
        )
    }
}
