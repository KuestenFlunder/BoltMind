package com.boltmind.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.boltmind.app.R

// ============================================================================
// Typografie -- 1:1 aus dem Design-Prototyp.
//
// Drei Familien mit klarer Rollenteilung:
//   Barlow Condensed  Grossschrift, Aktionen, die riesige Schrittnummer
//   Barlow            Fliesstext, Beschriftungen
//   JetBrains Mono    Zahlen: Auftragsnummern, Uhrzeiten, Zaehler
//
// Der Prototyp verwendet 57 verschiedene Textstile. Hier stehen die
// wiederkehrenden Rollen; Einzelfaelle bauen ueber [barlow], [barlowCondensed]
// und [mono] ihren Stil direkt.
// Referenz: docs/specs/design-system.md
// ============================================================================

val BarlowFamily = FontFamily(
    Font(R.font.barlow_regular, FontWeight.Normal),
    Font(R.font.barlow_medium, FontWeight.Medium),
    Font(R.font.barlow_semibold, FontWeight.SemiBold),
    Font(R.font.barlow_bold, FontWeight.Bold)
)

/**
 * Der Prototyp verwendet Barlow Condensed ausschliesslich in 700.
 * Weitere Schnitte werden erst mitgeliefert, wenn ein Design sie braucht.
 */
val BarlowCondensedFamily = FontFamily(
    Font(R.font.barlow_condensed_bold, FontWeight.Bold)
)

val MonoFamily = FontFamily(
    Font(R.font.jetbrains_mono_medium, FontWeight.Medium),
    Font(R.font.jetbrains_mono_semibold, FontWeight.SemiBold),
    Font(R.font.jetbrains_mono_bold, FontWeight.Bold)
)

/**
 * CSS misst Zeilenhoehe ab Grundlinie und kennt kein Font-Padding. Ohne diese
 * beiden Einstellungen sitzt jeder Text in Compose ein paar dp tiefer als im
 * Entwurf -- bei der 118sp grossen Schrittnummer mit Zeilenhoehe 0.72 faellt
 * das sofort auf.
 */
@OptIn(ExperimentalTextApi::class)
private val KompaktesLayout = TextStyle(
    platformStyle = PlatformTextStyle(includeFontPadding = false),
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.Both
    )
)

private fun stil(
    family: FontFamily,
    weight: FontWeight,
    size: TextUnit,
    lineHeightFaktor: Float = 1f,
    letterSpacing: TextUnit = 0.sp
): TextStyle = KompaktesLayout.copy(
    fontFamily = family,
    fontWeight = weight,
    fontSize = size,
    lineHeight = size * lineHeightFaktor,
    letterSpacing = letterSpacing
)

/** Freier Barlow-Stil fuer Einzelfaelle. */
fun barlow(
    size: TextUnit,
    weight: FontWeight = FontWeight.Bold,
    lineHeightFaktor: Float = 1f,
    letterSpacing: TextUnit = 0.sp
) = stil(BarlowFamily, weight, size, lineHeightFaktor, letterSpacing)

/** Freier Barlow-Condensed-Stil. Nur Bold ist eingebunden. */
fun barlowCondensed(
    size: TextUnit,
    lineHeightFaktor: Float = 1f,
    letterSpacing: TextUnit = 0.sp
) = stil(BarlowCondensedFamily, FontWeight.Bold, size, lineHeightFaktor, letterSpacing)

/** Freier JetBrains-Mono-Stil. */
fun mono(
    size: TextUnit,
    weight: FontWeight = FontWeight.SemiBold,
    lineHeightFaktor: Float = 1f,
    letterSpacing: TextUnit = 0.sp
) = stil(MonoFamily, weight, size, lineHeightFaktor, letterSpacing)

/**
 * Die wiederkehrenden Rollen aus dem Prototyp.
 * Ueber `BoltMindTheme` als `BoltText.` erreichbar.
 */
object BoltTypo {

    // --- Schrittnummer und Wortmarke ---

    /** Die riesige Schrittnummer im Browser. 118px/0.72 im Entwurf. */
    val schrittZifferRiesig = barlowCondensed(118.sp, 0.72f)

    /** Wortmarke im Splash. */
    val wortmarkeGross = barlowCondensed(46.sp, letterSpacing = 11.sp)

    /** Wortmarke in der Kopfzeile. */
    val wortmarke = barlowCondensed(21.sp, letterSpacing = 4.sp)

    /** Ueberschrift des Abschluss-Screens. */
    val abschlussTitel = barlowCondensed(40.sp, letterSpacing = 2.sp)

    // --- Aktionen ---

    /** Grosse Primaeraktion: "LOS GEHT'S", "AB INS ARCHIV". */
    val aktionGross = barlowCondensed(26.sp, letterSpacing = 2.sp)

    /** Sheet-Aktion. */
    val aktionSheet = barlowCondensed(21.sp, letterSpacing = 1.5.sp)

    /** Beschriftung des FAB. */
    val aktionFab = barlowCondensed(21.sp, letterSpacing = 1.6.sp)

    /** Zahl im Haupt-Rundbutton. */
    val rundbuttonZahl = barlowCondensed(32.sp)

    /** Beschriftung unter der Zahl im Rundbutton. */
    val rundbuttonLabel = barlow(11.sp, letterSpacing = 1.3.sp)

    /** Beschriftung im kleinen Rundbutton. */
    val rundbuttonLabelKlein = barlow(10.sp, letterSpacing = 0.9.sp)

    // --- Tabs, Chips, Label ---

    /** Tab-Beschriftung "OFFEN" / "ARCHIV". */
    val tab = barlowCondensed(16.sp, letterSpacing = 1.6.sp)

