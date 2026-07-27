package com.boltmind.app.feature.browser

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.boltmind.app.R
import com.boltmind.app.ui.components.BoltSheet
import com.boltmind.app.ui.components.BoltSheetStil
import com.boltmind.app.ui.components.BoltText
import com.boltmind.app.ui.components.ColumnScopeMarker
import com.boltmind.app.ui.components.Flaeche
import com.boltmind.app.ui.components.Rundbutton
import com.boltmind.app.ui.components.SheetAktion
import com.boltmind.app.ui.components.boltKlick
import com.boltmind.app.ui.schrittbrowser.AmFahrzeugHinweis
import com.boltmind.app.ui.schrittbrowser.SchrittBrowser
import com.boltmind.app.ui.schrittbrowser.SchrittBrowserAktionen
import com.boltmind.app.ui.theme.BoltGruen
import com.boltmind.app.ui.theme.BoltGruenDunkel
import com.boltmind.app.ui.theme.BoltMindDimensions
import com.boltmind.app.ui.theme.BoltOrange
import com.boltmind.app.ui.theme.BoltTextHell
import com.boltmind.app.ui.theme.BoltTextLeise
import com.boltmind.app.ui.theme.BoltTextPrimaer
import com.boltmind.app.ui.theme.BoltTextRuhig
import com.boltmind.app.ui.theme.BoltTextSchwach
import com.boltmind.app.ui.theme.BoltTextWeiss
import com.boltmind.app.ui.theme.BoltTypo
import com.boltmind.app.ui.theme.BoltWeiss16
import com.boltmind.app.ui.theme.BoltWeiss22
import com.boltmind.app.ui.theme.GlasRezepte
import com.boltmind.app.ui.theme.glas

/**
 * Der Schritt-Browser als Screen. Ein Bildschirm, drei Betriebsarten -- so
 * zeichnet es der Entwurf.
 *
 * Zustandslos: alles kommt ueber [uiState], alles geht ueber die Rueckrufe.
 */
@Composable
fun BrowserScreen(
    uiState: BrowserUiState,
    modifier: Modifier = Modifier,
    onSchrittGewaehlt: (Int) -> Unit,
    onFotoGewaehlt: (Int) -> Unit,
    onVollbildOeffnen: () -> Unit,
    onVollbildSchliessen: () -> Unit,
    onLabelUmgeschaltet: (com.boltmind.app.data.model.SchrittFoto, com.boltmind.app.ui.schrittbrowser.LabelArt) -> Unit,
    onTimerUmgeschaltet: () -> Unit,
    onNaechstesTeil: () -> Unit,
    onWeiteresFoto: () -> Unit,
    onWiederholen: () -> Unit,
    onZumOffenenSchritt: () -> Unit,
    onEingebaut: () -> Unit,
    onHaekchenAnfragen: () -> Unit,
    onVorherigerSchritt: () -> Unit,
    onNaechsterSchritt: () -> Unit,
    onFeierabendAnfragen: () -> Unit,
    onVerlassen: () -> Unit,
    onSheetAktion: (SheetMarke) -> Unit,
    onSheetGeschlossen: () -> Unit
) {
    Box(modifier) {
        SchrittBrowser(
            zustand = uiState.alsBrowserZustand(),
            aktionen = SchrittBrowserAktionen(
                onSchrittGewaehlt = onSchrittGewaehlt,
                onFotoGewaehlt = onFotoGewaehlt,
                onVollbildOeffnen = onVollbildOeffnen,
                onVollbildSchliessen = onVollbildSchliessen,
                onLabelUmgeschaltet = onLabelUmgeschaltet
            ),
            kopfzeile = {
                Kopfzeile(uiState, onFeierabendAnfragen, onVerlassen)
            },
            ueberLabels = {
                if (!uiState.istArchiv) {
                    TimerKapsel(uiState, onTimerUmgeschaltet)
                }
                if (uiState.istMontage) {
                    Fortschritt(uiState)
                }
            },
            unterLabels = {
                if (uiState.amFahrzeugGeblieben) AmFahrzeugHinweis()
                if (uiState.istArchiv) ArchivPlakette()
            },
            bedienkreise = {
                Bedienkreise(
                    uiState = uiState,
                    onNaechstesTeil = onNaechstesTeil,
                    onWeiteresFoto = onWeiteresFoto,
                    onWiederholen = onWiederholen,
                    onZumOffenenSchritt = onZumOffenenSchritt,
                    onEingebaut = onEingebaut,
                    onHaekchenAnfragen = onHaekchenAnfragen,
                    onVorherigerSchritt = onVorherigerSchritt,
                    onNaechsterSchritt = onNaechsterSchritt,
                    onFeierabendAnfragen = onFeierabendAnfragen
                )
            }
        )

        uiState.sheet?.let { sheet ->
            BoltSheet(
                titel = sheet.titel,
                text = sheet.text,
                onAussenGetippt = onSheetGeschlossen
            ) {
                sheet.aktionen.forEach { a ->
                    SheetAktion(
                        text = a.text,
                        stil = when (a.stil) {
                            SheetStil.PRIMAER -> BoltSheetStil.PRIMAER
                            SheetStil.NORMAL -> BoltSheetStil.NORMAL
                            SheetStil.GEFAHR -> BoltSheetStil.GEFAHR
                        },
                        onKlick = { onSheetAktion(a.marke) }
                    )
                }
            }
        }
    }
}

