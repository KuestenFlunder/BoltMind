# Preview-View (Foto-Aufnahme + Vorschau)

## Zweck

Zentraler Screen fuer die Foto-Aufnahme im Demontage-Flow. Startet die System-Kamera automatisch beim Betreten, zeigt die Foto-Vorschau und bietet Bestaetigen/Wiederholen. Bei Kamera-Abbruch zeigt die View ein Platzhalter-Bild mit "Foto aufnehmen"-Button. Existiert in zwei Modi: **Bauteil-Modus** (Standard) und **Ablageort-Modus** (mit Hinweis-Banner).

## UI-Elemente

### Eintritts-Verhalten (Kamera-Auto-Start)

Beim Betreten der Preview-View wird die System-Kamera automatisch per `ActivityResultContracts.TakePicture()` Intent gestartet. Es ist kein manueller Button-Tap noetig. Die Schrittnummer ist erst nach Foto-Aufnahme im Vorschau-Zustand sichtbar.

### Nach Kamera-Abbruch (Abgebrochen-Zustand)

- **Schrittnummer** (prominent, z.B. "Schritt 3")
- **Platzhalter-Bild** (grauer Bereich mit Kamera-Icon)
- **"Foto aufnehmen"-Button** (gross, fuer Handschuhe geeignet -- startet System-Kamera erneut)
- **Ablageort-Hinweis** (nur im Ablageort-Modus): Text "Jetzt den Ablageort fotografieren"

### Nach Foto-Aufnahme (Vorschau-Zustand)

- **Schrittnummer** (prominent, z.B. "Schritt 3")
- **Vollbild-Fotovorschau** des aufgenommenen Bildes
- **Ablageort-Banner** (nur im Ablageort-Modus): Frage "Ist das der Ablageort?" ueber der Vorschau
- **Button "Bestaetigen"** -- Foto uebernehmen
- **Button "Wiederholen"** -- Foto verwerfen, System-Kamera erneut oeffnen

## Verhalten + DB-Interaktion

### Aus US-003.1 AK 1: System-Kamera startet automatisch beim Flow-Start

- **Given** ein Reparaturvorgang existiert und ist im Status OFFEN
  **When** der Mechaniker den Demontage-Flow startet (ueber Uebersicht oder direkt nach Anlage)
  **Then** wird ein neuer `Schritt` in der DB angelegt mit `gestartetAm` = aktueller Timestamp, `bauteilFotoPfad = null`, `typ = null`
  **And** die System-Kamera wird automatisch per Intent gestartet (kein manueller Button-Tap)

### Aus US-003.1 AK 2: Foto aufgenommen per System-Kamera

- **Given** die System-Kamera wurde automatisch gestartet (oder ueber "Foto aufnehmen"-Button nach Abbruch)
  **When** der Mechaniker ein Foto aufnimmt
  **Then** zeigt die Preview-View das aufgenommene Foto im Vorschau-Zustand mit der Schrittnummer, Bestaetigen- und Wiederholen-Buttons

### Aus US-003.1 AK 3: Wiederholen (Bauteil-Foto)

- **Given** die Preview-View zeigt das aufgenommene Foto im Vorschau-Zustand
  **When** der Mechaniker "Wiederholen" antippt
  **Then** wird die temporaere Foto-Datei geloescht
  **And** die System-Kamera wird erneut per Intent geoeffnet (direkt, kein Zwischenscreen)

### Aus US-003.1 AK 4: Bestaetigen (Bauteil-Foto)

- **Given** die Preview-View zeigt das aufgenommene Foto im Vorschau-Zustand
  **When** der Mechaniker "Bestaetigen" antippt
  **Then** wird das Foto von `photos/temp/` nach `photos/` verschoben
  **And** der `bauteilFotoPfad` im bestehenden `Schritt`-Entity wird aktualisiert

### Aus US-003.1 AK 5: Keine Kamera-App verfuegbar

- **Given** auf dem Geraet ist keine App installiert, die den `TakePicture()`-Intent bedienen kann
  **When** die Preview-View die System-Kamera automatisch starten will
  **Then** erscheint ein Hinweis-Dialog "Keine Kamera-App gefunden"
  **And** die Preview-View zeigt den Abgebrochen-Zustand (Platzhalter + Button)

### Aus US-003.1 AK 6: System-Kamera wird abgebrochen

- **Given** die System-Kamera wurde gestartet (automatisch oder ueber Button)
  **When** der Mechaniker die System-Kamera abbricht (Back-Taste oder Abbrechen)
  **Then** zeigt die Preview-View den Abgebrochen-Zustand: Schrittnummer + Platzhalter-Bild + "Foto aufnehmen"-Button
  **And** kein Foto wird gespeichert

### Aus US-003.3 AK 1: Ablageort-Modus mit Banner

