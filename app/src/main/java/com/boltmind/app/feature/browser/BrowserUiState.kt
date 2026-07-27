package com.boltmind.app.feature.browser

import androidx.compose.runtime.Immutable
import com.boltmind.app.data.model.Reparaturvorgang
import com.boltmind.app.data.model.SchrittMitFotos
import com.boltmind.app.ui.navigation.BrowserModus
import com.boltmind.app.ui.schrittbrowser.BrowserBetriebsart
import com.boltmind.app.ui.schrittbrowser.SchrittBrowserZustand

/** Ein Bottom-Sheet mit Titel, Text und bis zu drei Aktionen. */
@Immutable
data class SheetZustand(
    val titel: String,
    val text: String,
    val aktionen: List<SheetAktion>
)

@Immutable
data class SheetAktion(
    val text: String,
    val stil: SheetStil,
    val marke: SheetMarke
)

enum class SheetStil { PRIMAER, NORMAL, GEFAHR }

/** Welche Wirkung eine Sheet-Aktion hat. Das Sheet selbst kennt keine Logik. */
enum class SheetMarke {
    SCHLIESSEN,
    FEIERABEND_BESTAETIGEN,
    HAEKCHEN_ZURUECKNEHMEN
}

@Immutable
data class BrowserUiState(
    val laedt: Boolean = true,
    val vorgang: Reparaturvorgang? = null,
    val schritte: List<SchrittMitFotos> = emptyList(),
    val aktiverIndex: Int = 0,
    val aktivesFoto: Int = 0,
    val modus: BrowserModus = BrowserModus.DEMONTAGE,
    val vollbild: Boolean = false,
    val zeigeWischHinweis: Boolean = true,
    val sheet: SheetZustand? = null,

    /** Sekunden des betrachteten Schritts, laufend hochgezaehlt. */
    val schrittSekunden: Long = 0,
    val timerLaeuft: Boolean = false,
    /** Summe ueber alle Schritte des Vorgangs, in Sekunden. */
    val gesamtSekunden: Long = 0,

    val eingebauteAnzahl: Int = 0,
    val fertig: Boolean = false,
    val verlassen: Boolean = false,
    /** Meldung, wenn kein Kamera-Programm vorhanden ist. */
    val kameraFehlt: Boolean = false
) {
    val gesamtAnzahl: Int get() = schritte.size

    val aktiverSchritt: SchrittMitFotos? get() = schritte.getOrNull(aktiverIndex)

    val istDemontage: Boolean get() = modus == BrowserModus.DEMONTAGE
    val istMontage: Boolean get() = modus == BrowserModus.MONTAGE
    val istArchiv: Boolean get() = modus == BrowserModus.ARCHIV

    /**
     * Der offene Schritt der Demontage, also der zuletzt angelegte ohne
     * Abschlusszeitpunkt. Nur wenn der betrachtete Schritt dieser ist, duerfen
     * "Naechstes Teil" und "Feierabend" erscheinen -- sonst haengt ein Fehltipp
     * beim Nachschlagen eines alten Schritts eine Schrittnummer an, die
     * moeglicherweise schon auf einem Etikett klebt.
     */
    val offenerIndex: Int?
        get() = schritte.indexOfLast { it.schritt.abgeschlossenAm == null }
            .takeIf { it >= 0 }

    val betrachtetOffenenSchritt: Boolean
        get() = offenerIndex != null && offenerIndex == aktiverIndex

    /** Nummer, die der naechste Schritt bekaeme. Immer MAX + 1. */
    val naechsteSchrittNummer: Int
        get() = (schritte.maxOfOrNull { it.schritt.schrittNummer } ?: 0) + 1

    /** In der Montage: dieser Schritt ist noch nicht eingebaut. */
    val nichtEingebaut: Boolean
        get() = istMontage && aktiverSchritt?.schritt?.eingebautBeiMontage == false

    /** In der Montage: das Teil ist am Fahrzeug geblieben, es gibt kein Ablageort-Foto. */
    val amFahrzeugGeblieben: Boolean
        get() = istMontage && aktiverSchritt?.fotos?.none { it.istAblageort } == true

    /**
     * In der Montage: jedes Teil ist wieder drin, archiviert ist der Vorgang
     * aber noch nicht.
     *
     * In diesem Zustand gibt es keinen offenen Schritt mehr, dessen Abhaken den
     * Abschluss-Screen ausloesen koennte -- deshalb braucht es hier den eigenen
     * Weg dorthin (montage.md, US-004.5).
     *
     * Eine leere Schrittliste zaehlt ausdruecklich **nicht** dazu: `all {}` ist
     * auf ihr wahr, ein Abschluss ueber null Teile waere Unsinn.
     */
    val alleEingebaut: Boolean
        get() = istMontage && schritte.isNotEmpty() &&
            schritte.all { it.schritt.eingebautBeiMontage }


    val hatFotosImSchritt: Boolean get() = aktiverSchritt?.fotos?.isNotEmpty() == true

    val istErsterSchritt: Boolean get() = aktiverIndex <= 0

    val istLetzterSchritt: Boolean get() = aktiverIndex >= schritte.lastIndex

    val betriebsart: BrowserBetriebsart
        get() = when (modus) {
            BrowserModus.DEMONTAGE -> BrowserBetriebsart.BEARBEITBAR
            BrowserModus.MONTAGE -> BrowserBetriebsart.LESEND_MIT_AKTIONEN
            BrowserModus.ARCHIV -> BrowserBetriebsart.NUR_LESEN
        }

    fun alsBrowserZustand(): SchrittBrowserZustand = SchrittBrowserZustand(
        schritte = schritte,
        aktiverIndex = aktiverIndex,
        aktivesFoto = aktivesFoto,
        betriebsart = betriebsart,
        vollbild = vollbild,
        zeigeWischHinweis = zeigeWischHinweis,
        zeigeErledigt = istMontage
    )
}
