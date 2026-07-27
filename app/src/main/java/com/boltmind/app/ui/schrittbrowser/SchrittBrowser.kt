package com.boltmind.app.ui.schrittbrowser

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.boltmind.app.R
import com.boltmind.app.ui.components.BoltText
import com.boltmind.app.ui.components.boltKlick
import com.boltmind.app.ui.theme.BoltMindDimensions
import com.boltmind.app.ui.theme.BoltSchwarz00
import com.boltmind.app.ui.theme.BoltSchwarz08
import com.boltmind.app.ui.theme.BoltSchwarz50
import com.boltmind.app.ui.theme.BoltSchwarz80
import com.boltmind.app.ui.theme.BoltSchwarz93
import com.boltmind.app.ui.theme.BoltTextSchwach
import com.boltmind.app.ui.theme.BoltTextWeiss
import com.boltmind.app.ui.theme.BoltTypo
import com.boltmind.app.ui.theme.BoltWeiss26
import com.boltmind.app.ui.theme.GlasRezepte
import com.boltmind.app.ui.theme.glas

/**
 * Der Schritt-Browser (F-006).
 *
 * Zustandslos: der Consumer liefert [zustand] und reagiert auf [aktionen]. Der
 * Browser selbst navigiert nicht und schreibt nichts.
 *
 * Er bringt mit: das Foto-Karussell, die Thumbnail-Leiste, die Label-Spalte, die
 * Punkt-Indikatoren und das Vollbild. Alles Uebrige haengt der Consumer ueber die
 * Slots ein -- die Kopfzeile, was ueber den Labeln steht (Timer, Fortschritt) und
 * die Aktionskreise unten rechts.
 *
 * Spec: docs/specs/F-006-schritt-browser/browser.md
 */
@Composable
fun SchrittBrowser(
    zustand: SchrittBrowserZustand,
    aktionen: SchrittBrowserAktionen,
    modifier: Modifier = Modifier,
    kopfzeile: @Composable BoxScope.() -> Unit = {},
    ueberLabels: @Composable ColumnScope.() -> Unit = {},
    unterLabels: @Composable ColumnScope.() -> Unit = {},
    bedienkreise: @Composable BoxScope.() -> Unit = {}
) {
    Box(modifier.fillMaxSize().background(androidx.compose.ui.graphics.Color.Black)) {

        // --- Bildebene ---------------------------------------------------
        FotoKarussell(
            fotos = zustand.fotos,
            aktuellesFoto = zustand.fotoIndex,
            schrittNummer = zustand.aktiverSchritt?.schritt?.schrittNummer ?: 0,
            modifier = Modifier.fillMaxSize(),
            onFotoGewaehlt = aktionen.onFotoGewaehlt,
            onFotoGetippt = aktionen.onVollbildOeffnen
        )

        // Abdunklung, damit Text auf jedem Foto lesbar bleibt. Zwei Lagen wie im
        // Entwurf: ein radialer Schleier von oben und ein linearer von unten.
        // Eine einzelne senkrechte Lage reicht nicht -- auf hellen Fotos
        // verschwinden Kopfzeile und Label sonst darin.
        Box(
            Modifier
                .fillMaxSize()
                .drawBehind {
                    drawRect(
                        Brush.radialGradient(
                            colorStops = arrayOf(0f to BoltSchwarz80, 0.46f to BoltSchwarz08),
                            center = Offset(size.width / 2f, 0f),
                            radius = size.width * 1.2f
                        )
                    )
                    drawRect(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.00f to BoltSchwarz00,
                                0.46f to BoltSchwarz00,
                                0.72f to BoltSchwarz50,
                                1.00f to BoltSchwarz93
                            )
                        )
                    )
                }
        )

        // --- Bedienebene ---------------------------------------------------
        Box(Modifier.fillMaxSize().safeDrawingPadding()) {

            Box(Modifier.fillMaxWidth().align(Alignment.TopStart), content = kopfzeile)

            ThumbnailLeistenSpalte(
                schritte = zustand.schritte,
                aktiverIndex = zustand.aktiverIndex,
                zeigeErledigt = zustand.zeigeErledigt,
                onSchrittGewaehlt = aktionen.onSchrittGewaehlt,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight(0.46f)
                    .padding(end = 12.dp)
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(
                        start = BoltMindDimensions.screenRand,
                        bottom = BoltMindDimensions.rundbuttonGross + 40.dp
                    )
                    // Breit genug fuer den laengsten Hinweis ("AM FAHRZEUG GEBLIEBEN"),
                    // aber schmal genug, dass die Thumbnail-Leiste rechts frei bleibt.
                    .fillMaxWidth(0.68f),
                verticalArrangement = Arrangement.spacedBy(BoltMindDimensions.touchAbstandMin)
            ) {
                ueberLabels()
                if (zustand.hatFotos) {
                    LabelZeile(
                        foto = zustand.sichtbaresFoto,
                        aenderbar = zustand.betriebsart.labelAenderbar,
                        onLabelUmgeschaltet = aktionen.onLabelUmgeschaltet
                    )
                }
                unterLabels()
                KarussellAnzeige(
                    anzahl = zustand.fotos.size,
                    aktuell = zustand.fotoIndex,
                    zeigeWischHinweis = zustand.zeigeWischHinweis
                )
            }

            // Dekorative Kugel hinter den Aktionskreisen.
            Box(
                Modifier
                    .align(Alignment.BottomEnd)
                    .size(BoltMindDimensions.kugelDurchmesser)
                    .padding(0.dp)
                    .glas(
                        GlasRezepte.kapsel,
                        androidx.compose.foundation.shape.CircleShape,
                        BoltMindDimensions.kugelDurchmesser / 2,
                        rund = true
                    )
            )

            Box(Modifier.align(Alignment.BottomEnd), content = bedienkreise)
        }

        if (zustand.vollbild) {
            VollbildAnsicht(zustand = zustand, aktionen = aktionen)
        }
    }
}

