# F-003-kern: Demontage-Flow (MVP)

## Kontext

Der Demontage-Flow ist der zentrale Workflow der BoltMind-App. Ein Mechaniker in der Werkstatt demontiert ein Fahrzeug und muss jeden Schritt dokumentieren, um später beim Zusammenbau die richtige Reihenfolge und die Ablageorte der Teile zu kennen.

**Problem:** Ohne visuelle Dokumentation ist es schwierig, sich nach Tagen oder Wochen an die korrekte Reihenfolge und die Ablageorte der ausgebauten Teile zu erinnern. Handschriftliche Notizen sind unpraktisch (dreckige Hände) und fehleranfällig.

**Lösung:** Schritt-für-Schritt-Fotodokumentation mit minimalem Interaktionsaufwand. Jeder Schritt wird sofort persistiert, sodass keine Daten verloren gehen (auch bei App-Unterbrechung durch Anrufe, leeren Akku etc.).

**Primärer Nutzer:** Mechaniker in der Werkstatt, der ein Fahrzeug demontiert und dabei Handschuhe trägt, dreckige Hände hat und schnell arbeiten muss.

**Situation:** Der Mechaniker steht am Fahrzeug, hat gerade ein Bauteil identifiziert, das ausgebaut werden soll, und möchte den aktuellen Zustand dokumentieren bevor er weiterarbeitet.

### Schritt-Definition

Ein **Schritt** entspricht einem eigenständigen Bauteil oder einer Baugruppe, die als Einheit ausgebaut wird. Der Mechaniker entscheidet selbst über die Granularität.

**Beispiele:**
- Bremsscheibe vorne links (inkl. aller Befestigungsschrauben) = 1 Schritt
- Bremssattel vorne links = 1 Schritt
- Kabelbaum-Stecker lösen = 1 Schritt (wenn er als separate Aktion dokumentiert werden soll)

**Nicht:** Einzelne Schrauben oder Kleinteile als separate Schritte, es sei denn der Mechaniker möchte es explizit.

### Schrittnummer-Konzept

Jeder Schritt erhält eine automatisch hochzählende **Schrittnummer** (1, 2, 3, ...). Die Schrittnummer dient zwei Zwecken:

1. **Fortschritts-Tracking:** Beim Zusammenbau (F-004) zeigt die Schrittnummer, wie weit der Mechaniker im Prozess ist (z.B. "Schritt 12 von 15, noch 3").
2. **Optionale Ablageort-Korrelation:** Der Mechaniker kann seine physischen Ablageorte mit den Schrittnummern beschriften. Die App erzwingt dies nicht, aber das Ablageort-Foto zeigt dann die Nummer auf dem physischen Label.

Es gibt mehr Schrittnummern als physische Ablageorte, weil manche Teile am Fahrzeug verbleiben (z.B. gelöste Kabel, die nur zur Seite gelegt werden).

### Schritt-Typen

Jeder Schritt hat einen **Typ**, der angibt, ob das Bauteil ausgebaut und abgelegt wurde oder am Fahrzeug verblieben ist:

| Typ | Bedeutung | Ablageort-Foto |
|-----|-----------|----------------|
| `AUSGEBAUT` | Bauteil wurde ausgebaut und an einem separaten Ort abgelegt | Ja (vorhanden) |
| `AM_FAHRZEUG` | Bauteil wurde abgebaut/gelöst, verbleibt aber am Fahrzeug | Nein |

Der Typ wird implizit durch die Dialog-Auswahl gesetzt:
- "Ablageort fotografieren" → `AUSGEBAUT`
- "Weiter ohne Ablageort" → `AM_FAHRZEUG`

**Nutzen des Typs:**
- Farbliche Kennzeichnung der Schrittkette im Zusammenbau-Flow (F-004)
- Schritt-Preview ohne alle Fotos laden zu müssen (z.B. Icon-Darstellung)
- Schnelle Filterung: "Zeige nur Schritte mit Ablageort"

---

## User Stories

### US-003.1: Foto aufnehmen und prüfen

**Als** Mechaniker
**möchte ich** ein Foto vom Bauteil aufnehmen und vor dem Speichern prüfen
**damit** nur brauchbare Fotos in der Dokumentation landen und ich unscharfe Aufnahmen wiederholen kann.

