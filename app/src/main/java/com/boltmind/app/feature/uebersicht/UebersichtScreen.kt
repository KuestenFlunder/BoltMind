package com.boltmind.app.feature.uebersicht

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.boltmind.app.R
import com.boltmind.app.data.model.Reparaturvorgang
import com.boltmind.app.data.model.ReparaturvorgangMitSchrittanzahl
import com.boltmind.app.data.model.VorgangStatus
import com.boltmind.app.ui.navigation.BoltMindRoute
import com.boltmind.app.ui.theme.BoltMindTheme
import org.koin.androidx.compose.koinViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun UebersichtScreen(
    onNavigate: (String) -> Unit,
    viewModel: UebersichtViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { route ->
            onNavigate(route)
        }
    }

    UebersichtInhalt(
        uiState = uiState,
        onTabGewechselt = viewModel::onTabGewechselt,
        onVorgangGetippt = viewModel::onVorgangGetippt,
        onLoeschenAngefragt = viewModel::onLoeschenAngefragt,
        onLoeschenBestaetigt = viewModel::onLoeschenBestaetigt,
        onLoeschenAbgebrochen = viewModel::onLoeschenAbgebrochen,
        onAuswahlGetroffen = viewModel::onAuswahlGetroffen,
        onAuswahlAbgebrochen = viewModel::onAuswahlAbgebrochen,
        onNeuerVorgang = { onNavigate(BoltMindRoute.NEUER_VORGANG.route) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UebersichtInhalt(
    uiState: UebersichtUiState,
    onTabGewechselt: (VorgangStatus) -> Unit,
    onVorgangGetippt: (ReparaturvorgangMitSchrittanzahl) -> Unit,
    onLoeschenAngefragt: (Reparaturvorgang) -> Unit,
    onLoeschenBestaetigt: () -> Unit,
    onLoeschenAbgebrochen: () -> Unit,
    onAuswahlGetroffen: (String) -> Unit,
    onAuswahlAbgebrochen: () -> Unit,
    onNeuerVorgang: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNeuerVorgang) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.fab_neuer_vorgang)
                )
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            UebersichtTabs(
                aktuellerTab = uiState.aktuellerTab,
                onTabGewechselt = onTabGewechselt
            )

            if (uiState.vorgaenge.isEmpty()) {
                LeererZustand(aktuellerTab = uiState.aktuellerTab)
            } else {
                VorgangListe(
                    vorgaenge = uiState.vorgaenge,
                    onVorgangGetippt = onVorgangGetippt,
                    onLoeschenAngefragt = onLoeschenAngefragt
                )
            }
        }

        uiState.loeschDialog?.let { vorgang ->
            LoeschDialog(
                vorgang = vorgang,
                onBestaetigt = onLoeschenBestaetigt,
                onAbgebrochen = onLoeschenAbgebrochen
            )
        }

        uiState.auswahlDialog?.let { vorgangMitAnzahl ->
            AuswahlDialog(
                vorgang = vorgangMitAnzahl,
                onAuswahlGetroffen = onAuswahlGetroffen,
                onAbgebrochen = onAuswahlAbgebrochen
            )
        }
    }
}

@Composable
private fun UebersichtTabs(
    aktuellerTab: VorgangStatus,
    onTabGewechselt: (VorgangStatus) -> Unit
) {
    val tabs = listOf(VorgangStatus.OFFEN, VorgangStatus.ARCHIVIERT)
    val selectedIndex = tabs.indexOf(aktuellerTab)

    TabRow(selectedTabIndex = selectedIndex) {
        Tab(
            selected = aktuellerTab == VorgangStatus.OFFEN,
            onClick = { onTabGewechselt(VorgangStatus.OFFEN) },
            text = { Text(stringResource(R.string.tab_offen)) }
        )
        Tab(
            selected = aktuellerTab == VorgangStatus.ARCHIVIERT,
            onClick = { onTabGewechselt(VorgangStatus.ARCHIVIERT) },
            text = { Text(stringResource(R.string.tab_archiv)) }
        )
    }
}

