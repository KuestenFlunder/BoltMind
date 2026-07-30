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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.boltmind.app.ui.components.FotoPlatzhalter
import coil.compose.SubcomposeAsyncImage
import com.boltmind.app.R
import com.boltmind.app.data.model.SchrittFoto
import com.boltmind.app.ui.components.BoltText
import com.boltmind.app.ui.components.Flaeche
import com.boltmind.app.ui.components.boltKlick
import com.boltmind.app.ui.theme.BoltHintergrund
import com.boltmind.app.ui.theme.BoltMindDimensions
import com.boltmind.app.ui.theme.BoltOrange
import com.boltmind.app.ui.theme.BoltOrangeHell
import com.boltmind.app.ui.theme.BoltRahmenGestrichelt
import com.boltmind.app.ui.theme.BoltTextSchwach
import com.boltmind.app.ui.theme.BoltTextSchwaecher
import com.boltmind.app.ui.theme.BoltTextSchwaechst
import com.boltmind.app.ui.theme.BoltTypo
import com.boltmind.app.ui.theme.BoltWeiss35
import com.boltmind.app.ui.theme.BoltMindTheme
import java.io.File

/**
 * Das Foto-Karussell eines Schritts.
 *
 * Waagerechtes Wischen wechselt das **Foto innerhalb des Schritts**, nie den
 * Schritt -- das ist eine verbindliche Produktentscheidung. Der Schrittwechsel
 * laeuft ausschliesslich ueber die Thumbnail-Leiste und Vor/Zurueck.
 *
 * Tippen oeffnet das Vollbild.
 */
@Composable
fun FotoKarussell(
    fotos: List<SchrittFoto>,
    aktuellesFoto: Int,
    schrittNummer: Int,
    modifier: Modifier = Modifier,
    zuschnitt: ContentScale = ContentScale.Crop,
    onFotoGewaehlt: (Int) -> Unit,
    onFotoGetippt: () -> Unit
) {
    if (fotos.isEmpty()) {
        LeererSchritt(schrittNummer, modifier)
        return
    }

    val fehlendBeschreibung = stringResource(R.string.browser_foto_fehlt_beschreibung)
    val pagerZustand = rememberPagerState(
        initialPage = aktuellesFoto.coerceIn(0, fotos.lastIndex),
        pageCount = { fotos.size }
    )

    // Sprung von aussen, etwa nach einem Schrittwechsel oder einer Neuaufnahme.
    LaunchedEffect(aktuellesFoto, fotos.size) {
        val ziel = aktuellesFoto.coerceIn(0, fotos.lastIndex)
        if (pagerZustand.currentPage != ziel) pagerZustand.scrollToPage(ziel)
    }

    // Wischen des Nutzers zurueckmelden.
    LaunchedEffect(pagerZustand) {
        snapshotFlow { pagerZustand.settledPage }.collect { onFotoGewaehlt(it) }
    }

    HorizontalPager(state = pagerZustand, modifier = modifier) { seite ->
        // SubcomposeAsyncImage statt AsyncImage: dessen `error`-Painter erbt den
        // contentScale des Erfolgsbildes (hier Crop) und wuerde das quadratische
        // App-Icon bildschirmfuellend beschneiden. Die Slot-API trennt das.
        SubcomposeAsyncImage(
            model = File(fotos[seite].pfad),
            contentDescription = stringResource(R.string.browser_foto_beschreibung),
            contentScale = zuschnitt,
            error = {
                FotoPlatzhalter(
                    Modifier.semantics {
                        contentDescription = fehlendBeschreibung
                    }
                )
            },
            modifier = Modifier
                .fillMaxSize()
                .boltKlick(stauchung = 1f, onKlick = onFotoGetippt)
        )
    }
}

/** Ein Schritt ohne Fotos -- waehrend der Arbeit ein gueltiger Zustand. */
@Composable
private fun LeererSchritt(schrittNummer: Int, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BoltHintergrund),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterVertically)
    ) {
        Box(
            Modifier
                .size(72.dp)
                .border(2.dp, BoltRahmenGestrichelt, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            BoltText(
                text = "%02d".format(schrittNummer),
                stil = BoltTypo.abschlussKennzahl,
                farbe = BoltTextSchwaechst
            )
        }
        BoltText(
            text = stringResource(R.string.browser_keine_fotos),
            stil = BoltTypo.vorgangTitel,
            farbe = BoltTextSchwaecher
        )
    }
}

/**
 * Punkt-Indikatoren, Zaehler und Wisch-Hinweis unter dem Karussell.
 */
@Composable
fun KarussellAnzeige(
    anzahl: Int,
    aktuell: Int,
    zeigeWischHinweis: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            repeat(anzahl) { i ->
                val an = i == aktuell
                Flaeche(
                    farbe = if (an) BoltOrange else BoltWeiss35,
                    form = RoundedCornerShape(3.dp),
                    modifier = Modifier
                        .size(
                            width = if (an) BoltMindDimensions.punktBreiteAktiv
                            else BoltMindDimensions.punktBreite,
                            height = BoltMindDimensions.punktHoehe
                        )
                )
            }
        }
        BoltText(
            text = if (anzahl == 0) stringResource(R.string.browser_kein_foto)
            else stringResource(R.string.browser_foto_zaehler, aktuell + 1, anzahl),
            stil = BoltTypo.zaehlerMini,
            farbe = BoltTextSchwach
        )
        if (zeigeWischHinweis && anzahl > 1) {
            val puls = rememberInfiniteTransition(label = "wischHinweis")
            val deckkraft by puls.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(950), RepeatMode.Reverse),
                label = "wischHinweisDeckkraft"
            )
            BoltText(
                text = stringResource(R.string.browser_wisch_hinweis),
                stil = BoltTypo.zaehlerMini,
                farbe = BoltOrangeHell,
                modifier = Modifier.alpha(deckkraft)
            )
        }
    }
}

@Preview(widthDp = 372, heightDp = 120, backgroundColor = 0xFF000000, showBackground = true)
@Composable
private fun KarussellAnzeigeVorschau() {
    BoltMindTheme {
        Box(Modifier.padding(16.dp).clip(RoundedCornerShape(0.dp))) {
            KarussellAnzeige(anzahl = 3, aktuell = 1, zeigeWischHinweis = true)
        }
    }
}
