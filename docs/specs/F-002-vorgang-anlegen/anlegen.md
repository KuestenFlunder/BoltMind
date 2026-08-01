# F-002: Reparaturvorgang anlegen

## Kontext

Der Mechaniker steht vor dem Fahrzeug und will einen neuen Reparaturvorgang beginnen. Der Flow ist bewusst Foto-first: Zuerst wird das Fahrzeug fotografiert (visuelles Wiederfinden in der Uebersicht), dann werden die Auftragsdaten erfasst. Nur die Auftragsnummer ist Pflicht — alles andere soll den Mechaniker nicht aufhalten.

Der Flow hat genau einen Weg vorwaerts: **„LOS GEHT'S"** legt den Vorgang an und fuehrt unmittelbar in die Demontage (F-003); den ersten Schritt legt dort F-003 an. Er hat genau einen Weg zurueck: den System-Back-Button bzw. den Zurueck-Chip, der nichts anlegt. Ein versehentlich angelegter Vorgang wird in der Uebersicht (F-001) per Swipe geloescht.

Farben, Schriften, Glasflaechen und Maße stehen in [../design-system.md](../design-system.md); diese Spec nennt nur Wortlaute und Verhalten.

## User Stories

### US-002.1: Fahrzeug fotografieren

**Als** Mechaniker
**möchte ich** beim Anlegen eines neuen Vorgangs als erstes ein Foto des Fahrzeugs aufnehmen
**damit** ich den Vorgang in der Übersicht visuell wiederfinde, ohne einen Fahrzeugnamen eintippen zu müssen.

#### Akzeptanzkriterien

- **Given** der Mechaniker hat in der Übersicht (F-001) auf den FAB getippt
  **When** der Anlage-Flow startet
  **Then** wird sofort die System-Kamera gestartet, ohne app-eigenen Zwischenscreen

- **Given** die System-Kamera ist gestartet
  **When** der Mechaniker die Aufnahme in der System-Kamera bestätigt
  **Then** wird das Foto als Fahrzeugfoto übernommen und das Formular (US-002.2) mit dem Foto angezeigt

- **Given** die System-Kamera ist gestartet und es gibt noch kein Fahrzeugfoto
  **When** der Mechaniker die Aufnahme abbricht (System-Back-Button oder "Abbrechen" in der System-Kamera)
  **Then** wird zur Übersicht (F-001) zurückgekehrt, ohne dass ein Vorgang angelegt wird
  **And** die für die Aufnahme angelegte Zieldatei unter `photos/` wird gelöscht

- **Given** auf dem Gerät ist keine App installiert, die den Foto-Intent bedienen kann
  **When** der Anlage-Flow die System-Kamera starten will
  **Then** erscheint ein Hinweis mit dem Text (wörtlich) **„Keine Kamera-App gefunden"** und der Bestätigung (wörtlich) **„OK"**
  **And** nach dem Schließen wird zur Übersicht (F-001) zurückgekehrt, ohne dass ein Vorgang angelegt wird

