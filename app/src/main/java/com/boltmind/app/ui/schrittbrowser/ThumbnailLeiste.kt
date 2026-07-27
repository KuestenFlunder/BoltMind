package com.boltmind.app.ui.schrittbrowser

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.boltmind.app.R
import com.boltmind.app.data.model.SchrittMitFotos
import com.boltmind.app.ui.components.BoltText
import com.boltmind.app.ui.components.boltKlick
import com.boltmind.app.ui.theme.BoltAufGruen
import com.boltmind.app.ui.theme.BoltGruen
import com.boltmind.app.ui.theme.BoltHintergrund
import com.boltmind.app.ui.theme.BoltMindDimensions
import com.boltmind.app.ui.theme.BoltOrange
import com.boltmind.app.ui.theme.BoltPanel60
import com.boltmind.app.ui.theme.BoltPanel90
import com.boltmind.app.ui.theme.BoltSchwarz00
import com.boltmind.app.ui.theme.BoltSchwarz55
import com.boltmind.app.ui.theme.BoltTextRuhig
import com.boltmind.app.ui.theme.BoltTextWeiss
import com.boltmind.app.ui.theme.BoltTypo
import com.boltmind.app.ui.theme.BoltWeiss20
import com.boltmind.app.ui.theme.BoltWeiss22
import com.boltmind.app.ui.theme.BoltWeiss50
import java.io.File

/**
 * Die senkrechte Thumbnail-Leiste am rechten Rand.
 *
 * Sie ist der Hauptweg zwischen den Schritten: ein Tipp auf eine Kachel springt
 * direkt dorthin. Eine Nummerneingabe gibt es bewusst nicht.
 *
 * Der Entwurf zeigt vier Kacheln. Wie viele wirklich passen, rechnet die Leiste
 * aus der verfuegbaren Hoehe aus -- auf kleinen Geraeten waeren vier sonst
 * abgeschnitten.
 */
@Composable
fun ThumbnailLeiste(
    schritte: List<SchrittMitFotos>,
    aktiverIndex: Int,
    zeigeErledigt: Boolean,
    modifier: Modifier = Modifier,
    onSchrittGewaehlt: (Int) -> Unit
) {
    if (schritte.isEmpty()) return

    BoxWithConstraints(modifier) {
        val proKachel = BoltMindDimensions.thumbAktiv + BoltMindDimensions.thumbAbstand
        val passend = (maxHeight / proKachel).toInt().coerceAtLeast(1)
        val sichtbar = minOf(BoltMindDimensions.THUMB_FENSTER, passend, schritte.size)

        // Fenster so schieben, dass der aktive Schritt moeglichst mittig liegt.
        val start = (aktiverIndex - (sichtbar - 1) / 2)
            .coerceIn(0, (schritte.size - sichtbar).coerceAtLeast(0))
        val ueberOben = start
        val ueberUnten = (schritte.size - start - sichtbar).coerceAtLeast(0)

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(
                BoltMindDimensions.thumbAbstand,
                Alignment.CenterVertically
            )
        ) {
            if (ueberOben > 0) UeberlaufZaehler(ueberOben, obenPfeil = true)
            for (i in start until start + sichtbar) {
                Thumbnail(
                    schritt = schritte[i],
                    aktiv = i == aktiverIndex,
                    zeigeErledigt = zeigeErledigt,
                    onKlick = { onSchrittGewaehlt(i) }
                )
            }
            if (ueberUnten > 0) UeberlaufZaehler(ueberUnten, obenPfeil = false)
        }
    }
}

