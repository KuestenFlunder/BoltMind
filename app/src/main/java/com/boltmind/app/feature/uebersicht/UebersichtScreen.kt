package com.boltmind.app.feature.uebersicht

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.boltmind.app.R
import com.boltmind.app.ui.components.BoltMindButton
import com.boltmind.app.ui.components.BoltMindButtonStyle
import com.boltmind.app.ui.components.BoltMindCard
import com.boltmind.app.ui.components.BoltMindDialog
import com.boltmind.app.ui.components.BoltMindTopBar
import com.boltmind.app.ui.components.FotoPreview
import com.boltmind.app.ui.components.StatusBadge
import com.boltmind.app.ui.components.StatusBadgeTyp
import com.boltmind.app.ui.theme.BoltMindDimensions
import com.boltmind.app.ui.theme.BoltMindTheme

@Composable
private fun datumText(datum: DatumAnzeige): String = when (datum) {
    is DatumAnzeige.Heute -> stringResource(R.string.datum_heute)
    is DatumAnzeige.Gestern -> stringResource(R.string.datum_gestern)
    is DatumAnzeige.Formatiert -> datum.text
}

@Composable
private fun dauerText(dauer: DauerAnzeige): String = when (dauer) {
    is DauerAnzeige.WenigerAlsEineMinute -> stringResource(R.string.dauer_weniger_als_eine_minute)
    is DauerAnzeige.Minuten -> stringResource(R.string.dauer_minuten, dauer.minuten)
    is DauerAnzeige.StundenMinuten -> stringResource(R.string.dauer_stunden_minuten, dauer.stunden, dauer.minuten)
}

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
            BoltMindTopBar(title = stringResource(R.string.app_name))
        },
        floatingActionButton = {
            if (uiState.selectedTab == 0) {
                FloatingActionButton(
                    onClick = onNeuerVorgangGetippt,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(BoltMindDimensions.fabSize),
                ) {
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
            contentPadding = PaddingValues(BoltMindDimensions.spacingM),
            verticalArrangement = Arrangement.spacedBy(BoltMindDimensions.spacingM),
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
            contentPadding = PaddingValues(BoltMindDimensions.spacingM),
            verticalArrangement = Arrangement.spacedBy(BoltMindDimensions.spacingM),
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
                    .clip(MaterialTheme.shapes.medium)
                    .background(color)
                    .padding(horizontal = BoltMindDimensions.spacingL),
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
    BoltMindCard(
        onClick = onClick,
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FotoPreview(
                fotoPfad = vorgang.fahrzeugFotoPfad,
                contentDescription = stringResource(R.string.fahrzeugfoto),
            )
            Spacer(modifier = Modifier.width(BoltMindDimensions.spacingM))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(BoltMindDimensions.spacingS),
                ) {
                    Text(
                        text = "#${vorgang.auftragsnummer}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    StatusBadge(typ = StatusBadgeTyp.Offen)
                }
                Spacer(modifier = Modifier.height(BoltMindDimensions.spacingXs))
                Text(
                    text = stringResource(R.string.schritte_anzahl, vorgang.anzahlSchritte),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = datumText(vorgang.erstelltAm),
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
    BoltMindCard(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FotoPreview(
                fotoPfad = vorgang.fahrzeugFotoPfad,
                contentDescription = stringResource(R.string.fahrzeugfoto),
            )
            Spacer(modifier = Modifier.width(BoltMindDimensions.spacingM))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(BoltMindDimensions.spacingS),
                ) {
                    Text(
                        text = "#${vorgang.auftragsnummer}",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    StatusBadge(typ = StatusBadgeTyp.Archiviert)
                }
                Spacer(modifier = Modifier.height(BoltMindDimensions.spacingXs))
                Text(
                    text = stringResource(R.string.schritte_anzahl, vorgang.anzahlSchritte),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(R.string.gesamtdauer, dauerText(vorgang.gesamtdauer)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(R.string.abgeschlossen_am, datumText(vorgang.abschlussDatum)),
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
    BoltMindDialog(onDismissRequest = onVerwerfen) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FotoPreview(
                fotoPfad = dialogState.fahrzeugFotoPfad,
                contentDescription = stringResource(R.string.fahrzeugfoto),
                groesse = BoltMindDimensions.touchTargetMin,
            )
            Spacer(modifier = Modifier.width(BoltMindDimensions.spacingM))
            Text(
                text = "#${dialogState.auftragsnummer}",
                style = MaterialTheme.typography.titleLarge,
            )
        }
        Spacer(modifier = Modifier.height(BoltMindDimensions.spacingL))
        BoltMindButton(
            text = stringResource(R.string.weiter_demontieren),
            onClick = onWeiterDemontieren,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
        )
        Spacer(modifier = Modifier.height(BoltMindDimensions.spacingM))
        BoltMindButton(
            text = stringResource(R.string.montage_starten),
            onClick = onMontageStarten,
            style = BoltMindButtonStyle.Secondary,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
        )
    }
}

@Composable
private fun LoeschenBestaetigungDialog(
    dialogState: LoeschenDialogState,
    onBestaetigt: () -> Unit,
    onAbgebrochen: () -> Unit,
) {
    BoltMindDialog(onDismissRequest = onAbgebrochen) {
        Text(
            text = stringResource(R.string.loeschen_bestaetigung, dialogState.auftragsnummer),
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(modifier = Modifier.height(BoltMindDimensions.spacingL))
        Column(
            verticalArrangement = Arrangement.spacedBy(BoltMindDimensions.spacingM),
        ) {
            BoltMindButton(
                text = stringResource(R.string.loeschen),
                onClick = onBestaetigt,
                style = BoltMindButtonStyle.Danger,
                modifier = Modifier.fillMaxWidth(),
            )
            BoltMindButton(
                text = stringResource(R.string.abbrechen),
                onClick = onAbgebrochen,
                style = BoltMindButtonStyle.Outlined,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF060B14)
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
                        erstelltAm = DatumAnzeige.Heute,
                    ),
                    VorgangUiItem(
                        id = 2L,
                        fahrzeugFotoPfad = null,
                        auftragsnummer = "2024-0712",
                        anzahlSchritte = 8,
                        erstelltAm = DatumAnzeige.Gestern,
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

@Preview(showBackground = true, backgroundColor = 0xFF060B14)
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

@Preview(showBackground = true, backgroundColor = 0xFF060B14)
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
                        gesamtdauer = DauerAnzeige.StundenMinuten(2, 15),
                        abschlussDatum = DatumAnzeige.Formatiert("01.05.2024"),
                    ),
                    ArchivVorgangUiItem(
                        id = 4L,
                        fahrzeugFotoPfad = null,
                        auftragsnummer = "2024-0320",
                        anzahlSchritte = 7,
                        gesamtdauer = DauerAnzeige.Minuten(45),
                        abschlussDatum = DatumAnzeige.Formatiert("20.03.2024"),
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

@Preview(showBackground = true, backgroundColor = 0xFF060B14)
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

@Preview(showBackground = true, backgroundColor = 0xFF060B14)
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

@Preview(showBackground = true, backgroundColor = 0xFF060B14)
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