#### Akzeptanzkriterien

- **Given** ein Reparaturvorgang existiert und ist im Status OFFEN
  **When** der Mechaniker den Demontage-Flow startet (über Übersicht oder direkt nach Anlage)
  **Then** öffnet sich die Kamera-Ansicht mit Vollbild-Vorschau, Auslöser-Button und Schrittnummer
  **And** ein neuer `Schritt` wird in der DB angelegt mit `gestartetAm` = aktueller Timestamp

- **Given** die Kamera-Ansicht ist geöffnet
  **When** der Mechaniker den Auslöser-Button antippt
  **Then** wird das Foto aufgenommen und die Preview-Ansicht mit dem Foto angezeigt
  **And** der Auslöser-Button ist für 300ms deaktiviert (Debounce gegen Doppel-Tap)

- **Given** die Preview-Ansicht zeigt das aufgenommene Foto
  **When** der Mechaniker "Wiederholen" antippt
  **Then** öffnet sich die Kamera-Ansicht erneut (temporäre Foto-Datei wird gelöscht)

- **Given** die Preview-Ansicht zeigt das aufgenommene Foto
  **When** der Mechaniker "Bestätigen" antippt
  **Then** wird das Foto von `photos/temp/` nach `photos/` verschoben (JPEG, mittlere Kompression)
  **And** der `bauteilFotoPfad` im bestehenden `Schritt`-Entity wird aktualisiert

- **Given** die App wurde gerade installiert und Kamera-Permission wurde noch nicht erteilt
  **When** die Kamera-Ansicht geöffnet wird
  **Then** erscheint der Android-System-Dialog zur Kamera-Berechtigung
  **And** die Kamera öffnet sich erst nach Erteilung der Berechtigung

- **Given** die Kamera-Permission wurde dauerhaft abgelehnt ("Nicht mehr fragen")
  **When** die Kamera-Ansicht geöffnet werden soll
  **Then** erscheint ein Hinweis-Screen mit Erklärung und Button "Zu Einstellungen"
  **And** der Button öffnet die Android App-Einstellungen

**Hinweis:** Kamera-Permission sollte bereits durch F-002 implementiert sein. Falls nicht, muss sie hier ergänzt werden.

---

### US-003.2: Nächste Aktion nach Ausbau wählen

**Als** Mechaniker
**möchte ich** nach dem Ausbau wählen, ob ich den Ablageort fotografiere, direkt weitermache oder beende
**damit** ich den Workflow flexibel an die Situation anpasse (Teil abgelegt vs. Teil bleibt am Fahrzeug).

#### Akzeptanzkriterien

- **Given** der Mechaniker hat ein Bauteil-Foto aufgenommen und bestätigt
  **When** die Preview-Ansicht geschlossen wird
  **Then** erscheint der Ausgebaut-Screen mit der aktuellen Schrittnummer (groß, prominent), dem aufgenommenen Bauteil-Foto und einem "Ausgebaut"-Button

- **Given** der Ausgebaut-Screen wird angezeigt
  **When** der Mechaniker "Ausgebaut" antippt
  **Then** öffnet sich ein Dialog mit 3 gleichwertigen Optionen:
  1. "Ablageort fotografieren"
  2. "Weiter ohne Ablageort"
  3. "Beenden"
  **And** der Button ist für 300ms nach dem Tap deaktiviert (Debounce)

- **Given** der Dialog wird angezeigt
  **When** der Mechaniker "Ablageort fotografieren" wählt
  **Then** wird `typ` im aktuellen `Schritt`-Entity auf `AUSGEBAUT` gesetzt
  **And** die Kamera-Ansicht für das Ablageort-Foto öffnet sich (US-003.3)

- **Given** der Dialog wird angezeigt
  **When** der Mechaniker "Weiter ohne Ablageort" wählt
  **Then** wird `typ` im aktuellen `Schritt`-Entity auf `AM_FAHRZEUG` gesetzt
  **And** `abgeschlossenAm` im aktuellen `Schritt`-Entity wird gesetzt
  **And** die Kamera-Ansicht öffnet sich für das nächste Bauteil (US-003.1)
  **And** die Schrittnummer wurde inkrementiert

