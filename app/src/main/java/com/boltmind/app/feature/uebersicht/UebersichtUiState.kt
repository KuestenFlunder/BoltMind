package com.boltmind.app.feature.uebersicht

import androidx.compose.runtime.Immutable
import com.boltmind.app.ui.navigation.BrowserModus

/** Die beiden Tabs der Uebersicht. Prototyp Z. 72-81. */
enum class UebersichtTab { OFFEN, ARCHIV }

/**
 * Eine Zeile der Vorgangsliste -- schon fertig formatiert.
 *
 * [datumText] kommt aus [DatumFormat] und traegt im Archiv bereits die Dauer;
 * der Screen formatiert nichts nach. [beschreibung] ist `null`, wenn der Vorgang
 * keine hat -- der Ersatztext ist UI und liegt in `strings_uebersicht.xml`.
 */
@Immutable
data class VorgangKarte(
    val id: Long,
    val auftragsnummer: String,
    val beschreibung: String?,
    val fahrzeugFotoPfad: String?,
    val schrittAnzahl: Int,
    val datumText: String
)

/** Das gerade offene Bottom-Sheet. Beide nutzen dieselbe Optik (Prototyp Z. 402-425). */
@Immutable
sealed interface UebersichtSheet {

    val karte: VorgangKarte

    /** Auswahl bei einem offenen Vorgang mit mindestens einem Schritt (US-001.2). */
    data class Auswahl(override val karte: VorgangKarte) : UebersichtSheet

    /** Rueckfrage nach dem Wischen (US-001.4). */
    data class Loeschen(override val karte: VorgangKarte) : UebersichtSheet
}

/** Einmaliger Navigationswunsch. Der Screen quittiert ihn mit `onZielVerbraucht`. */
@Immutable
sealed interface UebersichtZiel {

    /** FAB "+ NEUER AUFTRAG" (US-001.3). */
    data object NeuerVorgang : UebersichtZiel

    /** Schritt-Browser (F-006) im jeweiligen Modus. */
    data class Browser(val vorgangId: Long, val modus: BrowserModus) : UebersichtZiel
}

@Immutable
data class UebersichtUiState(
    val tab: UebersichtTab = UebersichtTab.OFFEN,
    val offene: List<VorgangKarte> = emptyList(),
    val archivierte: List<VorgangKarte> = emptyList(),
    val sheet: UebersichtSheet? = null,
    val ziel: UebersichtZiel? = null
) {
    val sichtbareListe: List<VorgangKarte>
        get() = if (tab == UebersichtTab.OFFEN) offene else archivierte

    val istLeer: Boolean get() = sichtbareListe.isEmpty()

    val anzahlOffen: Int get() = offene.size

    val anzahlArchiv: Int get() = archivierte.size
}
