package com.boltmind.app.feature.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.boltmind.app.R
import com.boltmind.app.ui.components.BoltText
import com.boltmind.app.ui.components.BoltRuhigerHintergrund
import com.boltmind.app.ui.components.Flaeche
import com.boltmind.app.ui.components.boltKlick
import com.boltmind.app.ui.theme.BoltMindDimensions
import com.boltmind.app.ui.theme.BoltMindTheme
import com.boltmind.app.ui.theme.BoltOrange
import com.boltmind.app.ui.theme.BoltOrangeHell
import com.boltmind.app.ui.theme.BoltSchwarz10
import com.boltmind.app.ui.theme.BoltSchwarz55
import com.boltmind.app.ui.theme.BoltSchwarz90
import com.boltmind.app.ui.theme.BoltTextWeiss
import com.boltmind.app.ui.theme.BoltTypo
import com.boltmind.app.ui.theme.BoltWeiss14
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ============================================================================
// Splash -- 1:1 aus dem Design-Prototyp "BoltMind App.dc.html", Zeilen 47-58
// sowie componentDidMount (Z. 448) und skipSplash (Z. 564).
//
// Kein Zustand, kein ViewModel: der Screen zaehlt nur die Zeit herunter und
// meldet sich genau einmal fertig -- per Tipp oder per Ablauf.
// ============================================================================

/** Laufzeit des Ladebalkens. Keyframe `bmBar 2.1s linear forwards`, Z. 54. */
private const val BALKEN_DAUER_MS = 2100

/** Einblendung der Wortmarke. Keyframe `bmIn .8s`, Z. 52. */
private const val WORTMARKE_DAUER_MS = 800

/** Einblendung der ganzen Flaeche. Keyframe `bmFade .4s ease`, Z. 48. */
private const val EINBLENDUNG_DAUER_MS = 400

/** Danach geht es automatisch weiter. `setTimeout(..., 2400)`, Z. 448. */
private const val SPLASH_DAUER_MS = 2400L

/** `cubic-bezier(.16,1,.3,1)` der Wortmarken-Einblendung, Z. 52. */
private val EinblendKurve = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)

/** CSS `ease` -- entspricht `cubic-bezier(.25,.1,.25,1)`, Z. 48. */
private val WeicheKurve = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1f)

/**
 * Der Verlauf ueber dem Grund:
 * `linear-gradient(180deg, rgba(0,0,0,.55), rgba(0,0,0,.1) 40%, rgba(0,0,0,.9))`, Z. 50.
 */
private val SplashSchleier = Brush.verticalGradient(
    0.0f to BoltSchwarz55,
    0.4f to BoltSchwarz10,
    1.0f to BoltSchwarz90
)

/**
 * Der Startbildschirm: Wortmarke, Claim und ein Ladebalken ueber dem Grund.
 *
 * [onFertig] wird genau einmal ausgeloest -- entweder durch den Tipp auf die
 * Flaeche oder nach [SPLASH_DAUER_MS]. Tippt jemand waehrend des Countdowns,
 * laeuft der Timer ins Leere.
 */
@Composable
fun SplashScreen(
    onFertig: () -> Unit,
    modifier: Modifier = Modifier
) {
    val aktuellesFertig by rememberUpdatedState(onFertig)
    var gemeldet by remember { mutableStateOf(false) }
    val melden: () -> Unit = remember {
        {
            if (!gemeldet) {
                gemeldet = true
                aktuellesFertig()
            }
        }
    }

    val einblendung = remember { Animatable(0f) }
    val wortmarke = remember { Animatable(0f) }
    val balken = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch { einblendung.animateTo(1f, tween(EINBLENDUNG_DAUER_MS, easing = WeicheKurve)) }
        launch { wortmarke.animateTo(1f, tween(WORTMARKE_DAUER_MS, easing = EinblendKurve)) }
        launch { balken.animateTo(1f, tween(BALKEN_DAUER_MS, easing = LinearEasing)) }
        delay(SPLASH_DAUER_MS)
        melden()
    }

    val versatzPx = with(LocalDensity.current) {
        BoltMindDimensions.splashEinblendVersatz.toPx()
    }
    val ueberspringen = stringResource(R.string.splash_ueberspringen)

    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer { alpha = einblendung.value }
    ) {
        // TODO: Der Entwurf legt hier eine Videoschleife (assets/splash-loop.mp4,
        //  Deckkraft .85, object-fit:cover, Z. 49). Die Datei liegt nicht vor und
        //  liess sich nicht exportieren. Bis sie nachgereicht wird, traegt die
        //  Stahltextur den Grund; der Verlauf darueber ist unveraendert der des
        //  Entwurfs.
        BoltRuhigerHintergrund()
        Box(Modifier.fillMaxSize().background(SplashSchleier))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(bottom = BoltMindDimensions.splashTextblockAbstandUnten),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Die Wortmarke faehrt beim Einblenden von unten hoch. Compose
            // verteilt letterSpacing halbseitig, deshalb sitzt sie ohne das
            // text-indent des Entwurfs (Z. 52) trotzdem mittig.
            BoltText(
                text = stringResource(R.string.splash_wortmarke),
                stil = BoltTypo.wortmarkeGross,
                farbe = BoltTextWeiss,
                modifier = Modifier.graphicsLayer {
                    alpha = wortmarke.value
                    translationY = versatzPx * (1f - wortmarke.value)
                }
            )

            Spacer(Modifier.height(BoltMindDimensions.splashElementAbstand))

            BoltText(
                text = stringResource(R.string.splash_claim),
                stil = BoltTypo.splashClaim,
                farbe = BoltOrangeHell
            )

            // gap 14 plus margin-top 18 des Balkens (Z. 51/54).
            Spacer(
                Modifier.height(
                    BoltMindDimensions.splashElementAbstand +
                        BoltMindDimensions.splashBalkenAbstand
                )
            )

            Ladebalken(anteil = { balken.value })
        }

        // Der ganzflaechige Tipp-Bereich des Entwurfs (Z. 56). Er hat dort keine
        // eigene Darstellung, deshalb bleibt die Stauchung des Klicks hier aus.
        Box(
            modifier = Modifier
                .matchParentSize()
                .semantics { contentDescription = ueberspringen }
                .boltKlick(stauchung = 1f, onKlick = melden)
        )
    }
}

/**
 * Der Ladebalken: 150 x 3 dp, Spur und Fuellung mit Radius 2 dp (Z. 54).
 *
 * [anteil] kommt als Funktion herein, damit die Animation nur neu zeichnet und
 * nicht 2,1 Sekunden lang rekomponiert.
 */
@Composable
private fun Ladebalken(
    anteil: () -> Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(BoltMindDimensions.splashBalkenBreite)
            .height(BoltMindDimensions.splashBalkenHoehe)
            .clip(RoundedCornerShape(BoltMindDimensions.splashBalkenRadius))
            .background(BoltWeiss14)
    ) {
        Flaeche(
            farbe = BoltOrange,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = anteil()
                    transformOrigin = TransformOrigin(0f, 0.5f)
                },
            form = RoundedCornerShape(BoltMindDimensions.splashBalkenRadius)
        )
    }
}

@Preview(showBackground = true, widthDp = 372, heightDp = 806)
@Composable
private fun SplashScreenPreview() {
    BoltMindTheme {
        SplashScreen(onFertig = {})
    }
}