- **Given** der Dialog wird angezeigt
  **When** der Mechaniker "Beenden" wählt
  **Then** wird `typ` im aktuellen `Schritt`-Entity auf `AM_FAHRZEUG` gesetzt
  **And** `abgeschlossenAm` im aktuellen `Schritt`-Entity wird gesetzt
  **And** die Demontage-Ansicht schließt sich
  **And** die Vorgangs-Übersicht wird angezeigt (F-001)

#### UI-Verhalten

Der Ausgebaut-Screen ist der Platzhalter für die spätere Arbeitsphase (Zeiterfassung, Kommentare, Pausieren — siehe `F-003-arbeitsphase-ideen.md`). Im MVP zeigt er die Schrittnummer, das aufgenommene Bauteil-Foto und den "Ausgebaut"-Button.

**Dialog-Layout:** Alle 3 Optionen sind gleichwertig dargestellt (gleiche Button-Größe, kein Default hervorgehoben).

**Typische Nutzungsmuster:**
- **Teil wird ausgebaut und abgelegt** (häufigster Fall): Mechaniker wählt "Ablageort fotografieren" → `typ = AUSGEBAUT`
- **Teil bleibt am Fahrzeug** (z.B. gelöster Stecker, zur Seite gelegtes Kabel): Mechaniker wählt "Weiter ohne Ablageort" → `typ = AM_FAHRZEUG`

---

### US-003.3: Ablageort fotografieren

**Als** Mechaniker
**möchte ich** optional ein Foto vom Ablageort aufnehmen
**damit** ich später nachvollziehen kann, wo das Teil liegt.

#### Akzeptanzkriterien

- **Given** der Mechaniker hat im Dialog "Ablageort fotografieren" gewählt
  **When** der Dialog geschlossen wird
  **Then** öffnet sich die Kamera-Ansicht (gleicher Flow wie US-003.1)
  **And** ein UI-Hinweis zeigt "Ablageort fotografieren" als Banner oberhalb der Kamera-Vorschau
  **And** die aktuelle Schrittnummer bleibt sichtbar

- **Given** die Kamera-Ansicht im Ablageort-Modus ist geöffnet
  **When** der Mechaniker ein Foto aufnimmt und bestätigt
  **Then** wird das Foto von `photos/temp/` nach `photos/` verschoben
  **And** der Pfad wird im selben `Schritt`-Entity unter `ablageortFotoPfad` hinterlegt
  **And** `abgeschlossenAm` wird im `Schritt`-Entity gesetzt

- **Given** der Mechaniker hat das Ablageort-Foto bestätigt
  **When** die Preview geschlossen wird
  **Then** öffnet sich die Kamera-Ansicht für das nächste Bauteil (US-003.1)
  **And** die Schrittnummer wurde inkrementiert

- **Given** die Preview des Ablageort-Fotos wird angezeigt
  **When** der Mechaniker "Wiederholen" antippt
  **Then** öffnet sich die Kamera-Ansicht erneut im Ablageort-Modus

---

### US-003.4: Schrittnummer anzeigen

**Als** Mechaniker
**möchte ich** sehen, beim wievielten Schritt ich bin
**damit** ich den Überblick über den Fortschritt behalte und optional meine Ablageorte entsprechend nummerieren kann.

#### Akzeptanzkriterien

- **Given** der Demontage-Flow ist aktiv
  **When** die Kamera-Ansicht geöffnet ist
  **Then** wird die aktuelle Schrittnummer prominent angezeigt (z.B. "Schritt 3")

- **Given** der Ausgebaut-Screen wird angezeigt
  **When** der Mechaniker die Schrittnummer sieht
  **Then** ist die Nummer groß und deutlich lesbar (der Mechaniker kann sie auf ein physisches Label übertragen)

- **Given** ein neuer Reparaturvorgang ohne Schritte existiert
  **When** der Demontage-Flow gestartet wird
  **Then** zeigt der Schrittzähler "Schritt 1"

- **Given** der Mechaniker hat einen Schritt abgeschlossen (Dialog-Auswahl getroffen)
  **When** die Kamera für den nächsten Schritt öffnet
  **Then** zeigt der Schrittzähler die um 1 erhöhte Nummer

