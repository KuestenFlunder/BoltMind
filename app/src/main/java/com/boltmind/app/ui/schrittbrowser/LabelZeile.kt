package com.boltmind.app.ui.schrittbrowser

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.boltmind.app.R
import com.boltmind.app.data.model.SchrittFoto
import com.boltmind.app.ui.components.BoltText
import com.boltmind.app.ui.components.boltKlick
import com.boltmind.app.ui.theme.BoltHintergrund
import com.boltmind.app.ui.theme.BoltLabelAblageortFlaeche
import com.boltmind.app.ui.theme.BoltLabelBauteilFlaeche
import com.boltmind.app.ui.theme.BoltLabelUebersichtFlaeche
import com.boltmind.app.ui.theme.BoltMindDimensions
import com.boltmind.app.ui.theme.BoltMindTheme
import com.boltmind.app.ui.theme.BoltTextGedaempft
import com.boltmind.app.ui.theme.BoltTextLeise
import com.boltmind.app.ui.theme.BoltTextSchwach
import com.boltmind.app.ui.theme.BoltTypo
import com.boltmind.app.ui.theme.BoltWeiss22
import com.boltmind.app.ui.theme.GlasRezepte
import com.boltmind.app.ui.theme.glas
import java.time.Instant

private val LabelReihenfolge = listOf(LabelArt.BAUTEIL, LabelArt.UEBERSICHT, LabelArt.ABLAGEORT)

private val LabelArt.flaeche: Color
    get() = when (this) {
        LabelArt.BAUTEIL -> BoltLabelBauteilFlaeche
        LabelArt.UEBERSICHT -> BoltLabelUebersichtFlaeche
        LabelArt.ABLAGEORT -> BoltLabelAblageortFlaeche
    }

@Composable
private fun LabelArt.text(): String = stringResource(
    when (this) {
        LabelArt.BAUTEIL -> R.string.label_bauteil
        LabelArt.UEBERSICHT -> R.string.label_uebersicht
        LabelArt.ABLAGEORT -> R.string.label_ablageort
    }
)

/**
 * Die drei Foto-Label des sichtbaren Fotos.
 *
 * Sie sind unabhaengig und frei kombinierbar; ein frisch aufgenommenes Foto ist
 * Bauteil. Alle drei abgewaehlt ist ein gueltiger Zustand.
 *
 * Aenderbar sind sie nur in der Demontage. Montage und Archiv zeigen sie als
 * schmale, nicht bedienbare Plaketten.
 */
@Composable
fun LabelZeile(
    foto: SchrittFoto?,
    aenderbar: Boolean,
    modifier: Modifier = Modifier,
    onLabelUmgeschaltet: (SchrittFoto, LabelArt) -> Unit
) {
    if (foto == null) return
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(BoltMindDimensions.touchAbstandMin),
        horizontalAlignment = Alignment.Start
    ) {
        LabelReihenfolge.forEach { art ->
            if (aenderbar) {
                LabelChip(art, foto.hat(art)) { onLabelUmgeschaltet(foto, art) }
            } else {
                LabelPlakette(art, foto.hat(art))
            }
        }
    }
}

@Composable
private fun LabelChip(art: LabelArt, gesetzt: Boolean, onKlick: () -> Unit) {
    val rezept = if (gesetzt) GlasRezepte.labelAn(art.farbe, art.flaeche) else GlasRezepte.labelAus
    Row(
        modifier = Modifier
            .height(BoltMindDimensions.labelChipHoehe)
            .boltKlick(onKlick = onKlick)
            .glas(rezept, RoundedCornerShape(BoltMindDimensions.radiusM), BoltMindDimensions.radiusM)
            .padding(horizontal = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Box(
            Modifier
                .size(BoltMindDimensions.labelKaestchen)
                .background(
                    if (gesetzt) art.farbe else Color.Transparent,
                    RoundedCornerShape(5.dp)
                )
                .border(
                    if (gesetzt) 0.dp else 1.dp,
                    if (gesetzt) Color.Transparent else BoltWeiss22,
                    RoundedCornerShape(5.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (gesetzt) {
                BoltText(
                    text = stringResource(R.string.zeichen_haken),
                    stil = BoltTypo.thumbAnzahl,
                    farbe = BoltHintergrund
                )
            }
        }
        BoltText(
            text = art.text(),
            stil = BoltTypo.labelChip,
            farbe = if (gesetzt) art.farbe else BoltTextSchwach
        )
    }
}

@Composable
private fun LabelPlakette(art: LabelArt, gesetzt: Boolean) {
    Row(
        modifier = Modifier
            .height(BoltMindDimensions.labelChipLesendHoehe)
            .glas(
                GlasRezepte.labelLesend(gesetzt),
                RoundedCornerShape(BoltMindDimensions.radiusS),
                BoltMindDimensions.radiusS
            )
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(BoltMindDimensions.touchAbstandMin)
    ) {
        Box(
            Modifier
                .size(BoltMindDimensions.labelPunkt)
                .background(if (gesetzt) art.farbe else BoltWeiss22, CircleShape)
        )
        BoltText(
            text = art.text(),
            stil = BoltTypo.labelChipLesend,
            farbe = if (gesetzt) BoltTextGedaempft else BoltTextSchwach
        )
    }
}

/** "AM FAHRZEUG GEBLIEBEN" -- kein Ablageort-Foto vorhanden. Nur in der Montage. */
@Composable
fun AmFahrzeugHinweis(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .height(BoltMindDimensions.labelChipHoehe)
            .glas(
                GlasRezepte.labelAus,
                RoundedCornerShape(BoltMindDimensions.radiusM),
                BoltMindDimensions.radiusM
            )
            .padding(horizontal = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BoltText(
            text = stringResource(R.string.montage_am_fahrzeug),
            stil = BoltTypo.labelChip,
            farbe = BoltTextLeise
        )
    }
}

@Preview(widthDp = 240, heightDp = 240, backgroundColor = 0xFF000000, showBackground = true)
@Composable
private fun LabelZeileVorschau() {
    BoltMindTheme {
        Box(Modifier.padding(16.dp)) {
            LabelZeile(
                foto = SchrittFoto(
                    id = 1, schrittId = 1, pfad = "", reihenfolge = 0,
                    istBauteil = true, istUebersicht = false, istAblageort = true,
                    aufgenommenAm = Instant.EPOCH
                ),
                aenderbar = true,
                onLabelUmgeschaltet = { _, _ -> }
            )
        }
    }
}