- **Given** der Mechaniker hat im Dialog "Ablageort fotografieren" gewaehlt
  **When** die Preview-View im Ablageort-Modus betreten wird
  **Then** startet die System-Kamera automatisch per Intent
  **And** nach Foto-Aufnahme zeigt die Preview-View die Frage "Ist das der Ablageort?" als Banner ueber der Vorschau
  **And** die aktuelle Schrittnummer bleibt sichtbar

### Aus US-003.3 AK 2: Ablageort-Modus Kamera-Abbruch

- **Given** die System-Kamera wurde im Ablageort-Modus gestartet
  **When** der Mechaniker die Kamera abbricht
  **Then** zeigt die Preview-View den Abgebrochen-Zustand mit Ablageort-Hinweis "Jetzt den Ablageort fotografieren" + Platzhalter-Bild + "Foto aufnehmen"-Button

### Aus US-003.3 AK 3: Wiederholen (Ablageort-Foto)

- **Given** die Preview des Ablageort-Fotos wird im Vorschau-Zustand angezeigt
  **When** der Mechaniker "Wiederholen" antippt
  **Then** wird die temporaere Foto-Datei geloescht
  **And** die System-Kamera wird erneut per Intent geoeffnet (direkt, kein Zwischenscreen)

### Aus US-003.3 AK 4: Bestaetigen (Ablageort-Foto)

- **Given** der Mechaniker hat das Ablageort-Foto aufgenommen und die Preview zeigt es
  **When** der Mechaniker "Bestaetigen" antippt
  **Then** wird das Foto von `photos/temp/` nach `photos/` verschoben
  **And** der Pfad wird im selben `Schritt`-Entity unter `ablageortFotoPfad` hinterlegt
  **And** `abgeschlossenAm` wird im `Schritt`-Entity gesetzt

### Aus US-003.4 AK 1: Schrittnummer in Preview

- **Given** der Demontage-Flow ist aktiv
  **When** die Preview-View angezeigt wird (Vorschau- oder Abgebrochen-Zustand)
  **Then** wird die aktuelle Schrittnummer prominent angezeigt (z.B. "Schritt 3")

### Aus US-003.4 AK 3: Schrittnummer bei neuem Vorgang

- **Given** ein neuer Reparaturvorgang ohne Schritte existiert
  **When** der Demontage-Flow gestartet wird und die System-Kamera automatisch startet
  **Then** zeigt der Vorschau-Zustand nach Foto-Aufnahme "Schritt 1"

## DB-Interaktion

| Aktion | Modus | DB-Operation |
|--------|-------|-------------|
| Preview wird betreten (Bauteil-Modus) | Bauteil | Neuen `Schritt` in DB anlegen: `schrittNummer`, `gestartetAm`, `bauteilFotoPfad = null`, `typ = null`, System-Kamera automatisch starten |
| Foto aufnehmen | Beide | System-Kamera -> Foto in `photos/temp/` (noch kein DB-Update) |
| Kamera abgebrochen | Beide | Kein DB-Update, Abgebrochen-Zustand anzeigen |
| Bestaetigen | Bauteil | Datei von `photos/temp/` nach `photos/` verschieben, `bauteilFotoPfad` im `Schritt`-Entity setzen |
| Bestaetigen | Ablageort | Datei von `photos/temp/` nach `photos/` verschieben, `ablageortFotoPfad` im `Schritt`-Entity setzen, `abgeschlossenAm` setzen |
| Wiederholen | Beide | Temporaere Foto-Datei loeschen (kein DB-Update) |

## Nicht-funktionale Anforderungen

- **Foto-Speicherung darf UI nicht blockieren** (async)
- **Debounce:** 300ms fuer Bestaetigen-, Wiederholen- und "Foto aufnehmen"-Buttons (Handschuhe, Doppel-Tap-Schutz)
- **Foto-Qualitaet:** Mittlere Kompression, ~2-3 MB pro Foto (Balance zwischen Qualitaet und Speicher)

## Technische Hinweise

- **System-Kamera:** `ActivityResultContracts.TakePicture()` -- KEIN CameraX
- **Auto-Start:** Kamera-Intent wird automatisch beim Composable-Eintritt ausgeloest (z.B. via `LaunchedEffect`), nicht durch Button-Tap
- **Foto-Speicherung:** System-Kamera speichert in uebergebene URI (`photos/temp/`), bei Bestaetigung nach `photos/` verschieben
- **Datei-Verschiebung:** `File.renameTo()` von `photos/temp/` nach `photos/` (rename, kein Copy)
- **Modus-Parameter:** Bauteil vs. Ablageort -- bestimmt UI-Elemente (Banner), welches DB-Feld (`bauteilFotoPfad` oder `ablageortFotoPfad`) aktualisiert wird
- **Intent-Fehlerbehandlung:** `resolveActivity()` pruefen bevor Intent gestartet wird; Fallback auf Abgebrochen-Zustand bei fehlender Kamera-App
- **Abgebrochen-Zustand:** Zeigt Platzhalter-Bild + "Foto aufnehmen"-Button, dient als Fallback nach Kamera-Cancel oder bei fehlender Kamera-App