- **Given** der Mechaniker hat "Weiter ohne Ablageort" gewählt (kein Ablageort-Foto)
  **When** die Kamera für den nächsten Schritt öffnet
  **Then** ist die Schrittnummer trotzdem inkrementiert

- **Given** der Demontage-Flow wurde bei Schritt 5 unterbrochen (App geschlossen)
  **When** der Mechaniker den Vorgang erneut öffnet und die Demontage fortsetzt
  **Then** prüft die App, ob Schritt 5 abgeschlossen ist (`abgeschlossenAm` vorhanden):
  - **Falls ja:** Kamera öffnet sich für Schritt 6
  - **Falls nein:** Ausgebaut-Screen für Schritt 5 wird angezeigt (Schritt fortsetzen)

---

### US-003.5: Demontage beenden

**Als** Mechaniker
**möchte ich** die Demontage abschließen
**damit** ich zur Vorgangs-Übersicht zurückkehre und den dokumentierten Vorgang sehe.

#### Akzeptanzkriterien

- **Given** der Dialog nach "Ausgebaut" wird angezeigt
  **When** der Mechaniker "Beenden" wählt
  **Then** wird der aktuelle Schritt als abgeschlossen markiert (`typ` + `abgeschlossenAm`)
  **And** die Demontage-Ansicht schließt sich
  **And** die Vorgangs-Übersicht (F-001) wird angezeigt

- **Given** der Mechaniker hat 5 Schritte dokumentiert und die Demontage beendet
  **When** die Vorgangs-Übersicht angezeigt wird
  **Then** sind alle 5 Schritte mit Fotos im Vorgang sichtbar

- **Given** die Demontage wurde beendet und die Übersicht zeigt 5 Schritte
  **When** der Mechaniker den Vorgang erneut antippt und "Demontage fortsetzen" wählt
  **Then** öffnet sich die Kamera für Schritt 6
  **And** die Schrittnummer zeigt "Schritt 6"

---

### US-003.6: Back-Navigation blockieren

**Als** Mechaniker
**möchte ich** nicht versehentlich durch die Zurück-Taste einen laufenden Schritt abbrechen
**damit** keine inkonsistenten Zustände durch unbeabsichtigte Navigation entstehen.

#### Akzeptanzkriterien

- **Given** der Demontage-Flow ist aktiv (Kamera, Preview, Ausgebaut-Screen oder Dialog)
  **When** der Mechaniker die Android-Zurück-Taste drückt
  **Then** passiert nichts (Back-Geste wird abgefangen und ignoriert)

- **Given** der Demontage-Flow ist aktiv
  **When** der Mechaniker die Demontage verlassen möchte
  **Then** muss er den regulären Weg über den Dialog → "Beenden" nehmen

**Hinweis:** Die Back-Blockierung gilt für den gesamten Demontage-Flow. Der einzige Weg zurück zur Übersicht ist über "Beenden" im Dialog (US-003.2).

---

## Nicht-funktionale Anforderungen

