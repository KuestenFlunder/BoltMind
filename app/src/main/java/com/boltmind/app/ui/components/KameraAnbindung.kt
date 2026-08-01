package com.boltmind.app.ui.components

import android.content.ActivityNotFoundException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File

/**
 * Haengt die System-Kamera an einen Kamera-Auftrag aus einem ViewModel.
 *
 * `TakePicture` schreibt ueber den `FileProvider` direkt in die vom ViewModel
 * angelegte Zieldatei. Keine CAMERA-Permission, keine app-eigene Bestaetigung --
 * so verlangt es [docs/specs/governance.md], Abschnitt "Kamera".
 *
 * Der Baustein ist bewusst hier und nicht in einem Feature-Paket: F-002 und F-003
 * brauchen dieselbe Anbindung, und der Waechter unten ist zu leicht zu vergessen,
 * um ihn ein zweites Mal abzuschreiben. Er nimmt Nummer und Zielpfad einzeln
 * entgegen statt eines gemeinsamen Auftragstyps -- die beiden Features tragen in
 * ihren Auftraegen unterschiedliche Zusatzfelder, die hier niemanden angehen.
 *
 * @param auftragsNummer laufende Nummer des offenen Auftrags, `null` wenn keiner aussteht
 * @param zielPfad Datei, in die die Kamera schreiben soll
 */
@Composable
fun KameraAnbindung(
    auftragsNummer: Int?,
    zielPfad: String?,
    onFotoAufgenommen: (String) -> Unit,
    onAbgebrochen: () -> Unit,
    onKeineKameraApp: () -> Unit
) {
    val aktuellerPfad by rememberUpdatedState(zielPfad)
    val starter = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { erfolgreich ->
        val pfad = aktuellerPfad
        if (erfolgreich && pfad != null) onFotoAufgenommen(pfad) else onAbgebrochen()
    }

    val kontext = LocalContext.current
    KameraAnbindung(
        auftragsNummer = auftragsNummer,
        zielPfad = zielPfad,
        onKeineKameraApp = onKeineKameraApp
    ) { pfad ->
        val uri = FileProvider.getUriForFile(
            kontext,
            "${kontext.packageName}.fileprovider",
            File(pfad)
        )
        starter.launch(uri)
    }
}

/**
 * Der Waechter ohne echten Launcher -- sichtbar, damit ein Test ihn ohne
 * Kamera-Activity fahren kann.
 *
 * [starte] wird fuer jeden Auftrag **genau einmal** aufgerufen. Ohne diese Sperre
 * oeffnete ein Konfigurationswechsel waehrend der laufenden Aufnahme die Kamera
 * ein zweites Mal: der Auftrag steht ja noch offen im ViewModel, das den
 * Aktivitaetswechsel ueberlebt, waehrend die Komposition neu aufgebaut wird und
 * der [LaunchedEffect] erneut anlaeuft.
 *
 * Die Marke traegt den Zielpfad und nicht nur die laufende Nummer: der Zaehler im
 * ViewModel faengt nach einem Prozesstod wieder bei eins an, waehrend diese Marke
 * ueber [rememberSaveable] den Prozesstod ueberlebt. Ein reiner Zahlenvergleich
 * wuerde die erste Aufnahme danach stillschweigend verschlucken.
 */
@Composable
internal fun KameraAnbindung(
    auftragsNummer: Int?,
    zielPfad: String?,
    onKeineKameraApp: () -> Unit,
    starte: (String) -> Unit
) {
    var zuletztGestartet by rememberSaveable { mutableStateOf<String?>(null) }
    val marke = if (auftragsNummer != null && zielPfad != null) {
        "$auftragsNummer@$zielPfad"
    } else {
        null
    }

    LaunchedEffect(marke) {
        val offen = marke ?: return@LaunchedEffect
        if (offen == zuletztGestartet) return@LaunchedEffect
        zuletztGestartet = offen
        try {
            starte(checkNotNull(zielPfad))
        } catch (_: ActivityNotFoundException) {
            // Kein Kamera-Programm auf dem Geraet.
            onKeineKameraApp()
        } catch (_: SecurityException) {
            // Deklariert die App die CAMERA-Berechtigung, ohne sie zu halten,
            // verweigert das System ACTION_IMAGE_CAPTURE. Governance verbietet die
            // Berechtigung -- der Fang hier haelt trotzdem den Absturz ab.
            onKeineKameraApp()
        }
    }
}
