package com.boltmind.app.feature.demontage

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.boltmind.app.R
import com.boltmind.app.ui.components.DebounceClickHandler
import com.boltmind.app.ui.theme.BoltMindTheme

@Composable
fun PreviewView(
    schrittNummer: Int,
    previewZustand: PreviewZustand,
    istAblageortModus: Boolean,
    tempFotoPfad: String?,
    keineKameraApp: Boolean,
    onFotoAufnehmenGetippt: () -> Unit,
    onFotoBestaetigt: () -> Unit,
    onFotoWiederholen: () -> Unit,
    onKameraAbgebrochen: () -> Unit,
    onKeineKameraDialogBestaetigt: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.demontage_schritt_nummer, schrittNummer),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        when (previewZustand) {
            PreviewZustand.INITIAL -> InitialZustand(
                istAblageortModus = istAblageortModus,
                onFotoAufnehmenGetippt = onFotoAufnehmenGetippt
            )
            PreviewZustand.VORSCHAU -> VorschauZustand(
                istAblageortModus = istAblageortModus,
                tempFotoPfad = tempFotoPfad,
                onFotoBestaetigt = onFotoBestaetigt,
                onFotoWiederholen = onFotoWiederholen
            )
        }
    }

    if (keineKameraApp) {
        AlertDialog(
            onDismissRequest = onKeineKameraDialogBestaetigt,
            title = { Text(stringResource(R.string.demontage_keine_kamera)) },
            confirmButton = {
                TextButton(onClick = onKeineKameraDialogBestaetigt) {
                    Text(stringResource(R.string.demontage_keine_kamera_ok))
                }
            }
        )
    }
}

@Composable
private fun InitialZustand(
    istAblageortModus: Boolean,
    onFotoAufnehmenGetippt: () -> Unit
) {
    if (istAblageortModus) {
        Text(
            text = stringResource(R.string.demontage_ablageort_hinweis),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
    }

    PlatzhalterBild(modifier = Modifier.padding(vertical = 8.dp))

    Spacer(modifier = Modifier.height(16.dp))

    val debounceHandler = remember { DebounceClickHandler() }
    Button(
        onClick = { debounceHandler.onClick { onFotoAufnehmenGetippt() } },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Text(
            text = stringResource(R.string.demontage_foto_aufnehmen),
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
private fun VorschauZustand(
    istAblageortModus: Boolean,
    tempFotoPfad: String?,
    onFotoBestaetigt: () -> Unit,
    onFotoWiederholen: () -> Unit
) {
    if (istAblageortModus) {
        Text(
            text = stringResource(R.string.demontage_ablageort_frage),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
    }

    FotoVorschau(
        fotoPfad = tempFotoPfad,
        modifier = Modifier.padding(vertical = 8.dp)
    )

    Spacer(modifier = Modifier.height(16.dp))

    val debounceBestaetigen = remember { DebounceClickHandler() }
    val debounceWiederholen = remember { DebounceClickHandler() }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = { debounceWiederholen.onClick { onFotoWiederholen() } },
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
        ) {
            Text(stringResource(R.string.demontage_wiederholen))
        }

        Button(
            onClick = { debounceBestaetigen.onClick { onFotoBestaetigt() } },
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
        ) {
            Text(stringResource(R.string.demontage_bestaetigen))
        }
    }
}

@Composable
private fun FotoVorschau(fotoPfad: String?, modifier: Modifier = Modifier) {
    if (fotoPfad != null) {
        val bitmap = remember(fotoPfad) {
            BitmapFactory.decodeFile(fotoPfad)?.asImageBitmap()
        }
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = null,
                modifier = modifier.fillMaxWidth(),
                contentScale = ContentScale.Fit
            )
        } else {
            PlatzhalterBild(modifier)
        }
    } else {
        PlatzhalterBild(modifier)
    }
}

@Composable
private fun PlatzhalterBild(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(4f / 3f)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_foto_platzhalter),
            contentDescription = stringResource(R.string.foto_platzhalter),
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewInitialBauteil() {
    BoltMindTheme {
        PreviewView(
            schrittNummer = 1,
            previewZustand = PreviewZustand.INITIAL,
            istAblageortModus = false,
            tempFotoPfad = null,
            keineKameraApp = false,
            onFotoAufnehmenGetippt = {},
            onFotoBestaetigt = {},
            onFotoWiederholen = {},
            onKameraAbgebrochen = {},
            onKeineKameraDialogBestaetigt = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewInitialAblageort() {
    BoltMindTheme {
        PreviewView(
            schrittNummer = 3,
            previewZustand = PreviewZustand.INITIAL,
            istAblageortModus = true,
            tempFotoPfad = null,
            keineKameraApp = false,
            onFotoAufnehmenGetippt = {},
            onFotoBestaetigt = {},
            onFotoWiederholen = {},
            onKameraAbgebrochen = {},
            onKeineKameraDialogBestaetigt = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewVorschauBauteil() {
    BoltMindTheme {
        PreviewView(
            schrittNummer = 1,
            previewZustand = PreviewZustand.VORSCHAU,
            istAblageortModus = false,
            tempFotoPfad = null,
            keineKameraApp = false,
            onFotoAufnehmenGetippt = {},
            onFotoBestaetigt = {},
            onFotoWiederholen = {},
            onKameraAbgebrochen = {},
            onKeineKameraDialogBestaetigt = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewVorschauAblageort() {
    BoltMindTheme {
        PreviewView(
            schrittNummer = 3,
            previewZustand = PreviewZustand.VORSCHAU,
            istAblageortModus = true,
            tempFotoPfad = null,
            keineKameraApp = false,
            onFotoAufnehmenGetippt = {},
            onFotoBestaetigt = {},
            onFotoWiederholen = {},
            onKameraAbgebrochen = {},
            onKeineKameraDialogBestaetigt = {}
        )
    }
}
