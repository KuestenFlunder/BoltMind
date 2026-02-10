# Kamera-View

## Zweck

Vollbild-Kameravorschau mit Ausloeser-Button zur Aufnahme von Bauteil- und Ablageort-Fotos. Die View existiert in zwei Modi: **Bauteil-Modus** (Standard) und **Ablageort-Modus** (mit Banner-Hinweis).

## UI-Elemente

- **Vollbild-Kameravorschau** (CameraX `PreviewView`)
- **Ausloeser-Button** (gross, fuer Handschuhe geeignet)
- **Schrittnummer-Anzeige** (prominent, z.B. "Schritt 3") -- in beiden Modi sichtbar
- **Ablageort-Banner** (nur im Ablageort-Modus): "Ablageort fotografieren" oberhalb der Kamera-Vorschau

## Verhalten + DB-Interaktion

### Aus US-003.1 AK 1: Kamera oeffnet sich mit Vollbild-Vorschau

- **Given** ein Reparaturvorgang existiert und ist im Status OFFEN
  **When** der Mechaniker den Demontage-Flow startet (ueber Uebersicht oder direkt nach Anlage)
  **Then** oeffnet sich die Kamera-Ansicht mit Vollbild-Vorschau, Ausloeser-Button und Schrittnummer
  **And** ein neuer `Schritt` wird in der DB angelegt mit `gestartetAm` = aktueller Timestamp, `bauteilFotoPfad = null`, `typ = null`

### Aus US-003.1 AK 2: Foto aufnehmen

- **Given** die Kamera-Ansicht ist geoeffnet
  **When** der Mechaniker den Ausloeser-Button antippt
  **Then** wird das Foto aufgenommen und die Preview-Ansicht mit dem Foto angezeigt
  **And** der Ausloeser-Button ist fuer 300ms deaktiviert (Debounce gegen Doppel-Tap)

### Aus US-003.1 AK 5: Permission nicht erteilt

- **Given** die App wurde gerade installiert und Kamera-Permission wurde noch nicht erteilt
  **When** die Kamera-Ansicht geoeffnet wird
  **Then** erscheint der Android-System-Dialog zur Kamera-Berechtigung
  **And** die Kamera oeffnet sich erst nach Erteilung der Berechtigung

**Hinweis:** Kamera-Permission sollte bereits durch F-002 implementiert sein. Falls nicht, muss sie hier ergaenzt werden.

### Aus US-003.1 AK 6: Permission dauerhaft abgelehnt

- **Given** die Kamera-Permission wurde dauerhaft abgelehnt ("Nicht mehr fragen")
  **When** die Kamera-Ansicht geoeffnet werden soll
  **Then** erscheint ein Hinweis-Screen mit Erklaerung und Button "Zu Einstellungen"
  **And** der Button oeffnet die Android App-Einstellungen

### Aus US-003.3 AK 1: Ablageort-Modus mit Banner

- **Given** der Mechaniker hat im Dialog "Ablageort fotografieren" gewaehlt
  **When** der Dialog geschlossen wird
  **Then** oeffnet sich die Kamera-Ansicht (gleicher Flow wie US-003.1)
  **And** ein UI-Hinweis zeigt "Ablageort fotografieren" als Banner oberhalb der Kamera-Vorschau
  **And** die aktuelle Schrittnummer bleibt sichtbar

### Aus US-003.4 AK 1: Schrittnummer in Kamera

- **Given** der Demontage-Flow ist aktiv
  **When** die Kamera-Ansicht geoeffnet ist
  **Then** wird die aktuelle Schrittnummer prominent angezeigt (z.B. "Schritt 3")

### Aus US-003.4 AK 3: Schrittnummer bei neuem Vorgang

- **Given** ein neuer Reparaturvorgang ohne Schritte existiert
  **When** der Demontage-Flow gestartet wird
  **Then** zeigt der Schrittzaehler "Schritt 1"

## DB-Interaktion

| Aktion | DB-Operation |
|--------|-------------|
| Kamera oeffnet sich (Bauteil-Modus) | Neuen `Schritt` in DB anlegen: `schrittNummer`, `gestartetAm`, `bauteilFotoPfad = null`, `typ = null` |
| Foto aufnehmen | `takePicture()` -> temporaere Datei in `photos/temp/` (noch kein DB-Update) |

## Nicht-funktionale Anforderungen

- **Kamera-Performance:** Kamera muss sofort ausloesen, keine Verzoegerung >500ms
- **Debounce:** 300ms fuer Ausloeser-Button (Handschuhe, Doppel-Tap-Schutz)
- **Kamera-Fehler:** Fehler-Dialog mit "Erneut versuchen"-Button bei Kamera-Fehler
- **Speicher-voll-Fehler:** Fehler-Dialog "Nicht genug Speicher. Bitte Speicherplatz freigeben." bevor Foto aufgenommen wird

## Technische Hinweise

- **CameraX:** `ImageCapture` Use Case fuer Fotoaufnahme
- **Preview:** `PreviewView` fuer Vollbild-Vorschau
- **Foto-Speicherung:** Temporaere Aufnahmen in `context.filesDir/photos/temp/` (JPEG mit Quality 85)
- **Modus-Parameter:** Bauteil vs. Ablageort (bestimmt Banner-Anzeige und welches DB-Feld spaeter aktualisiert wird)
