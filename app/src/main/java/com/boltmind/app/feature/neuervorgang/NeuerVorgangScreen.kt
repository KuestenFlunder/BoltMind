package com.boltmind.app.feature.neuervorgang

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview as ComposePreview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.boltmind.app.R
import com.boltmind.app.ui.components.BoltMindButton
import com.boltmind.app.ui.components.BoltMindButtonStyle
import com.boltmind.app.ui.components.BoltMindTopBar
import com.boltmind.app.ui.components.FotoPreview
import com.boltmind.app.ui.theme.BoltMindDimensions
import com.boltmind.app.ui.theme.BoltMindTheme
import java.io.File

@Composable
fun NeuerVorgangScreen(
    uiState: NeuerVorgangUiState,
    onFotoAufgenommen: (String) -> Unit,
    onBildWiederholen: () -> Unit,
    onKameraAbgebrochen: () -> Unit,
    onAuftragsnummerGeaendert: (String) -> Unit,
    onBeschreibungGeaendert: (String) -> Unit,
    onStartenGetippt: () -> Unit,
    onZurueck: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (uiState.schritt) {
        is NeuerVorgangSchritt.Kamera -> {
            KameraSchritt(
                onFotoAufgenommen = onFotoAufgenommen,
                onZurueck = if (uiState.fotoPfad != null) onKameraAbgebrochen else onZurueck,
                modifier = modifier,
            )
        }
        is NeuerVorgangSchritt.Formular -> {
            FormularSchritt(
                uiState = uiState,
                onBildWiederholen = onBildWiederholen,
                onAuftragsnummerGeaendert = onAuftragsnummerGeaendert,
                onBeschreibungGeaendert = onBeschreibungGeaendert,
                onStartenGetippt = onStartenGetippt,
                onZurueck = onZurueck,
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun KameraSchritt(
    onFotoAufgenommen: (String) -> Unit,
    onZurueck: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hatBerechtigung by remember { mutableStateOf(false) }
    val berechtigungLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { erteilt ->
        hatBerechtigung = erteilt
    }

    LaunchedEffect(Unit) {
        val permission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        if (permission == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            hatBerechtigung = true
        } else {
            berechtigungLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val imageCapture = remember { ImageCapture.Builder().build() }

    Scaffold(
        topBar = {
            BoltMindTopBar(
                title = stringResource(R.string.neuer_vorgang),
                onZurueck = onZurueck,
            )
        },
        floatingActionButton = {
            if (hatBerechtigung) {
                FloatingActionButton(
                    onClick = {
                        val fotoDatei = File(
                            context.filesDir,
                            "fotos/fahrzeug_${System.currentTimeMillis()}.jpg",
                        )
                        fotoDatei.parentFile?.mkdirs()

                        val outputOptions = ImageCapture.OutputFileOptions.Builder(fotoDatei).build()
                        imageCapture.takePicture(
                            outputOptions,
                            ContextCompat.getMainExecutor(context),
                            object : ImageCapture.OnImageSavedCallback {
                                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                    onFotoAufgenommen(fotoDatei.absolutePath)
                                }

                                override fun onError(exception: ImageCaptureException) {
                                    // Fehler beim Foto-Speichern - noop
                                }
                            },
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(BoltMindDimensions.fabSize),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_camera_placeholder),
                        contentDescription = stringResource(R.string.foto_aufnehmen),
                    )
                }
            }
        },
        modifier = modifier,
    ) { innerPadding ->
        if (hatBerechtigung) {
            var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }

            DisposableEffect(lifecycleOwner) {
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                cameraProviderFuture.addListener(
                    { cameraProvider = cameraProviderFuture.get() },
                    ContextCompat.getMainExecutor(context),
                )
                onDispose {
                    cameraProvider?.unbindAll()
                }
            }

            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).also { previewView ->
                        previewView.implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    }
                },
                update = { previewView ->
                    cameraProvider?.let { provider ->
                        provider.unbindAll()
                        val preview = Preview.Builder().build().also {
                            it.surfaceProvider = previewView.surfaceProvider
                        }
                        provider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageCapture,
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.kamera_berechtigung_erklaerung),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun FormularSchritt(
    uiState: NeuerVorgangUiState,
    onBildWiederholen: () -> Unit,
    onAuftragsnummerGeaendert: (String) -> Unit,
    onBeschreibungGeaendert: (String) -> Unit,
    onStartenGetippt: () -> Unit,
    onZurueck: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            BoltMindTopBar(
                title = stringResource(R.string.neuer_vorgang),
                onZurueck = onZurueck,
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = BoltMindDimensions.spacingM)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(BoltMindDimensions.spacingM))

            FotoPreview(
                fotoPfad = uiState.fotoPfad,
                contentDescription = stringResource(R.string.fahrzeugfoto),
                groesse = BoltMindDimensions.fotoPreviewLarge,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )

            Spacer(modifier = Modifier.height(BoltMindDimensions.spacingS))

            BoltMindButton(
                text = stringResource(R.string.bild_wiederholen),
                onClick = onBildWiederholen,
                style = BoltMindButtonStyle.Outlined,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(BoltMindDimensions.spacingM))

            OutlinedTextField(
                value = uiState.auftragsnummer,
                onValueChange = onAuftragsnummerGeaendert,
                label = { Text(stringResource(R.string.auftragsnummer_label)) },
                isError = uiState.validierungsFehler != null,
                supportingText = uiState.validierungsFehler?.let { fehler ->
                    { Text(fehler) }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = BoltMindDimensions.touchTargetMin),
            )

            Spacer(modifier = Modifier.height(BoltMindDimensions.spacingM))

            OutlinedTextField(
                value = uiState.beschreibung,
                onValueChange = onBeschreibungGeaendert,
                label = { Text(stringResource(R.string.beschreibung_label)) },
                minLines = 3,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(BoltMindDimensions.spacingL))

            BoltMindButton(
                text = stringResource(R.string.starten_button),
                onClick = onStartenGetippt,
                isLoading = uiState.isSpeichernd,
                enabled = !uiState.isSpeichernd,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(BoltMindDimensions.spacingM))
        }
    }
}

@ComposePreview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun NeuerVorgangFormularPreview() {
    BoltMindTheme {
        FormularSchritt(
            uiState = NeuerVorgangUiState(
                schritt = NeuerVorgangSchritt.Formular,
                fotoPfad = null,
                auftragsnummer = "2024-0815",
                beschreibung = "Bremsen vorne wechseln",
            ),
            onBildWiederholen = {},
            onAuftragsnummerGeaendert = {},
            onBeschreibungGeaendert = {},
            onStartenGetippt = {},
            onZurueck = {},
        )
    }
}

@ComposePreview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun NeuerVorgangFormularMitFehlerPreview() {
    BoltMindTheme {
        FormularSchritt(
            uiState = NeuerVorgangUiState(
                schritt = NeuerVorgangSchritt.Formular,
                fotoPfad = null,
                validierungsFehler = "Auftragsnummer ist erforderlich",
            ),
            onBildWiederholen = {},
            onAuftragsnummerGeaendert = {},
            onBeschreibungGeaendert = {},
            onStartenGetippt = {},
            onZurueck = {},
        )
    }
}
