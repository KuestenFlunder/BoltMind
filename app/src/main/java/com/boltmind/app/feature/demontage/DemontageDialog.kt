package com.boltmind.app.feature.demontage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview as ComposePreview
import com.boltmind.app.R
import com.boltmind.app.ui.components.BoltMindButton
import com.boltmind.app.ui.components.BoltMindButtonStyle
import com.boltmind.app.ui.theme.BoltMindDimensions
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
            .fillMaxSize()
            .padding(BoltMindDimensions.spacingM),
        verticalArrangement = Arrangement.spacedBy(BoltMindDimensions.spacingM),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BoltMindButton(
            text = stringResource(R.string.demontage_ablageort_fotografieren),
            onClick = onAblageortFotografieren,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_camera_placeholder),
                    contentDescription = null,
                    modifier = Modifier.size(BoltMindDimensions.iconLarge),
                )
            },
            iconAboveText = true,
        )
        BoltMindButton(
            text = stringResource(R.string.demontage_weiter_ohne_ablageort),
            onClick = onWeiterOhneAblageort,
            style = BoltMindButtonStyle.Secondary,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(BoltMindDimensions.iconLarge),
                )
            },
            iconAboveText = true,
        )
        BoltMindButton(
            text = stringResource(R.string.demontage_beenden),
            onClick = onBeenden,
            style = BoltMindButtonStyle.Outlined,
            modifier = Modifier
                .fillMaxWidth()
                .height(BoltMindDimensions.buttonCompact),
        )
    }
}

@ComposePreview(showBackground = true, backgroundColor = 0xFF121110)
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
