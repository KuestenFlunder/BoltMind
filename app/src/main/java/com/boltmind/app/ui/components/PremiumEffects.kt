package com.boltmind.app.ui.components

import android.graphics.BlurMaskFilter
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.boltmind.app.ui.theme.BoltMindDimensions
import com.boltmind.app.ui.theme.BoltPrimary
import com.boltmind.app.ui.theme.BoltSurface
import com.boltmind.app.ui.theme.BoltSurfaceVariant
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

/**
 * Ambient Glow - Verstärkter Dual-Layer Lichtschein hinter Elementen.
 */
fun Modifier.ambientGlow(
    color: Color = BoltPrimary,
    innerAlpha: Float = 0.15f,
    outerAlpha: Float = 0.06f,
    innerRadius: Dp = BoltMindDimensions.glowRadius,
    outerRadius: Dp = 40.dp,
    cornerRadius: Dp = BoltMindDimensions.cornerM,
    spread: Dp = 8.dp,
) = drawBehind {
    drawIntoCanvas { canvas ->
        val cr = cornerRadius.toPx()
        val spreadPx = spread.toPx()

        val outerPaint = Paint().apply {
            this.color = color.copy(alpha = outerAlpha)
            asFrameworkPaint().maskFilter =
                BlurMaskFilter(outerRadius.toPx(), BlurMaskFilter.Blur.NORMAL)
        }
        canvas.drawRoundRect(
            left = -spreadPx,
            top = -spreadPx,
            right = size.width + spreadPx,
            bottom = size.height + spreadPx,
            radiusX = cr,
            radiusY = cr,
            paint = outerPaint,
        )

        val innerPaint = Paint().apply {
            this.color = color.copy(alpha = innerAlpha)
            asFrameworkPaint().maskFilter =
                BlurMaskFilter(innerRadius.toPx(), BlurMaskFilter.Blur.NORMAL)
        }
        canvas.drawRoundRect(
            left = 0f,
            top = 0f,
            right = size.width,
            bottom = size.height,
            radiusX = cr,
            radiusY = cr,
            paint = innerPaint,
        )
    }
}

/**
 * Staggered Fade-In für einzelne Items.
 */
@Composable
fun StaggeredFadeIn(
    index: Int,
    modifier: Modifier = Modifier,
    delayPerItemMs: Long = 50L,
    baseDurationMs: Int = 400,
    slideOffsetY: Int = 50,
    content: @Composable () -> Unit,
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(index * delayPerItemMs)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            animationSpec = tween(durationMillis = baseDurationMs),
        ) + slideInVertically(
            initialOffsetY = { slideOffsetY },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow,
            ),
        ),
        modifier = modifier,
    ) {
        content()
    }
}

/**
 * LazyListScope Extension: Items mit automatischem Staggered Fade-In.
 */
inline fun <T> LazyListScope.staggeredItems(
    items: List<T>,
    noinline key: ((T) -> Any)? = null,
    delayPerItemMs: Long = 50L,
    crossinline itemContent: @Composable LazyItemScope.(index: Int, item: T) -> Unit,
) {
    items(
        count = items.size,
        key = if (key != null) { index -> key(items[index]) } else null,
    ) { index ->
        StaggeredFadeIn(
            index = index,
            delayPerItemMs = delayPerItemMs,
        ) {
            itemContent(index, items[index])
        }
    }
}

/**
 * Animierter Zahlencounter.
 */
@Composable
fun AnimatedCounter(
    targetValue: Int,
    modifier: Modifier = Modifier,
    durationMs: Int = 800,
    prefix: String = "",
    suffix: String = "",
    style: TextStyle = MaterialTheme.typography.headlineLarge,
    color: Color = MaterialTheme.colorScheme.primary,
    fontWeight: FontWeight = FontWeight.Bold,
) {
    val animatable = remember { Animatable(0f) }
    var displayValue by remember { mutableIntStateOf(0) }

    LaunchedEffect(targetValue) {
        animatable.animateTo(
            targetValue = targetValue.toFloat(),
            animationSpec = tween(
                durationMillis = durationMs,
                easing = FastOutSlowInEasing,
            ),
        )
    }

    displayValue = animatable.value.roundToInt()

    Text(
        text = "$prefix$displayValue$suffix",
        style = style,
        color = color,
        fontWeight = fontWeight,
        modifier = modifier,
    )
}

/**
 * Shimmer-Loading-Modifier.
 */
fun Modifier.shimmerEffect(
    durationMs: Int = 1200,
    baseColor: Color = BoltSurface,
    highlightColor: Color = BoltSurfaceVariant,
) = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateX by transition.animateFloat(
        initialValue = -300f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmerTranslate",
    )

    val shimmerBrush = Brush.linearGradient(
        colors = listOf(baseColor, highlightColor, baseColor),
        start = Offset(translateX, 0f),
        end = Offset(translateX + 300f, 0f),
    )

    background(shimmerBrush)
}

/**
 * Skeleton-Platzhalter für eine VorgangKarte.
 */
@Composable
fun VorgangKarteSkeleton(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(BoltMindDimensions.spacingM),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(BoltMindDimensions.fotoPreviewSmall)
                    .clip(RoundedCornerShape(BoltMindDimensions.cornerS))
                    .shimmerEffect(),
            )
            Spacer(modifier = Modifier.width(BoltMindDimensions.spacingM))
            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerEffect(),
                )
                Spacer(modifier = Modifier.height(BoltMindDimensions.spacingS))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.35f)
                        .height(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerEffect(),
                )
                Spacer(modifier = Modifier.height(BoltMindDimensions.spacingXs))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.25f)
                        .height(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerEffect(),
                )
            }
        }
    }
}
