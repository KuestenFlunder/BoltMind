package com.boltmind.app.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================================
// BoltMind Farbtoken -- 1:1 aus dem Design-Prototyp "BoltMind App.dc.html".
//
// Orange auf Nahezu-Schwarz, Glas ueber Mesh-Verlauf. Nur Dark Mode.
// Jeder Wert hier kommt woertlich aus dem Prototyp; nichts ist interpoliert.
// Referenz: docs/specs/design-system.md
// ============================================================================

// --- Hintergruende und opake Flaechen ---------------------------------------

/** Grundflaeche aller Screens. */
val BoltHintergrund = Color(0xFF0B0C0D)

/** Browser, Kamera und Vollbild liegen auf reinem Schwarz. */
val BoltSchwarz = Color(0xFF000000)

/** Chip-Flaeche, z.B. "n TEILE" auf der Vorgangskarte. */
val BoltChipFlaeche = Color(0xFF1F2225)

/** Rahmen um das Anlage-Foto. */
val BoltRahmenDunkel = Color(0xFF2A2E33)

/** Gestrichelter Rahmen der Leerzustaende. */
val BoltRahmenGestrichelt = Color(0xFF33383D)

/** Trennlinie zwischen den Kennzahlen im Abschluss-Screen. */
val BoltTrennlinie = Color(0xFF2A2E33)

/** Kreisflaeche hinter dem Haken im Abschluss-Screen. */
val BoltErfolgFlaeche = Color(0xFF16241D)

// --- Orange / Primaer --------------------------------------------------------

/** Leitfarbe. Aktive Label, Fortschrittsbalken, Akzentkanten. */
val BoltOrange = Color(0xFFFF7A1A)

/** Hellere Variante: Ueberschriften auf Glas, Claim im Splash. */
val BoltOrangeHell = Color(0xFFFF9A4D)

/** Verlaufsanfang des Logo-Quadrats und der Primaerflaechen. */
val BoltOrangeVerlaufOben = Color(0xFFFF8F33)

/** Verlaufsende. */
val BoltOrangeVerlaufUnten = Color(0xFFEF6A05)

/** Zaehler im aktiven Tab. */
val BoltOrangeGedaempft = Color(0xFFC96716)

/** Schrift auf orangem Grund (Logo-Quadrat). */
val BoltAufOrange = Color(0xFF160C03)

/** Link-Hover im Prototyp-Rahmen. */
val BoltOrangeLink = Color(0xFFFFA259)

// --- Gruen / Erfolg ----------------------------------------------------------

/** "SITZT!", Erledigt-Haken, Fortschrittsbalken der Montage, Label "Bauteil". */
val BoltGruen = Color(0xFF24C48A)

/** Beschriftung im Zustand "DRIN". */
val BoltGruenDunkel = Color(0xFF1A8D63)

/** Schrift auf gruenem Grund. */
val BoltAufGruen = Color(0xFF062A1D)

// --- Blau / Info -------------------------------------------------------------

/** Label "Uebersicht". */
val BoltBlau = Color(0xFF3B9DFF)

// --- Rot / Fehler ------------------------------------------------------------

/** Fehler-Punkt im Formular. */
val BoltFehler = Color(0xFFEF4444)

/** Fehlertext. */
val BoltFehlerText = Color(0xFFF87171)

// --- Textstufen, hell nach dunkel --------------------------------------------

val BoltTextWeiss = Color(0xFFFFFFFF)

/** Standard-Ueberschrift. */
val BoltTextPrimaer = Color(0xFFEDEFF1)

/** Etwas gedaempfter -- Kopfzeile im Browser. */
val BoltTextRuhig = Color(0xFFE3E7EA)

/** Beschriftungen auf Rundbuttons. */
val BoltTextHell = Color(0xFFE5E8EA)

val BoltTextGedaempft = Color(0xFFDFE3E6)

/** Sekundaertext, z.B. Vorgangstitel in der Liste. */
val BoltTextSekundaer = Color(0xFFAEB5BB)

/** Werte in Chips. */
val BoltTextTertiaer = Color(0xFFCFD4D8)

/** Beschriftung von Sekundaeraktionen. */
val BoltTextLeise = Color(0xFFC3C9CE)

/** Statuszeile, Metadaten. */
val BoltTextSchwach = Color(0xFF9AA1A7)

/** Datum, Hinweise. */
val BoltTextSchwaecher = Color(0xFF6F767C)

/** Zaehler im inaktiven Tab, Chevron. */
val BoltTextSchwaechst = Color(0xFF4C5257)

/** Statusleiste. */
val BoltTextStatusleiste = Color(0xFFA8AEB3)

/** Format-Hinweis. */
val BoltTextMini = Color(0xFF878E95)

/** Rahmen des Akku-Symbols. */
val BoltStatusleisteRahmen = Color(0xFF7D848A)

