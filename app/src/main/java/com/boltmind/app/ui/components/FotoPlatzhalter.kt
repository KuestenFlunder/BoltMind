package com.boltmind.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.boltmind.app.R
import com.boltmind.app.ui.theme.BoltHintergrund
import com.boltmind.app.ui.theme.BoltMindTheme
import com.boltmind.app.ui.theme.BoltTextSchwaecher
import com.boltmind.app.ui.theme.BoltTypo

/**
 * Steht fuer ein Foto, dessen **Datei fehlt** -- etwa nach einem Backup, das die
 * Datenbank mitgenommen hat, aber nicht den Bilderordner.
 *
 * `governance.md`, Abschnitt "Fehlende Dateien", verlangt dafuer ein
 * Platzhalter-Bild: "Kein Crash, kein leerer Screen." Welches, ist in
 * `docs/specs/F-004-montage/montage.md` festgelegt -- das App-Icon.
 *
 * **Nicht zu verwechseln mit dem Leer-Zustand.** Ein Schritt ohne Fotos
 * (US-006.9) ist waehrend der Arbeit voellig normal und sieht deshalb anders
 * aus: gestrichelter Rahmen mit der Schrittnummer. Hier dagegen ist etwas
 * kaputt, und das soll man sehen. Die beiden Faelle haengen auch an
 * verschiedenen Schichten -- der Leer-Zustand an einer Zustandsentscheidung,
 * dieser hier am Ladezustand von Coil. Es gibt keinen Codepfad, auf dem sie sich
 * vermischen koennen.
 *
 * Eine **leere** Datei (0 Byte) zaehlt fuer Coil ebenfalls als Fehler und landet
 * hier. Das ist richtig so: eine 0-Byte-Huelle ist ein kaputtes Foto, kein
 * fehlendes.
 */
@Composable
fun FotoPlatzhalter(
    modifier: Modifier = Modifier,
    symbolGroesse: Dp = 56.dp,
    mitBeschriftung: Boolean = true
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BoltHintergrund),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
    ) {
        Image(
            // Das App-Icon aus mipmap, nicht das 512px-Logo aus drawable-nodpi:
            // nodpi wird ohne Dichteskalierung dekodiert und kostet fuer eine
            // Thumbnail-Kachel rund ein Megabyte.
            painter = painterResource(R.mipmap.ic_launcher),
            contentDescription = null,
            modifier = Modifier
                .size(symbolGroesse)
                .alpha(0.45f)
        )
        if (mitBeschriftung) {
            BoltText(
                text = stringResource(R.string.browser_foto_fehlt),
                stil = BoltTypo.vorgangTitel,
                farbe = BoltTextSchwaecher
            )
        }
    }
}

@Preview(name = "Fehlende Datei -- gross")
@Composable
private fun VorschauGross() {
    BoltMindTheme { FotoPlatzhalter(Modifier.size(280.dp)) }
}

@Preview(name = "Fehlende Datei -- Thumbnail")
@Composable
private fun VorschauKlein() {
    BoltMindTheme {
        FotoPlatzhalter(Modifier.size(54.dp), symbolGroesse = 26.dp, mitBeschriftung = false)
    }
}
