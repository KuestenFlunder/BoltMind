package com.boltmind.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.boltmind.app.R
import com.boltmind.app.feature.browser.BrowserScreen
import com.boltmind.app.feature.browser.BrowserViewModel
import com.boltmind.app.feature.browser.SheetAktion
import com.boltmind.app.feature.browser.SheetMarke
import com.boltmind.app.feature.browser.SheetStil
import com.boltmind.app.feature.browser.SheetZustand
import com.boltmind.app.ui.components.KameraAnbindung
import org.koin.androidx.compose.koinViewModel

/**
 * Verbindet den Schritt-Browser mit Navigation und System-Kamera.
 *
 * Die Kamera haengt an [KameraAnbindung] und schreibt direkt in die Zieldatei
 * unter `photos/`. Es gibt kein temp-Verzeichnis und keine app-eigene
 * Bestaetigung -- die System-Kamera bestaetigt selbst.
 */
@Composable
fun BrowserRoute(
    onVerlassen: () -> Unit,
    onMontageFertig: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: BrowserViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Die Kamera startet, was das ViewModel anfordert -- nicht, was ein Tap
    // ausloest. Nur so steht der Schritt, fuer den aufgenommen wird, sicher schon
    // in der Datenbank (F-003 workflow.md, "Reihenfolge beim Schritt-Start").
    KameraAnbindung(
        auftragsNummer = uiState.kameraAuftrag?.nummer,
        zielPfad = uiState.kameraAuftrag?.zielPfad,
        onFotoAufgenommen = { viewModel.onFotoAufgenommen() },
        onAbgebrochen = viewModel::onKameraAbgebrochen,
        // Kein Kamera-Programm auf dem Geraet: derselbe Weg wie ein Abbruch --
        // die leere Huelle verschwindet, ein eben eroeffneter Schritt wird
        // zurueckgenommen.
        onKeineKameraApp = viewModel::onKameraAbgebrochen
    )

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
        onNaechstesTeil = viewModel::onNaechstesTeil,
        onWeiteresFoto = viewModel::onWeiteresFoto,
        // Wiederholen: erst die Kamera, das alte Foto verschwindet erst nach
        // bestaetigter Neuaufnahme (Governance).
        onWiederholen = viewModel::onWiederholen,
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
        onZumAbschluss = viewModel::onZumAbschluss,
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