#### UI-Verhalten
- Kein app-eigener Kamera-Screen: Aufnahme und Bestätigung übernimmt die System-Kamera
- Zwischen FAB-Tap und System-Kamera zeigt die App keinen eigenen Screen (Quality Goal #3)
- Die System-Kamera schreibt direkt in eine Zieldatei unter `photos/`; einen `photos/temp/`-Ordner gibt es nicht
- Identisches Kamera-Verhalten wie im Demontage-Flow (F-003)

---

### US-002.2: Auftragsdaten erfassen und Vorgang starten

**Als** Mechaniker
**möchte ich** nach dem Foto die Auftragsnummer eintragen und den Vorgang starten
**damit** ich schnell mit der Demontage beginnen kann.

#### Akzeptanzkriterien

##### AK 1: Aufbau des Formulars

- **Given** ein Fahrzeugfoto wurde aufgenommen
  **When** das Formular angezeigt wird
  **Then** enthält es von oben nach unten: den Zurück-Chip mit dem Titel (wörtlich) **„NEUER AUFTRAG"**, das Fahrzeugfoto mit der Aktion (wörtlich) **„↺ NEU KNIPSEN"**, das Pflichtfeld (wörtlich) **„AUFTRAGSNUMMER"** mit orangem Stern, das Feld (wörtlich) **„WAS IST ZU TUN?"** und am unteren Rand die Primäraktion (wörtlich) **„LOS GEHT'S →"**
  **And** das Nummernfeld zeigt als Platzhalter (wörtlich) **„2026-0815"**, das Beschreibungsfeld (wörtlich) **„Bremsen vorne wechseln"**
  **And** „WAS IST ZU TUN?" bildet `Reparaturvorgang.beschreibung` ab und trägt keine Pflichtmarkierung

##### AK 2: „LOS GEHT'S" legt den Vorgang an und übergibt an die Demontage

- **Given** das Formular zeigt ein Fahrzeugfoto und eine ausgefüllte Auftragsnummer
  **When** der Mechaniker „LOS GEHT'S" antippt
  **Then** wird der Reparaturvorgang mit Fahrzeugfoto, Auftragsnummer und Beschreibung im Status OFFEN gespeichert
  **And** es wird **kein** `Schritt` angelegt — die Schritt-Anlage gehört vollständig F-003
  **And** die Schritt-Ansicht der Demontage (F-003) wird für diesen Vorgang geöffnet
  **And** F-003 findet dort keinen offenen Schritt vor, legt daher Schritt 1 an und startet die System-Kamera automatisch (siehe [../F-003-demontage/workflow.md](../F-003-demontage/workflow.md), Entry-Transition)

##### AK 3: Der Anlage-Screen bleibt nicht im Rückwärtsstapel

- **Given** der Mechaniker ist über „LOS GEHT'S" in der Demontage gelandet
  **When** er die System-Zurück-Geste auslöst
  **Then** führt sie nicht in das Anlage-Formular zurück
  **And** es entsteht kein zweiter Vorgang und kein zweiter Schritt 1

##### AK 4: Fehlende Auftragsnummer

- **Given** das Formular wird angezeigt und das Nummernfeld ist leer
  **When** der Mechaniker „LOS GEHT'S" antippt
  **Then** erscheint unter dem Nummernfeld die Fehlerzeile (wörtlich) **„Ohne Nummer geht's nicht."**
  **And** es wird kein Vorgang und kein Schritt angelegt und der Screen bleibt stehen

- **Given** die Fehlerzeile ist sichtbar
  **When** der Mechaniker ein Zeichen in das Nummernfeld eingibt
  **Then** verschwindet die Fehlerzeile sofort

- **Given** das Formular wird angezeigt und das Nummernfeld ist leer
  **When** der Mechaniker tippt, ohne „LOS GEHT'S" ausgelöst zu haben
  **Then** erscheint keine Fehlerzeile — sie erscheint erst nach einem Versuch

##### AK 5: Beschreibung ist fakultativ

- **Given** das Formular zeigt eine ausgefüllte Auftragsnummer und ein leeres Feld „WAS IST ZU TUN?"
  **When** der Mechaniker „LOS GEHT'S" antippt
  **Then** wird der Vorgang trotzdem angelegt, mit leerer Beschreibung

##### AK 6: Abbruch verwirft alles

- **Given** das Formular wird angezeigt
  **When** der Mechaniker den System-Back-Button oder den Zurück-Chip antippt
  **Then** wird zur Übersicht (F-001) zurückgekehrt, ohne dass ein Vorgang oder ein Schritt angelegt wird
  **And** die bereits aufgenommene Foto-Datei unter `photos/` wird gelöscht (sie hat keine DB-Referenz)

#### UI-Verhalten
- Foto-Vorschau oben, nicht editierbar; „↺ NEU KNIPSEN" liegt als Aktion über dem Foto
- Das Nummernfeld ist einzeilig und setzt in Versalien; das Beschreibungsfeld ist mehrzeilig
- „LOS GEHT'S" liegt fest am unteren Bildschirmrand, das Formular darüber scrollt
- Große Eingabefelder für Werkstatt-Bedingungen (Quality Goal #1)

---

### US-002.3: Foto wiederholen

**Als** Mechaniker
**möchte ich** vor „LOS GEHT'S" das Fahrzeugfoto nochmal neu aufnehmen können
**damit** ich ein unscharfes oder falsches Foto korrigieren kann.

#### Akzeptanzkriterien

- **Given** das Formular mit der Foto-Vorschau wird angezeigt
  **When** der Mechaniker „↺ NEU KNIPSEN" antippt
  **Then** wird die System-Kamera erneut gestartet
  **And** das vorhandene Foto bleibt dabei unangetastet (Governance: [../governance.md](../governance.md), Abschnitt "Kamera")

- **Given** die System-Kamera wurde nach „↺ NEU KNIPSEN" gestartet
  **When** der Mechaniker die neue Aufnahme in der System-Kamera bestätigt
  **Then** ersetzt das neue Foto das vorherige in der Foto-Vorschau
  **And** erst jetzt wird die alte Foto-Datei unter `photos/` gelöscht

- **Given** die System-Kamera wurde nach „↺ NEU KNIPSEN" gestartet
  **When** der Mechaniker die Aufnahme abbricht oder keine Kamera-App vorhanden ist
  **Then** bleibt das Formular mit dem vorherigen Foto und allen bereits eingegebenen Daten stehen
  **And** gelöscht wird nur die vorbereitete, leer gebliebene Zieldatei

#### UI-Verhalten
- „↺ NEU KNIPSEN" ist keine Kamera-Bestätigung, sondern eine Aktion am bereits aufgenommenen Foto
- Bereits eingegebene Formulardaten überleben jeden Kamera-Start

## Nicht-funktionale Anforderungen

- **Bedienbarkeit** (Quality Goal #1): Minimale Pflichtfelder (nur Auftragsnummer). Die verbindlichen Mindestmaße für Touch-Targets und Abstände stehen in [../governance.md](../governance.md), Abschnitt "Touch-Targets"; die konkreten Größen dieses Screens in [../design-system.md](../design-system.md), Abschnitt "Maße und Trefferflächen".
- **Zuverlässigkeit** (Quality Goal #2): Die System-Kamera schreibt das Foto direkt in die Zieldatei unter `photos/`; es gibt keinen `photos/temp/`-Ordner. Wird die Kamera abgebrochen oder der Anlage-Flow verworfen, wird diese Datei sofort gelöscht. Verbleibende Dateien ohne DB-Referenz räumt die Cleanup-Regel beim App-Start auf ([../governance.md](../governance.md), Abschnitt "Speicherort").
- **Performance** (Quality Goal #3): Die App startet den Kamera-Intent direkt nach dem FAB-Tap und zeigt dazwischen keinen eigenen Screen. Die Startzeit der System-Kamera-App selbst liegt außerhalb des App-Einflusses — ein bewusst akzeptierter Kompromiss der Entscheidung "nur System-Kamera".

## Technische Hinweise

- System-Kamera: `ActivityResultContracts.TakePicture()` + `FileProvider`. Kein CameraX, keine app-eigene Kameraansicht, keine `CAMERA`-Permission, keine app-eigene Foto-Bestätigung (siehe [../governance.md](../governance.md), Abschnitt "Kamera")
- Intent-Fehlerbehandlung: Fängt kein Programm den Intent, erscheint der Hinweis "Keine Kamera-App gefunden" und der Flow endet in F-001
- Foto-Speicherung: App-interner Speicher (`filesDir/photos/`). Die Zieldatei wird vor dem Kamera-Start angelegt und per `FileProvider` übergeben — kein `photos/temp/`, kein Verschiebe-Schritt. Auflösung und Kompression bestimmt die System-Kamera; die App übernimmt die Datei, wie sie ist, und entfernt beim Übernehmen die EXIF-Metadaten
- Room Entity: `Reparaturvorgang(id, fahrzeugFotoPfad, auftragsnummer, beschreibung, status, erstelltAm, aktualisiertAm)`
- `beschreibung`: Nullable. Ein leer gelassenes Feld „WAS IST ZU TUN?" wird als `null` gespeichert, nicht als leerer Text
- `aktualisiertAm`: Zeitstempel der letzten Änderung. F-001 sortiert danach und leitet daraus das Abschlussdatum archivierter Vorgänge ab. Ein eigener Archivierungs-Zeitstempel existiert **nicht**
- „LOS GEHT'S" schreibt den `Reparaturvorgang` und navigiert dann zur Schritt-Ansicht (F-003) mit `vorgangId`. Der Anlage-Screen wird dabei aus dem Rückwärtsstapel entfernt. Den `Schritt` legt F-003 an — F-002 kennt die Schrittnummern-Logik nicht
- Doppel-Tap-Schutz: „LOS GEHT'S" ist debounced (300ms, [../governance.md](../governance.md), Abschnitt "Debounce") und wirkt nach dem ersten erfolgreichen Anlegen nicht erneut
- Abbruch vor dem Anlegen: die angelegte Zieldatei unter `photos/` sofort löschen. Verbleibende Dateien ohne DB-Referenz — geprüft gegen `SchrittFoto.pfad` und `Reparaturvorgang.fahrzeugFotoPfad` — räumt die Cleanup-Regel beim App-Start auf
- Spätere Erweiterung: OCR-Scanner für die Auftragsnummer vom Auftragszettel

### Kamera-Autostart für Schritt 1 (entschieden am 2026-08-01)

**Die Kamera startet automatisch.** Nach „LOS GEHT'S" landet der Mechaniker nicht in einer leeren Maske, sondern direkt in der System-Kamera für Schritt 1 — so, wie es der Design-Prototyp zeichnet. Der frühere Zustand (leeres Karussell, Kamera erst über „NOCH'N FOTO") war eine Abweichung vom Soll und ist behoben.

Zuständig dafür ist **F-003, nicht F-002**: der Autostart hängt nicht am Anlage-Flow, sondern an der Entry-Bedingung „kein offener Schritt vorhanden". Dieselbe Regel greift beim Wiedereinstieg über F-001 nach dem Feierabend. F-002 legt deshalb keinen Schritt mehr an; die Regel steht vollständig in [../F-003-demontage/workflow.md](../F-003-demontage/workflow.md).

## UI-Skizze

### Schritt 1: Fahrzeugfoto (System-Kamera)
```
┌─────────────────────────────┐
│                             │
│   System-Kamera-App         │
│                             │
│   Aufnahme und Bestätigung  │
│   liegen bei der System-    │
│   Kamera. BoltMind zeigt    │
│   hier keinen eigenen       │
│   Screen.                   │
│                             │
│   Bestätigt → Schritt 2     │
│   Abgebrochen → F-001       │
└─────────────────────────────┘
```

### Schritt 2: Auftragsdaten
```
┌─────────────────────────────┐
│  ‹   NEUER AUFTRAG          │
│                             │
│  ┌─────────────────────┐    │
│  │  [Fahrzeug-Foto]    │    │
│  │        ↺ NEU KNIPSEN│    │
│  └─────────────────────┘    │
│                             │
│  AUFTRAGSNUMMER *           │
│  ┌─────────────────────┐    │
│  │ 2026-0815           │    │
│  └─────────────────────┘    │
│  ! Ohne Nummer geht's nicht.│
│                             │
│  WAS IST ZU TUN?            │
│  ┌─────────────────────┐    │
│  │ Bremsen vorne       │    │
│  │ wechseln            │    │
│  └─────────────────────┘    │
│                             │
│  ┌─────────────────────┐    │
│  │    LOS GEHT'S  →    │    │
│  └─────────────────────┘    │
└─────────────────────────────┘
```

## Änderungshistorie

| Datum | Änderung |
|---|---|
| 2026-07-27 | Auf das Design-System nachgezogen: System-Kamera ist Ist-Zustand (CameraX-Warnungen entfallen), „LOS GEHT'S" legt Vorgang und Schritt 1 an und verlässt den Rückwärtsstapel (AK 2/AK 3), Wortlaute „NEUER AUFTRAG", „↺ NEU KNIPSEN", „AUFTRAGSNUMMER", „WAS IST ZU TUN?", „LOS GEHT'S", „Ohne Nummer geht's nicht." (erst nach einem Versuch) festgeschrieben, Platzhalter „2026-0815" / „Bremsen vorne wechseln". |
| 2026-08-01 | `[OFFEN] Kamera-Autostart für Schritt 1` entschieden: die Kamera startet automatisch. Die Schritt-Anlage wandert dabei von F-002 zu F-003 (AK 2 umformuliert) — der Autostart hängt an der Entry-Bedingung „kein offener Schritt", nicht am Anlage-Flow, und gilt damit auch beim Wiedereinstieg über F-001. |
