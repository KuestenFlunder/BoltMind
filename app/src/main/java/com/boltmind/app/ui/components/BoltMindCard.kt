package com.boltmind.app.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.boltmind.app.ui.theme.BoltMindDimensions
import com.boltmind.app.ui.theme.BoltMindTheme
import com.boltmind.app.ui.theme.BoltOutlineVariant

@Composable
fun BoltMindCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    if (onClick != null) {
        var lastClickTime by remember { mutableLongStateOf(0L) }
        val cardModifier = modifier
            .fillMaxWidth()
            .heightIn(min = BoltMindDimensions.touchTargetMin)
            .border(BoltMindDimensions.borderThin, BoltOutlineVariant, MaterialTheme.shapes.medium)
        Surface(
            onClick = {
                val now = System.currentTimeMillis()
                if (now - lastClickTime >= 300L) {
                    lastClickTime = now
                    onClick()
                }
            },
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            modifier = cardModifier,
        ) {
            Box(modifier = Modifier.padding(BoltMindDimensions.spacingM)) {
                content()
            }
        }
    } else {
        val cardModifier = modifier.fillMaxWidth()
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            modifier = cardModifier,
        ) {
            Box(modifier = Modifier.padding(BoltMindDimensions.spacingM)) {
                content()
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121110)
@Composable
private fun BoltMindCardPreview() {
    BoltMindTheme {
        BoltMindCard {
            Text("Inhalt der Karte", color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121110)
@Composable
private fun BoltMindCardClickablePreview() {
    BoltMindTheme {
        BoltMindCard(onClick = {}) {
            Text("Klickbare Karte", color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
