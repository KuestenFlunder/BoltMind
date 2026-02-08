# F-003: Demontage-Flow

## Beschreibung

Der Kernflow der App. Der Mechaniker dokumentiert den Auseinanderbau Schritt für Schritt: Foto aufnehmen, Ablageort-Nummer eingeben, nächster Schritt. Jeder Schritt wird sofort persistiert. Die Zeiterfassung läuft im Hintergrund mit (siehe F-005).

## Anforderungen

### Funktional

- **Kamera-Ansicht** als Hauptelement des Screens
  - Vollbild-Kameravorschau (CameraX PreviewView)
  - Großer Auslöser-Button
  - Foto wird sofort auf dem Filesystem gespeichert
- **Ablageort-Bestätigung** nach Fotoaufnahme
  - App schlägt automatisch die nächste Ablageort-Nummer vor (hochzählend ab 1, Wrap-Around bei Erreichen der max. Ablageorte)
  - Vorgeschlagene Nummer wird groß angezeigt
  - **Bestätigen-Button**: Übernimmt den Vorschlag, speichert Schritt sofort in DB, öffnet Kamera für nächsten Schritt
  - **Ändern-Button**: Öffnet Freitext-Feld mit Numpad als Default-Tastatur, Mechaniker kann abweichende Nummer eingeben
- **Schritt-Zähler** zeigt aktuelle Schrittnummer an
- **Vorschau** des zuletzt aufgenommenen Fotos (klein, im Hintergrund)
- **Demontage beenden** Button zum Abschließen (zurück zur Übersicht)
- **Pause/Fortsetzen**: Mechaniker kann jederzeit die App verlassen und später weitermachen (Sofort-Save)

### Nicht-Funktional

- Kamera muss sofort auslösen, keine Verzögerung (Quality Goal #3)
- Jedes Foto wird sofort auf dem Filesystem gespeichert (Quality Goal #2)
- Jeder Schritt wird sofort in der DB persistiert (Quality Goal #2)
- Minimale Interaktion: Foto → Bestätigen → weiter (im Regelfall nur 1 Tap für Ablageort) (Quality Goal #1)
- Foto-Qualität: Mittlere Kompression, ~2-3 MB pro Foto

## Akzeptanzkriterien

- [ ] Kamera-Vorschau wird im Vollbild angezeigt
- [ ] Foto wird per Button ausgelöst und sofort gespeichert
- [ ] Nach Foto: Vorgeschlagene Ablageort-Nummer wird groß angezeigt
- [ ] Bestätigen-Button übernimmt Vorschlag und speichert Schritt in DB
- [ ] Ändern-Button öffnet Freitext-Feld mit Numpad
- [ ] Ablageort-Nummer zählt automatisch hoch (1, 2, 3, ... Wrap-Around)
- [ ] Nach Bestätigung öffnet sich automatisch die Kamera für nächsten Schritt
- [ ] Schrittzähler zeigt aktuelle Nummer
- [ ] Demontage kann beendet werden (zurück zur Übersicht)
- [ ] App-Unterbrechung (Anruf, Home-Button) verliert keine Daten
- [ ] Kamera-Permission wird beim ersten Start abgefragt

## Flow-Diagramm

```
┌──────────────┐
│ Kamera-       │
│ Vorschau      │──── [Foto aufnehmen]
│               │           │
│  Schritt: 5   │           ▼
│  [Beenden]    │    ┌──────────────┐
└──────────────┘    │ Foto-Preview  │
                    │               │
                    │ Ablageort: [7]│
                    │               │
                    │ [✓ BESTÄTIGEN]│
                    │ [✎ ÄNDERN]    │
                    └──────┬───────┘
                           │
                    Sofort-Save in DB
                           │
                           ▼
                    ┌──────────────┐
                    │ Kamera-       │
                    │ Vorschau      │
                    │ Schritt: 6   │
                    └──────────────┘
```

## UI-Skizze

### Kamera-Ansicht
```
┌─────────────────────────────┐
│  ← Bremsen vorne    Schritt 5│
├─────────────────────────────┤
│                             │
│                             │
│      [ Kamera-Vorschau ]    │
│                             │
│                             │
│                             │
│                             │
│  [Letztes Foto]        ◉   │
│                    [Beenden]│
└─────────────────────────────┘
```

### Ablageort-Eingabe (nach Foto)
```
┌─────────────────────────────┐
│  ← Bremsen vorne    Schritt 5│
├─────────────────────────────┤
│                             │
│   ┌─────────────────────┐   │
│   │   [Foto-Preview]    │   │
│   └─────────────────────┘   │
│                             │
│   Ablageort                  │
│   ┌─────────────────────┐   │
│   │         7            │   │
│   └─────────────────────┘   │
│   (auto-vorgeschlagen)      │
│                             │
│   ┌─────────────────────┐   │
│   │   ✓  BESTÄTIGEN      │   │
│   └─────────────────────┘   │
│   ┌─────────────────────┐   │
│   │   ✎  ÄNDERN          │   │
│   └─────────────────────┘   │
└─────────────────────────────┘
```

## Technische Hinweise

- CameraX: `ImageCapture` Use Case für Fotoaufnahme
- Foto-Speicherung: App-interner Speicher, JPEG mit mittlerer Kompression
- Room Entity: `Step(id, repairJobId, photoPath, storageLocationNumber, sequenceNumber, startedAt, completedAt)`
- Sofort-Insert: Foto wird gespeichert → Step-Eintrag in DB mit `photoPath` → Ablageort wird per Update ergänzt
- Auto-Vorschlag: `nextLocation = (lastLocation % repairJob.storageLocationCount) + 1`
- Zeitstempel: `startedAt` = Kamera öffnet sich, `completedAt` = Ablageort bestätigt
