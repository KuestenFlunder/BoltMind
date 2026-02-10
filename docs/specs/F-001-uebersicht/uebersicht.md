# F-001: Vorgangs-Übersicht

## Kontext

Die Vorgangs-Übersicht ist der Startscreen der App. Der Mechaniker hat typischerweise 1-3 offene Reparaturvorgänge gleichzeitig. Sein primäres Ziel beim Öffnen der App ist ein schneller Überblick: Was ist offen, wo mache ich weiter?

Das Archiv ist strategisch wichtig: Die erfassten Zeitdaten (F-005) sollen langfristig genutzt werden, um Arbeitsdauern zu analysieren und Angebote besser kalkulieren zu können.

## User Stories

### US-001.1: Offene Vorgänge anzeigen

**Als** Mechaniker
**möchte ich** beim Öffnen der App sofort alle offenen Reparaturvorgänge sehen
**damit** ich weiß, welche Aufträge aktuell in Arbeit sind und direkt weiterarbeiten kann.

#### Akzeptanzkriterien

- **Given** die App wird geöffnet und es existieren offene Vorgänge
  **When** der Startscreen geladen ist
  **Then** werden alle offenen Vorgänge als Liste angezeigt, sortiert nach letzter Bearbeitung (neueste oben)

- **Given** ein offener Vorgang existiert
  **When** der Vorgang in der Liste angezeigt wird
  **Then** sind folgende Informationen sichtbar: Fahrzeugfoto (Thumbnail), Auftragsnummer, Anzahl Schritte, Erstellungsdatum

- **Given** die App wird geöffnet und es existieren keine offenen Vorgänge
  **When** der Startscreen geladen ist
  **Then** wird ein Hinweis angezeigt, dass keine Vorgänge vorhanden sind (z.B. "Noch keine Vorgänge. Tippe auf + um zu starten.")

