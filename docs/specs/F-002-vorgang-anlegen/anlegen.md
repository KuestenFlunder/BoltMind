# F-002: Reparaturvorgang anlegen

## Kontext

Der Mechaniker steht vor dem Fahrzeug und will einen neuen Reparaturvorgang beginnen. Der Flow ist bewusst Foto-first: Zuerst wird das Fahrzeug fotografiert (visuelles Wiederfinden in der Übersicht), dann werden die Auftragsdaten erfasst. Nur die Auftragsnummer ist Pflicht - alles andere soll den Mechaniker nicht aufhalten.

Der Abbruch geschieht über den System-Back-Button. Ein versehentlich angelegter Vorgang kann in der Übersicht (F-001) per Swipe gelöscht werden.

## User Stories

### US-002.1: Fahrzeug fotografieren

**Als** Mechaniker
**möchte ich** beim Anlegen eines neuen Vorgangs als erstes ein Foto des Fahrzeugs aufnehmen
**damit** ich den Vorgang in der Übersicht visuell wiederfinde, ohne einen Fahrzeugnamen eintippen zu müssen.

#### Akzeptanzkriterien

- **Given** der Mechaniker hat in der Übersicht (F-001) auf "+" getippt
  **When** der Anlage-Flow startet
  **Then** öffnet sich sofort die Kamera im Vollbild

- **Given** die Kamera ist geöffnet
  **When** der Mechaniker den Auslöser-Button tippt
  **Then** wird ein Foto aufgenommen und als Preview angezeigt

- **Given** die Kamera ist geöffnet
  **When** der Mechaniker den System-Back-Button drückt
  **Then** wird zur Übersicht (F-001) zurückgekehrt, ohne dass ein Vorgang angelegt wird

- **Given** die App hat keine Kamera-Berechtigung
  **When** der Anlage-Flow gestartet wird
  **Then** wird die Kamera-Berechtigung angefragt mit einer verständlichen Erklärung

#### UI-Verhalten
- Vollbild-Kameravorschau mit großem Auslöser-Button
- Kamera muss sofort auslösen (Quality Goal #3)
- Identisches Kamera-UX wie im Demontage-Flow (F-003) für Konsistenz

---

### US-002.2: Auftragsdaten erfassen und Vorgang starten

**Als** Mechaniker
**möchte ich** nach dem Foto die Auftragsnummer eintragen und den Vorgang starten
**damit** ich schnell mit der Demontage beginnen kann.

#### Akzeptanzkriterien

- **Given** ein Fahrzeugfoto wurde aufgenommen
  **When** die Foto-Preview angezeigt wird
  **Then** ist das Foto oben sichtbar und darunter das Eingabeformular mit Auftragsnummer und Beschreibung

- **Given** das Formular wird angezeigt
  **When** der Mechaniker die Auftragsnummer eingibt und auf "Starten" tippt
  **Then** wird der Vorgang mit Foto und Auftragsnummer in der DB gespeichert und der Demontage-Flow (F-003) geöffnet

- **Given** das Formular wird angezeigt
  **When** der Mechaniker auf "Starten" tippt ohne Auftragsnummer
  **Then** wird eine Validierungsmeldung angezeigt ("Auftragsnummer ist erforderlich")

- **Given** das Formular wird angezeigt
  **When** der Mechaniker keine Beschreibung eingibt und auf "Starten" tippt
  **Then** wird der Vorgang trotzdem gespeichert (Beschreibung ist optional)

- **Given** das Formular wird angezeigt
  **When** der Mechaniker den System-Back-Button drückt
  **Then** wird zur Übersicht (F-001) zurückgekehrt, das temporäre Foto wird verworfen

#### UI-Verhalten
- Foto-Preview oben (nicht editierbar, nur Vorschau)
- Auftragsnummer-Feld mit Stern (*) als Pflichtfeld-Markierung
- Beschreibung-Feld ohne Pflichtmarkierung
- "Starten"-Button prominent am unteren Bildschirmrand
- Große Eingabefelder für Werkstatt-Bedingungen (Quality Goal #1)

---

### US-002.3: Foto wiederholen

**Als** Mechaniker
**möchte ich** vor dem Starten das Fahrzeugfoto nochmal neu aufnehmen können
**damit** ich ein unscharfes oder falsches Foto korrigieren kann.

#### Akzeptanzkriterien

- **Given** das Formular mit Foto-Preview wird angezeigt
  **When** der Mechaniker auf "Bild wiederholen" tippt
  **Then** öffnet sich die Kamera erneut

- **Given** die Kamera ist nach "Bild wiederholen" geöffnet
  **When** der Mechaniker ein neues Foto aufnimmt
  **Then** ersetzt das neue Foto das vorherige im Preview und das alte temporäre Foto wird gelöscht

- **Given** die Kamera ist nach "Bild wiederholen" geöffnet
  **When** der Mechaniker Back drückt
  **Then** wird zurück zum Formular navigiert mit dem vorherigen Foto (kein Datenverlust der Formulareingaben)

#### UI-Verhalten
- "Bild wiederholen"-Button unter dem Foto-Preview, kleiner als "Starten"
- Bereits eingegebene Formulardaten bleiben erhalten wenn die Kamera erneut geöffnet wird

## Nicht-funktionale Anforderungen

- **Bedienbarkeit** (Quality Goal #1): Minimale Pflichtfelder (nur Auftragsnummer). Große Buttons und Eingabefelder.
- **Zuverlässigkeit** (Quality Goal #2): Foto wird sofort auf dem Filesystem gespeichert. Bei Abbruch werden temporäre Fotos aufgeräumt.
- **Performance** (Quality Goal #3): Kamera öffnet und löst sofort aus, keine Wartezeit.

## Technische Hinweise

- System-Kamera: `ActivityResultContracts.TakePicture()` fuer Fahrzeugfoto (gleicher Ansatz wie F-003, keine CAMERA-Permission noetig)
- Foto-Speicherung: App-interner Speicher, JPEG mit mittlerer Kompression
- Room Entity: `Reparaturvorgang(id, fahrzeugFotoPfad, auftragsnummer, beschreibung, status, erstelltAm)`
- `fahrzeugFotoPfad`: Pfad zum Fahrzeugfoto auf dem Filesystem (Pflicht)
- `beschreibung`: Nullable (fakultativ)
- Sofort-Insert in DB, dann Navigation zu F-003 mit `vorgangId`
- Abbruch vor Speicherung: Temporäres Foto wieder löschen
- Spätere Erweiterung: OCR-Scanner für Auftragsnummer per Kamera vom Auftragszettel

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
