package com.boltmind.app.feature.demontage

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview as ComposePreview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.boltmind.app.R
import com.boltmind.app.ui.theme.BoltMindTheme
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import java.io.File

@Composable
fun DemontageScreen(
    vorgangId: Long,
    onNavigateBack: () -> Unit,
    viewModel: DemontageViewModel = koinViewModel(parameters = { parametersOf(vorgangId) })
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is DemontageNavigationEvent.ZurueckZurUebersicht -> onNavigateBack()
            }
        }
    }

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    when (uiState.phase) {
        DemontagePhase.KAMERA -> KameraAnsicht(
            uiState = uiState,
            onFotoAufgenommen = viewModel::onFotoAufgenommen,
            onFotoFehler = viewModel::onFotoFehler,
            onBeendenGeklickt = viewModel::onDemontageBeenden,
            onZurueck = viewModel::onDemontageBeenden
        )
        DemontagePhase.ABLAGEORT_BESTAETIGUNG -> AblageortBestaetigung(
            uiState = uiState,
            onBestaetigt = viewModel::onAblageortBestaetigt,
            onAendern = viewModel::onAblageortAendern,
            onNummerGeaendert = viewModel::onAblageortNummerGeaendert,
            onZurueck = viewModel::onDemontageBeenden
        )
    }

    if (uiState.zeigAlleBesetztDialog) {
        AlleBesetztDialog(
            anzahlAblageorte = uiState.anzahlAblageorte,
            onBeenden = viewModel::onAlleBesetztBeenden,
            onErweitern = viewModel::onAlleBesetztErweitern
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KameraAnsicht(
    uiState: DemontageUiState,
    onFotoAufgenommen: (String) -> Unit,
    onFotoFehler: (String) -> Unit,
    onBeendenGeklickt: () -> Unit,
    onZurueck: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hatKameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { erteilt ->
        hatKameraPermission = erteilt
    }

    LaunchedEffect(Unit) {
        if (!hatKameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val imageCapture = remember { ImageCapture.Builder().build() }
    val fotoManager = remember { FotoManager(context) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(uiState.fahrzeug)
                },
                navigationIcon = {
                    IconButton(onClick = onZurueck) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.nav_uebersicht)
                        )
                    }
                },
                actions = {
                    Text(
                        text = stringResource(R.string.demontage_schritt_label, uiState.aktuellerSchritt),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (hatKameraPermission) {
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx)
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.surfaceProvider = previewView.surfaceProvider
                            }

                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                imageCapture
                            )
                        }, ContextCompat.getMainExecutor(ctx))

                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.demontage_kamera_permission),
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(32.dp)
                        )
                        Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                            Text(stringResource(R.string.demontage_permission_erteilen))
                        }
                    }
                }
            }

            // Letzte Foto-Vorschau (links unten)
            uiState.letzteFotoPath?.let { pfad ->
                AsyncImage(
                    model = File(pfad),
                    contentDescription = stringResource(R.string.demontage_letztes_foto),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                        .size(64.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(2.dp, Color.White, RoundedCornerShape(8.dp))
                )
            }

            // Auslöser-Button (unten mitte)
            IconButton(
                onClick = {
                    val executor = ContextCompat.getMainExecutor(context)
                    fotoManager.fotoAufnehmen(
                        imageCapture = imageCapture,
                        executor = executor,
                        onFotoGespeichert = onFotoAufgenommen,
                        onFehler = onFotoFehler
                    )
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
                    .size(72.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                    .border(4.dp, Color.White, CircleShape)
            ) {
                // Innerer Kreis als Auslöser-Symbol
            }

            // Beenden-Button (rechts unten)
            OutlinedButton(
                onClick = onBeendenGeklickt,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                )
            ) {
                Text(stringResource(R.string.demontage_beenden))
            }

            // Fehler-Anzeige
            uiState.fehler?.let { fehler ->
                Text(
                    text = fehler,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp)
                        .background(
                            MaterialTheme.colorScheme.errorContainer,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AblageortBestaetigung(
    uiState: DemontageUiState,
    onBestaetigt: () -> Unit,
    onAendern: () -> Unit,
    onNummerGeaendert: (String) -> Unit,
    onZurueck: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.fahrzeug) },
                navigationIcon = {
                    IconButton(onClick = onZurueck) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.dialog_abbrechen)
                        )
                    }
                },
                actions = {
                    Text(
                        text = stringResource(R.string.demontage_schritt_label, uiState.aktuellerSchritt),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Foto-Preview
            uiState.aktuellesFotoPath?.let { pfad ->
                AsyncImage(
                    model = File(pfad),
                    contentDescription = stringResource(R.string.demontage_foto_vorschau),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Ablageort-Anzeige
            Text(
                text = stringResource(R.string.demontage_ablageort_label),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.istAblageortAenderungAktiv) {
                OutlinedTextField(
                    value = uiState.ablageortNummer,
                    onValueChange = onNummerGeaendert,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.headlineLarge.copy(
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier
                        .width(120.dp)
                        .height(72.dp)
                )
            } else {
                Text(
                    text = uiState.ablageortNummer,
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = stringResource(R.string.demontage_auto_vorgeschlagen),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bestätigen-Button
            Button(
                onClick = onBestaetigt,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = stringResource(R.string.demontage_bestaetigen),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Ändern-Button
            if (!uiState.istAblageortAenderungAktiv) {
                OutlinedButton(
                    onClick = onAendern,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(stringResource(R.string.demontage_aendern))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun AlleBesetztDialog(
    anzahlAblageorte: Int,
    onBeenden: () -> Unit,
    onErweitern: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { /* Dialog nicht dismissbar */ },
        title = {
            Text(stringResource(R.string.demontage_alle_belegt_titel))
        },
        text = {
            Text(stringResource(R.string.demontage_alle_belegt_text, anzahlAblageorte))
        },
        confirmButton = {
            Button(onClick = onErweitern) {
                Text(stringResource(R.string.demontage_alle_belegt_erweitern))
            }
        },
        dismissButton = {
            TextButton(onClick = onBeenden) {
                Text(stringResource(R.string.demontage_alle_belegt_beenden))
            }
        }
    )
}

@ComposePreview(showBackground = true, name = "Ablageort Bestaetigung")
@Composable
private fun AblageortBestaetigungPreview() {
    BoltMindTheme {
        AblageortBestaetigung(
            uiState = DemontageUiState(
                phase = DemontagePhase.ABLAGEORT_BESTAETIGUNG,
                fahrzeug = "BMW 320d Blau",
                aktuellerSchritt = 5,
                ablageortNummer = "7",
                vorgeschlageneAblageortNummer = 7,
                anzahlAblageorte = 10,
                isLoading = false
            ),
            onBestaetigt = {},
            onAendern = {},
            onNummerGeaendert = {},
            onZurueck = {}
        )
    }
}

@ComposePreview(showBackground = true, name = "Ablageort Aenderung aktiv")
@Composable
private fun AblageortAenderungPreview() {
    BoltMindTheme {
        AblageortBestaetigung(
            uiState = DemontageUiState(
                phase = DemontagePhase.ABLAGEORT_BESTAETIGUNG,
                fahrzeug = "BMW 320d Blau",
                aktuellerSchritt = 5,
                ablageortNummer = "3",
                istAblageortAenderungAktiv = true,
                anzahlAblageorte = 10,
                isLoading = false
            ),
            onBestaetigt = {},
            onAendern = {},
            onNummerGeaendert = {},
            onZurueck = {}
        )
    }
}
