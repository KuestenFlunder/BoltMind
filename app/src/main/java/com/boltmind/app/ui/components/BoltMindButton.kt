package com.boltmind.app.ui.components

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.boltmind.app.ui.theme.BoltMindDimensions
import com.boltmind.app.ui.theme.BoltMindTheme

enum class BoltMindButtonStyle {
    Primary,
    Secondary,
    Danger,
    Outlined,
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
            Button(
                onClick = debouncedClick,
                enabled = enabled && !isLoading,
                shape = MaterialTheme.shapes.medium,
                modifier = buttonModifier,
            ) {
                ButtonContent(text = text, isLoading = isLoading, icon = icon, iconAboveText = iconAboveText)
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
            Button(
                onClick = debouncedClick,
                enabled = enabled && !isLoading,
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                ),
                modifier = buttonModifier,
            ) {
                ButtonContent(text = text, isLoading = isLoading, icon = icon, iconAboveText = iconAboveText)
            }
        }
        BoltMindButtonStyle.Outlined -> {
            OutlinedButton(
                onClick = debouncedClick,
                enabled = enabled && !isLoading,
                shape = MaterialTheme.shapes.medium,
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

@Preview(showBackground = true, backgroundColor = 0xFF000000)
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

@Preview(showBackground = true, backgroundColor = 0xFF000000)
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

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun BoltMindButtonDangerPreview() {
    BoltMindTheme {
        BoltMindButton(
            text = "Löschen",
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
            style = BoltMindButtonStyle.Danger,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
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