**Performance (Quality Goal #3):**
- Kamera muss sofort auslösen, keine Verzögerung >500ms
- Foto-Speicherung darf UI nicht blockieren (async)
- Globaler Debounce: 300ms für alle Buttons im Demontage-Flow (Handschuhe, Doppel-Tap-Schutz)

**Zuverlässigkeit (Quality Goal #2):**
- Jedes Foto wird sofort auf dem Filesystem gespeichert
- Jeder Schritt wird sofort in der DB persistiert (Sofort-Save)
- App-Unterbrechung (Anruf, Home-Button, Akku leer) darf keine Daten verlieren
- Unterbrochene Schritte (ohne `abgeschlossenAm`) werden beim Fortsetzen erkannt und am Ausgebaut-Screen fortgesetzt
- Temporäre Foto-Dateien (`photos/temp/`) werden beim nächsten App-Start aufgeräumt

**Bedienbarkeit (Quality Goal #1):**
- Minimale Interaktion pro Schritt:
  - Ohne Ablageort: Foto → Bestätigen → Ausgebaut → Dialog = 4 Taps
  - Mit Ablageort: Foto → Bestätigen → Ausgebaut → Dialog → Ablageort-Foto → Bestätigen = 6 Taps
- Große Touch-Targets für "Ausgebaut"-Button und Auslöser (dreckige Hände, Handschuhe)
- Foto-Qualität: Mittlere Kompression, ~2-3 MB pro Foto (Balance zwischen Qualität und Speicher)
- Schrittnummer immer sichtbar: In Kamera-Ansicht, auf Ausgebaut-Screen und im Ablageort-Modus
- Back-Navigation blockiert: Kein versehentliches Verlassen des Flows

**Datensicherheit:**
- Fotos werden im app-internen Speicher abgelegt (nicht in der öffentlichen Galerie)
- Keine Metadaten (GPS, Zeitstempel) werden in EXIF-Daten geschrieben (Datenschutz)

**Fehlerbehandlung:**
- Bei vollem Speicher: Fehler-Dialog "Nicht genug Speicher. Bitte Speicherplatz freigeben." bevor Foto aufgenommen wird
- Bei Kamera-Fehler: Fehler-Dialog mit "Erneut versuchen"-Button
- Fehlende Foto-Dateien (z.B. nach Backup/Restore): Platzhalter-Bild in der Anzeige statt Crash

---

## Technische Hinweise

### Entities

**SchrittTyp (Enum):**
```kotlin
enum class SchrittTyp {
    AUSGEBAUT,    // Bauteil ausgebaut und an separatem Ort abgelegt
    AM_FAHRZEUG   // Bauteil abgebaut/gelöst, verbleibt am Fahrzeug
}
```

**Schritt (Room Entity):**
```kotlin
@Entity(
    tableName = "schritt",
    foreignKeys = [ForeignKey(
        entity = Reparaturvorgang::class,
        parentColumns = ["id"],
        childColumns = ["reparaturvorgangId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Schritt(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val reparaturvorgangId: Long,

    val schrittNummer: Int,                // Schrittnummer (1, 2, 3, ...) — auto-increment

    val typ: SchrittTyp? = null,           // AUSGEBAUT oder AM_FAHRZEUG (null = Schritt noch nicht abgeschlossen)

    val bauteilFotoPfad: String? = null,   // Pfad zum Bauteil-Foto (Pflicht, initial null bei Schritt-Anlage)
    val ablageortFotoPfad: String? = null,  // Pfad zum Ablageort-Foto (nur bei typ = AUSGEBAUT)

    val eingebautBeiMontage: Boolean = false, // Wird von F-004 (Montage-Flow) auf true gesetzt

    val gestartetAm: Instant,              // Timestamp: Kamera öffnet sich für diesen Schritt
    val abgeschlossenAm: Instant? = null    // Timestamp: Dialog-Auswahl getroffen (null = unterbrochen)
)
```

**Hinweis:** `bauteilFotoPfad` ist initial `null` wenn der Schritt in der DB angelegt wird (beim Kamera-Öffnen). Der Pfad wird per Update ergänzt, sobald das Foto bestätigt wurde. Ein Schritt ohne `bauteilFotoPfad` ist ein Artefakt einer Unterbrechung und kann beim Fortsetzen mit neuem Foto befüllt werden.

**Hinweis:** `typ` ist initial `null` und wird erst bei der Dialog-Auswahl gesetzt. Ein Schritt mit `typ = null` ist ein nicht abgeschlossener Schritt (Unterbrechung vor Dialog-Auswahl).

### Kamera-Integration

- **CameraX:** `ImageCapture` Use Case für Fotoaufnahme
- **Preview:** `PreviewView` für Vollbild-Vorschau
- **Foto-Speicherung:** Temporäre Aufnahmen in `context.filesDir/photos/temp/`, bestätigte Fotos in `context.filesDir/photos/` (JPEG mit Quality 85)
- **Debounce:** Global 300ms für alle Buttons im Demontage-Flow

### Foto-Ordner-Struktur

```
context.filesDir/
  photos/
    temp/          ← Nicht bestätigte Aufnahmen (werden beim App-Start aufgeräumt)
    bauteil_1.jpg  ← Bestätigte Bauteil-Fotos
    ablageort_1.jpg ← Bestätigte Ablageort-Fotos
    ...
```

**Cleanup-Regel:** Beim App-Start wird `photos/temp/` geleert. Bestätigte Fotos in `photos/` bleiben dauerhaft.

### Foto-Flow Logik

1. **Schritt anlegen:** Kamera öffnet sich → `Schritt`-Entity in DB anlegen mit `schrittNummer` und `gestartetAm`, `bauteilFotoPfad = null`, `typ = null`
2. **Foto aufnehmen:** Kamera → `takePicture()` → temporäre Datei in `photos/temp/`
3. **Preview anzeigen:** Bild laden, in Preview-Composable darstellen
4. **Wiederholen:** Temporäre Datei löschen, zurück zu Schritt 2
5. **Bestätigen:**
   - Datei von `photos/temp/` nach `photos/` verschieben (rename)
   - `bauteilFotoPfad` im `Schritt`-Entity per Update setzen
   - Weiter zu Ausgebaut-Screen
6. **Dialog-Auswahl:** `typ` und `abgeschlossenAm` setzen, dann:
   - "Ablageort fotografieren" → `typ = AUSGEBAUT`, Kamera im Ablageort-Modus
   - "Weiter ohne Ablageort" → `typ = AM_FAHRZEUG`, `abgeschlossenAm` setzen, nächsten Schritt starten
   - "Beenden" → `typ = AM_FAHRZEUG`, `abgeschlossenAm` setzen, zurück zur Übersicht

### Schrittnummer-Logik

- `schrittNummer` wird beim Anlegen des `Schritt`-Entity gesetzt (= letzte Nummer + 1)
- Wird von der DB geladen beim Fortsetzen (letzte `schrittNummer` + 1, bzw. aktuelle Nummer bei unterbrochenem Schritt)
- Inkrementiert unabhängig davon ob Ablageort-Foto gemacht wurde
- Keine manuelle Eingabe möglich, rein auto-increment
- Keine Obergrenze

### Timestamp-Semantik

| Feld | Wird gesetzt wenn... | Bedeutung |
|------|---------------------|-----------|
| `gestartetAm` | Kamera für diesen Schritt öffnet sich | Beginn der Arbeit am Schritt |
| `abgeschlossenAm` | Dialog-Auswahl getroffen ("Weiter"/"Beenden") ODER Ablageort-Foto bestätigt | Schritt vollständig dokumentiert |

**Sonderfall:** Wenn der Mechaniker die App nach Foto-Bestätigung aber vor Dialog-Auswahl schließt, ist `abgeschlossenAm = null` und `typ = null`. Beim Fortsetzen wird der Ausgebaut-Screen für diesen Schritt erneut angezeigt.

### Navigation

```
Kamera (Schritt N)
  → Preview
    → [Bestätigen] → Ausgebaut-Screen (Schrittnummer groß + Bauteil-Foto)
      → [Ausgebaut] → Dialog
        → [Ablageort fotografieren] → typ=AUSGEBAUT → Kamera (Ablageort) → Preview
          → [Bestätigen] → Kamera (Schritt N+1)
        → [Weiter ohne Ablageort] → typ=AM_FAHRZEUG → Kamera (Schritt N+1)
        → [Beenden] → typ=AM_FAHRZEUG → Übersicht (F-001)
    → [Wiederholen] → Kamera (Schritt N, erneut)

Back-Taste: Blockiert im gesamten Flow (→ "Beenden" über Dialog)
```

- Navigation über `NavController` (Jetpack Navigation)
- State Hoisting: ViewModel hält State, Screens sind stateless
- `BackHandler` in allen Demontage-Screens: `onBack = { /* nichts */ }`

### Sofort-Save Strategie

- **Schritt-Entity:** Wird beim Kamera-Öffnen sofort in DB angelegt (mit `gestartetAm`, ohne `bauteilFotoPfad`, `typ = null`)
- **Bauteil-Foto:** Wird beim Bestätigen von `photos/temp/` nach `photos/` verschoben und Pfad in DB aktualisiert
- **Ablageort-Foto:** Wird beim Bestätigen von `photos/temp/` nach `photos/` verschoben und Pfad in DB aktualisiert
- **Typ:** Wird bei Dialog-Auswahl sofort in DB geschrieben (`AUSGEBAUT` oder `AM_FAHRZEUG`)
- **Abschluss:** `abgeschlossenAm` wird bei Dialog-Auswahl bzw. nach Ablageort-Foto sofort in DB geschrieben
- **Unterbrechung:** Schritt ohne `abgeschlossenAm` wird beim nächsten Start erkannt und fortgesetzt
- **Orphaned Schritte:** Unterbrochene Schritte bleiben in der DB und werden beim Fortsetzen weiterbearbeitet (kein Löschen)
- **Orphaned Fotos:** Temporäre Dateien in `photos/temp/` werden beim App-Start aufgeräumt

### App-Unterbrechungs-Verhalten

| Unterbrechung bei... | Persistierter Zustand | Verhalten beim Fortsetzen |
|----------------------|----------------------|--------------------------|
| Kamera offen, kein Foto | `Schritt` in DB (ohne Foto, `typ = null`) | Kamera öffnet sich erneut für diesen Schritt |
| Preview angezeigt | `Schritt` in DB (ohne Foto, `typ = null`), temp. Datei | Temp-Datei cleanup, Kamera öffnet sich erneut |
| Ausgebaut-Screen | `Schritt` in DB (mit Foto, `typ = null`, ohne `abgeschlossenAm`) | Ausgebaut-Screen wird angezeigt |
| Dialog offen | `Schritt` in DB (mit Foto, `typ = null`, ohne `abgeschlossenAm`) | Ausgebaut-Screen wird angezeigt |
| Ablageort-Kamera | `Schritt` in DB (mit Bauteil-Foto, `typ = AUSGEBAUT`, ohne Ablageort, ohne `abgeschlossenAm`) | Ausgebaut-Screen wird angezeigt |

---

## Offene Fragen

- [x] **Ablageort-Foto vs. Ablageort-Nummer:** Entschieden — Schrittnummer wird automatisch vergeben (auto-increment). Ablageort-Foto bleibt optional. Die Schrittnummer dient sowohl als Fortschritts-Indikator als auch als optionale Ablageort-Korrelation.
- [x] **Arbeitsphase-Umfang:** Entschieden — MVP nur "Ausgebaut"-Button, Erweiterung (Timer, Kommentare, Sprachnotizen) kommt in separater Spec (siehe `F-003-arbeitsphase-ideen.md`).
- [x] **Undo/Korrektur:** Entschieden — Kein Undo im MVP. Fehlerhafte Schritte bleiben bestehen.
- [x] **Textnotizen:** Entschieden — Nicht im MVP. Text-to-Speech kommt mit Arbeitsphase-Erweiterung.
- [x] **Manuelle Ablageort-Nummer:** Entschieden — Keine manuelle Eingabe. Nur auto-increment Schrittnummer.
- [x] **Back-Navigation:** Entschieden — Back ist im gesamten Demontage-Flow blockiert. Verlassen nur über Dialog → "Beenden".
- [x] **Orphaned Schritte:** Entschieden — Unterbrochene Schritte bleiben für Fortsetzung erhalten.
- [x] **Schritt-Typ:** Entschieden — `enum class SchrittTyp { AUSGEBAUT, AM_FAHRZEUG }` wird beim Dialog gesetzt.
- [x] **Entity-Feldname:** Entschieden — `schrittNummer` (nicht `sequenzNummer`).
- [x] **Tabellen-Name:** Entschieden — `tableName = "schritt"` (Singular).
- [x] **Debounce:** Entschieden — Global 300ms für alle Buttons.
- [x] **Foto-Ordner:** Entschieden — Separate Ordner `photos/temp/` und `photos/`.
- [x] **Ausgebaut-Screen:** Entschieden — Zeigt Bauteil-Foto neben Schrittnummer.
- [ ] **OFFEN:** Soll der Mechaniker im Ablageort-Modus ein bereits aufgenommenes Ablageort-Foto wiederverwenden können (z.B. mehrere Teile in derselben Kiste)? → Für MVP: Nein, jedes Mal neues Foto. Später: Foto-Vorschläge aus Historie.
- [ ] **OFFEN:** UI-Hinweis "Ablageort fotografieren" — Banner oberhalb der Kamera-Vorschau (vorgeschlagen, UX-Entscheidung bei Implementierung).
