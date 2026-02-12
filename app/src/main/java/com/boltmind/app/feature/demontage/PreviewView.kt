package com.boltmind.app.feature.demontage

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.boltmind.app.R
import com.boltmind.app.ui.components.BoltMindButton
import com.boltmind.app.ui.components.BoltMindButtonStyle
import com.boltmind.app.ui.components.BoltMindDialog
import com.boltmind.app.ui.components.SchrittNummer
import com.boltmind.app.ui.components.SchrittNummerGroesse
import com.boltmind.app.ui.theme.BoltMindDimensions
import com.boltmind.app.ui.theme.BoltMindTheme
import com.boltmind.app.ui.theme.BoltOutlineVariant

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
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(BoltMindDimensions.spacingM),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SchrittNummer(
            schrittNummer = schrittNummer,
            groesse = SchrittNummerGroesse.Medium,
        )

        Spacer(modifier = Modifier.height(BoltMindDimensions.spacingM))

        when (previewZustand) {
            PreviewZustand.INITIAL -> this.InitialZustand(
                istAblageortModus = istAblageortModus,
                onFotoAufnehmenGetippt = onFotoAufnehmenGetippt,
            )
            PreviewZustand.VORSCHAU -> this.VorschauZustand(
                istAblageortModus = istAblageortModus,
                tempFotoPfad = tempFotoPfad,
                onFotoBestaetigt = onFotoBestaetigt,
                onFotoWiederholen = onFotoWiederholen,
            )
        }
    }

    if (keineKameraApp) {
        BoltMindDialog(
            onDismissRequest = onKeineKameraDialogBestaetigt,
            dismissOnClickOutside = false,
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(BoltMindDimensions.iconLarge),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
            Spacer(modifier = Modifier.height(BoltMindDimensions.spacingM))
            Text(
                text = stringResource(R.string.demontage_keine_kamera),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(BoltMindDimensions.spacingL))
            BoltMindButton(
                text = stringResource(R.string.demontage_keine_kamera_ok),
                onClick = onKeineKameraDialogBestaetigt,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun ColumnScope.InitialZustand(
    istAblageortModus: Boolean,
    onFotoAufnehmenGetippt: () -> Unit,
) {
    if (istAblageortModus) {
        Text(
            text = stringResource(R.string.demontage_ablageort_hinweis),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(BoltMindDimensions.spacingM))
    }

    PlatzhalterBild(modifier = Modifier.padding(vertical = BoltMindDimensions.spacingS))

    Spacer(modifier = Modifier.height(BoltMindDimensions.spacingM))

    BoltMindButton(
        text = stringResource(R.string.demontage_foto_aufnehmen),
        onClick = onFotoAufnehmenGetippt,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun ColumnScope.VorschauZustand(
    istAblageortModus: Boolean,
    tempFotoPfad: String?,
    onFotoBestaetigt: () -> Unit,
    onFotoWiederholen: () -> Unit,
) {
    if (istAblageortModus) {
        Text(
            text = stringResource(R.string.demontage_ablageort_frage),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(BoltMindDimensions.spacingS))
    }

    FotoVorschau(
        fotoPfad = tempFotoPfad,
        modifier = Modifier.padding(vertical = BoltMindDimensions.spacingS),
    )

    Spacer(modifier = Modifier.height(BoltMindDimensions.spacingM))

    BoltMindButton(
        text = stringResource(R.string.demontage_bestaetigen),
        onClick = onFotoBestaetigt,
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
    )

    Spacer(modifier = Modifier.height(BoltMindDimensions.spacingM))

    BoltMindButton(
        text = stringResource(R.string.demontage_wiederholen),
        onClick = onFotoWiederholen,
        style = BoltMindButtonStyle.Outlined,
        modifier = Modifier
            .fillMaxWidth()
            .height(BoltMindDimensions.buttonCompact),
    )
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
                contentScale = ContentScale.Fit,
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
            .clip(MaterialTheme.shapes.medium)
            .border(BoltMindDimensions.borderThin, BoltOutlineVariant, MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_foto_platzhalter),
            contentDescription = stringResource(R.string.foto_platzhalter),
            modifier = Modifier.size(BoltMindDimensions.iconLarge),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121110)
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
            onKeineKameraDialogBestaetigt = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121110)
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
            onKeineKameraDialogBestaetigt = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121110)
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
            onKeineKameraDialogBestaetigt = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121110)
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
            onKeineKameraDialogBestaetigt = {},
        )
    }
}
