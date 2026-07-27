package com.boltmind.app.ui.schrittbrowser

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.boltmind.app.data.model.FotoKategorie
import com.boltmind.app.data.model.SchrittFoto
import com.boltmind.app.data.model.SchrittMitFotos
import com.boltmind.app.ui.theme.BoltBlau
import com.boltmind.app.ui.theme.BoltGruen
import com.boltmind.app.ui.theme.BoltKategorieOhne
import com.boltmind.app.ui.theme.BoltOrange

/**
 * Die Betriebsart des Schritt-Browsers.
 *
 * Der Browser besitzt die Schritt-Navigation vollstaendig -- Thumbnail-Sprung
 * und Vor/Zurueck. Was er darueber hinaus anbietet, entscheidet der Modus.
 *
 * Spec: docs/specs/F-006-schritt-browser/browser.md
 */
enum class BrowserBetriebsart {
    /** F-003 Demontage: Label aenderbar, Fotos aufnehmbar. */
    BEARBEITBAR,

    /** F-004 Montage: Label nur sichtbar, aber der Consumer haengt Aktionen ein. */
    LESEND_MIT_AKTIONEN,

    /** F-001 Archiv: nur blaettern. */
    NUR_LESEN;

    val labelAenderbar: Boolean get() = this == BEARBEITBAR
}

/** Eines der drei Foto-Label. */
enum class LabelArt { BAUTEIL, UEBERSICHT, ABLAGEORT }

/** Die Farbe, mit der eine Kategorie im Browser codiert wird. */
val FotoKategorie.farbe: Color
    get() = when (this) {
        FotoKategorie.ABLAGEORT -> BoltOrange
        FotoKategorie.UEBERSICHT -> BoltBlau
        FotoKategorie.BAUTEIL -> BoltGruen
        FotoKategorie.OHNE -> BoltKategorieOhne
    }

val LabelArt.farbe: Color
    get() = when (this) {
        LabelArt.BAUTEIL -> BoltGruen
        LabelArt.UEBERSICHT -> BoltBlau
        LabelArt.ABLAGEORT -> BoltOrange
    }

fun SchrittFoto.hat(art: LabelArt): Boolean = when (art) {
    LabelArt.BAUTEIL -> istBauteil
    LabelArt.UEBERSICHT -> istUebersicht
    LabelArt.ABLAGEORT -> istAblageort
}

/**
 * Der vollstaendige Zustand des Browsers. Der Browser haelt selbst nichts --
 * der Consumer liefert diesen Zustand und reagiert auf die Rueckrufe.
 *
 * [schritte] steht bereits in Anzeigereihenfolge: aufsteigend in der Demontage,
 * absteigend in der Montage. Der Browser sortiert nicht um.
 */
@Immutable
data class SchrittBrowserZustand(
    val schritte: List<SchrittMitFotos> = emptyList(),
    val aktiverIndex: Int = 0,
    val aktivesFoto: Int = 0,
    val betriebsart: BrowserBetriebsart = BrowserBetriebsart.NUR_LESEN,
    val vollbild: Boolean = false,
    /** Blendet den Wisch-Hinweis ein, bis der Mechaniker einmal gewischt hat. */
    val zeigeWischHinweis: Boolean = false,
    /** Markiert erledigte Schritte in der Leiste. Nur in der Montage sinnvoll. */
    val zeigeErledigt: Boolean = false
) {
    val aktiverSchritt: SchrittMitFotos? get() = schritte.getOrNull(aktiverIndex)

    val fotos: List<SchrittFoto> get() = aktiverSchritt?.fotos.orEmpty()

    /** Auf die tatsaechliche Fotozahl geklemmter Index -- die Liste kann schrumpfen. */
    val fotoIndex: Int get() = aktivesFoto.coerceIn(0, (fotos.size - 1).coerceAtLeast(0))

    val sichtbaresFoto: SchrittFoto? get() = fotos.getOrNull(fotoIndex)

    val hatFotos: Boolean get() = fotos.isNotEmpty()

    val istErster: Boolean get() = aktiverIndex <= 0

    val istLetzter: Boolean get() = aktiverIndex >= schritte.lastIndex
}

/**
 * Die Rueckrufe des Browsers. Bewusst ein eigenes Buendel statt einzelner
 * Parameter -- der Browser wird von drei Screens komponiert, und ein Buendel
 * haelt deren Signaturen stabil, wenn eine Aktion dazukommt.
 */
@Immutable
data class SchrittBrowserAktionen(
    val onSchrittGewaehlt: (index: Int) -> Unit = {},
    val onFotoGewaehlt: (index: Int) -> Unit = {},
    val onVollbildOeffnen: () -> Unit = {},
    val onVollbildSchliessen: () -> Unit = {},
    val onLabelUmgeschaltet: (foto: SchrittFoto, art: LabelArt) -> Unit = { _, _ -> }
)