@Composable
private fun Kopfzeile(
    uiState: BrowserUiState,
    onFeierabend: () -> Unit,
    onVerlassen: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = 10.dp, end = 14.dp, top = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        BoltText(
            text = "%02d".format(uiState.aktiverSchritt?.schritt?.schrittNummer ?: 0),
            stil = BoltTypo.schrittZifferRiesig,
            farbe = BoltTextWeiss
        )
        Column(
            Modifier
                .padding(start = 9.dp, top = 12.dp)
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            BoltText(
                text = stringResource(
                    when {
                        uiState.istDemontage -> R.string.browser_modus_demontage
                        uiState.istMontage -> R.string.browser_modus_montage
                        else -> R.string.browser_modus_archiv
                    }
                ),
                stil = BoltTypo.browserModus,
                farbe = BoltOrange
            )
            BoltText(
                text = "#${uiState.vorgang?.auftragsnummer.orEmpty()}",
                stil = BoltTypo.browserVorgang,
                farbe = BoltTextRuhig
            )
            BoltText(
                text = if (uiState.istArchiv) {
                    Zeitformat.mmss(uiState.schrittSekunden) +
                        " · Σ " + Zeitformat.lang(uiState.gesamtSekunden)
                } else {
                    uiState.vorgang?.beschreibung.orEmpty()
                },
                stil = BoltTypo.browserStatus,
                farbe = BoltTextSchwach
            )
        }
        // Nur die Demontage kann einen unfertigen Schritt hinterlassen, deshalb
        // fragt nur sie nach. Die Montage verlaesst der Nutzer ueber "RAUS", das
        // Archiv ueber das Kreuz -- zwei verschiedene Ausstiege im selben Modus
        // waeren ein Bedienfehler.
        when {
            uiState.istDemontage -> Row(
                Modifier
                    .padding(top = 6.dp)
                    .height(BoltMindDimensions.feierabendHoehe)
                    .boltKlick(onKlick = onFeierabend)
                    .glas(
                        GlasRezepte.neutral09,
                        RoundedCornerShape(BoltMindDimensions.radiusStandard),
                        BoltMindDimensions.radiusStandard
                    )
                    .padding(horizontal = 15.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Flaeche(BoltTextLeise, Modifier.size(9.dp), RoundedCornerShape(2.dp))
                BoltText(stringResource(R.string.browser_feierabend), BoltTypo.tab, BoltTextRuhig)
            }

            uiState.istArchiv -> Box(
                Modifier
                    .padding(top = 6.dp)
                    .size(BoltMindDimensions.schliesserGroesse)
                    .boltKlick(onKlick = onVerlassen)
                    .glas(
                        GlasRezepte.neutral09Flach,
                        RoundedCornerShape(BoltMindDimensions.radiusStandard),
                        BoltMindDimensions.radiusStandard
                    ),
                contentAlignment = Alignment.Center
            ) {
                BoltText(stringResource(R.string.zeichen_kreuz), BoltTypo.glyphe, BoltTextRuhig)
            }
        }
    }
}

