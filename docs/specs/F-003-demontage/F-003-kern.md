# F-003-kern: Demontage-Flow (MVP)

## Kontext

Der Demontage-Flow ist der zentrale Workflow der BoltMind-App. Ein Mechaniker in der Werkstatt demontiert ein Fahrzeug und muss jeden Schritt dokumentieren, um später beim Zusammenbau die richtige Reihenfolge und die Ablageorte der Teile zu kennen.

**Problem:** Ohne visuelle Dokumentation ist es schwierig, sich nach Tagen oder Wochen an die korrekte Reihenfolge und die Ablageorte der ausgebauten Teile zu erinnern. Handschriftliche Notizen sind unpraktisch (dreckige Hände) und fehleranfällig.

**Lösung:** Schritt-für-Schritt-Fotodokumentation mit minimalem Interaktionsaufwand. Jeder Schritt wird sofort persistiert, sodass keine Daten verloren gehen (auch bei App-Unterbrechung durch Anrufe, leeren Akku etc.).

**Primärer Nutzer:** Mechaniker in der Werkstatt, der ein Fahrzeug demontiert und dabei Handschuhe trägt, dreckige Hände hat und schnell arbeiten muss.

**Situation:** Der Mechaniker steht am Fahrzeug, hat gerade ein Bauteil identifiziert, das ausgebaut werden soll, und möchte den aktuellen Zustand dokumentieren bevor er weiterarbeitet.

## User Stories

### US-003.1: Foto aufnehmen und prüfen

**Als** Mechaniker
**möchte ich** ein Foto vom Bauteil aufnehmen und vor dem Speichern prüfen
**damit** nur brauchbare Fotos in der Dokumentation landen und ich unscharfe Aufnahmen wiederholen kann.

#### Akzeptanzkriterien

- **Given** ein Reparaturvorgang existiert und wurde in der Übersicht ausgewählt
  **When** der Mechaniker "Demontage starten" antippt
  **Then** öffnet sich die Kamera-Ansicht mit Vollbild-Vorschau und Auslöser-Button

- **Given** die Kamera-Ansicht ist geöffnet
  **When** der Mechaniker den Auslöser-Button antippt
  **Then** wird das Foto aufgenommen und die Preview-Ansicht mit dem Foto angezeigt

- **Given** die Preview-Ansicht zeigt das aufgenommene Foto
  **When** der Mechaniker "Wiederholen" antippt
  **Then** öffnet sich die Kamera-Ansicht erneut (vorheriges Foto wird verworfen)

- **Given** die Preview-Ansicht zeigt das aufgenommene Foto
  **When** der Mechaniker "Bestätigen" antippt
  **Then** wird das Foto sofort auf dem Filesystem gespeichert (JPEG, mittlere Kompression)
  **And** der Pfad wird in der DB im `Schritt`-Entity hinterlegt

- **Given** die App wurde gerade installiert und Kamera-Permission wurde noch nicht erteilt
  **When** die Kamera-Ansicht geöffnet wird
  **Then** erscheint der Android-System-Dialog zur Kamera-Berechtigung
  **And** die Kamera öffnet sich erst nach Erteilung der Berechtigung

**Hinweis:** Kamera-Permission sollte bereits durch F-001/F-002 implementiert sein. Falls nicht, muss sie hier ergänzt werden.

---

### US-003.2: Nächste Aktion nach Ausbau wählen

**Als** Mechaniker
**möchte ich** nach dem Ausbau wählen, ob ich den Ablageort fotografiere, direkt weitermache oder beende
**damit** ich den Workflow flexibel an die Situation anpasse.

#### Akzeptanzkriterien

- **Given** der Mechaniker hat ein Bauteil-Foto aufgenommen und bestätigt
  **When** die Preview-Ansicht geschlossen wird
  **Then** erscheint ein Screen mit einem großen "Ausgebaut"-Button

- **Given** der "Ausgebaut"-Button wird angezeigt
  **When** der Mechaniker "Ausgebaut" antippt
  **Then** öffnet sich ein Dialog mit 3 Optionen: "Foto von Ablageort", "Weiter ohne Foto", "Beenden"

- **Given** der Dialog wird angezeigt
  **When** der Mechaniker "Foto von Ablageort" wählt
  **Then** öffnet sich die Kamera-Ansicht für das Ablageort-Foto (US-003.3)

