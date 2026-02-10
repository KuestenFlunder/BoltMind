# Preview-View

## Zweck

Foto-Vorschau mit den Optionen "Bestaetigen" und "Wiederholen". Die View wird sowohl fuer Bauteil-Fotos als auch fuer Ablageort-Fotos verwendet -- ein Modus-Parameter steuert, welches DB-Feld aktualisiert wird.

## UI-Elemente

- **Vollbild-Fotovorschau** des aufgenommenen Bildes
- **Button "Bestaetigen"** -- Foto uebernehmen
- **Button "Wiederholen"** -- Foto verwerfen und Kamera erneut oeffnen

## Verhalten + DB-Interaktion

### Aus US-003.1 AK 3: Wiederholen (Bauteil-Foto)

- **Given** die Preview-Ansicht zeigt das aufgenommene Foto
  **When** der Mechaniker "Wiederholen" antippt
  **Then** oeffnet sich die Kamera-Ansicht erneut (temporaere Foto-Datei wird geloescht)

### Aus US-003.1 AK 4: Bestaetigen (Bauteil-Foto)

- **Given** die Preview-Ansicht zeigt das aufgenommene Foto
  **When** der Mechaniker "Bestaetigen" antippt
  **Then** wird das Foto von `photos/temp/` nach `photos/` verschoben (JPEG, mittlere Kompression)
  **And** der `bauteilFotoPfad` im bestehenden `Schritt`-Entity wird aktualisiert

### Aus US-003.3 AK 3: Wiederholen (Ablageort-Foto)

- **Given** die Preview des Ablageort-Fotos wird angezeigt
  **When** der Mechaniker "Wiederholen" antippt
  **Then** oeffnet sich die Kamera-Ansicht erneut im Ablageort-Modus

### Aus US-003.3 AK 4: Bestaetigen (Ablageort-Foto)

- **Given** der Mechaniker hat das Ablageort-Foto aufgenommen und die Preview zeigt es
  **When** der Mechaniker "Bestaetigen" antippt
  **Then** wird das Foto von `photos/temp/` nach `photos/` verschoben
  **And** der Pfad wird im selben `Schritt`-Entity unter `ablageortFotoPfad` hinterlegt
  **And** `abgeschlossenAm` wird im `Schritt`-Entity gesetzt

## DB-Interaktion

| Aktion | Modus | DB-Operation |
|--------|-------|-------------|
| Bestaetigen | Bauteil | Datei von `photos/temp/` nach `photos/` verschieben, `bauteilFotoPfad` im `Schritt`-Entity setzen |
| Bestaetigen | Ablageort | Datei von `photos/temp/` nach `photos/` verschieben, `ablageortFotoPfad` im `Schritt`-Entity setzen, `abgeschlossenAm` setzen |
| Wiederholen | Beide | Temporaere Foto-Datei loeschen (kein DB-Update) |

## Nicht-funktionale Anforderungen

- **Foto-Speicherung darf UI nicht blockieren** (async)
- **Debounce:** 300ms fuer Bestaetigen- und Wiederholen-Buttons
- **Foto-Qualitaet:** Mittlere Kompression, ~2-3 MB pro Foto (Balance zwischen Qualitaet und Speicher)

## Technische Hinweise

- **Modus-Parameter:** Bauteil vs. Ablageort -- bestimmt welches DB-Feld (`bauteilFotoPfad` oder `ablageortFotoPfad`) aktualisiert wird
- **Datei-Verschiebung:** `File.renameTo()` von `photos/temp/` nach `photos/` (rename, kein Copy)
