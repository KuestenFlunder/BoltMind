package com.boltmind.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp

// ============================================================================
// Der Rezeptkatalog aus dem Prototyp.
//
// Kuerzel wie in der Extraktion:
//   GN  Glas neutral (Weiss-Tint)      GO  Glas orange
//   GG  Glas gruen                     GD  Glas dunkel (Panels)
//   GL  Label-Chip                     GS  Sheet
//
// Jedes Rezept ist wortgleich aus dem Entwurf uebernommen.
// Referenz: docs/specs/design-system.md
// ============================================================================

private fun senkrecht(oben: androidx.compose.ui.graphics.Color, unten: androidx.compose.ui.graphics.Color) =
    Brush.verticalGradient(listOf(oben, unten))

object GlasRezepte {

    // --- GN: neutrale Weiss-Leiter -----------------------------------------

    /** Inaktiver Tab. */
    val neutral05 = GlasRezept.einfarbig(
        fuellung = BoltWeiss05,
        randFarbe = BoltWeiss12,
        randBreite = BoltMindDimensions.rahmenStark,
        innenGlanz = BoltWeiss07
    )

    /** Zurueck-Chip im Anlage-Screen. */
    val neutral07 = GlasRezept.einfarbig(
        fuellung = BoltWeiss07,
        randFarbe = BoltWeiss14,
        innenGlanz = BoltWeiss10
    )

    /** Zweiter Rundbutton: "NOCH'N FOTO", "ZURUECK". */
    val neutral08 = GlasRezept.einfarbig(
        fuellung = BoltWeiss08,
        randFarbe = BoltWeiss16,
        innenGlanz = BoltWeiss12
    )

    /** Archiv-Pfeile -- wie neutral08, aber ohne Lichtkante. */
    val neutral08Flach = GlasRezept.einfarbig(
        fuellung = BoltWeiss08,
        randFarbe = BoltWeiss16
    )

    /** Timer-Schalter im Zustand "STEHT". */
    val neutral08Timer = GlasRezept.einfarbig(
        fuellung = BoltWeiss08,
        randFarbe = BoltWeiss20,
        randBreite = BoltMindDimensions.rahmenStark,
        innenGlanz = BoltWeiss12
    )

    /** "FEIERABEND". */
    val neutral09 = GlasRezept.einfarbig(
        fuellung = BoltWeiss09,
        randFarbe = BoltWeiss20,
        innenGlanz = BoltWeiss12
    )

    /** Schliesser im Lesemodus. */
    val neutral09Flach = GlasRezept.einfarbig(
        fuellung = BoltWeiss09,
        randFarbe = BoltWeiss20
    )

    /** Wiederholen und "RAUS". */
    val neutral09Klein = GlasRezept.einfarbig(
        fuellung = BoltWeiss09,
        randFarbe = BoltWeiss16
    )

    /** Vollbild-Schliesser. */
    val neutral10 = GlasRezept.einfarbig(
        fuellung = BoltWeiss10,
        randFarbe = BoltWeiss22
    )

    /** "NEU KNIPSEN" auf dem Fahrzeugfoto. */
    val neutral12 = GlasRezept.einfarbig(
        fuellung = BoltWeiss12,
        randFarbe = BoltWeiss24,
        innenGlanz = BoltWeiss16
    )

    // --- GO: orange ---------------------------------------------------------

    /** Flaechige Primaeraktion: aktiver Tab, FAB, "LOS GEHT'S". */
    val orangeFlach = GlasRezept(
        fuellung = senkrecht(BoltGlasOrangeFlachOben, BoltGlasOrangeFlachUnten),
        randFarbe = BoltGlasOrangeFlachRand,
        randBreite = BoltMindDimensions.rahmenStark,
        innenGlanz = BoltWeiss22,
        leuchten = listOf(
            Leuchten(BoltLeuchtOrange35, 16.dp),
            Leuchten(BoltLeuchtOrange18, 40.dp)
        )
    )

    /** Haupt-Rundbutton "NAECHSTES". */
    val orangeVoll = GlasRezept(
        fuellung = senkrecht(BoltGlasOrangeVollOben, BoltGlasOrangeVollUnten),
        randFarbe = BoltGlasOrangeVollRand,
        innenGlanz = BoltWeiss45,
        leuchten = listOf(
            Leuchten(BoltGlasOrangeRing, 1.dp),
            Leuchten(BoltLeuchtOrange50, 20.dp),
            Leuchten(BoltLeuchtOrange28, 56.dp),
            Leuchten(BoltLeuchtOrange50, 30.dp, versatzY = 10.dp, ausbreitung = (-6).dp)
        )
    )

    /** "AB INS ARCHIV". */
    val orangeArchiv = GlasRezept(
        fuellung = senkrecht(BoltGlasOrangeArchivOben, BoltGlasOrangeArchivUnten),
        randFarbe = BoltGlasOrangeArchivRand,
        innenGlanz = BoltWeiss40,
        leuchten = listOf(
            Leuchten(BoltGlasOrangeRing, 1.dp),
            Leuchten(BoltLeuchtOrange50, 20.dp),
            Leuchten(BoltLeuchtOrange28, 56.dp),
            Leuchten(BoltLeuchtOrange45, 26.dp, versatzY = 10.dp, ausbreitung = (-8).dp)
        )
    )

