package com.boltmind.app.feature.uebersicht

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.boltmind.app.R
import com.boltmind.app.ui.components.BoltMeshHintergrund
import com.boltmind.app.ui.components.BoltSheet
import com.boltmind.app.ui.components.BoltSheetStil
import com.boltmind.app.ui.components.BoltText
import com.boltmind.app.ui.components.SheetAktion
import com.boltmind.app.ui.components.boltKlick
import com.boltmind.app.ui.theme.BoltAufOrange
import com.boltmind.app.ui.theme.BoltChipFlaeche
import com.boltmind.app.ui.theme.BoltHintergrund
import com.boltmind.app.ui.theme.BoltMindDimensions
import com.boltmind.app.ui.theme.BoltOrangeGedaempft
import com.boltmind.app.ui.theme.BoltOrangeHell
import com.boltmind.app.ui.theme.BoltOrangeVerlaufOben
import com.boltmind.app.ui.theme.BoltOrangeVerlaufUnten
import com.boltmind.app.ui.theme.BoltRahmenGestrichelt
import com.boltmind.app.ui.theme.BoltSchwarz00
import com.boltmind.app.ui.theme.BoltTextPrimaer
import com.boltmind.app.ui.theme.BoltTextSchwaecher
import com.boltmind.app.ui.theme.BoltTextSchwaechst
import com.boltmind.app.ui.theme.BoltTextSekundaer
import com.boltmind.app.ui.theme.BoltTextTertiaer
import com.boltmind.app.ui.theme.BoltTextWeiss
import com.boltmind.app.ui.theme.BoltTypo
import com.boltmind.app.ui.theme.GlasBuehne
import com.boltmind.app.ui.theme.GlasRezepte
import com.boltmind.app.ui.theme.glas
import java.io.File

/**
 * Die Uebersicht (F-001): zwei Tabs, die Vorgangsliste, der FAB.
 *
 * Zustandslos -- der Screen bekommt [uiState] und meldet ueber die Rueckrufe.
 */
