package com.boltmind.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.boltmind.app.R
import com.boltmind.app.ui.theme.BoltMindDimensions
import com.boltmind.app.ui.theme.BoltMindTheme
import com.boltmind.app.ui.theme.BoltOutlineVariant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoltMindTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onZurueck: (() -> Unit)? = null,
) {
    Column(modifier = modifier) {
        TopAppBar(
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                )
            },
            navigationIcon = {
                if (onZurueck != null) {
                    IconButton(
                        onClick = onZurueck,
                        modifier = Modifier.size(BoltMindDimensions.touchTargetMin),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.zurueck),
                            modifier = Modifier.size(BoltMindDimensions.topBarIconSize),
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            ),
        )
        HorizontalDivider(
            color = BoltOutlineVariant,
            thickness = BoltMindDimensions.borderThin,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121110)
@Composable
private fun BoltMindTopBarPreview() {
    BoltMindTheme {
        BoltMindTopBar(title = "BoltMind")
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121110)
@Composable
private fun BoltMindTopBarMitZurueckPreview() {
    BoltMindTheme {
        BoltMindTopBar(
            title = "Neuer Vorgang",
            onZurueck = {},
        )
    }
}
