package com.boltmind.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import coil.compose.AsyncImage
import com.boltmind.app.R
import com.boltmind.app.ui.theme.BoltMindDimensions
import com.boltmind.app.ui.theme.BoltMindTheme
import com.boltmind.app.ui.theme.BoltOutlineVariant
import java.io.File

/**
 * Prueft ob ein Foto angezeigt werden soll.
 *
 * @param fotoPfad Absoluter Pfad zum Foto, null wenn kein Foto vorhanden
 * @return true wenn der Pfad gueltig ist und die Datei existiert
 */
fun sollteFotoAnzeigen(fotoPfad: String?): Boolean {
    if (fotoPfad.isNullOrBlank()) return false
    return File(fotoPfad).exists()
}

/**
 * Wiederverwendbare FotoPreview-Composable.
 * Zeigt ein Foto als Thumbnail an oder einen Platzhalter wenn kein Foto vorhanden.
 *
 * @param fotoPfad Absoluter Pfad zum Foto auf dem Filesystem, null fuer Platzhalter
 * @param contentDescription Beschreibung fuer Accessibility
 * @param groesse Groesse des Thumbnails (quadratisch)
 * @param modifier Modifier fuer die Composable
 */
@Composable
fun FotoPreview(
    fotoPfad: String?,
    contentDescription: String,
    modifier: Modifier = Modifier,
    groesse: Dp = BoltMindDimensions.fotoPreviewSmall,
) {
    val shape = MaterialTheme.shapes.small

    if (sollteFotoAnzeigen(fotoPfad)) {
        AsyncImage(
            model = File(fotoPfad!!),
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = modifier
                .size(groesse)
                .clip(shape)
                .border(BoltMindDimensions.borderThin, BoltOutlineVariant, shape),
        )
    } else {
        Box(
            modifier = modifier
                .size(groesse)
                .clip(shape)
                .border(BoltMindDimensions.borderThin, BoltOutlineVariant, shape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_camera_placeholder),
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121110)
@Composable
private fun FotoPreviewMitBildPreview() {
    BoltMindTheme {
        FotoPreview(
            fotoPfad = "/fake/path/foto.jpg",
            contentDescription = "Fahrzeugfoto",
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121110)
@Composable
private fun FotoPreviewOhneBildPreview() {
    BoltMindTheme {
        FotoPreview(
            fotoPfad = null,
            contentDescription = "Kein Foto",
        )
    }
}