@Composable
fun UebersichtScreen(
    uiState: UebersichtUiState,
    modifier: Modifier = Modifier,
    onTabGewaehlt: (UebersichtTab) -> Unit,
    onVorgangGeoeffnet: (VorgangKarte) -> Unit,
    onLoeschenAngefragt: (VorgangKarte) -> Unit,
    onWeiterDemontieren: (Long) -> Unit,
    onMontageStarten: (Long) -> Unit,
    onLoeschenBestaetigt: (Long) -> Unit,
    onSheetGeschlossen: () -> Unit,
    onNeuerVorgang: () -> Unit
) {
    Box(modifier.fillMaxSize()) {
        GlasBuehne(
            hintergrund = { BoltMeshHintergrund() },
            inhalt = {
                Column(Modifier.fillMaxSize().safeDrawingPadding()) {
                    Kopfzeile()
                    Tableiste(uiState, onTabGewaehlt)
                    Box(Modifier.weight(1f)) {
                        if (uiState.istLeer) {
                            Leerzustand(uiState.tab)
                        } else {
                            Vorgangsliste(uiState, onVorgangGeoeffnet, onLoeschenAngefragt)
                        }
                        // Verlauf, damit Karten nicht gegen den FAB stossen.
                        Box(
                            Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .height(120.dp)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(BoltSchwarz00, BoltHintergrund.copy(alpha = 0.55f))
                                    )
                                )
                        )
                    }
                }

                Box(Modifier.fillMaxSize().safeDrawingPadding()) {
                    BoltText(
                        text = stringResource(R.string.uebersicht_motivation),
                        stil = BoltTypo.motivation,
                        farbe = BoltTextWeiss,
                        maxZeilen = 3,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 18.dp, bottom = 26.dp)
                    )
                    if (uiState.tab == UebersichtTab.OFFEN) {
                        NeuerAuftragKnopf(
                            onKlick = onNeuerVorgang,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = 18.dp, bottom = 26.dp)
                        )
                    }
                }
            }
        )

        uiState.sheet?.let { sheet ->
            when (sheet) {
                is UebersichtSheet.Auswahl -> BoltSheet(
                    titel = "#${sheet.karte.auftragsnummer}",
                    text = sheet.karte.beschreibung
                        ?: stringResource(R.string.uebersicht_ohne_beschreibung),
                    fotoPfad = sheet.karte.fahrzeugFotoPfad,
                    onAussenGetippt = onSheetGeschlossen
                ) {
                    SheetAktion(
                        text = stringResource(R.string.uebersicht_weiter_demontieren),
                        stil = BoltSheetStil.PRIMAER,
                        onKlick = { onWeiterDemontieren(sheet.karte.id) }
                    )
                    SheetAktion(
                        text = stringResource(R.string.uebersicht_montage_starten),
                        stil = BoltSheetStil.NORMAL,
                        onKlick = { onMontageStarten(sheet.karte.id) }
                    )
                }

                is UebersichtSheet.Loeschen -> BoltSheet(
                    titel = stringResource(R.string.uebersicht_loeschen_titel),
                    text = stringResource(R.string.uebersicht_loeschen_frage),
                    fotoPfad = sheet.karte.fahrzeugFotoPfad,
                    onAussenGetippt = onSheetGeschlossen
                ) {
                    SheetAktion(
                        text = stringResource(R.string.uebersicht_loeschen_abbrechen),
                        stil = BoltSheetStil.NORMAL,
                        onKlick = onSheetGeschlossen
                    )
                    SheetAktion(
                        text = stringResource(R.string.uebersicht_loeschen),
                        stil = BoltSheetStil.GEFAHR,
                        onKlick = { onLoeschenBestaetigt(sheet.karte.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun Kopfzeile() {
    Row(
        Modifier.padding(start = 18.dp, end = 18.dp, top = 10.dp, bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        Box(
            Modifier
                .size(BoltMindDimensions.logoQuadrat)
                .background(
                    Brush.verticalGradient(
                        listOf(BoltOrangeVerlaufOben, BoltOrangeVerlaufUnten)
                    ),
                    RoundedCornerShape(BoltMindDimensions.logoRadius)
                ),
            contentAlignment = Alignment.Center
        ) {
            BoltText(stringResource(R.string.uebersicht_logo), BoltTypo.thumbNummerAktiv, BoltAufOrange)
        }
        BoltText(stringResource(R.string.uebersicht_wortmarke), BoltTypo.wortmarke, BoltTextPrimaer)
    }
}

@Composable
private fun Tableiste(uiState: UebersichtUiState, onTabGewaehlt: (UebersichtTab) -> Unit) {
    Row(
        Modifier.padding(horizontal = BoltMindDimensions.screenRand).padding(bottom = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(BoltMindDimensions.touchAbstandMin)
    ) {
        Tab(
            beschriftung = stringResource(R.string.uebersicht_tab_offen),
            anzahl = uiState.anzahlOffen,
            aktiv = uiState.tab == UebersichtTab.OFFEN,
            modifier = Modifier.weight(1f),
            onKlick = { onTabGewaehlt(UebersichtTab.OFFEN) }
        )
        Tab(
            beschriftung = stringResource(R.string.uebersicht_tab_archiv),
            anzahl = uiState.anzahlArchiv,
            aktiv = uiState.tab == UebersichtTab.ARCHIV,
            modifier = Modifier.weight(1f),
            onKlick = { onTabGewaehlt(UebersichtTab.ARCHIV) }
        )
    }
}

@Composable
private fun Tab(
    beschriftung: String,
    anzahl: Int,
    aktiv: Boolean,
    modifier: Modifier = Modifier,
    onKlick: () -> Unit
) {
    Row(
        modifier
            .height(BoltMindDimensions.tabHoehe)
            .boltKlick(onKlick = onKlick)
            .glas(
                if (aktiv) GlasRezepte.orangeFlach else GlasRezepte.neutral05,
                RoundedCornerShape(BoltMindDimensions.radiusStandard),
                BoltMindDimensions.radiusStandard
            ),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BoltText(
            text = beschriftung,
            stil = BoltTypo.tab,
            farbe = if (aktiv) BoltOrangeHell else BoltTextSchwaecher
        )
        BoltText(
            text = anzahl.toString(),
            stil = BoltTypo.tabZaehler,
            farbe = if (aktiv) BoltOrangeGedaempft else BoltTextSchwaechst
        )
    }
}

@Composable
private fun Vorgangsliste(
    uiState: UebersichtUiState,
    onGeoeffnet: (VorgangKarte) -> Unit,
    onLoeschenAngefragt: (VorgangKarte) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = BoltMindDimensions.screenRand,
            end = BoltMindDimensions.screenRand,
            bottom = 130.dp
        ),
        verticalArrangement = Arrangement.spacedBy(BoltMindDimensions.abstandM)
    ) {
        items(uiState.sichtbareListe, key = { it.id }) { karte ->
            VorgangKarteZeile(karte, { onGeoeffnet(karte) }, { onLoeschenAngefragt(karte) })
        }
    }
}

@Composable
private fun VorgangKarteZeile(
    karte: VorgangKarte,
    onKlick: () -> Unit,
    onLangGedrueckt: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .boltKlick(stauchung = 0.99f, onKlick = onKlick)
            .glas(
                GlasRezepte.karte,
                RoundedCornerShape(BoltMindDimensions.radiusVorgangKarte),
                BoltMindDimensions.radiusVorgangKarte
            )
            .padding(BoltMindDimensions.abstandM),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(BoltMindDimensions.vorgangFoto)
                .background(BoltHintergrund, RoundedCornerShape(BoltMindDimensions.radiusStandard))
        ) {
            karte.fahrzeugFotoPfad?.let { pfad ->
                AsyncImage(
                    model = File(pfad),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            BoltText("#${karte.auftragsnummer}", BoltTypo.vorgangNummer, BoltTextPrimaer)
            BoltText(
                text = karte.beschreibung ?: stringResource(R.string.uebersicht_ohne_beschreibung),
                stil = BoltTypo.vorgangTitel,
                farbe = BoltTextSekundaer,
                maxZeilen = 2
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .background(BoltChipFlaeche, RoundedCornerShape(7.dp))
                        .padding(horizontal = 9.dp, vertical = 4.dp)
                ) {
                    BoltText(
                        pluralStringResource(R.plurals.uebersicht_teile, karte.schrittAnzahl, karte.schrittAnzahl),
                        BoltTypo.teileChip,
                        BoltTextTertiaer
                    )
                }
                BoltText(karte.datumText, BoltTypo.vorgangDatum, BoltTextSchwaecher)
            }
        }
        Box(
            Modifier
                .size(BoltMindDimensions.touchTargetMin)
                .boltKlick(onKlick = onLangGedrueckt),
            contentAlignment = Alignment.Center
        ) {
            BoltText(stringResource(R.string.uebersicht_chevron), BoltTypo.glyphe, BoltTextSchwaechst)
        }
    }
}

@Composable
private fun Leerzustand(tab: UebersichtTab) {
    Column(
        Modifier.fillMaxSize().padding(horizontal = 30.dp, vertical = 120.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(BoltMindDimensions.abstandM)
    ) {
        Box(
            Modifier
                .size(BoltMindDimensions.leerSymbol)
                .border(2.dp, BoltRahmenGestrichelt, RoundedCornerShape(19.dp)),
            contentAlignment = Alignment.Center
        ) {
            BoltText("0", BoltTypo.abschlussKennzahl, BoltTextSchwaechst)
        }
        BoltText(
            text = stringResource(
                if (tab == UebersichtTab.OFFEN) R.string.uebersicht_leer_titel_offen
                else R.string.uebersicht_leer_titel_archiv
            ),
            stil = BoltTypo.leerTitel,
            farbe = BoltTextSekundaer
        )
        BoltText(
            text = stringResource(
                if (tab == UebersichtTab.OFFEN) R.string.uebersicht_leer_text_offen
                else R.string.uebersicht_leer_text_archiv
            ),
            stil = BoltTypo.leerText,
            farbe = BoltTextSchwaecher,
            maxZeilen = 2,
            ausrichtung = TextAlign.Center
        )
    }
}

@Composable
private fun NeuerAuftragKnopf(onKlick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier
            .height(BoltMindDimensions.fabHoehe)
            .boltKlick(onKlick = onKlick)
            .glas(
                GlasRezepte.orangeFlach,
                RoundedCornerShape(BoltMindDimensions.radiusFab),
                BoltMindDimensions.radiusFab
            )
            .padding(horizontal = 26.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(BoltMindDimensions.abstandM)
    ) {
        BoltText(stringResource(R.string.uebersicht_fab_plus), BoltTypo.rundbuttonZahl, BoltTextWeiss)
        BoltText(stringResource(R.string.uebersicht_fab), BoltTypo.aktionFab, BoltTextWeiss)
    }
}
