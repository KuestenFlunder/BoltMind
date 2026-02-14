package com.boltmind.app.ui.components

import android.graphics.BlurMaskFilter
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.boltmind.app.ui.theme.BoltMindTheme
import com.boltmind.app.ui.theme.BoltOnPrimary
import com.boltmind.app.ui.theme.BoltPrimary
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
    val circleSize: Dp
    val fontSize: Int
    val glowAlpha: Float

    when (groesse) {
        SchrittNummerGroesse.Large -> {
            circleSize = 72.dp
            fontSize = 32
            glowAlpha = 0.35f
        }
        SchrittNummerGroesse.Medium -> {
            circleSize = 52.dp
            fontSize = 22
            glowAlpha = 0.25f
        }
    }

    val gradient = Brush.radialGradient(
        listOf(BoltPrimaryLight, BoltPrimary),
    )

    Box(
        modifier = modifier
            .size(circleSize)
            .drawBehind {
                drawIntoCanvas { canvas ->
                    val paint = Paint().apply {
                        color = BoltPrimary.copy(alpha = glowAlpha)
                        asFrameworkPaint().maskFilter =
                            BlurMaskFilter(14.dp.toPx(), BlurMaskFilter.Blur.NORMAL)
                    }
                    val outline = CircleShape.createOutline(
                        size,
                        layoutDirection,
                        this@drawBehind,
                    )
                    canvas.drawOutline(outline, paint)
                }
            }
            .clip(CircleShape)
            .background(gradient),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = schrittNummer.toString(),
            color = BoltOnPrimary,
            fontSize = fontSize.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            lineHeight = fontSize.sp,
        )
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

@Preview(showBackground = true, backgroundColor = 0xFF060B14)
@Composable
private fun SchrittNummerZweistelligPreview() {
    BoltMindTheme {
        SchrittNummer(
            schrittNummer = 12,
            groesse = SchrittNummerGroesse.Large,
        )
    }
}