#### UI-Verhalten
- Vorgangskarte: Thumbnail links, Auftragsnummer + Metadaten rechts
- Liste lädt sofort ohne Spinner (Quality Goal #3)
- Karten sind groß genug für Bedienung mit Handschuhen/öligen Händen (Quality Goal #1)

---

### US-001.2: Vorgang für Weiterarbeit öffnen

**Als** Mechaniker
**möchte ich** einen Vorgang antippen und wählen ob ich demontieren oder montieren will
**damit** ich direkt im richtigen Modus weiterarbeiten kann.

#### Akzeptanzkriterien

- **Given** ein offener Vorgang mit 0 Schritten existiert (frisch angelegt)
  **When** der Mechaniker den Vorgang antippt
  **Then** öffnet sich direkt der Demontage-Flow (F-003)

- **Given** ein offener Vorgang mit mindestens 1 Schritt existiert
  **When** der Mechaniker den Vorgang antippt
  **Then** erscheint ein Auswahl-Dialog mit "Weiter demontieren" und "Montage starten"

- **Given** der Auswahl-Dialog ist sichtbar
  **When** der Mechaniker "Weiter demontieren" wählt
  **Then** öffnet sich der Demontage-Flow (F-003) für diesen Vorgang

- **Given** der Auswahl-Dialog ist sichtbar
  **When** der Mechaniker "Montage starten" wählt
  **Then** öffnet sich der Montage-Flow (F-004) für diesen Vorgang

#### UI-Verhalten
- Auswahl-Dialog: BottomSheet oder Dialog mit zwei großen Buttons
- Fahrzeugfoto und Auftragsnummer im Dialog-Header zur Bestätigung

---

### US-001.3: Neuen Vorgang starten

**Als** Mechaniker
**möchte ich** über einen gut sichtbaren Button einen neuen Reparaturvorgang anlegen können
**damit** ich schnell mit einem neuen Auftrag beginnen kann.

#### Akzeptanzkriterien

- **Given** der Mechaniker ist auf dem Startscreen
  **When** er auf den "+"-Button tippt
  **Then** wird der Anlage-Flow (F-002) gestartet

#### UI-Verhalten
- FAB (Floating Action Button) oder prominenter "+"-Button
- Immer sichtbar, auch wenn die Liste scrollbar ist

---

### US-001.4: Vorgang löschen

**Als** Mechaniker
**möchte ich** einen Vorgang per Wischgeste löschen können
**damit** ich fehlerhafte oder nicht mehr benötigte Vorgänge entfernen kann.

#### Akzeptanzkriterien

- **Given** ein Vorgang wird in der Liste angezeigt (offen oder archiviert)
  **When** der Mechaniker die Karte nach links wischt
  **Then** wird ein Löschen-Button sichtbar

- **Given** der Löschen-Button ist sichtbar
  **When** der Mechaniker auf "Löschen" tippt
  **Then** erscheint ein Bestätigungsdialog ("Vorgang und alle Fotos unwiderruflich löschen?")

- **Given** der Bestätigungsdialog ist sichtbar
  **When** der Mechaniker "Löschen" bestätigt
  **Then** werden der Vorgang, alle zugehörigen Schritte und alle Fotos gelöscht

- **Given** der Bestätigungsdialog ist sichtbar
  **When** der Mechaniker "Abbrechen" wählt
  **Then** bleibt der Vorgang erhalten und die Swipe-Aktion wird zurückgesetzt

#### UI-Verhalten
- Swipe-to-Delete (Material-Pattern)
- Bestätigungsdialog mit rotem "Löschen"-Button (versehentliches Löschen verhindern)

---

### US-001.5: Archivierte Vorgänge einsehen

**Als** Mechaniker
**möchte ich** abgeschlossene Reparaturvorgänge im Archiv einsehen können
**damit** ich bei Reklamationen nachschauen und Arbeitsdauern für die Kalkulation analysieren kann.

#### Akzeptanzkriterien

- **Given** der Mechaniker ist auf dem Startscreen im Tab "Offen"
  **When** er auf den Tab "Archiv" wechselt
  **Then** werden alle archivierten Vorgänge angezeigt

- **Given** archivierte Vorgänge existieren
  **When** ein archivierter Vorgang in der Liste angezeigt wird
  **Then** sind folgende Informationen sichtbar: Fahrzeugfoto (Thumbnail), Auftragsnummer, Anzahl Schritte, Gesamtdauer, Abschlussdatum

- **Given** keine archivierten Vorgänge existieren
  **When** der Mechaniker den Archiv-Tab öffnet
  **Then** wird ein Hinweis angezeigt (z.B. "Noch keine abgeschlossenen Vorgänge.")

- **Given** ein archivierter Vorgang wird angezeigt
  **When** der Mechaniker den Vorgang antippt
  **Then** öffnet sich eine Nur-Lese-Ansicht mit allen Schritten, Fotos und Zeitdaten

## Nicht-funktionale Anforderungen

- **Bedienbarkeit** (Quality Goal #1): Große Karten, gut treffbar mit Handschuhen/öligen Händen. Swipe-Geste großzügig tolerant.
- **Performance** (Quality Goal #3): Liste lädt sofort beim App-Start, kein Spinner.
- **Zuverlässigkeit** (Quality Goal #2): Löschung ist kaskadierend und vollständig (keine verwaisten Fotos).

## Technische Hinweise

- Room-Datenbank: `Reparaturvorgang` Entity mit Status-Feld (`OFFEN`, `ARCHIVIERT`)
- Compose: `LazyColumn` für die Liste, `SwipeToDismiss` für Löschen
- Navigation: Compose Navigation zu F-002, F-003, F-004
- Auswahl-Dialog: `ModalBottomSheet` oder `AlertDialog` mit zwei Buttons
- Thumbnail-Loading: Foto aus Filesystem laden, skaliert auf Karten-Größe (kein Full-Size laden)
- Sortierung: `ORDER BY updatedAt DESC` (offene), `ORDER BY archivedAt DESC` (archivierte)
- Archiv-Karten: Zusätzlich Gesamtdauer anzeigen (berechnet aus Schritt-Zeitstempeln, siehe F-005)

## UI-Skizze

### Startscreen (Offene Vorgänge)
```
┌─────────────────────────────┐
│  BoltMind                   │
├──────────┬──────────────────┤
│  Offen   │   Archiv         │
├──────────┴──────────────────┤
│                             │
│  ┌──────┬──────────────┐    │
│  │[Foto]│ #2024-0815    │    │
│  │      │ 12 Schritte   │    │
│  │      │ Heute         │    │
│  └──────┴──────────────┘    │
│                             │
│  ┌──────┬──────────────┐    │
│  │[Foto]│ #2024-0712    │    │
│  │      │ 8 Schritte    │    │
│  │      │ Gestern       │    │
│  └──────┴──────────────┘    │
│                             │
│                        [+]  │
└─────────────────────────────┘
```

### Auswahl-Dialog
```
┌─────────────────────────────┐
│                             │
│  [Foto] · #2024-0815        │
│                             │
│  ┌─────────────────────┐    │
│  │  Weiter demontieren  │    │
│  └─────────────────────┘    │
│  ┌─────────────────────┐    │
│  │  Montage starten     │    │
│  └─────────────────────┘    │
│                             │
└─────────────────────────────┘
```

### Leerer Zustand
```
┌─────────────────────────────┐
│  BoltMind                   │
├──────────┬──────────────────┤
│  Offen   │   Archiv         │
├──────────┴──────────────────┤
│                             │
│                             │
│     Noch keine Vorgänge.    │
│     Tippe auf + um zu       │
│     starten.                │
│                             │
│                             │
│                        [+]  │
└─────────────────────────────┘
```
