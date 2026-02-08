package com.boltmind.app.feature.uebersicht

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    onWeiterDemontieren: () -> Unit,
    onMontageStarten: () -> Unit,
    onDialogVerwerfen: () -> Unit,
    onLoeschenAngefragt: (Long) -> Unit,
    onLoeschenBestaetigt: () -> Unit,
    onLoeschenAbgebrochen: () -> Unit,
    onTabGewaehlt: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
            )
        },
        floatingActionButton = {
            if (uiState.selectedTab == 0) {
                FloatingActionButton(onClick = onNeuerVorgangGetippt) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.neuer_vorgang),
                    )
                }
            }
        },
        modifier = modifier,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            TabRow(selectedTabIndex = uiState.selectedTab) {
                Tab(
                    selected = uiState.selectedTab == 0,
                    onClick = { onTabGewaehlt(0) },
                    text = { Text(stringResource(R.string.tab_offen)) },
                )
                Tab(
                    selected = uiState.selectedTab == 1,
                    onClick = { onTabGewaehlt(1) },
                    text = { Text(stringResource(R.string.tab_archiv)) },
                )
            }

            when (uiState.selectedTab) {
                0 -> OffeneVorgaengeListe(
                    vorgaenge = uiState.vorgaenge,
                    onVorgangGetippt = onVorgangGetippt,
                    onLoeschenAngefragt = onLoeschenAngefragt,
                )
                1 -> ArchivListe(
                    archivierteVorgaenge = uiState.archivierteVorgaenge,
                )
            }
        }
    }

    uiState.auswahlDialog?.let { dialog ->
        AuswahlDialog(
            dialogState = dialog,
            onWeiterDemontieren = onWeiterDemontieren,
            onMontageStarten = onMontageStarten,
            onVerwerfen = onDialogVerwerfen,
        )
    }

    uiState.loeschenDialog?.let { dialog ->
        LoeschenBestaetigungDialog(
            dialogState = dialog,
            onBestaetigt = onLoeschenBestaetigt,
            onAbgebrochen = onLoeschenAbgebrochen,
        )
    }
}

@Composable
private fun OffeneVorgaengeListe(
    vorgaenge: List<VorgangUiItem>,
    onVorgangGetippt: (Long) -> Unit,
    onLoeschenAngefragt: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (vorgaenge.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
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
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(
                items = vorgaenge,
                key = { it.id },
            ) { vorgang ->
                SwipeToDeleteVorgangKarte(
                    vorgang = vorgang,
                    onClick = { onVorgangGetippt(vorgang.id) },
                    onLoeschenAngefragt = { onLoeschenAngefragt(vorgang.id) },
                )
            }
        }
    }
}

@Composable
private fun ArchivListe(
    archivierteVorgaenge: List<ArchivVorgangUiItem>,
    modifier: Modifier = Modifier,
) {
    if (archivierteVorgaenge.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.keine_archivierten_vorgaenge),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(
                items = archivierteVorgaenge,
                key = { it.id },
            ) { vorgang ->
                ArchivVorgangKarte(vorgang = vorgang)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteVorgangKarte(
    vorgang: VorgangUiItem,
    onClick: () -> Unit,
    onLoeschenAngefragt: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                onLoeschenAngefragt()
                false
            } else {
                false
            }
        },
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color by animateColorAsState(
                targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                label = "swipeBackground",
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.loeschen_hintergrund),
                    tint = MaterialTheme.colorScheme.onError,
                )
            }
        },
        enableDismissFromStartToEnd = false,
        modifier = modifier,
    ) {
        VorgangKarte(
            vorgang = vorgang,
            onClick = onClick,
        )
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

@Composable
private fun ArchivVorgangKarte(
    vorgang: ArchivVorgangUiItem,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
                    text = stringResource(R.string.gesamtdauer, vorgang.gesamtdauer),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(R.string.abgeschlossen_am, vorgang.abschlussDatum),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun AuswahlDialog(
    dialogState: AuswahlDialogState,
    onWeiterDemontieren: () -> Unit,
    onMontageStarten: () -> Unit,
    onVerwerfen: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onVerwerfen,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FotoPreview(
                    fotoPfad = dialogState.fahrzeugFotoPfad,
                    contentDescription = stringResource(R.string.fahrzeugfoto),
                    groesse = 56.dp,
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "#${dialogState.auftragsnummer}",
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = onWeiterDemontieren,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.weiter_demontieren))
                }
                Button(
                    onClick = onMontageStarten,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.montage_starten))
                }
            }
        },
        confirmButton = {},
    )
}

