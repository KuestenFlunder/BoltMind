package com.boltmind.app.feature.uebersicht

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.boltmind.app.R
import com.boltmind.app.ui.components.FotoPreview
import com.boltmind.app.ui.theme.BoltMindTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UebersichtScreen(
    uiState: UebersichtUiState,
    onVorgangGetippt: (Long) -> Unit,
    onNeuerVorgangGetippt: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNeuerVorgangGetippt) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.neuer_vorgang),
                )
            }
        },
        modifier = modifier,
    ) { innerPadding ->
        if (uiState.vorgaenge.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.keine_vorgaenge_hinweis),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(
                    items = uiState.vorgaenge,
                    key = { it.id },
                ) { vorgang ->
                    VorgangKarte(
                        vorgang = vorgang,
                        onClick = { onVorgangGetippt(vorgang.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun VorgangKarte(
    vorgang: VorgangUiItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FotoPreview(
                fotoPfad = vorgang.fahrzeugFotoPfad,
                contentDescription = stringResource(R.string.fahrzeugfoto),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "#${vorgang.auftragsnummer}",
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.schritte_anzahl, vorgang.anzahlSchritte),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = vorgang.erstelltAm,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun UebersichtScreenMitVorgaengenPreview() {
    BoltMindTheme {
        UebersichtScreen(
            uiState = UebersichtUiState(
                vorgaenge = listOf(
                    VorgangUiItem(
                        id = 1L,
                        fahrzeugFotoPfad = null,
                        auftragsnummer = "2024-0815",
                        anzahlSchritte = 12,
                        erstelltAm = "Heute",
                    ),
                    VorgangUiItem(
                        id = 2L,
                        fahrzeugFotoPfad = null,
                        auftragsnummer = "2024-0712",
                        anzahlSchritte = 8,
                        erstelltAm = "Gestern",
                    ),
                ),
            ),
            onVorgangGetippt = {},
            onNeuerVorgangGetippt = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun UebersichtScreenLeerPreview() {
    BoltMindTheme {
        UebersichtScreen(
            uiState = UebersichtUiState(),
            onVorgangGetippt = {},
            onNeuerVorgangGetippt = {},
        )
    }
}
