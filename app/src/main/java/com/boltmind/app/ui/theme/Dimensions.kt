package com.boltmind.app.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Masse aus dem Design-Prototyp.
 *
 * Der Entwurf positioniert in einem festen Rahmen von 372 x 806 px. Die Werte
 * hier sind die Groessen und Abstaende daraus; die Positionierung selbst
 * uebersetzen die Screens in ein mitwachsendes Layout, damit das Bild auf
 * anderen Geraeten nicht bricht.
 *
 * Wo der Entwurf unter das Governance-Mindestmass von 56dp faellt, steht der
 * Originalwert als Kommentar daneben. Bei Kacheln, deren Groesse Bedeutung
 * traegt (aktives vs. inaktives Thumbnail), bleibt die optische Groesse
 * erhalten und nur die Trefferflaeche waechst -- siehe [thumbTrefferflaeche].
 */
object BoltMindDimensions {

    // --- Governance: Mindestmasse ------------------------------------------

    /** Verbindliches Mindestmass fuer jede Trefferflaeche. */
    val touchTargetMin = 56.dp

    /** Verbindlicher Mindestabstand zwischen benachbarten Trefferflaechen. */
    val touchAbstandMin = 8.dp

    // --- Abstaende ----------------------------------------------------------

    val abstandXxs = 2.dp
    val abstandXs = 4.dp
    val abstandS = 8.dp
    val abstandM = 12.dp
    val abstandL = 16.dp
    val abstandXl = 22.dp
    val abstandXxl = 32.dp

    /** Seitenrand der Screens. */
    val screenRand = 16.dp

    /** Seitenrand der Kopfzeilen. */
    val kopfRand = 18.dp

    // --- Radien -------------------------------------------------------------

    val radiusXs = 6.dp
    val radiusS = 9.dp
    val radiusM = 12.dp

    /** Der haeufigste Radius im Entwurf: Tabs, Chips, kleine Glasflaechen. */
    val radiusStandard = 14.dp

    val radiusL = 16.dp
    val radiusXl = 18.dp
    val radiusVorgangKarte = 20.dp
    val radiusAktionGross = 20.dp
    val radiusFab = 22.dp
    val radiusThumbAktiv = 17.dp
    val radiusSheet = 28.dp

    // --- Rahmen -------------------------------------------------------------

    val rahmenDuenn = 1.dp
    val rahmenStark = 1.5.dp
    val rahmenSehrStark = 2.dp
    val rahmenAbschluss = 3.dp

    // --- Glas ---------------------------------------------------------------

    /** Der durchgehende Unschaerferadius. Im Entwurf 53-mal identisch. */
    val glasBlur = 14.dp

    /** Scrim hinter dem Sheet. */
    val glasBlurScrim = 3.dp

    /** Sheet-Flaeche. */
    val glasBlurSheet = 26.dp

    /** Sheet-Aktionen. */
    val glasBlurSheetAktion = 20.dp

    // --- Uebersicht ---------------------------------------------------------

    /** Tabs "OFFEN" / "ARCHIV". Entwurf 52dp, auf das Mindestmass gehoben. */
    val tabHoehe = 56.dp

    /** Vorschaubild auf der Vorgangskarte. */
    val vorgangFoto = 96.dp

    /** FAB "NEUER AUFTRAG". */
    val fabHoehe = 74.dp

    /** Leerzustand-Symbol. */
    val leerSymbol = 64.dp

    // --- Anlage -------------------------------------------------------------

    /** Zurueck-Chip. Entwurf 46dp, auf das Mindestmass gehoben. */
    val zurueckChip = 56.dp

    /** Vorschau des Fahrzeugfotos. */
    val anlageFotoHoehe = 196.dp

    /** "NEU KNIPSEN". Entwurf 44dp, auf das Mindestmass gehoben. */
    val bildWiederholenHoehe = 56.dp

    /** Einzeiliges Eingabefeld. */
    val eingabeHoehe = 66.dp

    /** Mehrzeiliges Eingabefeld. */
    val textbereichHoehe = 96.dp

    /** "LOS GEHT'S". */
    val aktionGrossHoehe = 78.dp

    /** "AB INS ARCHIV". */
    val archivierenHoehe = 80.dp

    // --- Browser ------------------------------------------------------------

    /** "FEIERABEND". Entwurf 50dp, auf das Mindestmass gehoben. */
    val feierabendHoehe = 56.dp

    /** Schliesser im Lesemodus. Entwurf 50dp, auf das Mindestmass gehoben. */
    val schliesserGroesse = 56.dp

