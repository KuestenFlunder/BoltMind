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
  **Then** wird sofort die System-Kamera gestartet, ohne app-eigenen Zwischenscreen

- **Given** die System-Kamera ist gestartet
  **When** der Mechaniker die Aufnahme in der System-Kamera bestätigt
  **Then** wird das Foto als Fahrzeugfoto übernommen und das Formular (US-002.2) mit dem Foto angezeigt

- **Given** die System-Kamera ist gestartet
  **When** der Mechaniker die Aufnahme abbricht (System-Back-Button oder "Abbrechen" in der System-Kamera)
  **Then** wird zur Übersicht (F-001) zurückgekehrt, ohne dass ein Vorgang angelegt wird
  **And** die für die Aufnahme angelegte Zieldatei unter `photos/` wird gelöscht

- **Given** auf dem Gerät ist keine App installiert, die den Foto-Intent bedienen kann
  **When** der Anlage-Flow die System-Kamera starten will
  **Then** erscheint ein Hinweis-Dialog "Keine Kamera-App gefunden"
  **And** nach dem Schließen des Dialogs wird zur Übersicht (F-001) zurückgekehrt, ohne dass ein Vorgang angelegt wird

#### UI-Verhalten
- Kein app-eigener Kamera-Screen: Aufnahme und Bestätigung übernimmt die System-Kamera
- Zwischen "+"-Tap und System-Kamera zeigt die App keinen eigenen Screen (Quality Goal #3)
- Die System-Kamera schreibt direkt in eine Zieldatei unter `photos/`; es gibt keinen `photos/temp/`-Ordner
- Identisches Kamera-Verhalten wie im Demontage-Flow (F-003) für Konsistenz

---

### US-002.2: Auftragsdaten erfassen und Vorgang starten

**Als** Mechaniker
**möchte ich** nach dem Foto die Auftragsnummer eintragen und den Vorgang starten
**damit** ich schnell mit der Demontage beginnen kann.

#### Akzeptanzkriterien

- **Given** ein Fahrzeugfoto wurde aufgenommen
  **When** das Formular angezeigt wird
  **Then** ist das Foto oben als Vorschau sichtbar und darunter das Eingabeformular mit Auftragsnummer und Beschreibung

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
  **Then** wird zur Übersicht (F-001) zurückgekehrt, ohne dass ein Vorgang angelegt wird
  **And** die bereits aufgenommene Foto-Datei unter `photos/` wird gelöscht (sie hat keine DB-Referenz)

#### UI-Verhalten
- Foto-Vorschau oben (nicht editierbar, nur Vorschau)
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

- **Given** das Formular mit der Foto-Vorschau wird angezeigt
  **When** der Mechaniker auf "Bild wiederholen" tippt
  **Then** wird die System-Kamera erneut gestartet

- **Given** die System-Kamera wurde nach "Bild wiederholen" gestartet
  **When** der Mechaniker die neue Aufnahme in der System-Kamera bestätigt
  **Then** ersetzt das neue Foto das vorherige in der Foto-Vorschau
  **And** die alte Foto-Datei unter `photos/` wird gelöscht

- **Given** die System-Kamera wurde nach "Bild wiederholen" gestartet
  **When** der Mechaniker die Aufnahme abbricht
  **Then** wird zurück zum Formular navigiert mit dem vorherigen Foto (kein Datenverlust der Formulareingaben)

#### UI-Verhalten
- "Bild wiederholen"-Button unter der Foto-Vorschau, kleiner als "Starten"
- "Bild wiederholen" ist keine Kamera-Bestätigung, sondern eine Aktion am bereits aufgenommenen Foto (Governance: Kamera)
- Bereits eingegebene Formulardaten bleiben erhalten wenn die System-Kamera erneut gestartet wird

## Nicht-funktionale Anforderungen

- **Bedienbarkeit** (Quality Goal #1): Minimale Pflichtfelder (nur Auftragsnummer). Große Buttons und Eingabefelder — die verbindlichen Mindestmaße für Touch-Targets und Abstände stehen in [../governance.md](../governance.md), Abschnitt "Touch-Targets".
- **Zuverlässigkeit** (Quality Goal #2): Die System-Kamera schreibt das Foto direkt in die Zieldatei unter `photos/`; es gibt keinen `photos/temp/`-Ordner. Wird die Kamera abgebrochen oder der Anlage-Flow verworfen, wird diese Datei sofort gelöscht. Verbleibende Dateien ohne DB-Referenz räumt die Cleanup-Regel beim App-Start auf ([../governance.md](../governance.md)).
- **Performance** (Quality Goal #3): Die App startet den Kamera-Intent direkt nach dem "+"-Tap und zeigt dazwischen keinen eigenen Screen. Die Startzeit der System-Kamera-App selbst liegt außerhalb des App-Einflusses — ein bewusst akzeptierter Kompromiss der Entscheidung "nur System-Kamera" gegenüber Quality Goal #3.

## Technische Hinweise

- System-Kamera: `ActivityResultContracts.TakePicture()` + `FileProvider` für das Fahrzeugfoto (gleicher Ansatz wie F-003, keine CAMERA-Permission nötig, keine app-eigene Foto-Bestätigung — siehe [../governance.md](../governance.md), Abschnitt "Kamera")
- Intent-Fehlerbehandlung: Vor dem Start prüfen, ob der Foto-Intent aufgelöst werden kann; andernfalls Hinweis-Dialog "Keine Kamera-App gefunden" und Rückkehr zu F-001
- Foto-Speicherung: App-interner Speicher (`filesDir/photos/`). Die Zieldatei wird vor dem Kamera-Start angelegt und per `FileProvider` an die System-Kamera übergeben — kein `photos/temp/`. Auflösung und Kompression bestimmt die System-Kamera; die App übernimmt die gelieferte Datei, wie sie ist (Erwartungswert siehe [../governance.md](../governance.md), Abschnitt "Qualität")
- Room Entity: `Reparaturvorgang(id, fahrzeugFotoPfad, auftragsnummer, beschreibung, status, erstelltAm, aktualisiertAm)`
- `fahrzeugFotoPfad`: Pfad zum Fahrzeugfoto auf dem Filesystem (Pflicht)
- `beschreibung`: Nullable (fakultativ)
- `aktualisiertAm`: Zeitstempel der letzten Änderung. F-001 sortiert danach und leitet daraus das Abschlussdatum archivierter Vorgänge ab. Ein eigener Archivierungs-Zeitstempel existiert **nicht**
- Sofort-Insert in DB, dann Navigation zu F-003 mit `vorgangId`
- Abbruch vor Speicherung (Kamera abgebrochen oder Anlage-Flow verworfen): die angelegte Zieldatei unter `photos/` sofort löschen. Verbleibende Dateien ohne DB-Referenz — geprüft gegen `SchrittFoto.pfad` und `Reparaturvorgang.fahrzeugFotoPfad` — räumt die Cleanup-Regel beim App-Start auf ([../governance.md](../governance.md))
- Spätere Erweiterung: OCR-Scanner für Auftragsnummer per Kamera vom Auftragszettel

### [OFFEN] Abweichung: aktuelle Implementierung nutzt CameraX

> **Achtung:** Der oben beschriebene Zielzustand "System-Kamera" ist in F-002 **noch nicht umgesetzt**. Die Spec beschreibt hier den Sollzustand, nicht den Istzustand.
>
> **Istzustand:** `feature/neuervorgang/NeuerVorgangScreen.kt` implementiert eine app-eigene Kameraansicht auf Basis von CameraX (`androidx.camera.core`, `androidx.camera.camera2`, `androidx.camera.lifecycle`, `androidx.camera.view`) mit Vollbild-`PreviewView` und eigenem Auslöser-Button und fragt zuvor die CAMERA-Permission ab. Im Manifest steht `<uses-permission android:name="android.permission.CAMERA" />`.
>
> **Notwendige Umstellung:**
> - App-eigene CameraX-Ansicht durch `ActivityResultContracts.TakePicture()` + `FileProvider` ersetzen
> - Permission-Abfrage und die zugehörigen UI-Zustände (Berechtigung angefragt / abgelehnt) ersatzlos entfernen
> - `<uses-permission android:name="android.permission.CAMERA" />` aus dem `AndroidManifest.xml` entfernen
> - CameraX-Dependencies aus `app/build.gradle.kts` und `gradle/libs.versions.toml` entfernen
> - Tests auf das Verhalten der System-Kamera umstellen (Start, Bestätigung, Abbruch, keine Kamera-App)
>
> Nach der Umstellung entfällt dieser Hinweis.

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
