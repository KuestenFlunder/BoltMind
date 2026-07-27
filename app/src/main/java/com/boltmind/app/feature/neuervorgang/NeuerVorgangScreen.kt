package com.boltmind.app.feature.neuervorgang

import android.content.ActivityNotFoundException
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.boltmind.app.R
import com.boltmind.app.ui.components.BoltMeshHintergrund
import com.boltmind.app.ui.components.BoltText
import com.boltmind.app.ui.components.GlasAktion
import com.boltmind.app.ui.components.GlasFlaeche
import com.boltmind.app.ui.components.boltKlick
import com.boltmind.app.ui.theme.BoltEingabeFlaeche
import com.boltmind.app.ui.theme.BoltFehler
import com.boltmind.app.ui.theme.BoltFehlerText
import com.boltmind.app.ui.theme.BoltMindDimensions
import com.boltmind.app.ui.theme.BoltMindTheme
import com.boltmind.app.ui.theme.BoltOrange
import com.boltmind.app.ui.theme.BoltRahmenDunkel
import com.boltmind.app.ui.theme.BoltScrim
import com.boltmind.app.ui.theme.BoltTextGedaempft
import com.boltmind.app.ui.theme.BoltTextHell
import com.boltmind.app.ui.theme.BoltTextSchwaecher
import com.boltmind.app.ui.theme.BoltTextSekundaer
import com.boltmind.app.ui.theme.BoltTextWeiss
import com.boltmind.app.ui.theme.BoltTypo
import com.boltmind.app.ui.theme.GlasRezepte
import com.boltmind.app.ui.theme.GlasBuehne
import com.boltmind.app.ui.theme.barlow
import com.boltmind.app.ui.theme.glas
import org.koin.androidx.compose.koinViewModel
import java.io.File
import com.boltmind.app.ui.theme.BoltText as BoltTextFarbe

// ============================================================================
// F-002 "Neuer Auftrag" -- Prototyp Zeile 145-182.
//
// Der Prototyp positioniert in 372 x 806. Hier steht dasselbe Bild als Spalte:
// Kopfzeile fest, Formular mitwachsend und scrollbar, Primaeraktion fest unten.
// Systemleisten und Tastatur bleiben ueber safeDrawing frei.
// ============================================================================

/** Deckkraft der Stahltextur im Anlage-Screen (Prototyp Zeile 148). */
private const val TEXTUR_DECKKRAFT = 0.28f

private val RadiusFoto = 18.dp
private val RadiusWiederholen = 12.dp
private val RadiusEingabe = 16.dp
private val RadiusZurueck = BoltMindDimensions.radiusStandard

/**
 * Verdrahtung des Anlage-Screens.
 *
 * Der Screen selbst navigiert nicht -- die beiden Signale [NeuerVorgangUiState.verlassen]
 * und [NeuerVorgangUiState.gestarteterVorgangId] werden hier ausgewertet.
 */
@Composable
fun NeuerVorgangRoute(
    onVorgangGestartet: (Long) -> Unit,
    onAbgebrochen: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NeuerVorgangViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.verlassen) {
        if (uiState.verlassen) onAbgebrochen()
    }
    LaunchedEffect(uiState.gestarteterVorgangId) {
        uiState.gestarteterVorgangId?.let(onVorgangGestartet)
    }

    NeuerVorgangScreen(
        uiState = uiState,
        onFotoAufgenommen = viewModel::onFotoAufgenommen,
        onKameraAbgebrochen = viewModel::onKameraAbgebrochen,
        onBildWiederholen = viewModel::onBildWiederholen,
        onAuftragsnummerGeaendert = viewModel::onAuftragsnummerGeaendert,
        onBeschreibungGeaendert = viewModel::onBeschreibungGeaendert,
        onStartenGetippt = viewModel::onStartenGetippt,
        onZurueck = viewModel::onZurueck,
        modifier = modifier
    )
}

