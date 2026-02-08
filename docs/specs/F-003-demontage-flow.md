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
  - App schlägt automatisch die nächste Ablageort-Nummer vor (sequentiell hochzählend ab 1)
  - Bereits belegte Ablageort-Nummern werden **übersprungen** (z.B. wenn Platz 5 manuell gewählt wurde, wird er später beim Hochzählen übersprungen)
  - Vorgeschlagene Nummer wird groß angezeigt
  - **Bestätigen-Button**: Übernimmt den Vorschlag, speichert Schritt sofort in DB, öffnet Kamera für nächsten Schritt
  - **Ändern-Button**: Öffnet Freitext-Feld mit Numpad als Default-Tastatur, Mechaniker kann abweichende Nummer eingeben
  - **Manuelle Auswahl bricht Sequenz nicht**: Wenn der Mechaniker manuell einen anderen Ablageort wählt (z.B. 5 statt 2), springt der Auto-Vorschlag danach auf die nächste Nummer in der Sequenz zurück (3), nicht auf 6
  - **Alle Ablageorte belegt**: Wenn alle Nummern 1..max vergeben sind, erscheint ein Dialog:
    - **"Beenden"**: Demontage abschließen, zurück zur Übersicht
    - **"+1 Erweitern"**: Weiter mit Nummern über max hinaus (max+1, max+2, ...)
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
- [ ] Ablageort-Nummer zählt automatisch hoch (1, 2, 3, ...) und überspringt belegte
- [ ] Manuelle Ablageort-Wahl bricht die Sequenz nicht (Vorschlag springt zurück)
- [ ] Wenn alle Ablageorte belegt: Dialog mit "Beenden" oder "+1 Erweitern"
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
                    Nächste freie Nummer?
                      ├── JA ──────────┐
                      │                ▼
                      │         ┌──────────────┐
                      │         │ Kamera-       │
                      │         │ Vorschau      │
                      │         │ Schritt: 6   │
                      │         └──────────────┘
                      │
                      └── NEIN (alle belegt)
                                │
                         ┌──────────────┐
                         │ Alle Ablage-  │
                         │ orte belegt!  │
                         │               │
                         │ [Beenden]     │
                         │ [+1 Erweitern]│
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
- Auto-Vorschlag: `naechsteFreieNummer(sequenzCounter, belegteNummern, max)` — sucht ab Counter aufwärts die erste freie Nummer, überspringt belegte. Wenn alle belegt → Dialog.
- Sequenz-Counter: Zählt nach jedem Schritt +1 (unabhängig ob auto oder manuell gewählt), Wrap bei max.
- Belegte Nummern: Set aller bereits vergebenen Ablageort-Nummern (aus DB geladen beim Start).
- Zeitstempel: `startedAt` = Kamera öffnet sich, `completedAt` = Ablageort bestätigt