// --- Weiss-Leiter: Raender, Glasfuellungen, Overlays -------------------------

val BoltWeiss03 = Color(0x08FFFFFF)
val BoltWeiss05 = Color(0x0DFFFFFF)
val BoltWeiss07 = Color(0x12FFFFFF)
val BoltWeiss08 = Color(0x14FFFFFF)
val BoltWeiss09 = Color(0x17FFFFFF)
val BoltWeiss10 = Color(0x1AFFFFFF)
val BoltWeiss12 = Color(0x1FFFFFFF)
val BoltWeiss14 = Color(0x24FFFFFF)
val BoltWeiss15 = Color(0x26FFFFFF)
val BoltWeiss16 = Color(0x29FFFFFF)
val BoltWeiss20 = Color(0x33FFFFFF)
val BoltWeiss22 = Color(0x38FFFFFF)
val BoltWeiss24 = Color(0x3DFFFFFF)
val BoltWeiss25 = Color(0x40FFFFFF)
val BoltWeiss26 = Color(0x42FFFFFF)
val BoltWeiss28 = Color(0x47FFFFFF)
val BoltWeiss30 = Color(0x4DFFFFFF)
val BoltWeiss34 = Color(0x57FFFFFF)
val BoltWeiss35 = Color(0x59FFFFFF)
val BoltWeiss40 = Color(0x66FFFFFF)
val BoltWeiss45 = Color(0x73FFFFFF)
val BoltWeiss50 = Color(0x80FFFFFF)
val BoltWeiss55 = Color(0x8CFFFFFF)
val BoltWeiss70 = Color(0xB3FFFFFF)
val BoltWeiss90 = Color(0xE6FFFFFF)
val BoltWeiss97 = Color(0xF7FFFFFF)

// --- Schwarz-Leiter: Verlaeufe und Abdunklungen ------------------------------

val BoltSchwarz00 = Color(0x00000000)
val BoltSchwarz08 = Color(0x14000000)
val BoltSchwarz10 = Color(0x1A000000)
val BoltSchwarz50 = Color(0x80000000)
val BoltSchwarz55 = Color(0x8C000000)
val BoltSchwarz60 = Color(0x99000000)
val BoltSchwarz65 = Color(0xA6000000)
val BoltSchwarz70 = Color(0xB3000000)
val BoltSchwarz80 = Color(0xCC000000)
val BoltSchwarz85 = Color(0xD9000000)
val BoltSchwarz90 = Color(0xE6000000)
val BoltSchwarz93 = Color(0xED000000)

// --- Dunkelgrau-Leiter: Panels, Kapseln, Sheets ------------------------------

/** Vorgangskarte. */
val BoltPanel82 = Color(0xD1121416)

/** Vorgangskarte im Hover-Zustand. */
val BoltPanelHover = Color(0xE6181B1E)

/**
 * Transparenter Anfang des Fussverlaufs in der Uebersicht.
 * Prototyp Z. 113: `linear-gradient(180deg,rgba(11,12,13,0),rgba(11,12,13,.55))`.
 * Muss dieselbe Grundfarbe tragen wie [BoltPanel55], sonst laeuft der Verlauf
 * ueber Grau statt sauber auszublenden.
 */
val BoltPanel00 = Color(0x000B0C0D)

/** Timer-Kapsel, Label-Chip inaktiv. */
val BoltPanel55 = Color(0x8C0B0C0D)
val BoltPanel50 = Color(0x800B0C0D)
val BoltPanel60 = Color(0x990B0C0D)
val BoltPanel62 = Color(0x9E0B0C0D)
val BoltPanel72 = Color(0xB80B0C0D)
val BoltPanel90 = Color(0xE60B0C0D)

/** Eingabefeld und Textarea. */
val BoltEingabeFlaeche = Color(0xAD0C0E10)

/** Sheet-Verlauf oben. */
val BoltSheetOben = Color(0x9E121417)

/** Sheet-Verlauf unten. */
val BoltSheetUnten = Color(0xC70A0B0C)

/** Scrim hinter dem Sheet. */
val BoltScrim = Color(0x6B060708)

/** Kugel unten rechts im Browser. */
val BoltKugelHell = Color(0x8C282C30)

// --- Mesh-Verlauf der Uebersicht und des Anlage-Screens ----------------------
// Sieben radiale Farbwolken auf BoltHintergrund. Reihenfolge und Position
// stehen in BoltMeshHintergrund (Backgrounds.kt).

val BoltMeshGruen = Color(0xD926D696)
val BoltMeshBlau = Color(0xE63B9DFF)
val BoltMeshPink = Color(0xD9E93A6E)
val BoltMeshOrange = Color(0xF2FF7A1A)
val BoltMeshGelb = Color(0xE6FFCB3C)
val BoltMeshDunkel96 = Color(0xF5090A0B)
val BoltMeshDunkel90 = Color(0xE6090A0B)