@Composable
fun NeuerVorgangScreen(
    uiState: NeuerVorgangUiState,
    onFotoAufgenommen: (String) -> Unit,
    onKameraAbgebrochen: () -> Unit,
    onBildWiederholen: () -> Unit,
    onAuftragsnummerGeaendert: (String) -> Unit,
    onBeschreibungGeaendert: (String) -> Unit,
    onStartenGetippt: () -> Unit,
    onZurueck: () -> Unit,
    modifier: Modifier = Modifier
) {
    var keineKameraApp by remember { mutableStateOf(false) }

    KameraAnbindung(
        auftrag = uiState.kameraAuftrag,
        onFotoAufgenommen = onFotoAufgenommen,
        onKameraAbgebrochen = onKameraAbgebrochen,
        onKeineKameraApp = { keineKameraApp = true }
    )

    BackHandler(onBack = onZurueck)

    GlasBuehne(
        modifier = modifier.fillMaxSize(),
        hintergrund = { BoltMeshHintergrund(texturDeckkraft = TEXTUR_DECKKRAFT) }
    ) {
        Column(Modifier.fillMaxSize().safeDrawingPadding()) {
            Kopfzeile(onZurueck = onZurueck)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(
                        start = BoltMindDimensions.screenRand,
                        end = BoltMindDimensions.screenRand,
                        bottom = BoltMindDimensions.abstandXl
                    ),
                verticalArrangement = Arrangement.spacedBy(BoltMindDimensions.abstandL)
            ) {
                Fahrzeugfoto(
                    fotoPfad = uiState.fahrzeugFotoPfad,
                    onBildWiederholen = onBildWiederholen
                )
                FeldAuftragsnummer(
                    wert = uiState.auftragsnummer,
                    nummerFehlt = uiState.nummerFehlt,
                    onWertGeaendert = onAuftragsnummerGeaendert
                )
                FeldBeschreibung(
                    wert = uiState.beschreibung,
                    onWertGeaendert = onBeschreibungGeaendert
                )
            }

            Fussleiste(onStartenGetippt = onStartenGetippt)
        }

        if (keineKameraApp) {
            KeineKameraHinweis(onSchliessen = onZurueck)
        }
    }
}

// ---------------------------------------------------------------------------
// Kamera
// ---------------------------------------------------------------------------

/**
 * Haengt die System-Kamera ein: `TakePicture` schreibt ueber den `FileProvider`
 * direkt in die vom ViewModel angelegte Zieldatei. Keine CAMERA-Permission, keine
 * app-eigene Bestaetigung.
 */
@Composable
private fun KameraAnbindung(
    auftrag: KameraAuftrag?,
    onFotoAufgenommen: (String) -> Unit,
    onKameraAbgebrochen: () -> Unit,
    onKeineKameraApp: () -> Unit
) {
    val kontext = LocalContext.current
    val zielPfad by rememberUpdatedState(auftrag?.zielPfad)

    val starter = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { erfolgreich ->
        val pfad = zielPfad
        if (erfolgreich && pfad != null) onFotoAufgenommen(pfad) else onKameraAbgebrochen()
    }

    LaunchedEffect(auftrag) {
        val offen = auftrag ?: return@LaunchedEffect
        val uri = FileProvider.getUriForFile(
            kontext,
            "${kontext.packageName}.fileprovider",
            File(offen.zielPfad)
        )
        try {
            starter.launch(uri)
        } catch (_: ActivityNotFoundException) {
            onKeineKameraApp()
        }
    }
}

// ---------------------------------------------------------------------------
// Bausteine
// ---------------------------------------------------------------------------

/** Zurueck-Chip und Titel. Prototyp Zeile 151-154. */
@Composable
private fun Kopfzeile(onZurueck: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = BoltMindDimensions.screenRand,
                end = BoltMindDimensions.screenRand,
                top = BoltMindDimensions.abstandS,
                bottom = BoltMindDimensions.abstandM
            ),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GlasFlaeche(
            rezept = GlasRezepte.neutral07,
            eckRadius = RadiusZurueck,
            modifier = Modifier
                .size(BoltMindDimensions.zurueckChip)
                .boltKlick(onKlick = onZurueck)
        ) {
            BoltText(
                text = stringResource(R.string.nv_glyphe_zurueck),
                stil = barlow(20.sp),
                farbe = BoltTextGedaempft
            )
        }
        BoltText(
            text = stringResource(R.string.nv_titel),
            stil = BoltTypo.screenTitel,
            farbe = BoltTextFarbe
        )
    }
}