    /** Timer-Schalter. Entwurf 54dp, auf das Mindestmass gehoben. */
    val timerSchalter = 56.dp

    /** Haupt-Rundbutton: "NAECHSTES", "SITZT!", "DRIN". */
    val rundbuttonGross = 124.dp

    /** Zweiter Rundbutton: "NOCH'N FOTO", "ZURUECK". */
    val rundbuttonMittel = 86.dp

    /** Dritter Rundbutton: Wiederholen, "RAUS". */
    val rundbuttonKlein = 60.dp

    /** Archiv-Weiter. */
    val rundbuttonArchivWeiter = 100.dp

    /** Archiv-Zurueck. */
    val rundbuttonArchivZurueck = 82.dp

    /** Dekorative Kugel hinter den Rundbuttons. */
    val kugelDurchmesser = 296.dp

    /** Aktives Thumbnail -- optisch groesser als die inaktiven, das traegt Bedeutung. */
    val thumbAktiv = 66.dp

    /** Inaktives Thumbnail. Optische Groesse aus dem Entwurf. */
    val thumbInaktiv = 54.dp

    /**
     * Trefferflaeche eines Thumbnails. Die optische Groesse bleibt bei
     * [thumbInaktiv], damit die Hervorhebung des aktiven Schritts erhalten
     * bleibt; antippbar ist trotzdem das volle Mindestmass.
     */
    val thumbTrefferflaeche = 56.dp

    /** Abstand in der Thumbnail-Leiste. Entwurf 7dp, auf das Mindestmass gehoben. */
    val thumbAbstand = 8.dp

    /** Sichtbare Thumbnails im Fenster. */
    const val THUMB_FENSTER = 4

    /** Label-Chip. Entwurf 46dp, auf das Mindestmass gehoben. */
    val labelChipHoehe = 56.dp

    /** Label-Chip im Lesemodus -- nicht bedienbar, deshalb ohne Mindestmass. */
    val labelChipLesendHoehe = 34.dp

    /** Kaestchen im Label-Chip. */
    val labelKaestchen = 18.dp

    /** Punkt im Label-Chip des Lesemodus. */
    val labelPunkt = 8.dp

    /** Farbstreifen unten am Thumbnail. */
    val thumbStreifenAktiv = 5.dp
    val thumbStreifenInaktiv = 4.dp

    /** Erledigt-Haken am Thumbnail. */
    val thumbHaken = 22.dp

    /** Punkt-Indikator des Karussells. */
    val punktBreiteAktiv = 26.dp
    val punktBreite = 14.dp
    val punktHoehe = 5.dp

    /** Fortschrittsbalken der Montage. */
    val fortschrittHoehe = 8.dp

    /** Vollbild-Schliesser. */
    val vollbildSchliesser = 56.dp

    // --- Sheet --------------------------------------------------------------

    /** Aktionszeile im Sheet. */
    val sheetAktionHoehe = 70.dp

    /** Abstand zwischen Sheet-Aktionen. Entwurf 9dp. */
    val sheetAktionAbstand = 9.dp

    /** Greifer oben im Sheet. */
    val sheetGreiferBreite = 46.dp
    val sheetGreiferHoehe = 5.dp

    /** Vorschaubild im Sheet. */
    val sheetFoto = 68.dp

    // --- Abschluss ----------------------------------------------------------

    val abschlussSiegel = 120.dp

    // --- Splash -------------------------------------------------------------

    val splashBalkenBreite = 150.dp
    val splashBalkenHoehe = 3.dp

    /** Eckenradius des Ladebalkens. Entwurf `border-radius:2px`, L54. */
    val splashBalkenRadius = 2.dp

    /** Abstand des Textblocks zur Unterkante. Entwurf `bottom:104px`, L51. */
    val splashTextblockAbstandUnten = 104.dp

    /** Abstand zwischen Wortmarke, Claim und Balken. Entwurf `gap:14px`, L51. */
    val splashElementAbstand = 14.dp

    /** Zusaetzlicher Abstand ueber dem Balken. Entwurf `margin-top:18px`, L54. */
    val splashBalkenAbstand = 18.dp

    /** Strecke, die die Wortmarke beim Einblenden hochfaehrt. Entwurf `bmIn`
     *  mit `translateY(10px)`, L21/L52. */
    val splashEinblendVersatz = 10.dp

    // --- Logo ---------------------------------------------------------------

    val logoQuadrat = 30.dp
    val logoQuadratKlein = 26.dp
    val logoRadius = 8.dp
}
