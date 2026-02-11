# Design-System Spec

Cross-Cutting Design-Vorgaben fuer die gesamte BoltMind-App. Jede Feature-Spec erbt diese Regeln implizit. Aenderungen hier wirken sich auf alle Screens aus.

**Leitprinzip:** Werkstatt-tauglich. Schmutzige Haende, Handschuhe, schnelle Bedienung ohne praezises Zielen. Jede Design-Entscheidung wird an diesem Prinzip gemessen.

## Farbpalette

Inspiriert von [autohaus-drusche.de](https://autohaus-drusche.de/) — professionell, zurueckhaltend, hoher Kontrast.

### Modus

**Nur Dark Mode.** Kein Light Mode, kein Dynamic Color (Material You deaktiviert).

Gruende:
- Weniger Blendung bei wechselnden Lichtverhaeltnissen (Halle, Grube, Aussenlicht)
- Schmutz auf dem Display faellt weniger auf
- OLED-Akku-Schonung
- Ein Theme = weniger Komplexitaet, weniger Fehlerquellen

### Farb-Tokens

Alle Farben werden ueber `MaterialTheme.colorScheme.*` referenziert. Keine hardcodierten `Color()`-Werte in Feature-Code.

#### Hintergruende & Oberflaechen

| Token | Hex | Verwendung |
|---|---|---|
| `background` | `#0E1012` | App-Hintergrund, tiefste Ebene |
| `surface` | `#1A1D21` | Cards, Sheets, Dialoge |
| `surfaceVariant` | `#32373C` | Sekundaere Container, Foto-Platzhalter |
| `surfaceContainerLow` | `#151719` | Zwischen Background und Surface |
| `surfaceContainerHigh` | `#262B30` | Hervorgehobene Bereiche |
| `inverseSurface` | `#E2E3E6` | Snackbars, temporaere Hinweise |

#### Primaer-Akzent

| Token | Hex | Verwendung |
|---|---|---|
| `primary` | `#9CBDCC` | Primaer-Buttons, aktive Elemente, Links |
| `onPrimary` | `#0A1E28` | Text/Icons auf Primary-Buttons |
| `primaryContainer` | `#2E4A58` | Gedrueckter/selektierter Zustand |
| `onPrimaryContainer` | `#C8DEE8` | Text auf PrimaryContainer |

#### Sekundaer

| Token | Hex | Verwendung |
|---|---|---|
| `secondary` | `#ABB8C3` | Sekundaere Aktionen, weniger prominente Buttons |
| `onSecondary` | `#1A2630` | Text auf Secondary-Elementen |

#### Text & Icons

| Token | Hex | Verwendung |
|---|---|---|
| `onBackground` | `#E4E6E9` | Primaerer Text auf Background |
| `onSurface` | `#E4E6E9` | Primaerer Text auf Surfaces |
| `onSurfaceVariant` | `#8E949B` | Sekundaerer Text, Untertitel, Hinweise |
| `outline` | `#5A6068` | Trennlinien, deaktivierte Raender |
| `outlineVariant` | `#3A4046` | Subtile Trennlinien |

#### Status-Farben

| Token | Hex | Verwendung |
|---|---|---|
| `error` | `#FFB4AB` | Fehler, Loeschen-Aktionen |
| `onError` | `#690005` | Text/Icons auf Error-Elementen |
| `errorContainer` | `#93000A` | Fehler-Hintergruende |

#### Hinweis zu Kontrast

Alle Text-auf-Hintergrund-Kombinationen muessen WCAG AA erfuellen (mindestens 4.5:1 fuer normalen Text, 3:1 fuer grossen Text). Bei Aenderungen an Farben: Kontrast pruefen.

## Typografie

System-Default Sans-Serif (kein Custom-Font). Angelehnt an autohaus-drusche.de: sauber, neutral, lesbar.

### Typografie-Skala

Alle Textstile werden ueber `MaterialTheme.typography.*` referenziert. Keine hardcodierten `fontSize`-Werte in Feature-Code.

| Token | Groesse | Gewicht | Verwendung |
|---|---|---|---|
| `displayLarge` | 40sp | Bold | Schrittnummern, zentrale Zahlen |
| `headlineLarge` | 28sp | SemiBold | Screen-Titel (Preview, Arbeitsphase) |
| `headlineMedium` | 24sp | SemiBold | Abschnitts-Ueberschriften |
| `titleLarge` | 22sp | Medium | Dialog-Titel |
| `titleMedium` | 18sp | Medium | Card-Titel, Button-Labels (primaer) |
| `bodyLarge` | 16sp | Normal | Fliesstext, Beschreibungen |
| `bodyMedium` | 14sp | Normal | Sekundaertext, Metadaten |
| `bodySmall` | 12sp | Normal | Zeitstempel, technische Details |
| `labelLarge` | 16sp | Medium | Button-Text |
| `labelMedium` | 14sp | Medium | Chips, Tags |

### Regeln

- **Mindestgroesse:** 12sp. Kein Text unter 12sp in der gesamten App.
- **Schrittnummern:** Immer `displayLarge` — muessen aus 1m Entfernung lesbar sein.
- **Buttons:** Immer `labelLarge` oder `titleMedium` — nie `bodySmall`.

## Dimensionen

Alle Abstands- und Groessenwerte werden ueber ein zentrales `BoltMindDimensions`-Objekt referenziert. Keine hardcodierten `.dp`-Werte in Feature-Code.

### Spacing-Skala

| Name | Wert | Verwendung |
|---|---|---|
| `spacingXs` | 4dp | Minimaler Abstand (zwischen Icon und Label) |
| `spacingS` | 8dp | Kompakter Abstand (innerhalb einer Gruppe) |
| `spacingM` | 16dp | Standard-Abstand (Screen-Padding, zwischen Elementen) |
| `spacingL` | 24dp | Grosser Abstand (zwischen Sektionen) |
| `spacingXl` | 32dp | Sehr grosser Abstand (Top/Bottom-Margins) |

### Touch-Targets

**Absolute Prioritaet: Bedienbarkeit mit Handschuhen und schmutzigen Haenden.**

| Name | Wert | Verwendung |
|---|---|---|
| `touchTargetMin` | 64dp | Minimum fuer ALLE interaktiven Elemente |
| `touchTargetPrimary` | 72dp | Primaere Aktions-Buttons (Bestaetigen, Ausgebaut) |
| `touchTargetCamera` | 80dp | Kamera-/Foto-Buttons |
| `touchTargetSpacing` | 16dp | Minimaler Abstand zwischen interaktiven Elementen |

**Regel:** Kein Button, kein tappbares Element darf kleiner als `touchTargetMin` (64dp) sein. Auch Sekundaer-Aktionen und Navigations-Elemente.

### Komponentengroessen

| Name | Wert | Verwendung |
|---|---|---|
| `buttonHeightPrimary` | 72dp | Primaere Buttons (Bestaetigen, Ausgebaut, Dialog-Optionen) |
| `buttonHeightSecondary` | 64dp | Sekundaere Buttons (Wiederholen, Abbrechen) |
| `iconSizeM` | 28dp | Standard-Icon-Groesse |
| `iconSizeL` | 36dp | Grosse Icons (in Buttons) |
| `fotoPreviewSmall` | 80dp | Kleine Foto-Vorschau (Listen-Items) |
| `fotoPreviewLarge` | 240dp | Grosse Foto-Vorschau (Preview-View) |
| `cornerRadiusS` | 8dp | Kleine Rundungen (Chips, kleine Cards) |
| `cornerRadiusM` | 12dp | Standard-Rundungen (Cards, Buttons) |
| `cornerRadiusL` | 16dp | Grosse Rundungen (Dialoge, Sheets) |

## Shapes

Shapes werden ueber `MaterialTheme.shapes.*` referenziert.

| Token | Wert | Verwendung |
|---|---|---|
| `small` | 8dp | Chips, kleine Container |
| `medium` | 12dp | Cards, Buttons |
| `large` | 16dp | Dialoge, Bottom Sheets |
| `extraLarge` | 24dp | Modale Overlays |

## Implementierungs-Vorgaben

### Datei-Struktur

```
ui/theme/
  Color.kt         <- Farb-Tokens (Dark-Palette)
  Type.kt          <- Typografie-Skala
  Shape.kt (neu)   <- Shape-Definitionen
  Dimensions.kt (neu) <- Spacing, Touch-Targets, Komponentengroessen
  Theme.kt         <- BoltMindTheme (nur Dark, kein Dynamic Color)
```

### Enforcement im Code

1. **Farben:** `MaterialTheme.colorScheme.primary` — nie `Color(0xFF...)` in Feature-Code
2. **Typografie:** `MaterialTheme.typography.bodyLarge` — nie `fontSize = 16.sp` in Feature-Code
3. **Dimensionen:** `BoltMindDimensions.spacingM` — nie `16.dp` direkt in Feature-Code
4. **Shapes:** `MaterialTheme.shapes.medium` — nie `RoundedCornerShape(12.dp)` direkt

### Dimensions-Pattern (Kotlin)

```kotlin
object BoltMindDimensions {
    // Spacing
    val spacingXs = 4.dp
    val spacingS = 8.dp
    val spacingM = 16.dp
    val spacingL = 24.dp
    val spacingXl = 32.dp

    // Touch-Targets
    val touchTargetMin = 64.dp
    val touchTargetPrimary = 72.dp
    val touchTargetCamera = 80.dp
    val touchTargetSpacing = 16.dp

    // Buttons
    val buttonHeightPrimary = 72.dp
    val buttonHeightSecondary = 64.dp

    // Icons
    val iconSizeM = 28.dp
    val iconSizeL = 36.dp

    // Fotos
    val fotoPreviewSmall = 80.dp
    val fotoPreviewLarge = 240.dp

    // Corner Radii
    val cornerRadiusS = 8.dp
    val cornerRadiusM = 12.dp
    val cornerRadiusL = 16.dp
}
```

### Migration bestehender Screens

Alle existierenden Screens muessen auf das Design-System migriert werden:
- Hardcodierte `.dp`-Werte durch `BoltMindDimensions.*` ersetzen
- Purple/Pink-Fallback-Farben durch Dark-Palette ersetzen
- Dynamic Color entfernen
- Light-Mode-ColorScheme entfernen
- Button-Hoehen an Touch-Target-Vorgaben anpassen

## Abhaengigkeiten

- **governance.md:** Referenziert design.md fuer Touch-Targets und Debounce-Regeln
- **Alle Feature-Specs:** Erben Design-Vorgaben implizit
- **CLAUDE.md:** Referenziert design.md als Pflichtlektuere

## Offene Fragen

- [ ] Exakte Farbwerte nach Implementierung am Geraet pruefen (OLED vs LCD koennen abweichen)
- [ ] Custom-Font spaeter evaluieren? Fuer MVP: System-Default
- [ ] Animations-/Transitions-Vorgaben? Fuer MVP: Material3-Defaults
