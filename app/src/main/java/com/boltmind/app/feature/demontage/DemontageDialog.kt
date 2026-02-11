package com.boltmind.app.feature.demontage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview as ComposePreview
import androidx.compose.ui.unit.dp
import com.boltmind.app.R
import com.boltmind.app.ui.theme.BoltMindTheme

@Composable
fun DemontageDialog(
    onAblageortFotografieren: () -> Unit,
    onWeiterOhneAblageort: () -> Unit,
    onBeenden: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        DebounceButton(
            text = stringResource(R.string.demontage_ablageort_fotografieren),
            onClick = onAblageortFotografieren,
        )
        DebounceButton(
            text = stringResource(R.string.demontage_weiter_ohne_ablageort),
            onClick = onWeiterOhneAblageort,
        )
        DebounceButton(
            text = stringResource(R.string.demontage_beenden),
            onClick = onBeenden,
        )
    }
}

@Composable
private fun DebounceButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val lastClick = remember { mutableLongStateOf(0L) }
    OutlinedButton(
        onClick = {
            val now = System.currentTimeMillis()
            if (now - lastClick.longValue >= 300L) {
                lastClick.longValue = now
                onClick()
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@ComposePreview(showBackground = true)
@Composable
private fun DemontageDialogPreview() {
    BoltMindTheme {
        DemontageDialog(
            onAblageortFotografieren = {},
            onWeiterOhneAblageort = {},
            onBeenden = {},
        )
    }
}