/** Fahrzeugfoto mit dem Knopf "NEU KNIPSEN" unten rechts. Prototyp Zeile 157-160. */
@Composable
private fun Fahrzeugfoto(
    fotoPfad: String?,
    onBildWiederholen: () -> Unit
) {
    val form = RoundedCornerShape(RadiusFoto)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(BoltMindDimensions.anlageFotoHoehe)
            .clipUndRahmen(form)
    ) {
        if (fotoPfad == null) {
            Box(Modifier.fillMaxSize().background(BoltEingabeFlaeche))
        } else {
            AsyncImage(
                model = File(fotoPfad),
                contentDescription = stringResource(R.string.nv_fahrzeugfoto_beschreibung),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        GlasAktion(
            rezept = GlasRezepte.neutral12,
            hoehe = BoltMindDimensions.bildWiederholenHoehe,
            eckRadius = RadiusWiederholen,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(10.dp),
            onKlick = onBildWiederholen
        ) {
            Row(
                modifier = Modifier.padding(horizontal = BoltMindDimensions.abstandL),
                horizontalArrangement = Arrangement.spacedBy(BoltMindDimensions.abstandS),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BoltText(
                    text = stringResource(R.string.nv_glyphe_wiederholen),
                    stil = barlow(15.sp),
                    farbe = BoltTextFarbe
                )
                BoltText(
                    text = stringResource(R.string.nv_neu_knipsen),
                    stil = BoltTypo.labelChipKlein,
                    farbe = BoltTextFarbe
                )
            }
        }
    }
}

/** Beschnitt und Rahmen des Fotorahmens -- `overflow:hidden` plus 1px `#2a2e33`. */
@Composable
private fun Modifier.clipUndRahmen(form: RoundedCornerShape): Modifier =
    this
        .androidClip(form)
        .border(BoltMindDimensions.rahmenDuenn, BoltRahmenDunkel, form)

@Composable
private fun Modifier.androidClip(form: RoundedCornerShape): Modifier =
    this.then(androidx.compose.ui.draw.clip(form))

/** Feld "AUFTRAGSNUMMER *" samt Fehlerzeile. Prototyp Zeile 162-168. */
@Composable
private fun FeldAuftragsnummer(
    wert: String,
    nummerFehlt: Boolean,
    onWertGeaendert: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(BoltMindDimensions.abstandS)) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            BoltText(
                text = stringResource(R.string.nv_label_auftragsnummer),
                stil = BoltTypo.feldLabel,
                farbe = BoltTextSekundaer,
                modifier = Modifier.alignByBaseline()
            )
            BoltText(
                text = stringResource(R.string.nv_pflichtstern),
                stil = barlow(14.sp),
                farbe = BoltOrange,
                modifier = Modifier.alignByBaseline()
            )
        }

        BoltEingabe(
            wert = wert,
            onWertGeaendert = onWertGeaendert,
            platzhalter = stringResource(R.string.nv_platzhalter_auftragsnummer),
            stil = BoltTypo.feldEingabeMono,
            hoehe = BoltMindDimensions.eingabeHoehe,
            innenAbstand = PaddingValues(horizontal = BoltMindDimensions.kopfRand),
            einzeilig = true,
            inhaltAusrichtung = Alignment.CenterStart,
            tastatur = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                imeAction = ImeAction.Next
            )
        )

        if (nummerFehlt) {
            Fehlerzeile()
        }
    }
}

/** Roter Punkt mit Ausrufezeichen und Fehlertext. Prototyp Zeile 166. */
@Composable
private fun Fehlerzeile() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(BoltMindDimensions.abstandS),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(18.dp).background(BoltFehler, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            BoltText(
                text = stringResource(R.string.nv_glyphe_fehler),
                stil = barlow(12.sp),
                farbe = BoltTextWeiss
            )
        }
        BoltText(
            text = stringResource(R.string.nv_fehler_nummer),
            stil = BoltTypo.feldFehler,
            farbe = BoltFehlerText
        )
    }
}

/** Feld "WAS IST ZU TUN?" -- bildet `Reparaturvorgang.beschreibung` ab. */
@Composable
private fun FeldBeschreibung(
    wert: String,
    onWertGeaendert: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(BoltMindDimensions.abstandS)) {
        BoltText(
            text = stringResource(R.string.nv_label_beschreibung),
            stil = BoltTypo.feldLabel,
            farbe = BoltTextSekundaer
        )
        BoltEingabe(
            wert = wert,
            onWertGeaendert = onWertGeaendert,
            platzhalter = stringResource(R.string.nv_platzhalter_beschreibung),
            stil = BoltTypo.feldEingabe,
            hoehe = BoltMindDimensions.textbereichHoehe,
            innenAbstand = PaddingValues(
                horizontal = BoltMindDimensions.kopfRand,
                vertical = 14.dp
            ),
            einzeilig = false,
            inhaltAusrichtung = Alignment.TopStart,
            tastatur = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Default
            )
        )
    }
}

/** Ein Eingabefeld im Glas-Rezept [GlasRezepte.eingabe]. */
@Composable
private fun BoltEingabe(
    wert: String,
    onWertGeaendert: (String) -> Unit,
    platzhalter: String,
    stil: TextStyle,
    hoehe: Dp,
    innenAbstand: PaddingValues,
    einzeilig: Boolean,
    inhaltAusrichtung: Alignment,
    tastatur: KeyboardOptions,
    modifier: Modifier = Modifier
) {
    BasicTextField(
        value = wert,
        onValueChange = onWertGeaendert,
        textStyle = stil.copy(color = BoltTextFarbe),
        singleLine = einzeilig,
        keyboardOptions = tastatur,
        cursorBrush = SolidColor(BoltOrange),
        modifier = modifier
            .fillMaxWidth()
            .height(hoehe)
            .glas(GlasRezepte.eingabe, RoundedCornerShape(RadiusEingabe), RadiusEingabe),
        decorationBox = { innen ->
            Box(
                modifier = Modifier.fillMaxSize().padding(innenAbstand),
                contentAlignment = inhaltAusrichtung
            ) {
                if (wert.isEmpty()) {
                    BoltText(
                        text = platzhalter,
                        stil = stil,
                        farbe = BoltTextSchwaecher,
                        maxZeilen = if (einzeilig) 1 else 2
                    )
                }
                innen()
            }
        }
    )
}

