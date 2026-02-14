package com.boltmind.app.ui.components

import android.graphics.BlurMaskFilter
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas

import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.boltmind.app.ui.theme.BoltError
import com.boltmind.app.ui.theme.BoltErrorLight
import com.boltmind.app.ui.theme.BoltMindDimensions
import com.boltmind.app.ui.theme.BoltMindTheme
import com.boltmind.app.ui.theme.BoltPrimary
import com.boltmind.app.ui.theme.BoltPrimaryLight

enum class BoltMindButtonStyle {
    Primary,
    Secondary,
    Danger,
    Outlined,
}

private fun Modifier.glowEffect(
    color: Color,
    shape: Shape,
    blurRadius: Dp = BoltMindDimensions.glowRadius,
    alpha: Float = 0.45f,
) = drawBehind {
    drawIntoCanvas { canvas ->
        val paint = Paint().apply {
            this.color = color.copy(alpha = alpha)
            asFrameworkPaint().maskFilter =
                BlurMaskFilter(blurRadius.toPx(), BlurMaskFilter.Blur.NORMAL)
        }
        val outline = shape.createOutline(size, layoutDirection, this@drawBehind)
        canvas.drawOutline(outline, paint)
    }
}

@Composable
fun BoltMindButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: BoltMindButtonStyle = BoltMindButtonStyle.Primary,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: @Composable (() -> Unit)? = null,
    iconAboveText: Boolean = false,
) {
    var lastClickTime by remember { mutableLongStateOf(0L) }

    val debouncedClick: () -> Unit = {
        val now = System.currentTimeMillis()
        if (now - lastClickTime >= 300L) {
            lastClickTime = now
            onClick()
        }
    }

    val buttonHeight = when (style) {
        BoltMindButtonStyle.Primary -> BoltMindDimensions.buttonPrimary
        BoltMindButtonStyle.Secondary -> BoltMindDimensions.buttonSecondary
        BoltMindButtonStyle.Danger -> BoltMindDimensions.buttonPrimary
        BoltMindButtonStyle.Outlined -> BoltMindDimensions.buttonSecondary
    }

    val buttonModifier = modifier.heightIn(min = buttonHeight)

    when (style) {
        BoltMindButtonStyle.Primary -> {
            val shape = MaterialTheme.shapes.medium
            val gradient = Brush.horizontalGradient(
                listOf(BoltPrimary, BoltPrimaryLight),
            )
            Box(
                modifier = buttonModifier
                    .glowEffect(
                        color = BoltPrimary,
                        shape = shape,
                        blurRadius = BoltMindDimensions.glowRadiusLarge,
                    )
                    .clip(shape)
                    .background(gradient, shape),
            ) {
                Button(
                    onClick = debouncedClick,
                    enabled = enabled && !isLoading,
                    shape = shape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                    modifier = Modifier.matchParentSize(),
                ) {
                    ButtonContent(text = text, isLoading = isLoading, icon = icon, iconAboveText = iconAboveText)
                }
            }
        }
        BoltMindButtonStyle.Secondary -> {
            Button(
                onClick = debouncedClick,
                enabled = enabled && !isLoading,
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ),
                modifier = buttonModifier,
            ) {
                ButtonContent(text = text, isLoading = isLoading, icon = icon, iconAboveText = iconAboveText)
            }
        }
        BoltMindButtonStyle.Danger -> {
            val shape = MaterialTheme.shapes.medium
            val gradient = Brush.horizontalGradient(
                listOf(BoltError, BoltErrorLight),
            )
            Box(
                modifier = buttonModifier
                    .glowEffect(
                        color = BoltError,
                        shape = shape,
                        blurRadius = BoltMindDimensions.glowRadiusLarge,
                    )
                    .clip(shape)
                    .background(gradient, shape),
            ) {
                Button(
                    onClick = debouncedClick,
                    enabled = enabled && !isLoading,
                    shape = shape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.White,
                    ),
                    modifier = Modifier.matchParentSize(),
                ) {
                    ButtonContent(text = text, isLoading = isLoading, icon = icon, iconAboveText = iconAboveText)
                }
            }
        }
        BoltMindButtonStyle.Outlined -> {
            OutlinedButton(
                onClick = debouncedClick,
                enabled = enabled && !isLoading,
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary,
                ),
                modifier = buttonModifier,
            ) {
                ButtonContent(text = text, isLoading = isLoading, icon = icon, iconAboveText = iconAboveText)
            }
        }
    }
}

@Composable
private fun ButtonContent(
    text: String,
    isLoading: Boolean,
    icon: @Composable (() -> Unit)? = null,
    iconAboveText: Boolean = false,
) {
    if (isLoading) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            color = MaterialTheme.colorScheme.onPrimary,
            strokeWidth = 2.dp,
        )
    } else if (iconAboveText && icon != null) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            icon()
            Spacer(modifier = Modifier.height(BoltMindDimensions.spacingS))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                maxLines = 2,
            )
        }
    } else {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            if (icon != null) {
                icon()
                Spacer(modifier = Modifier.width(BoltMindDimensions.spacingS))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF060B14)
@Composable
private fun BoltMindButtonPrimaryPreview() {
    BoltMindTheme {
        BoltMindButton(
            text = "Ausgebaut",
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF060B14)
@Composable
private fun BoltMindButtonSecondaryPreview() {
    BoltMindTheme {
        BoltMindButton(
            text = "Weiter ohne Ablageort",
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
            style = BoltMindButtonStyle.Secondary,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF060B14)
@Composable
private fun BoltMindButtonDangerPreview() {
    BoltMindTheme {
        BoltMindButton(
            text = "Loeschen",
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
            style = BoltMindButtonStyle.Danger,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF060B14)
@Composable
private fun BoltMindButtonOutlinedPreview() {
    BoltMindTheme {
        BoltMindButton(
            text = "Beenden",
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
            style = BoltMindButtonStyle.Outlined,
        )
    }
}
