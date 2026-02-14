package com.boltmind.app.ui.components

import android.graphics.BlurMaskFilter
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.boltmind.app.R
import com.boltmind.app.ui.theme.BoltMindDimensions
import com.boltmind.app.ui.theme.BoltMindTheme
import com.boltmind.app.ui.theme.BoltPrimary
import com.boltmind.app.ui.theme.BoltPrimaryContainer
import com.boltmind.app.ui.theme.BoltPrimaryLight

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
            val pillShape = RoundedCornerShape(50)
            val gradient = Brush.horizontalGradient(
                listOf(BoltPrimaryContainer, BoltPrimaryContainer.copy(alpha = 0.7f)),
            )
            Box(
                modifier = modifier
                    .drawBehind {
                        drawIntoCanvas { canvas ->
                            val paint = Paint().apply {
                                color = BoltPrimary.copy(alpha = 0.15f)
                                asFrameworkPaint().maskFilter =
                                    BlurMaskFilter(16.dp.toPx(), BlurMaskFilter.Blur.NORMAL)
                            }
                            val outline = pillShape.createOutline(
                                size,
                                layoutDirection,
                                this@drawBehind,
                            )
                            canvas.drawOutline(outline, paint)
                        }
                    }
                    .clip(pillShape)
                    .background(gradient)
                    .padding(
                        horizontal = BoltMindDimensions.spacingM,
                        vertical = BoltMindDimensions.spacingS,
                    )
                    .widthIn(min = 80.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.demontage_schritt_nummer, schrittNummer),
                    style = MaterialTheme.typography.displayLarge,
                    color = BoltPrimaryLight,
                    textAlign = TextAlign.Center,
                )
            }
        }
        SchrittNummerGroesse.Medium -> {
            Box(
                modifier = modifier
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(
                        horizontal = BoltMindDimensions.spacingM,
                        vertical = BoltMindDimensions.spacingXs,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.demontage_schritt_nummer, schrittNummer),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF060B14)
@Composable
private fun SchrittNummerLargePreview() {
    BoltMindTheme {
        SchrittNummer(
            schrittNummer = 3,
            groesse = SchrittNummerGroesse.Large,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF060B14)
@Composable
private fun SchrittNummerMediumPreview() {
    BoltMindTheme {
        SchrittNummer(
            schrittNummer = 7,
            groesse = SchrittNummerGroesse.Medium,
        )
    }
}