    /** Timer-Schalter im Zustand "LAEUFT". */
    val orangeTimer = GlasRezept(
        fuellung = senkrecht(BoltGlasOrangeVollOben, BoltGlasOrangeVollUnten),
        randFarbe = BoltGlasOrangeVollRand,
        randBreite = BoltMindDimensions.rahmenStark,
        innenGlanz = BoltWeiss30,
        leuchten = listOf(
            Leuchten(BoltLeuchtOrange35, 1.dp),
            Leuchten(BoltLeuchtOrange45, 18.dp)
        )
    )

    // --- GG: gruen ----------------------------------------------------------

    /** "SITZT!". */
    val gruenVoll = GlasRezept(
        fuellung = senkrecht(BoltGlasGruenVollOben, BoltGlasGruenVollUnten),
        randFarbe = BoltGlasGruenVollRand,
        innenGlanz = BoltWeiss45,
        leuchten = listOf(
            Leuchten(BoltGlasGruenRing, 1.dp),
            Leuchten(BoltLeuchtGruen45, 20.dp),
            Leuchten(BoltLeuchtGruen25, 56.dp),
            Leuchten(BoltLeuchtGruen45, 30.dp, versatzY = 10.dp, ausbreitung = (-6).dp)
        )
    )

    /**
     * "DRIN" -- der bereits eingebaute Zustand. Flach und ohne Verlauf; das
     * unterscheidet Zustand von Aktion.
     */
    val gruenZustand = GlasRezept.einfarbig(
        fuellung = BoltGruenZustandFlaeche,
        randFarbe = BoltGruenZustandRand,
        randBreite = BoltMindDimensions.rahmenSehrStark,
        innenGlanz = BoltWeiss14,
        leuchten = listOf(
            Leuchten(BoltLeuchtGruen30, 18.dp),
            Leuchten(BoltLeuchtGruen16, 44.dp)
        )
    )

    // --- GD: dunkle Panels ---------------------------------------------------

    /** Vorgangskarte in der Uebersicht. */
    val karte = GlasRezept.einfarbig(
        fuellung = BoltPanel82,
        randFarbe = BoltWeiss09
    )

    /** Timer-Kapsel. */
    val kapsel = GlasRezept.einfarbig(
        fuellung = BoltPanel55,
        randFarbe = BoltWeiss15
    )

    /** Eingabefeld. Ohne Unschaerfe -- im Entwurf hat es keinen backdrop-filter. */
    val eingabe = GlasRezept.einfarbig(
        fuellung = BoltEingabeFlaeche,
        randFarbe = BoltWeiss16,
        randBreite = BoltMindDimensions.rahmenStark,
        unschaerfe = false
    )

    /** Badge "ARCHIV - NUR LESEN". */
    val badge = GlasRezept.einfarbig(
        fuellung = BoltPanel72,
        randFarbe = BoltWeiss16,
        unschaerfe = false
    )

    /** Anzahl-Badge am Thumbnail. */
    val zaehlerBadge = GlasRezept.einfarbig(
        fuellung = BoltPanel90,
        randFarbe = BoltWeiss22,
        unschaerfe = false
    )

    // --- GL: Label-Chips -----------------------------------------------------

    /** Label-Chip im nicht gesetzten Zustand. */
    val labelAus = GlasRezept.einfarbig(
        fuellung = BoltPanel55,
        randFarbe = BoltWeiss20,
        randBreite = BoltMindDimensions.rahmenStark,
        innenGlanz = BoltWeiss12
    )

    fun labelAn(farbe: androidx.compose.ui.graphics.Color, flaeche: androidx.compose.ui.graphics.Color) =
        GlasRezept.einfarbig(
            fuellung = flaeche,
            randFarbe = farbe,
            randBreite = BoltMindDimensions.rahmenStark,
            innenGlanz = BoltWeiss12
        )

    /** Label-Chip im Lesemodus. */
    fun labelLesend(gesetzt: Boolean) = GlasRezept.einfarbig(
        fuellung = BoltPanel50,
        randFarbe = if (gesetzt) BoltWeiss22 else BoltWeiss10
    )

    // --- GS: Sheet -----------------------------------------------------------

    val sheet = GlasRezept(
        fuellung = senkrecht(BoltSheetOben, BoltSheetUnten),
        randFarbe = BoltWeiss16,
        randBreite = 0.dp
    )

    /** Sheet-Aktion, Stil "normal". */
    val sheetAktion = GlasRezept(
        fuellung = Brush.linearGradient(listOf(BoltWeiss10, BoltWeiss03)),
        randFarbe = BoltWeiss16,
        randBreite = BoltMindDimensions.rahmenStark,
        innenGlanz = BoltWeiss22
    )

    /** Sheet-Aktion, Stil "primaer". */
    val sheetAktionPrimaer = GlasRezept(
        fuellung = Brush.linearGradient(
            listOf(BoltGlasOrangeSheetHell, BoltGlasOrangeSheetMitte, BoltGlasOrangeSheetUnten)
        ),
        randFarbe = BoltGlasOrangeSheetRand,
        randBreite = BoltMindDimensions.rahmenStark,
        innenGlanz = BoltWeiss34,
        leuchten = listOf(
            Leuchten(BoltLeuchtOrange30, 16.dp),
            Leuchten(BoltLeuchtOrange16, 40.dp)
        )
    )

    /** Sheet-Aktion, Stil "gefahr". */
    val sheetAktionGefahr = GlasRezept(
        fuellung = SolidColor(BoltGefahrFlaeche),
        randFarbe = BoltGefahrRand,
        randBreite = BoltMindDimensions.rahmenStark,
        innenGlanz = BoltWeiss14,
        leuchten = listOf(Leuchten(BoltGefahrLeuchten, 18.dp))
    )
}
