package com.boltmind.app.ui.navigation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.FileProvider
import com.boltmind.app.R
import com.boltmind.app.data.foto.FotoManager
import com.boltmind.app.feature.browser.BrowserScreen
import com.boltmind.app.feature.browser.BrowserViewModel
import com.boltmind.app.feature.browser.SheetAktion
import com.boltmind.app.feature.browser.SheetMarke
import com.boltmind.app.feature.browser.SheetStil
import com.boltmind.app.feature.browser.SheetZustand
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

/**
 * Verbindet den Schritt-Browser mit Navigation und System-Kamera.
 *
 * Die Kamera laeuft ueber [ActivityResultContracts.TakePicture] und schreibt
 * direkt in die Zieldatei unter `photos/`. Es gibt kein temp-Verzeichnis und
 * keine app-eigene Bestaetigung -- die System-Kamera bestaetigt selbst.
 */
@Composable
fun BrowserRoute(
    onVerlassen: () -> Unit,
    onMontageFertig: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: BrowserViewModel = koinViewModel(),
    fotoManager: FotoManager = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val kamera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        if (ok) viewModel.onFotoAufgenommen() else viewModel.onKameraAbgebrochen()
    }

    fun starteKamera(ersetztFotoId: Long?) {
        val ziel = fotoManager.erstelleZieldatei("schritt")
        viewModel.aufnahmeAngemeldet(ziel.absolutePath, ersetztFotoId)
        val uri = FileProvider.getUriForFile(
            context, "${context.packageName}.fileprovider", ziel
        )
        runCatching { kamera.launch(uri) }.onFailure {
            // Kein Kamera-Programm auf dem Geraet: die leere Huelle wieder wegraeumen.
            viewModel.onKameraAbgebrochen()
        }
    }

    LaunchedEffect(uiState.fertig) {
        if (uiState.fertig) {
            viewModel.onNavigationAbgeschlossen()
            onMontageFertig()
        }
    }

    LaunchedEffect(uiState.verlassen) {
        if (uiState.verlassen) {
            viewModel.onNavigationAbgeschlossen()
            onVerlassen()
        }
    }

    val feierabendTitel = stringResource(R.string.sheet_feierabend_titel)
    val feierabendText = stringResource(R.string.sheet_feierabend_text)
    val weiterArbeiten = stringResource(R.string.sheet_weiter_arbeiten)
    val jaFeierabend = stringResource(R.string.sheet_ja_feierabend)
    val haekchenTitel = stringResource(R.string.sheet_haekchen_titel)
    val haekchenText = stringResource(R.string.sheet_haekchen_text)
    val abbrechen = stringResource(R.string.sheet_abbrechen)
    val haekchenWeg = stringResource(R.string.sheet_haekchen_weg)

    BrowserScreen(
        uiState = uiState,
        modifier = modifier,
        onSchrittGewaehlt = viewModel::onSchrittGewaehlt,
        onFotoGewaehlt = viewModel::onFotoGewaehlt,
        onVollbildOeffnen = viewModel::onVollbildOeffnen,
        onVollbildSchliessen = viewModel::onVollbildSchliessen,
        onLabelUmgeschaltet = viewModel::onLabelUmgeschaltet,
        onTimerUmgeschaltet = viewModel::onTimerUmgeschaltet,
        onNaechstesTeil = {
            viewModel.onNaechstesTeil()
            starteKamera(null)
        },
        onWeiteresFoto = { starteKamera(null) },
        // Wiederholen: erst die Kamera, das alte Foto verschwindet erst nach
        // bestaetigter Neuaufnahme (Governance).
        onWiederholen = { starteKamera(uiState.aktiverSchritt?.fotos?.getOrNull(uiState.aktivesFoto)?.id) },
        onZumOffenenSchritt = viewModel::onZumOffenenSchritt,
        onEingebaut = viewModel::onEingebaut,
        onHaekchenAnfragen = {
            viewModel.zeigeSheet(
                SheetZustand(
                    titel = haekchenTitel,
                    text = haekchenText,
                    aktionen = listOf(
                        SheetAktion(abbrechen, SheetStil.NORMAL, SheetMarke.SCHLIESSEN),
                        SheetAktion(haekchenWeg, SheetStil.GEFAHR, SheetMarke.HAEKCHEN_ZURUECKNEHMEN)
                    )
                )
            )
        },
        onVorherigerSchritt = viewModel::onVorherigerSchritt,
        onNaechsterSchritt = viewModel::onNaechsterSchritt,
        onFeierabendAnfragen = {
            viewModel.zeigeSheet(
                SheetZustand(
                    titel = feierabendTitel,
                    text = feierabendText,
                    aktionen = listOf(
                        SheetAktion(weiterArbeiten, SheetStil.NORMAL, SheetMarke.SCHLIESSEN),
                        SheetAktion(jaFeierabend, SheetStil.PRIMAER, SheetMarke.FEIERABEND_BESTAETIGEN)
                    )
                )
            )
        },
        onVerlassen = onVerlassen,
        onSheetAktion = { marke ->
            when (marke) {
                SheetMarke.SCHLIESSEN -> viewModel.onSheetGeschlossen()
                SheetMarke.FEIERABEND_BESTAETIGEN -> viewModel.onFeierabendBestaetigt()
                SheetMarke.HAEKCHEN_ZURUECKNEHMEN -> viewModel.onHaekchenZuruecknehmenBestaetigt()
            }
        },
        onSheetGeschlossen = viewModel::onSheetGeschlossen
    )
}
