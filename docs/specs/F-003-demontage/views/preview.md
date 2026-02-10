# Preview-View (Foto-Aufnahme + Vorschau)

## Zweck

Zentraler Screen fuer die Foto-Aufnahme im Demontage-Flow. Zeigt die Schrittnummer, ruft die System-Kamera per Intent auf, zeigt die Foto-Vorschau und bietet Bestaetigen/Wiederholen. Existiert in zwei Modi: **Bauteil-Modus** (Standard) und **Ablageort-Modus** (mit Hinweis-Banner).

## UI-Elemente

### Vor Foto-Aufnahme (Initial-Zustand)

- **Schrittnummer** (prominent, z.B. "Schritt 3")
- **"Foto aufnehmen"-Button** (gross, fuer Handschuhe geeignet -- oeffnet System-Kamera)
- **Ablageort-Hinweis** (nur im Ablageort-Modus): Text "Jetzt den Ablageort fotografieren"

### Nach Foto-Aufnahme (Vorschau-Zustand)

- **Schrittnummer** (weiterhin sichtbar)
- **Vollbild-Fotovorschau** des aufgenommenen Bildes
- **Ablageort-Banner** (nur im Ablageort-Modus): Frage "Ist das der Ablageort?" ueber der Vorschau
- **Button "Bestaetigen"** -- Foto uebernehmen
- **Button "Wiederholen"** -- Foto verwerfen, System-Kamera erneut oeffnen

## Verhalten + DB-Interaktion

### Aus US-003.1 AK 1: Preview oeffnet sich beim Flow-Start

- **Given** ein Reparaturvorgang existiert und ist im Status OFFEN
  **When** der Mechaniker den Demontage-Flow startet (ueber Uebersicht oder direkt nach Anlage)
  **Then** oeffnet sich die Preview-View im Initial-Zustand mit Schrittnummer und "Foto aufnehmen"-Button
  **And** ein neuer `Schritt` wird in der DB angelegt mit `gestartetAm` = aktueller Timestamp, `bauteilFotoPfad = null`, `typ = null`

### Aus US-003.1 AK 2: Foto aufnehmen per System-Kamera

- **Given** die Preview-View ist im Initial-Zustand
  **When** der Mechaniker den "Foto aufnehmen"-Button antippt
  **Then** wird die System-Kamera per `ActivityResultContracts.TakePicture()` Intent geoeffnet
  **And** nach Rueckkehr zeigt die Preview-View das aufgenommene Foto im Vorschau-Zustand

### Aus US-003.1 AK 3: Wiederholen (Bauteil-Foto)

- **Given** die Preview-View zeigt das aufgenommene Foto im Vorschau-Zustand
  **When** der Mechaniker "Wiederholen" antippt
  **Then** wird die temporaere Foto-Datei geloescht
  **And** die System-Kamera wird erneut per Intent geoeffnet

### Aus US-003.1 AK 4: Bestaetigen (Bauteil-Foto)

- **Given** die Preview-View zeigt das aufgenommene Foto im Vorschau-Zustand
  **When** der Mechaniker "Bestaetigen" antippt
  **Then** wird das Foto von `photos/temp/` nach `photos/` verschoben
  **And** der `bauteilFotoPfad` im bestehenden `Schritt`-Entity wird aktualisiert

### Aus US-003.1 AK 5: Keine Kamera-App verfuegbar

- **Given** auf dem Geraet ist keine App installiert, die den `TakePicture()`-Intent bedienen kann
  **When** der "Foto aufnehmen"-Button getippt wird
  **Then** erscheint ein Hinweis-Dialog "Keine Kamera-App gefunden"
  **And** die Preview-View bleibt im Initial-Zustand

### Aus US-003.1 AK 6: System-Kamera wird abgebrochen

- **Given** die System-Kamera wurde per Intent geoeffnet
  **When** der Mechaniker die System-Kamera abbricht (Back-Taste oder Abbrechen)
  **Then** kehrt die App zur Preview-View im Initial-Zustand zurueck
  **And** kein Foto wird gespeichert

### Aus US-003.3 AK 1: Ablageort-Modus mit Banner