- **Given** der Dialog wird angezeigt
  **When** der Mechaniker "Weiter ohne Foto" wählt
  **Then** schließt sich der Dialog
  **And** die Kamera-Ansicht öffnet sich für das nächste Bauteil (US-003.1)
  **And** der Schrittzähler wurde inkrementiert

- **Given** der Dialog wird angezeigt
  **When** der Mechaniker "Beenden" wählt
  **Then** schließt sich die Demontage-Ansicht
  **And** die Vorgangs-Übersicht wird angezeigt (F-001)

#### UI-Verhalten

Der "Ausgebaut"-Button ist ein Platzhalter für die spätere Arbeitsphase (Zeiterfassung, Kommentare, Pausieren). Im MVP ist es nur ein Button, der den Dialog triggert.

---

### US-003.3: Ablageort fotografieren

**Als** Mechaniker
**möchte ich** optional ein Foto vom Ablageort aufnehmen
**damit** ich später nachvollziehen kann, wo das Teil liegt.

#### Akzeptanzkriterien

- **Given** der Mechaniker hat im Dialog "Foto von Ablageort" gewählt
  **When** der Dialog geschlossen wird
  **Then** öffnet sich die Kamera-Ansicht (gleicher Flow wie US-003.1)
  **And** ein UI-Hinweis zeigt "Ablageort fotografieren" (zur Orientierung)

- **Given** die Kamera-Ansicht im Ablageort-Modus ist geöffnet
  **When** der Mechaniker ein Foto aufnimmt und bestätigt
  **Then** wird das Foto sofort auf dem Filesystem gespeichert
  **And** der Pfad wird im selben `Schritt`-Entity unter `ablageortFotoPfad` hinterlegt

- **Given** der Mechaniker hat das Ablageort-Foto bestätigt
  **When** die Preview geschlossen wird
  **Then** öffnet sich die Kamera-Ansicht für das nächste Bauteil (US-003.1)
  **And** der Schrittzähler wurde inkrementiert

- **Given** die Preview des Ablageort-Fotos wird angezeigt
  **When** der Mechaniker "Wiederholen" antippt
  **Then** öffnet sich die Kamera-Ansicht erneut im Ablageort-Modus

---

### US-003.4: Schrittzähler anzeigen

**Als** Mechaniker
**möchte ich** sehen, beim wievielten Schritt ich bin
**damit** ich den Überblick über den Fortschritt behalte.

#### Akzeptanzkriterien

- **Given** der Demontage-Flow ist aktiv
  **When** die Kamera-Ansicht geöffnet ist
  **Then** wird die aktuelle Schrittnummer prominent angezeigt (z.B. "Schritt 3")

- **Given** ein neuer Reparaturvorgang ohne Schritte existiert
  **When** der Demontage-Flow gestartet wird
  **Then** zeigt der Schrittzähler "Schritt 1"

- **Given** der Mechaniker hat einen Schritt abgeschlossen (Bauteil-Foto + Dialog-Auswahl)
  **When** die Kamera für den nächsten Schritt öffnet
  **Then** zeigt der Schrittzähler die um 1 erhöhte Nummer

- **Given** der Mechaniker hat "Weiter ohne Foto" gewählt (kein Ablageort-Foto)
  **When** die Kamera für den nächsten Schritt öffnet
  **Then** ist der Zähler trotzdem inkrementiert

- **Given** der Demontage-Flow ist bei Schritt 5 aktiv
  **When** die App geschlossen und wieder geöffnet wird
  **Then** zeigt der Schrittzähler nach Fortsetzung "Schritt 5" (oder 6 beim nächsten Schritt)

---

### US-003.5: Demontage beenden

**Als** Mechaniker
**möchte ich** die Demontage abschließen
**damit** ich zur Vorgangs-Übersicht zurückkehre und den dokumentierten Vorgang sehe.

#### Akzeptanzkriterien

- **Given** der Dialog nach "Ausgebaut" wird angezeigt
  **When** der Mechaniker "Beenden" wählt
  **Then** schließt sich der Demontage-Flow
  **And** die Vorgangs-Übersicht (F-001) wird angezeigt

- **Given** der Mechaniker hat 5 Schritte dokumentiert und die Demontage beendet
  **When** die Vorgangs-Übersicht angezeigt wird
  **Then** sind alle 5 Schritte mit Fotos sichtbar

- **Given** die Demontage wurde beendet und die Übersicht zeigt 5 Schritte
  **When** der Mechaniker den Vorgang erneut antippt und "Demontage fortsetzen" wählt
  **Then** öffnet sich die Kamera für Schritt 6
  **And** der Schrittzähler zeigt "Schritt 6"