/** Primaeraktion "LOS GEHT'S ->". Prototyp Zeile 176-179. */
@Composable
private fun Fussleiste(onStartenGetippt: () -> Unit) {
    Box(
        Modifier.padding(
            start = BoltMindDimensions.screenRand,
            end = BoltMindDimensions.screenRand,
            top = BoltMindDimensions.abstandM,
            bottom = BoltMindDimensions.abstandXl
        )
    ) {
        GlasAktion(
            rezept = GlasRezepte.orangeFlach,
            hoehe = BoltMindDimensions.aktionGrossHoehe,
            eckRadius = BoltMindDimensions.radiusAktionGross,
            modifier = Modifier.fillMaxWidth(),
            onKlick = onStartenGetippt
        ) {
            BoltText(
                text = stringResource(R.string.nv_los_gehts),
                stil = BoltTypo.aktionGross,
                farbe = BoltTextWeiss
            )
            Spacer(Modifier.width(BoltMindDimensions.abstandM))
            BoltText(
                text = stringResource(R.string.nv_glyphe_pfeil),
                stil = barlow(22.sp),
                farbe = BoltTextWeiss
            )
        }
    }
}

/**
 * Faengt kein Kamera-Programm den Intent, bleibt der Screen stehen und zeigt
 * diesen Hinweis. Danach zurueck zur Uebersicht, ohne dass etwas angelegt wurde.
 */
@Composable
private fun BoxScope.KeineKameraHinweis(onSchliessen: () -> Unit) {
    Box(
        modifier = Modifier
            .matchParentSize()
            .background(BoltScrim)
            .boltKlick(stauchung = 1f, onKlick = onSchliessen),
        contentAlignment = Alignment.Center
    ) {
        GlasFlaeche(
            rezept = GlasRezepte.sheet,
            eckRadius = BoltMindDimensions.radiusSheet,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = BoltMindDimensions.abstandXl)
        ) {
            Column(
                modifier = Modifier.padding(BoltMindDimensions.abstandXl),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                BoltText(
                    text = stringResource(R.string.nv_keine_kamera_app),
                    stil = BoltTypo.dialogTitel,
                    farbe = BoltTextFarbe,
                    maxZeilen = 2
                )
                GlasAktion(
                    rezept = GlasRezepte.sheetAktion,
                    hoehe = BoltMindDimensions.sheetAktionHoehe,
                    eckRadius = BoltMindDimensions.radiusXl,
                    modifier = Modifier.fillMaxWidth(),
                    onKlick = onSchliessen
                ) {
                    BoltText(
                        text = stringResource(R.string.nv_keine_kamera_app_ok),
                        stil = BoltTypo.aktionSheet,
                        farbe = BoltTextHell
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Vorschau
// ---------------------------------------------------------------------------

@Preview(name = "Neuer Auftrag", widthDp = 372, heightDp = 806)
@Composable
private fun NeuerVorgangVorschau() {
    BoltMindTheme {
        NeuerVorgangScreen(
            uiState = NeuerVorgangUiState(
                auftragsnummer = "2026-0815",
                beschreibung = "Bremsen vorne wechseln"
            ),
            onFotoAufgenommen = {},
            onKameraAbgebrochen = {},
            onBildWiederholen = {},
            onAuftragsnummerGeaendert = {},
            onBeschreibungGeaendert = {},
            onStartenGetippt = {},
            onZurueck = {}
        )
    }
}

@Preview(name = "Neuer Auftrag -- Nummer fehlt", widthDp = 372, heightDp = 806)
@Composable
private fun NeuerVorgangFehlerVorschau() {
    BoltMindTheme {
        NeuerVorgangScreen(
            uiState = NeuerVorgangUiState(nummerFehlt = true),
            onFotoAufgenommen = {},
            onKameraAbgebrochen = {},
            onBildWiederholen = {},
            onAuftragsnummerGeaendert = {},
            onBeschreibungGeaendert = {},
            onStartenGetippt = {},
            onZurueck = {}
        )
    }
}
