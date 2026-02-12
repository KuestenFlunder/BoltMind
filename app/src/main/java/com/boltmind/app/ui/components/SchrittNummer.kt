package com.boltmind.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.boltmind.app.R
import com.boltmind.app.ui.theme.BoltMindDimensions
import com.boltmind.app.ui.theme.BoltMindTheme

enum class SchrittNummerGroesse {
    Large,
    Medium,
}

@Composable
fun SchrittNummer(
    schrittNummer: Int,
    groesse: SchrittNummerGroesse,
    modifier: Modifier = Modifier,
) {
    when (groesse) {
        SchrittNummerGroesse.Large -> {
            Box(
                modifier = modifier
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = BoltMindDimensions.spacingM, vertical = BoltMindDimensions.spacingS)
                    .widthIn(min = 80.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.demontage_schritt_nummer, schrittNummer),
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center,
                )
            }
        }
        SchrittNummerGroesse.Medium -> {
            Box(
                modifier = modifier
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = BoltMindDimensions.spacingM, vertical = BoltMindDimensions.spacingXs),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.demontage_schritt_nummer, schrittNummer),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun SchrittNummerLargePreview() {
    BoltMindTheme {
        SchrittNummer(
            schrittNummer = 3,
            groesse = SchrittNummerGroesse.Large,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun SchrittNummerMediumPreview() {
    BoltMindTheme {
        SchrittNummer(
            schrittNummer = 7,
            groesse = SchrittNummerGroesse.Medium,
        )
    }
}