@Composable
private fun LoeschenBestaetigungDialog(
    dialogState: LoeschenDialogState,
    onBestaetigt: () -> Unit,
    onAbgebrochen: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onAbgebrochen,
        title = null,
        text = {
            Text(
                text = stringResource(R.string.loeschen_bestaetigung, dialogState.auftragsnummer),
                style = MaterialTheme.typography.bodyLarge,
            )
        },
        confirmButton = {
            Button(
                onClick = onBestaetigt,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                ),
            ) {
                Text(stringResource(R.string.loeschen))
            }
        },
        dismissButton = {
            TextButton(onClick = onAbgebrochen) {
                Text(stringResource(R.string.abbrechen))
            }
        },
    )
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
            onWeiterDemontieren = {},
            onMontageStarten = {},
            onDialogVerwerfen = {},
            onLoeschenAngefragt = {},
            onLoeschenBestaetigt = {},
            onLoeschenAbgebrochen = {},
            onTabGewaehlt = {},
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
            onWeiterDemontieren = {},
            onMontageStarten = {},
            onDialogVerwerfen = {},
            onLoeschenAngefragt = {},
            onLoeschenBestaetigt = {},
            onLoeschenAbgebrochen = {},
            onTabGewaehlt = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun UebersichtScreenArchivPreview() {
    BoltMindTheme {
        UebersichtScreen(
            uiState = UebersichtUiState(
                selectedTab = 1,
                archivierteVorgaenge = listOf(
                    ArchivVorgangUiItem(
                        id = 3L,
                        fahrzeugFotoPfad = null,
                        auftragsnummer = "2024-0501",
                        anzahlSchritte = 15,
                        gesamtdauer = "2h 15min",
                        abschlussDatum = "01.05.2024",
                    ),
                    ArchivVorgangUiItem(
                        id = 4L,
                        fahrzeugFotoPfad = null,
                        auftragsnummer = "2024-0320",
                        anzahlSchritte = 7,
                        gesamtdauer = "45 min",
                        abschlussDatum = "20.03.2024",
                    ),
                ),
            ),
            onVorgangGetippt = {},
            onNeuerVorgangGetippt = {},
            onWeiterDemontieren = {},
            onMontageStarten = {},
            onDialogVerwerfen = {},
            onLoeschenAngefragt = {},
            onLoeschenBestaetigt = {},
            onLoeschenAbgebrochen = {},
            onTabGewaehlt = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun UebersichtScreenArchivLeerPreview() {
    BoltMindTheme {
        UebersichtScreen(
            uiState = UebersichtUiState(selectedTab = 1),
            onVorgangGetippt = {},
            onNeuerVorgangGetippt = {},
            onWeiterDemontieren = {},
            onMontageStarten = {},
            onDialogVerwerfen = {},
            onLoeschenAngefragt = {},
            onLoeschenBestaetigt = {},
            onLoeschenAbgebrochen = {},
            onTabGewaehlt = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AuswahlDialogPreview() {
    BoltMindTheme {
        AuswahlDialog(
            dialogState = AuswahlDialogState(
                vorgangId = 1L,
                auftragsnummer = "2024-0815",
                fahrzeugFotoPfad = null,
            ),
            onWeiterDemontieren = {},
            onMontageStarten = {},
            onVerwerfen = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoeschenBestaetigungDialogPreview() {
    BoltMindTheme {
        LoeschenBestaetigungDialog(
            dialogState = LoeschenDialogState(
                vorgangId = 1L,
                auftragsnummer = "2024-0815",
            ),
            onBestaetigt = {},
            onAbgebrochen = {},
        )
    }
}
