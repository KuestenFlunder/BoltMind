package com.boltmind.app.feature.demontage

import android.content.ActivityNotFoundException
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import org.koin.androidx.compose.koinViewModel
import java.io.File

@Composable
fun DemontageScreen(
    viewModel: DemontageViewModel = koinViewModel(),
    onFlowBeendet: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.flowBeendet) {
        if (uiState.flowBeendet) {
            onFlowBeendet()
        }
    }

    BackHandler { /* Back-Taste blockiert im gesamten Demontage-Flow (US-003.6) */ }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            when (uiState.flowState) {
                DemontageFlowState.PREVIEW_BAUTEIL -> viewModel.onFotoAufgenommen()
                DemontageFlowState.PREVIEW_ABLAGEORT -> viewModel.onAblageortFotoAufgenommen()
                else -> { /* sollte nicht vorkommen */ }
            }
        } else {
            viewModel.onKameraAbgebrochen()
        }
    }

    LaunchedEffect(uiState.kameraIntentAktiv) {
        if (uiState.kameraIntentAktiv) {
            val tempFile = File(uiState.tempFotoPfad!!)
            tempFile.parentFile?.mkdirs()
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                tempFile
            )
            try {
                takePictureLauncher.launch(uri)
            } catch (_: ActivityNotFoundException) {
                viewModel.onKeineKameraApp()
            }
        }
    }

    when (uiState.flowState) {
        DemontageFlowState.PREVIEW_BAUTEIL -> PreviewView(
            schrittNummer = uiState.schrittNummer,
            previewZustand = uiState.previewZustand,
            istAblageortModus = false,
            tempFotoPfad = uiState.tempFotoPfad,
            keineKameraApp = uiState.keineKameraApp,
            onFotoAufnehmenGetippt = viewModel::onFotoAufnehmenGetippt,
            onFotoBestaetigt = viewModel::onFotoBestaetigt,
            onFotoWiederholen = viewModel::onFotoWiederholen,
            onKameraAbgebrochen = viewModel::onKameraAbgebrochen,
            onKeineKameraDialogBestaetigt = viewModel::onKeineKameraDialogBestaetigt,
        )
        DemontageFlowState.ARBEITSPHASE -> ArbeitsphaseView(
            schrittNummer = uiState.schrittNummer,
            bauteilFotoPfad = uiState.bauteilFotoPfad ?: "",
            onAusgebautGetippt = viewModel::onAusgebautGetippt,
        )
        DemontageFlowState.DIALOG -> DemontageDialog(
            onAblageortFotografieren = viewModel::onAblageortFotografieren,
            onWeiterOhneAblageort = viewModel::onWeiterOhneAblageort,
            onBeenden = viewModel::onBeenden,
        )
        DemontageFlowState.PREVIEW_ABLAGEORT -> PreviewView(
            schrittNummer = uiState.schrittNummer,
            previewZustand = uiState.previewZustand,
            istAblageortModus = true,
            tempFotoPfad = uiState.tempFotoPfad,
            keineKameraApp = uiState.keineKameraApp,
            onFotoAufnehmenGetippt = viewModel::onAblageortFotoAufnehmen,
            onFotoBestaetigt = viewModel::onAblageortFotoBestaetigt,
            onFotoWiederholen = viewModel::onAblageortFotoWiederholen,
            onKameraAbgebrochen = viewModel::onKameraAbgebrochen,
            onKeineKameraDialogBestaetigt = viewModel::onKeineKameraDialogBestaetigt,
        )
    }
}
