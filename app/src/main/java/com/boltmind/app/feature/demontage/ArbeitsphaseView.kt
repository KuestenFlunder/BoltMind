package com.boltmind.app.feature.demontage

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.boltmind.app.R
import com.boltmind.app.ui.theme.BoltMindTheme
import java.io.File

@Composable
fun ArbeitsphaseView(
    schrittNummer: Int,
    bauteilFotoPfad: String,
    onAusgebautGetippt: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.demontage_schritt_nummer, schrittNummer),
            style = MaterialTheme.typography.displayLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        BauteilFoto(
            fotoPfad = bauteilFotoPfad,
            schrittNummer = schrittNummer,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )

        var lastClickTime by remember { mutableLongStateOf(0L) }
        Button(
            onClick = {
                val now = System.currentTimeMillis()
                if (now - lastClickTime >= 300L) {
                    lastClickTime = now
                    onAusgebautGetippt()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
        ) {
            Text(text = stringResource(R.string.demontage_ausgebaut))
        }
    }
}

@Composable
private fun BauteilFoto(
    fotoPfad: String,
    schrittNummer: Int,
    modifier: Modifier = Modifier
) {
    val file = remember(fotoPfad) { File(fotoPfad) }
    if (file.exists()) {
        val bitmap = remember(fotoPfad) {
            BitmapFactory.decodeFile(fotoPfad)?.asImageBitmap()
        }
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = stringResource(
                    R.string.demontage_bauteil_foto,
                    schrittNummer
                ),
                contentScale = ContentScale.Fit,
                modifier = modifier
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
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.foto_platzhalter),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ArbeitsphaseViewPreview() {
    BoltMindTheme {
        ArbeitsphaseView(
            schrittNummer = 3,
            bauteilFotoPfad = "/nonexistent/shows/placeholder.jpg",
            onAusgebautGetippt = {}
        )
    }
}