@Composable
private fun TimerKapsel(uiState: BrowserUiState, onUmschalten: () -> Unit) {
    Row(
        Modifier
            .glas(
                GlasRezepte.kapsel,
                RoundedCornerShape(17.dp),
                17.dp
            )
            .padding(start = 6.dp, end = 14.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        Box(
            Modifier
                .size(BoltMindDimensions.timerSchalter)
                .boltKlick(stauchung = 0.94f, onKlick = onUmschalten)
                .glas(
                    if (uiState.timerLaeuft) GlasRezepte.orangeTimer else GlasRezepte.neutral08Timer,
                    RoundedCornerShape(BoltMindDimensions.radiusStandard),
                    BoltMindDimensions.radiusStandard
                ),
            contentAlignment = Alignment.Center
        ) {
            BoltText(
                text = stringResource(
                    if (uiState.timerLaeuft) R.string.zeichen_pause else R.string.zeichen_start
                ),
                stil = BoltTypo.labelChip,
                farbe = if (uiState.timerLaeuft) BoltTextWeiss else BoltTextHell
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            BoltText(
                text = stringResource(
                    if (uiState.timerLaeuft) R.string.timer_laeuft else R.string.timer_steht
                ),
                stil = BoltTypo.timerLabel,
                farbe = BoltTextSchwach
            )
            BoltText(Zeitformat.mmss(uiState.schrittSekunden), BoltTypo.timerZeit, BoltTextWeiss)
        }
    }
}

@Composable
private fun ColumnScope.Fortschritt(uiState: BrowserUiState) {
    Column(Modifier.width(112.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        BoltText(
            text = stringResource(
                R.string.montage_fortschritt,
                uiState.eingebauteAnzahl,
                uiState.gesamtAnzahl
            ),
            stil = BoltTypo.fortschritt,
            farbe = BoltTextLeise
        )
        Box(
            Modifier
                .fillMaxWidth()
                .height(BoltMindDimensions.fortschrittHoehe)
                .glas(GlasRezepte.labelAus, RoundedCornerShape(4.dp), 4.dp)
        ) {
            val anteil = if (uiState.gesamtAnzahl == 0) 0f
            else uiState.eingebauteAnzahl.toFloat() / uiState.gesamtAnzahl
            Flaeche(
                farbe = BoltGruen,
                form = RoundedCornerShape(4.dp),
                modifier = Modifier.fillMaxWidth(anteil).height(BoltMindDimensions.fortschrittHoehe)
            )
        }
    }
}

@Composable
private fun ArchivPlakette() {
    Box(
        Modifier
            .glas(
                GlasRezepte.badge,
                RoundedCornerShape(BoltMindDimensions.radiusS),
                BoltMindDimensions.radiusS
            )
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        BoltText(stringResource(R.string.archiv_nur_lesen), BoltTypo.badge, BoltTextSchwach)
    }
}

/**
 * Die Aktionskreise unten rechts. Welche erscheinen, entscheidet der Modus --
 * und in der Demontage zusaetzlich, ob der betrachtete Schritt der offene ist.
 */
@Composable
private fun Bedienkreise(
    uiState: BrowserUiState,
    onNaechstesTeil: () -> Unit,
    onWeiteresFoto: () -> Unit,
    onWiederholen: () -> Unit,
    onZumOffenenSchritt: () -> Unit,
    onEingebaut: () -> Unit,
    onHaekchenAnfragen: () -> Unit,
    onVorherigerSchritt: () -> Unit,
    onNaechsterSchritt: () -> Unit,
    onFeierabendAnfragen: () -> Unit
) {
    Box(Modifier.padding(end = 22.dp, bottom = 24.dp)) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(BoltMindDimensions.touchAbstandMin)
        ) {
            when {
                uiState.istDemontage -> {
                    if (uiState.hatFotosImSchritt) {
                        Rundbutton(
                            rezept = GlasRezepte.neutral09Klein,
                            durchmesser = BoltMindDimensions.rundbuttonKlein,
                            onKlick = onWiederholen
                        ) {
                            BoltText(
                                stringResource(R.string.zeichen_wiederholen),
                                BoltTypo.glyphe,
                                BoltTextRuhig
                            )
                        }
                    }
                    Rundbutton(
                        rezept = GlasRezepte.neutral08,
                        durchmesser = BoltMindDimensions.rundbuttonMittel,
                        onKlick = onWeiteresFoto
                    ) {
                        Flaeche(BoltTextHell, Modifier.size(15.dp), CircleShape)
                        BoltText(
                            stringResource(R.string.browser_weiteres_foto),
                            BoltTypo.rundbuttonLabelKlein,
                            BoltTextLeise
                        )
                    }
                    // Am offenen Schritt legt der grosse Kreis das naechste Teil an.
                    // Beim Nachschlagen eines abgeschlossenen Schritts fuehrt er
                    // stattdessen dorthin zurueck -- sonst verbrennt ein Fehltipp
                    // eine Schrittnummer, die schon auf einem Etikett klebt.
                    if (uiState.betrachtetOffenenSchritt) {
                        Rundbutton(
                            rezept = GlasRezepte.orangeVoll,
                            durchmesser = BoltMindDimensions.rundbuttonGross,
                            onKlick = onNaechstesTeil
                        ) {
                            BoltText(
                                "%02d".format(uiState.naechsteSchrittNummer),
                                BoltTypo.rundbuttonZahl,
                                BoltTextWeiss
                            )
                            BoltText(
                                stringResource(R.string.browser_naechstes),
                                BoltTypo.rundbuttonLabel,
                                BoltTextWeiss
                            )
                        }
                    } else {
                        val offene = uiState.offenerIndex
                            ?.let { uiState.schritte.getOrNull(it)?.schritt?.schrittNummer }
                        Rundbutton(
                            rezept = GlasRezepte.neutral08,
                            durchmesser = BoltMindDimensions.rundbuttonGross,
                            aktiv = offene != null,
                            onKlick = onZumOffenenSchritt
                        ) {
                            BoltText(
                                "%02d".format(offene ?: 0),
                                BoltTypo.rundbuttonZahl,
                                BoltTextHell
                            )
                            BoltText(
                                stringResource(R.string.browser_zurueck_zu_schritt),
                                BoltTypo.rundbuttonLabel,
                                BoltTextLeise
                            )
                        }
                    }
                }

                uiState.istMontage -> {
                    Rundbutton(
                        rezept = GlasRezepte.neutral09Klein,
                        durchmesser = BoltMindDimensions.rundbuttonKlein,
                        onKlick = onFeierabendAnfragen
                    ) {
                        BoltText(stringResource(R.string.montage_raus), BoltTypo.timerLabel, BoltTextSchwach)
                    }
                    Rundbutton(
                        rezept = GlasRezepte.neutral08,
                        durchmesser = BoltMindDimensions.rundbuttonMittel,
                        aktiv = !uiState.istErsterSchritt,
                        onKlick = onVorherigerSchritt
                    ) {
                        BoltText(stringResource(R.string.zeichen_hoch), BoltTypo.glyphe, BoltTextHell)
                        BoltText(
                            stringResource(R.string.browser_zurueck),
                            BoltTypo.rundbuttonLabelKlein,
                            BoltTextLeise
                        )
                    }
                    if (uiState.nichtEingebaut) {
                        Rundbutton(
                            rezept = GlasRezepte.gruenVoll,
                            durchmesser = BoltMindDimensions.rundbuttonGross,
                            onKlick = onEingebaut
                        ) {
                            BoltText(stringResource(R.string.zeichen_haken), BoltTypo.rundbuttonZahl, BoltTextWeiss)
                            BoltText(stringResource(R.string.montage_sitzt), BoltTypo.aktionSheet, BoltTextWeiss)
                        }
                    } else {
                        Rundbutton(
                            rezept = GlasRezepte.gruenZustand,
                            durchmesser = BoltMindDimensions.rundbuttonGross,
                            onKlick = onHaekchenAnfragen
                        ) {
                            BoltText(stringResource(R.string.zeichen_haken), BoltTypo.rundbuttonZahl, BoltGruen)
                            BoltText(stringResource(R.string.montage_drin), BoltTypo.rundbuttonLabel, BoltGruenDunkel)
                        }
                    }
                }

                else -> {
                    Rundbutton(
                        rezept = GlasRezepte.neutral08Flach,
                        durchmesser = BoltMindDimensions.rundbuttonArchivZurueck,
                        aktiv = !uiState.istErsterSchritt,
                        onKlick = onVorherigerSchritt
                    ) {
                        BoltText(stringResource(R.string.zeichen_zurueck), BoltTypo.abschlussKennzahlKlein, BoltTextHell)
                    }
                    Rundbutton(
                        rezept = GlasRezepte.neutral08Flach,
                        durchmesser = BoltMindDimensions.rundbuttonArchivWeiter,
                        aktiv = !uiState.istLetzterSchritt,
                        onKlick = onNaechsterSchritt
                    ) {
                        BoltText(stringResource(R.string.zeichen_weiter), BoltTypo.abschlussKennzahl, BoltTextHell)
                    }
                }
            }
        }
    }
}