@Composable
private fun UeberlaufZaehler(anzahl: Int, obenPfeil: Boolean) {
    BoltText(
        text = stringResource(
            if (obenPfeil) R.string.browser_ueberlauf_oben else R.string.browser_ueberlauf_unten,
            anzahl
        ),
        stil = BoltTypo.zaehlerMini,
        farbe = BoltWeiss50,
        modifier = Modifier
            .background(BoltPanel60, RoundedCornerShape(BoltMindDimensions.radiusXs))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

@Composable
private fun Thumbnail(
    schritt: SchrittMitFotos,
    aktiv: Boolean,
    zeigeErledigt: Boolean,
    onKlick: () -> Unit
) {
    val kachel = if (aktiv) BoltMindDimensions.thumbAktiv else BoltMindDimensions.thumbInaktiv
    val radius = if (aktiv) BoltMindDimensions.radiusThumbAktiv else BoltMindDimensions.radiusStandard
    val erledigt = zeigeErledigt && schritt.schritt.eingebautBeiMontage
    val mehrFotos = schritt.fotos.size > 1

    // Der aktive Schritt atmet -- im Entwurf ein pulsierender Aussenschein.
    val puls = rememberInfiniteTransition(label = "thumbAtem")
    val atem by puls.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2600), RepeatMode.Restart),
        label = "thumbAtemFortschritt"
    )

    Box(
        modifier = Modifier
            // Optisch bleibt die Kachel klein, damit die Hervorhebung des aktiven
            // Schritts wirkt. Antippbar ist trotzdem das volle Mindestmass.
            .sizeIn(
                minWidth = BoltMindDimensions.thumbTrefferflaeche,
                minHeight = BoltMindDimensions.thumbTrefferflaeche
            )
            .boltKlick(onKlick = onKlick),
        contentAlignment = Alignment.Center
    ) {
        if (aktiv) {
            // Atmender Ring: waechst nach aussen und blendet dabei aus.
            Box(
                Modifier
                    .size(kachel + (20.dp * atem))
                    .alpha((1f - atem) * 0.5f)
                    .border(2.dp, BoltOrange, RoundedCornerShape(radius + (10.dp * atem)))
            )
        }
        Box(
            modifier = Modifier
                .size(kachel)
                .clip(RoundedCornerShape(radius))
                .alpha(if (aktiv) 1f else 0.55f)
                .border(
                    width = if (aktiv) 2.dp else 1.dp,
                    color = if (aktiv) BoltOrange else BoltWeiss20,
                    shape = RoundedCornerShape(radius)
                )
        ) {
            val erstesFoto = schritt.fotos.firstOrNull()
            if (erstesFoto != null) {
                AsyncImage(
                    model = File(erstesFoto.pfad),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(Modifier.fillMaxSize().background(BoltHintergrund))
            }
            // Abdunklung oben, damit die Nummer lesbar bleibt.
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(kachel * 0.55f)
                    .background(Brush.verticalGradient(listOf(BoltSchwarz55, BoltSchwarz00)))
            )
            BoltText(
                text = "%02d".format(schritt.schritt.schrittNummer),
                stil = if (aktiv) BoltTypo.thumbNummerAktiv else BoltTypo.thumbNummer,
                farbe = BoltTextWeiss,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = if (aktiv) 3.dp else 2.dp)
            )
            // Farbstreifen: Ablageort vor Uebersicht vor Bauteil.
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(
                        if (aktiv) BoltMindDimensions.thumbStreifenAktiv
                        else BoltMindDimensions.thumbStreifenInaktiv
                    )
                    .background(schritt.kategorie.farbe)
            )
        }

        if (erledigt) {
            Box(
                Modifier
                    .align(Alignment.TopStart)
                    .offset(x = (-6).dp, y = (-4).dp)
                    .size(BoltMindDimensions.thumbHaken)
                    .background(BoltGruen, CircleShape)
                    .border(2.dp, BoltHintergrund, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                BoltText(
                    text = stringResource(R.string.zeichen_haken),
                    stil = BoltTypo.thumbAnzahl,
                    farbe = BoltAufGruen
                )
            }
        }

        if (mehrFotos) {
            Box(
                Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 5.dp, y = (-6).dp)
                    .sizeIn(minWidth = 19.dp, minHeight = 19.dp)
                    .background(BoltPanel90, RoundedCornerShape(BoltMindDimensions.radiusXs))
                    .border(1.dp, BoltWeiss22, RoundedCornerShape(BoltMindDimensions.radiusXs))
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                BoltText(
                    text = schritt.fotos.size.toString(),
                    stil = BoltTypo.thumbAnzahl,
                    farbe = BoltTextRuhig
                )
            }
        }
    }
}

/** Feste Breite der Leiste -- so bleibt der Bildbereich links davon stabil. */
val ThumbnailLeisteBreite = BoltMindDimensions.thumbTrefferflaeche + 12.dp

@Composable
fun ThumbnailLeistenSpalte(
    schritte: List<SchrittMitFotos>,
    aktiverIndex: Int,
    zeigeErledigt: Boolean,
    modifier: Modifier = Modifier,
    onSchrittGewaehlt: (Int) -> Unit
) {
    ThumbnailLeiste(
        schritte = schritte,
        aktiverIndex = aktiverIndex,
        zeigeErledigt = zeigeErledigt,
        modifier = modifier.width(ThumbnailLeisteBreite),
        onSchrittGewaehlt = onSchrittGewaehlt
    )
}
