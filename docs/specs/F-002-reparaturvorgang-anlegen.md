# F-002: Reparaturvorgang anlegen

## Beschreibung

Der Mechaniker erstellt einen neuen Reparaturvorgang. Zuerst wird ein Foto des Fahrzeugs aufgenommen, danach werden die Auftragsdaten erfasst. Nach Bestätigung wird der Vorgang sofort in der Datenbank gespeichert und der Demontage-Flow (F-003) geöffnet.

## Anforderungen

### Funktional

- **Schritt 1: Fahrzeugfoto** (Kamera öffnet sich sofort nach Klick auf "+")
  - Vollbild-Kameravorschau (CameraX PreviewView)
  - Großer Auslöser-Button
  - Foto wird aufgenommen und als Preview angezeigt
- **Schritt 2: Auftragsdaten** (Formular mit Foto-Preview)
  - Foto-Preview oben im Formular
  - **Bild wiederholen**-Button unter dem Preview (öffnet Kamera erneut, ersetzt vorheriges Foto)
  - **Auftragsnummer** (Pflicht, Freitext) - z.B. "#2024-0815". Später: OCR-Scanner per Kamera vom Auftragszettel.
  - **Beschreibung** (Optional, Freitext) - z.B. "Bremsen vorne wechseln"
- Sofortige Speicherung in Room-DB beim Bestätigen
- Nach Speicherung: Direkter Übergang in den Demontage-Flow (F-003)
- Abbrechen kehrt zur Vorgangs-Übersicht (F-001) zurück (kein Foto gespeichert)

### Nicht-Funktional

- Große Eingabefelder, gut bedienbar mit Handschuhen/öligen Händen (Quality Goal #1)
- Speicherung sofort, kein Datenverlust (Quality Goal #2)
- Minimale Pflichtfelder, schnell ausfüllbar
- Kamera muss sofort auslösen (Quality Goal #3)

## Akzeptanzkriterien

- [ ] Klick auf "+" öffnet sofort die Kamera
- [ ] Foto wird aufgenommen und als Preview angezeigt
- [ ] "Bild wiederholen"-Button öffnet Kamera erneut und ersetzt vorheriges Foto
- [ ] Formular zeigt Foto-Preview, Auftragsnummer und Beschreibung
- [ ] Auftragsnummer ist Pflicht, Validierung vor Speicherung
- [ ] Beschreibung ist optional
- [ ] Vorgang wird sofort in der DB gespeichert (inkl. Foto auf Filesystem)
- [ ] Nach Speicherung öffnet sich der Demontage-Flow
- [ ] Abbrechen kehrt zur Übersicht zurück ohne zu speichern
- [ ] Kamera-Permission wird beim ersten Start abgefragt

## UI-Skizze

### Schritt 1: Fahrzeugfoto
```
┌─────────────────────────────┐
│  ← Neuer Vorgang            │
├─────────────────────────────┤
│                             │
│                             │
│      [ Kamera-Vorschau ]    │
│                             │
│                             │
│                             │
│                         ◉   │
└─────────────────────────────┘
```

### Schritt 2: Auftragsdaten
```
┌─────────────────────────────┐
│  ← Neuer Vorgang            │
├─────────────────────────────┤
│                             │
│  ┌─────────────────────┐    │
│  │  [Fahrzeug-Foto]     │    │
│  └─────────────────────┘    │
│  [Bild wiederholen]         │
│                             │
│  Auftragsnummer *           │
│  ┌─────────────────────┐    │
│  │ #2024-0815           │    │
│  └─────────────────────┘    │
│                             │
│  Beschreibung               │
│  ┌─────────────────────┐    │
│  │ Bremsen vorne        │    │
│  │ wechseln             │    │
│  └─────────────────────┘    │
│                             │
│  ┌─────────────────────┐    │
│  │     STARTEN          │    │
│  └─────────────────────┘    │
└─────────────────────────────┘
```

## Technische Hinweise

- CameraX: `ImageCapture` Use Case für Fahrzeugfoto (gleiche Implementierung wie F-003)
- Foto-Speicherung: App-interner Speicher, JPEG mit mittlerer Kompression
- Room Entity: `RepairJob(id, vehiclePhotoPath, orderNumber, description, status, createdAt)`
- `vehiclePhotoPath`: Pfad zum Fahrzeugfoto auf dem Filesystem (Pflicht)
- `description`: Nullable (fakultativ)
- Sofort-Insert in DB, dann Navigation zu F-003 mit `repairJobId`
- Abbruch vor Speicherung: Temporäres Foto wieder löschen