/** Abdunkelnder Verlauf ueber dem Mesh, damit Text lesbar bleibt. */
val BoltMeshSchleier88 = Color(0xE008090A)
val BoltMeshSchleier68 = Color(0xAD08090A)
val BoltMeshSchleier40 = Color(0x6608090A)
val BoltMeshSchleier14 = Color(0x2408090A)
val BoltMeshSchleier10 = Color(0x1A08090A)
val BoltMeshSchleier30 = Color(0x4D08090A)

// --- Orange-Toene der Glas-Rezepte -------------------------------------------

val BoltGlasOrangeFlachOben = Color(0x42FF8F33)
val BoltGlasOrangeFlachUnten = Color(0x29EF6A05)
val BoltGlasOrangeFlachRand = Color(0xB3FF8A28)

val BoltGlasOrangeVollOben = Color(0x61FF9842)
val BoltGlasOrangeVollUnten = Color(0x47EF6A05)
val BoltGlasOrangeVollRand = Color(0x99FFBE8C)

val BoltGlasOrangeArchivOben = Color(0x5CFF9842)
val BoltGlasOrangeArchivUnten = Color(0x42EF6A05)
val BoltGlasOrangeArchivRand = Color(0x8CFFBE8C)

/** Innerer Ring der Rundbuttons. */
val BoltGlasOrangeRing = Color(0x66FFB069)

val BoltLeuchtOrange50 = Color(0x80FF7A1A)
val BoltLeuchtOrange45 = Color(0x73FF7A1A)
val BoltLeuchtOrange35 = Color(0x59FF7A1A)
val BoltLeuchtOrange30 = Color(0x4DFF7A1A)
val BoltLeuchtOrange28 = Color(0x47FF7A1A)
val BoltLeuchtOrange18 = Color(0x2EFF7A1A)
val BoltLeuchtOrange16 = Color(0x29FF7A1A)
val BoltLeuchtOrange05 = Color(0x0DFF7A1A)

// Verlauf der primaeren Sheet-Aktion. Der Entwurf nutzt hier 160 Grad und drei
// Stopps -- eine eigene Mischung, nicht die Flaechenvariante.
val BoltGlasOrangeSheetHell = Color(0x4DFFA85C)
val BoltGlasOrangeSheetMitte = Color(0x24EF6A05)
val BoltGlasOrangeSheetUnten = Color(0x33FF8F33)
val BoltGlasOrangeSheetRand = Color(0xCCFF963C)

/** Fuellung des aktiven Labels "Ablageort". */
val BoltLabelAblageortFlaeche = Color(0x2EFF7A1A)

// --- Gruen-Toene der Glas-Rezepte --------------------------------------------

val BoltGlasGruenVollOben = Color(0x573FE0AF)
val BoltGlasGruenVollUnten = Color(0x3D12A171)
val BoltGlasGruenVollRand = Color(0x8C96F0CD)
val BoltGlasGruenRing = Color(0x6678F0C4)

val BoltLeuchtGruen45 = Color(0x7324C48A)
val BoltLeuchtGruen30 = Color(0x4D24C48A)
val BoltLeuchtGruen25 = Color(0x4024C48A)
val BoltLeuchtGruen16 = Color(0x2924C48A)

/** Zustandsflaeche "DRIN". */
val BoltGruenZustandFlaeche = Color(0x2424C48A)
val BoltGruenZustandRand = Color(0xBF24C48A)

/** Fuellung des aktiven Labels "Bauteil". */
val BoltLabelBauteilFlaeche = Color(0x2924C48A)

// --- Blau- und Rot-Toene der Glas-Rezepte ------------------------------------

/** Fuellung des aktiven Labels "Uebersicht". */
val BoltLabelUebersichtFlaeche = Color(0x2E3B9DFF)

/** Farbwolke oben rechts im Sheet. */
val BoltSheetWolkeBlau = Color(0x383B9DFF)

/**
 * Farbwolke oben links im Sheet -- das Gegenstueck zu [BoltSheetWolkeBlau].
 * Prototyp Z. 406: `radial-gradient(closest-side,rgba(255,122,26,.4),...)`.
 * Die Leuchtleiter kennt 0.45 und 0.35, aber keine 0.40.
 */
val BoltSheetWolkeOrange = Color(0x66FF7A1A)

/** Gefahr-Aktion im Sheet. */
val BoltGefahrFlaeche = Color(0x24EF4444)
val BoltGefahrRand = Color(0xB3EF4444)
val BoltGefahrLeuchten = Color(0x42EF4444)

/**
 * Farbcodierung eines Schritts ohne jedes Label. Erreichbar nur, wenn der
 * Mechaniker an einem Foto alle drei Haken entfernt -- laut Governance ein
 * gueltiger Zustand.
 */
val BoltKategorieOhne = Color(0xFF5A6167)
