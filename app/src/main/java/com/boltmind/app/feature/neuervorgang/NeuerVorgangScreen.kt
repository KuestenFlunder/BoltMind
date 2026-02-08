package com.boltmind.app.feature.neuervorgang

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.boltmind.app.R
import com.boltmind.app.ui.navigation.BoltMindRoute
import org.koin.androidx.compose.koinViewModel

@Composable
fun NeuerVorgangScreen(
    onNavigateToDemontage: (Long) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: NeuerVorgangViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is NavigationEvent.ZuDemontage -> onNavigateToDemontage(event.vorgangId)
                is NavigationEvent.Zurueck -> onNavigateBack()
            }
        }
    }

    NeuerVorgangContent(
        uiState = uiState,
        onFahrzeugGeaendert = viewModel::onFahrzeugGeaendert,
        onAuftragsnummerGeaendert = viewModel::onAuftragsnummerGeaendert,
        onBeschreibungGeaendert = viewModel::onBeschreibungGeaendert,
        onAnzahlAblageorteGeaendert = viewModel::onAnzahlAblageorteGeaendert,
        onStartenGeklickt = viewModel::onStartenGeklickt,
        onAbbrechenGeklickt = viewModel::onAbbrechenGeklickt
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeuerVorgangContent(
    uiState: NeuerVorgangUiState,
    onFahrzeugGeaendert: (String) -> Unit,
    onAuftragsnummerGeaendert: (String) -> Unit,
    onBeschreibungGeaendert: (String) -> Unit,
    onAnzahlAblageorteGeaendert: (String) -> Unit,
    onStartenGeklickt: () -> Unit,
    onAbbrechenGeklickt: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val focusRequesterAuftragsnummer = remember { FocusRequester() }
    val focusRequesterBeschreibung = remember { FocusRequester() }
    val focusRequesterAnzahl = remember { FocusRequester() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_neuer_vorgang)) },
                navigationIcon = {
                    IconButton(onClick = onAbbrechenGeklickt) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.dialog_abbrechen)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.fahrzeug,
                onValueChange = onFahrzeugGeaendert,
                label = { Text(stringResource(R.string.feld_fahrzeug)) },
                isError = uiState.fehler.containsKey(Feld.FAHRZEUG),
                supportingText = uiState.fehler[Feld.FAHRZEUG]?.let { fehlerKey ->
                    { Text(fehlerTextFuer(fehlerKey)) }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusRequesterAuftragsnummer.requestFocus() }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = uiState.auftragsnummer,
                onValueChange = onAuftragsnummerGeaendert,
                label = { Text(stringResource(R.string.feld_auftragsnummer)) },
                isError = uiState.fehler.containsKey(Feld.AUFTRAGSNUMMER),
                supportingText = uiState.fehler[Feld.AUFTRAGSNUMMER]?.let { fehlerKey ->
                    { Text(fehlerTextFuer(fehlerKey)) }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusRequesterBeschreibung.requestFocus() }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .focusRequester(focusRequesterAuftragsnummer)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = uiState.beschreibung,
                onValueChange = onBeschreibungGeaendert,
                label = { Text(stringResource(R.string.feld_beschreibung)) },
                isError = uiState.fehler.containsKey(Feld.BESCHREIBUNG),
                supportingText = uiState.fehler[Feld.BESCHREIBUNG]?.let { fehlerKey ->
                    { Text(fehlerTextFuer(fehlerKey)) }
                },
                maxLines = 3,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusRequesterAnzahl.requestFocus() }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequesterBeschreibung)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = uiState.anzahlAblageorte,
                onValueChange = onAnzahlAblageorteGeaendert,
                label = { Text(stringResource(R.string.feld_anzahl_ablageorte)) },
                isError = uiState.fehler.containsKey(Feld.ANZAHL_ABLAGEORTE),
                supportingText = uiState.fehler[Feld.ANZAHL_ABLAGEORTE]?.let { fehlerKey ->
                    { Text(fehlerTextFuer(fehlerKey)) }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .focusRequester(focusRequesterAnzahl)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onStartenGeklickt,
                enabled = !uiState.speichert,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(stringResource(R.string.button_starten))
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun fehlerTextFuer(fehlerKey: String): String {
    return when (fehlerKey) {
        NeuerVorgangViewModel.FEHLER_FAHRZEUG -> stringResource(R.string.fehler_fahrzeug_pflicht)
        NeuerVorgangViewModel.FEHLER_AUFTRAGSNUMMER -> stringResource(R.string.fehler_auftragsnummer_pflicht)
        NeuerVorgangViewModel.FEHLER_BESCHREIBUNG -> stringResource(R.string.fehler_beschreibung_pflicht)
        NeuerVorgangViewModel.FEHLER_ANZAHL_PFLICHT -> stringResource(R.string.fehler_anzahl_ablageorte_pflicht)
        NeuerVorgangViewModel.FEHLER_ANZAHL_UNGUELTIG -> stringResource(R.string.fehler_anzahl_ablageorte_ungueltig)
        else -> fehlerKey
    }
}

@Preview(showBackground = true, name = "Leeres Formular")
@Composable
private fun NeuerVorgangLeerPreview() {
    NeuerVorgangContent(
        uiState = NeuerVorgangUiState(),
        onFahrzeugGeaendert = {},
        onAuftragsnummerGeaendert = {},
        onBeschreibungGeaendert = {},
        onAnzahlAblageorteGeaendert = {},
        onStartenGeklickt = {},
        onAbbrechenGeklickt = {}
    )
}

@Preview(showBackground = true, name = "Ausgefuelltes Formular")
@Composable
private fun NeuerVorgangAusgefuelltPreview() {
    NeuerVorgangContent(
        uiState = NeuerVorgangUiState(
            fahrzeug = "BMW 320d Blau",
            auftragsnummer = "#2024-0815",
            beschreibung = "Bremsen vorne wechseln",
            anzahlAblageorte = "10"
        ),
        onFahrzeugGeaendert = {},
        onAuftragsnummerGeaendert = {},
        onBeschreibungGeaendert = {},
        onAnzahlAblageorteGeaendert = {},
        onStartenGeklickt = {},
        onAbbrechenGeklickt = {}
    )
}

@Preview(showBackground = true, name = "Formular mit Fehlern")
@Composable
private fun NeuerVorgangMitFehlerPreview() {
    NeuerVorgangContent(
        uiState = NeuerVorgangUiState(
            fehler = mapOf(
                Feld.FAHRZEUG to NeuerVorgangViewModel.FEHLER_FAHRZEUG,
                Feld.AUFTRAGSNUMMER to NeuerVorgangViewModel.FEHLER_AUFTRAGSNUMMER,
                Feld.BESCHREIBUNG to NeuerVorgangViewModel.FEHLER_BESCHREIBUNG,
                Feld.ANZAHL_ABLAGEORTE to NeuerVorgangViewModel.FEHLER_ANZAHL_PFLICHT
            )
        ),
        onFahrzeugGeaendert = {},
        onAuftragsnummerGeaendert = {},
        onBeschreibungGeaendert = {},
        onAnzahlAblageorteGeaendert = {},
        onStartenGeklickt = {},
        onAbbrechenGeklickt = {}
    )
}