/**
 * Das Vollbild eines Fotos. Schliesst ueber das Kreuz oder die Zurueck-Geste.
 *
 * Der [BackHandler] ist bewusst nur hier aktiv: eine unbedingte Sperre im
 * Demontage-Screen wuerde das Vollbild unschliessbar machen.
 */
@Composable
private fun VollbildAnsicht(
    zustand: SchrittBrowserZustand,
    aktionen: SchrittBrowserAktionen
) {
    BackHandler(enabled = true) { aktionen.onVollbildSchliessen() }

    Box(Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Color.Black)) {
        FotoKarussell(
            fotos = zustand.fotos,
            aktuellesFoto = zustand.fotoIndex,
            schrittNummer = zustand.aktiverSchritt?.schritt?.schrittNummer ?: 0,
            modifier = Modifier.fillMaxSize(),
            zuschnitt = ContentScale.Fit,
            onFotoGewaehlt = aktionen.onFotoGewaehlt,
            onFotoGetippt = {}
        )

        Box(
            Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .background(Brush.verticalGradient(listOf(BoltSchwarz80, BoltSchwarz00)))
                .safeDrawingPadding()
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                BoltText(
                    text = stringResource(
                        R.string.vollbild_schritt,
                        zustand.aktiverSchritt?.schritt?.schrittNummer ?: 0
                    ),
                    stil = BoltTypo.abschlussKennzahlKlein,
                    farbe = BoltTextWeiss
                )
                BoltText(
                    text = zustand.sichtbaresFoto.labelZusammenfassung(),
                    stil = BoltTypo.browserStatus,
                    farbe = BoltTextSchwach
                )
            }
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .size(BoltMindDimensions.vollbildSchliesser)
                    .boltKlick(onKlick = aktionen.onVollbildSchliessen)
                    .glas(
                        GlasRezepte.neutral10,
                        RoundedCornerShape(BoltMindDimensions.radiusL),
                        BoltMindDimensions.radiusL
                    ),
                contentAlignment = Alignment.Center
            ) {
                BoltText(
                    text = stringResource(R.string.zeichen_kreuz),
                    stil = BoltTypo.glyphe,
                    farbe = BoltTextWeiss
                )
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .safeDrawingPadding()
                .padding(bottom = 26.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            repeat(zustand.fotos.size) { i ->
                val an = i == zustand.fotoIndex
                Box(
                    Modifier
                        .size(width = if (an) 30.dp else 16.dp, height = 6.dp)
                        .background(
                            if (an) com.boltmind.app.ui.theme.BoltOrange else BoltWeiss26,
                            RoundedCornerShape(3.dp)
                        )
                )
            }
        }
    }
}

/** "Bauteil · Ablageort" oder, wenn nichts gesetzt ist, ein Hinweis darauf. */
@Composable
private fun com.boltmind.app.data.model.SchrittFoto?.labelZusammenfassung(): String {
    if (this == null) return ""
    val teile = buildList {
        if (istBauteil) add(stringResource(R.string.label_bauteil_lang))
        if (istUebersicht) add(stringResource(R.string.label_uebersicht_lang))
        if (istAblageort) add(stringResource(R.string.label_ablageort_lang))
    }
    return if (teile.isEmpty()) stringResource(R.string.label_ohne) else teile.joinToString(" · ")
}