    /** Zaehler im Tab. */
    val tabZaehler = mono(11.sp)

    /** Label-Chip "BAUTEIL" / "UEBERSICHT" / "ABLAGEORT". */
    val labelChip = barlow(13.sp, letterSpacing = 1.2.sp)

    /** Label-Chip im Lesemodus, kleiner. */
    val labelChipLesend = barlow(12.sp, FontWeight.SemiBold, letterSpacing = 1.1.sp)

    /** Chip "n TEILE" auf der Vorgangskarte. */
    val teileChip = mono(11.sp, FontWeight.Bold)

    // --- Listen ---

    /** Auftragsnummer auf der Vorgangskarte. */
    val vorgangNummer = mono(19.sp, FontWeight.Bold, letterSpacing = (-0.5).sp)

    /** Vorgangstitel. */
    val vorgangTitel = barlow(14.sp, FontWeight.SemiBold, 1.2f)

    /** Datum in der Liste. */
    val vorgangDatum = barlow(12.sp, FontWeight.Medium)

    // --- Formular ---

    /** Feldbeschriftung "AUFTRAGSNUMMER", "WAS IST ZU TUN?". */
    val feldLabel = barlow(13.sp, letterSpacing = 1.6.sp)

    /** Eingabe der Auftragsnummer. */
    val feldEingabeMono = mono(22.sp, FontWeight.Bold)

    /** Mehrzeilige Eingabe. */
    val feldEingabe = barlow(16.sp, FontWeight.Medium, 1.4f)

    /** Fehlertext unter dem Feld. */
    val feldFehler = barlow(13.sp, FontWeight.SemiBold)

    // --- Kopfzeile des Browsers ---

    /** "SCHRITT" / "EINBAU" / "ARCHIV" ueber der Auftragsnummer. */
    val browserModus = barlow(10.sp, letterSpacing = 2.4.sp)

    /** Auftragsnummer im Browser. */
    val browserVorgang = mono(13.sp, FontWeight.Bold)

    /** Statuszeile unter der Auftragsnummer. */
    val browserStatus = barlow(11.sp, FontWeight.Medium)

    /** Bildschirmtitel "NEUER AUFTRAG". */
    val screenTitel = barlowCondensed(20.sp, letterSpacing = 2.sp)

    // --- Timer und Zaehler ---

    /** Zustand "LAEUFT" / "STEHT". */
    val timerLabel = barlow(9.sp, letterSpacing = 1.7.sp)

    /** mm:ss. */
    val timerZeit = mono(22.sp, FontWeight.Bold)

    /** "FOTO 2/3", Ueberlaufzaehler der Thumbnail-Leiste. */
    val zaehlerMini = mono(10.sp, FontWeight.SemiBold)

    /** Fortschritt "3 VON 15 DRIN". */
    val fortschritt = mono(11.sp, FontWeight.Bold)

    /** Statusleiste. */
    val statusleiste = mono(11.sp, FontWeight.Medium)

    // --- Thumbnails ---

    /** Schrittnummer auf dem aktiven Thumbnail. */
    val thumbNummerAktiv = barlowCondensed(18.sp)

    /** Schrittnummer auf inaktiven Thumbnails. */
    val thumbNummer = barlowCondensed(14.sp)

    /** Anzahl-Badge am Thumbnail. */
    val thumbAnzahl = mono(10.sp, FontWeight.Bold)

    // --- Leerzustaende und Dialoge ---

    val leerTitel = barlowCondensed(19.sp, 1.2f, letterSpacing = 1.sp)
    val leerText = barlow(13.sp, FontWeight.Medium, 1.5f)

    val dialogTitel = barlowCondensed(24.sp, 1.1f, letterSpacing = 1.sp)
    val dialogText = barlow(14.sp, FontWeight.Medium, 1.4f)

    // --- Abschluss-Screen ---

    val abschlussText = barlow(15.sp, FontWeight.Medium, 1.5f)
    val abschlussKennzahl = barlowCondensed(30.sp)
    val abschlussKennzahlKlein = barlowCondensed(24.sp)
    val abschlussKennzahlLabel = barlow(10.sp, FontWeight.SemiBold, letterSpacing = 1.6.sp)

    // --- Splash ---

    val splashClaim = barlow(12.sp, FontWeight.SemiBold, letterSpacing = 3.4.sp)

    // --- Sonstiges ---

    /** Badge "ARCHIV - NUR LESEN". */
    val badge = barlow(11.sp, letterSpacing = 1.4.sp)

    /** Motivationszeile unten links in der Uebersicht. */
    val motivation = barlow(29.sp, lineHeightFaktor = 0.92f, letterSpacing = (-0.9).sp)

    /** Chevron und andere reine Glyphen. */
    val glyphe = barlow(22.sp)
}

/**
 * Material3 erwartet eine [Typography]. BoltMind benutzt fast ueberall die
 * Rollen aus [BoltTypo]; diese Zuordnung deckt die Faelle ab, in denen ein
 * Material-Baustein selbst Text setzt.
 */
val BoltMindTypography = Typography(
    displayLarge = BoltTypo.abschlussTitel,
    headlineLarge = BoltTypo.aktionGross,
    headlineMedium = BoltTypo.dialogTitel,
    titleLarge = BoltTypo.screenTitel,
    titleMedium = BoltTypo.vorgangTitel,
    bodyLarge = barlow(16.sp, FontWeight.Medium, 1.4f),
    bodyMedium = barlow(14.sp, FontWeight.Medium, 1.4f),
    bodySmall = BoltTypo.leerText,
    labelLarge = BoltTypo.aktionFab,
    labelMedium = BoltTypo.labelChip,
    labelSmall = BoltTypo.zaehlerMini
)