---

## Nicht-funktionale Anforderungen

**Performance (Quality Goal #3):**
- Kamera muss sofort auslösen, keine Verzögerung >500ms
- Foto-Speicherung darf UI nicht blockieren (async)

**Zuverlässigkeit (Quality Goal #2):**
- Jedes Foto wird sofort auf dem Filesystem gespeichert
- Jeder Schritt wird sofort in der DB persistiert (Sofort-Save)
- App-Unterbrechung (Anruf, Home-Button, Akku leer) darf keine Daten verlieren

**Bedienbarkeit (Quality Goal #1):**
- Minimale Interaktion: Foto → Bestätigen → Weiter (im Regelfall nur 2 Taps pro Schritt)
- Große Touch-Targets für "Ausgebaut"-Button und Auslöser (dreckige Hände, Handschuhe)
- Foto-Qualität: Mittlere Kompression, ~2-3 MB pro Foto (Balance zwischen Qualität und Speicher)

**Datensicherheit:**
- Fotos werden im app-internen Speicher abgelegt (nicht in der öffentlichen Galerie)
- Keine Metadaten (GPS, Zeitstempel) werden in EXIF-Daten geschrieben (Datenschutz)

---

## Technische Hinweise

### Entities

**Schritt (Room Entity):**
```kotlin
@Entity(tableName = "schritte")
data class Schritt(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val reparaturvorgangId: Long,

    val sequenzNummer: Int,              // Schrittzähler (1, 2, 3, ...)

    val bauteilFotoPfad: String,         // Pfad zum Bauteil-Foto (Pflicht)
    val ablageortFotoPfad: String? = null, // Pfad zum Ablageort-Foto (Optional)

    val startedAt: Long,                 // Timestamp: Schritt begonnen
    val completedAt: Long                // Timestamp: Schritt abgeschlossen
)
```

### Kamera-Integration

- **CameraX:** `ImageCapture` Use Case für Fotoaufnahme
- **Preview:** `PreviewView` für Vollbild-Vorschau
- **Foto-Speicherung:** App-interner Speicher (`context.filesDir/photos/`), JPEG mit Quality 85

### Foto-Flow Logik

1. **Foto aufnehmen:** Kamera → `takePicture()` → Datei im internen Speicher
2. **Preview anzeigen:** Bild laden, in Preview-Composable darstellen
3. **Wiederholen:** Datei löschen, zurück zu Schritt 1
4. **Bestätigen:**
   - Schritt-Entity in DB anlegen (mit `bauteilFotoPfad`)
   - `sequenzNummer` = letzte Nummer + 1
   - `startedAt` = aktueller Timestamp
   - Weiter zu "Ausgebaut"-Screen

### Schrittzähler-Logik

- `sequenzNummer` wird beim Anlegen des `Schritt`-Entity gesetzt
- Wird von der DB geladen beim Fortsetzen (letzte `sequenzNummer` + 1)
- Unabhängig davon ob Ablageort-Foto gemacht wurde

### Navigation

- **Kamera → Preview → Ausgebaut-Screen → Dialog → (Kamera Ablageort) → Kamera nächstes Bauteil**
- Navigation über `NavController` (Jetpack Navigation)
- State Hoisting: ViewModel hält State, Screens sind stateless

### Sofort-Save Strategie

- Jedes Foto wird sofort geschrieben (kein Batch-Save am Ende)
- Jeder `Schritt` wird sofort in DB inserted/updated
- Bei App-Unterbrechung: Letzter persistierter Schritt ist wiederherstellbar

---

## Offene Fragen

- [x] **Ablageort-Foto vs. Ablageort-Nummer:** Entschieden - nur Foto, keine Nummer im MVP. Zuordnung erfolgt analog (Zettel) oder später per Foto-Abgleich.
- [x] **Arbeitsphase-Umfang:** Entschieden - MVP nur "Ausgebaut"-Button, Erweiterung kommt später in separater Spec.
- [ ] **OFFEN:** Soll der Mechaniker im Ablageort-Modus ein bereits aufgenommenes Ablageort-Foto wiederverwenden können (z.B. mehrere Teile in derselben Kiste)? → Für MVP: Nein, jedes Mal neues Foto. Später: Foto-Vorschläge aus Historie.
- [ ] **OFFEN:** UI-Hinweis "Ablageort fotografieren" - welche Darstellung? Toast, Banner oder in der Top-Bar? → UX-Entscheidung, nicht kritisch für MVP.