@Composable
private fun LeererZustand(aktuellerTab: VorgangStatus) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Build,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (aktuellerTab == VorgangStatus.OFFEN) {
                stringResource(R.string.uebersicht_leer_offen)
            } else {
                stringResource(R.string.uebersicht_leer_archiv)
            },
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        if (aktuellerTab == VorgangStatus.OFFEN) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.uebersicht_leer_offen_hinweis),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun VorgangListe(
    vorgaenge: List<ReparaturvorgangMitSchrittanzahl>,
    onVorgangGetippt: (ReparaturvorgangMitSchrittanzahl) -> Unit,
    onLoeschenAngefragt: (Reparaturvorgang) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = 16.dp,
            vertical = 12.dp
        )
    ) {
        items(
            items = vorgaenge,
            key = { it.vorgang.id }
        ) { vorgangMitAnzahl ->
            SwipeVorgangCard(
                vorgangMitAnzahl = vorgangMitAnzahl,
                onGetippt = { onVorgangGetippt(vorgangMitAnzahl) },
                onLoeschen = { onLoeschenAngefragt(vorgangMitAnzahl.vorgang) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeVorgangCard(
    vorgangMitAnzahl: ReparaturvorgangMitSchrittanzahl,
    onGetippt: () -> Unit,
    onLoeschen: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onLoeschen()
                false
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color by animateColorAsState(
                targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) {
                    MaterialTheme.colorScheme.errorContainer
                } else {
                    MaterialTheme.colorScheme.surface
                },
                label = "swipe-background"
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = stringResource(R.string.dialog_loeschen_bestaetigen),
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        },
        enableDismissFromStartToEnd = false
    ) {
        VorgangCard(
            vorgangMitAnzahl = vorgangMitAnzahl,
            onClick = onGetippt
        )
    }
}

@Composable
private fun VorgangCard(
    vorgangMitAnzahl: ReparaturvorgangMitSchrittanzahl,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp.coerceAtLeast(72.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = vorgangMitAnzahl.vorgang.fahrzeug,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(
                    R.string.uebersicht_auftrag_prefix,
                    vorgangMitAnzahl.vorgang.auftragsnummer
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row {
                Text(
                    text = stringResource(
                        R.string.uebersicht_schritte_format,
                        vorgangMitAnzahl.schrittAnzahl
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = " \u00B7 ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatRelativesDatum(vorgangMitAnzahl.vorgang.erstelltAm),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun formatRelativesDatum(instant: Instant): String {
    val heute = LocalDate.now()
    val datum = instant.atZone(ZoneId.systemDefault()).toLocalDate()
    return when {
        datum == heute -> stringResource(R.string.uebersicht_datum_heute)
        datum == heute.minusDays(1) -> stringResource(R.string.uebersicht_datum_gestern)
        else -> datum.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
    }
}

@Composable
private fun LoeschDialog(
    vorgang: Reparaturvorgang,
    onBestaetigt: () -> Unit,
    onAbgebrochen: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onAbgebrochen,
        title = { Text(stringResource(R.string.dialog_loeschen_titel)) },
        text = {
            Text(stringResource(R.string.dialog_loeschen_text, vorgang.fahrzeug))
        },
        confirmButton = {
            TextButton(
                onClick = onBestaetigt,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(stringResource(R.string.dialog_loeschen_bestaetigen))
            }
        },
        dismissButton = {
            TextButton(onClick = onAbgebrochen) {
                Text(stringResource(R.string.dialog_abbrechen))
            }
        }
    )
}

@Composable
private fun AuswahlDialog(
    vorgang: ReparaturvorgangMitSchrittanzahl,
    onAuswahlGetroffen: (String) -> Unit,
    onAbgebrochen: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onAbgebrochen,
        icon = {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null
            )
        },
        title = {
            Text(
                text = "${vorgang.vorgang.fahrzeug} \u00B7 ${vorgang.vorgang.auftragsnummer}",
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = null,
        confirmButton = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TextButton(
                    onClick = {
                        onAuswahlGetroffen(
                            BoltMindRoute.demontageRoute(vorgang.vorgang.id)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.dialog_auswahl_demontage))
                }
                TextButton(
                    onClick = {
                        onAuswahlGetroffen(
                            BoltMindRoute.montageRoute(vorgang.vorgang.id)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.dialog_auswahl_montage))
                }
            }
        },
        dismissButton = null
    )
}

@Preview(showBackground = true)
@Composable
private fun UebersichtMitVorgaengenPreview() {
    val jetzt = Instant.now()
    val gestern = jetzt.minusSeconds(86400)
    val letzeWoche = jetzt.minusSeconds(604800)

    val beispielState = UebersichtUiState(
        vorgaenge = listOf(
            ReparaturvorgangMitSchrittanzahl(
                vorgang = Reparaturvorgang(
                    id = 1,
                    fahrzeug = "BMW 320d",
                    auftragsnummer = "#2024-0815",
                    beschreibung = "Getriebe defekt",
                    anzahlAblageorte = 5,
                    status = VorgangStatus.OFFEN,
                    erstelltAm = jetzt
                ),
                schrittAnzahl = 12
            ),
            ReparaturvorgangMitSchrittanzahl(
                vorgang = Reparaturvorgang(
                    id = 2,
                    fahrzeug = "VW Golf GTI",
                    auftragsnummer = "#2024-0712",
                    beschreibung = "Bremsen wechseln",
                    anzahlAblageorte = 3,
                    status = VorgangStatus.OFFEN,
                    erstelltAm = gestern
                ),
                schrittAnzahl = 8
            ),
            ReparaturvorgangMitSchrittanzahl(
                vorgang = Reparaturvorgang(
                    id = 3,
                    fahrzeug = "Audi A4 Avant",
                    auftragsnummer = "#2024-0601",
                    beschreibung = "Turbolader tauschen",
                    anzahlAblageorte = 8,
                    status = VorgangStatus.OFFEN,
                    erstelltAm = letzeWoche
                ),
                schrittAnzahl = 0
            )
        ),
        aktuellerTab = VorgangStatus.OFFEN
    )

    BoltMindTheme {
        UebersichtInhalt(
            uiState = beispielState,
            onTabGewechselt = {},
            onVorgangGetippt = {},
            onLoeschenAngefragt = {},
            onLoeschenBestaetigt = {},
            onLoeschenAbgebrochen = {},
            onAuswahlGetroffen = {},
            onAuswahlAbgebrochen = {},
            onNeuerVorgang = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun UebersichtLeerPreview() {
    BoltMindTheme {
        UebersichtInhalt(
            uiState = UebersichtUiState(),
            onTabGewechselt = {},
            onVorgangGetippt = {},
            onLoeschenAngefragt = {},
            onLoeschenBestaetigt = {},
            onLoeschenAbgebrochen = {},
            onAuswahlGetroffen = {},
            onAuswahlAbgebrochen = {},
            onNeuerVorgang = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoeschDialogPreview() {
    BoltMindTheme {
        LoeschDialog(
            vorgang = Reparaturvorgang(
                id = 1,
                fahrzeug = "BMW 320d",
                auftragsnummer = "#2024-0815",
                beschreibung = "Getriebe defekt",
                anzahlAblageorte = 5,
                status = VorgangStatus.OFFEN,
                erstelltAm = Instant.now()
            ),
            onBestaetigt = {},
            onAbgebrochen = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AuswahlDialogPreview() {
    BoltMindTheme {
        AuswahlDialog(
            vorgang = ReparaturvorgangMitSchrittanzahl(
                vorgang = Reparaturvorgang(
                    id = 1,
                    fahrzeug = "BMW 320d",
                    auftragsnummer = "#2024-0815",
                    beschreibung = "Getriebe defekt",
                    anzahlAblageorte = 5,
                    status = VorgangStatus.OFFEN,
                    erstelltAm = Instant.now()
                ),
                schrittAnzahl = 12
            ),
            onAuswahlGetroffen = {},
            onAbgebrochen = {}
        )
    }
}
