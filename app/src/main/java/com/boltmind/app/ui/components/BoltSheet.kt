package com.boltmind.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.boltmind.app.ui.theme.BoltGefahrRand
import com.boltmind.app.ui.theme.BoltMindDimensions
import com.boltmind.app.ui.theme.BoltScrim
import com.boltmind.app.ui.theme.BoltTextPrimaer
import com.boltmind.app.ui.theme.BoltTextHell
import com.boltmind.app.ui.theme.BoltTextMini
import com.boltmind.app.ui.theme.BoltTextWeiss
import com.boltmind.app.ui.theme.BoltTypo
import com.boltmind.app.ui.theme.BoltWeiss28
import com.boltmind.app.ui.theme.GlasRezept
import com.boltmind.app.ui.theme.GlasRezepte
import com.boltmind.app.ui.theme.glas
import java.io.File

/** Wie eine Sheet-Aktion aussieht. Die Wirkung kennt der Aufrufer. */
enum class BoltSheetStil { PRIMAER, NORMAL, GEFAHR }

private fun BoltSheetStil.rezept(): GlasRezept = when (this) {
    BoltSheetStil.PRIMAER -> GlasRezepte.sheetAktionPrimaer
    BoltSheetStil.NORMAL -> GlasRezepte.sheetAktion
    BoltSheetStil.GEFAHR -> GlasRezepte.sheetAktionGefahr
}

private fun BoltSheetStil.textfarbe(): Color = when (this) {
    BoltSheetStil.PRIMAER -> BoltTextWeiss
    BoltSheetStil.NORMAL -> BoltTextHell
    BoltSheetStil.GEFAHR -> BoltGefahrRand
}

/**
 * Das Bottom-Sheet der App.
 *
 * Alle Entscheidungen laufen darueber: die Auswahl beim Oeffnen eines Vorgangs,
 * die Feierabend-Rueckfrage, das Zuruecknehmen eines Haekchens. Aktionen sind
 * 70dp hoch und stehen untereinander -- nebeneinander waeren sie mit Handschuhen
 * zu leicht zu verwechseln.
 */
@Composable
fun BoltSheet(
    titel: String,
    text: String,
    modifier: Modifier = Modifier,
    fotoPfad: String? = null,
    onAussenGetippt: () -> Unit,
    aktionen: @Composable ColumnScopeMarker.() -> Unit
) {
    Box(
        modifier
            .fillMaxSize()
            .background(BoltScrim)
            .boltKlick(stauchung = 1f, onKlick = onAussenGetippt),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .glas(
                    GlasRezepte.sheet,
                    RoundedCornerShape(
                        topStart = BoltMindDimensions.radiusSheet,
                        topEnd = BoltMindDimensions.radiusSheet,
                        bottomStart = 0.dp,
                        bottomEnd = 0.dp
                    ),
                    BoltMindDimensions.radiusSheet
                )
                .navigationBarsPadding()
                .padding(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(BoltMindDimensions.sheetGreiferBreite)
                    .height(BoltMindDimensions.sheetGreiferHoehe)
                    .background(BoltWeiss28, RoundedCornerShape(3.dp))
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (fotoPfad != null) {
                    AsyncImage(
                        model = File(fotoPfad),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(BoltMindDimensions.sheetFoto)
                            .background(Color.Black, RoundedCornerShape(BoltMindDimensions.radiusXl))
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    BoltText(titel, BoltTypo.dialogTitel, BoltTextPrimaer, maxZeilen = 2)
                    BoltText(text, BoltTypo.dialogText, BoltTextMini, maxZeilen = 3)
                }
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(BoltMindDimensions.sheetAktionAbstand)
            ) {
                ColumnScopeMarker.aktionen()
            }
        }
    }
}

/** Marker, damit Aktionen nur innerhalb eines [BoltSheet] gebaut werden. */
object ColumnScopeMarker

@Composable
fun ColumnScopeMarker.SheetAktion(
    text: String,
    stil: BoltSheetStil,
    onKlick: () -> Unit
) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(BoltMindDimensions.sheetAktionHoehe)
            .boltKlick(stauchung = 0.985f, onKlick = onKlick)
            .glas(
                stil.rezept(),
                RoundedCornerShape(BoltMindDimensions.radiusXl),
                BoltMindDimensions.radiusXl
            ),
        contentAlignment = Alignment.Center
    ) {
        BoltText(text, BoltTypo.aktionSheet, stil.textfarbe())
    }
}