- **Given** der Mechaniker hat im Dialog "Ablageort fotografieren" gewaehlt
  **When** die Preview-View im Ablageort-Modus geoeffnet wird
  **Then** zeigt die Preview-View den Hinweis "Jetzt den Ablageort fotografieren" im Initial-Zustand
  **And** nach Foto-Aufnahme zeigt die Preview-View die Frage "Ist das der Ablageort?" als Banner ueber der Vorschau
  **And** die aktuelle Schrittnummer bleibt sichtbar

### Aus US-003.3 AK 3: Wiederholen (Ablageort-Foto)

- **Given** die Preview des Ablageort-Fotos wird im Vorschau-Zustand angezeigt
  **When** der Mechaniker "Wiederholen" antippt
  **Then** wird die temporaere Foto-Datei geloescht
  **And** die System-Kamera wird erneut per Intent im Ablageort-Modus geoeffnet

### Aus US-003.3 AK 4: Bestaetigen (Ablageort-Foto)

- **Given** der Mechaniker hat das Ablageort-Foto aufgenommen und die Preview zeigt es
  **When** der Mechaniker "Bestaetigen" antippt
  **Then** wird das Foto von `photos/temp/` nach `photos/` verschoben
  **And** der Pfad wird im selben `Schritt`-Entity unter `ablageortFotoPfad` hinterlegt
  **And** `abgeschlossenAm` wird im `Schritt`-Entity gesetzt

### Aus US-003.4 AK 1: Schrittnummer in Preview

- **Given** der Demontage-Flow ist aktiv
  **When** die Preview-View angezeigt wird (Initial- oder Vorschau-Zustand)
  **Then** wird die aktuelle Schrittnummer prominent angezeigt (z.B. "Schritt 3")

### Aus US-003.4 AK 3: Schrittnummer bei neuem Vorgang

- **Given** ein neuer Reparaturvorgang ohne Schritte existiert
  **When** der Demontage-Flow gestartet wird
  **Then** zeigt der Schrittzaehler "Schritt 1"

## DB-Interaktion

| Aktion | Modus | DB-Operation |
|--------|-------|-------------|
| Preview oeffnet sich (Bauteil-Modus) | Bauteil | Neuen `Schritt` in DB anlegen: `schrittNummer`, `gestartetAm`, `bauteilFotoPfad = null`, `typ = null` |
| Foto aufnehmen | Beide | System-Kamera -> Foto in `photos/temp/` (noch kein DB-Update) |
| Bestaetigen | Bauteil | Datei von `photos/temp/` nach `photos/` verschieben, `bauteilFotoPfad` im `Schritt`-Entity setzen |
| Bestaetigen | Ablageort | Datei von `photos/temp/` nach `photos/` verschieben, `ablageortFotoPfad` im `Schritt`-Entity setzen, `abgeschlossenAm` setzen |
| Wiederholen | Beide | Temporaere Foto-Datei loeschen (kein DB-Update) |

## Nicht-funktionale Anforderungen

- **Foto-Speicherung darf UI nicht blockieren** (async)
- **Debounce:** 300ms fuer Bestaetigen-, Wiederholen- und "Foto aufnehmen"-Buttons (Handschuhe, Doppel-Tap-Schutz)
- **Foto-Qualitaet:** Mittlere Kompression, ~2-3 MB pro Foto (Balance zwischen Qualitaet und Speicher)

## Technische Hinweise

- **System-Kamera:** `ActivityResultContracts.TakePicture()` -- KEIN CameraX
- **Foto-Speicherung:** System-Kamera speichert in uebergebene URI (`photos/temp/`), bei Bestaetigung nach `photos/` verschieben
- **Datei-Verschiebung:** `File.renameTo()` von `photos/temp/` nach `photos/` (rename, kein Copy)
- **Modus-Parameter:** Bauteil vs. Ablageort -- bestimmt UI-Elemente (Banner), welches DB-Feld (`bauteilFotoPfad` oder `ablageortFotoPfad`) aktualisiert wird, und den Initial-Hinweistext
- **Intent-Fehlerbehandlung:** `resolveActivity()` pruefen bevor Intent gestartet wird; Fallback-Dialog bei fehlender Kamera-App
