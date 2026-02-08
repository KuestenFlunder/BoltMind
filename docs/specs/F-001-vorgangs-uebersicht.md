# F-001: Vorgangs-Übersicht

## Beschreibung

Der Startscreen der App zeigt eine Übersicht aller Reparaturvorgänge. Der Mechaniker kann zwischen offenen und archivierten Vorgängen wechseln, neue Vorgänge anlegen und bestehende löschen.

## Anforderungen

### Funktional

- Liste aller offenen Reparaturvorgänge als Startansicht
- Jeder Listeneintrag zeigt: Fahrzeugfoto (Thumbnail), Auftragsnummer, Erstellungsdatum, Anzahl Schritte
- Umschalten zwischen "Offen" und "Archiv" (Tabs oder Filter)
- Button zum Anlegen eines neuen Vorgangs (führt zu F-002)
- Tippen auf einen offenen Vorgang:
  - **0 Schritte** (frisch angelegt): Direkt Demontage-Flow (F-003) öffnen
  - **≥1 Schritt**: Auswahl-Dialog mit zwei Optionen: "Weiter demontieren" (→ F-003) / "Montage starten" (→ F-004)
- Löschen eines Vorgangs mit Bestätigungsdialog (kaskadierend: Schritte + Fotos)
- Leerer Zustand: Freundliche Anzeige wenn keine Vorgänge existieren

### Nicht-Funktional

- Große, gut treffbare Listenelemente (Werkstatt-Bedingungen, Quality Goal #1)
- Liste lädt sofort, kein Spinner (Quality Goal #3)

## Akzeptanzkriterien

- [ ] Offene Vorgänge werden als Liste angezeigt
- [ ] Archivierte Vorgänge sind über Tab/Filter erreichbar
- [ ] Neuer Vorgang kann über FAB/Button gestartet werden
- [ ] Vorgang kann gelöscht werden (mit Bestätigung)
- [ ] Löschung entfernt alle zugehörigen Schritte und Fotos
- [ ] Leerer Zustand wird angemessen dargestellt
- [ ] Vorgang ohne Schritte öffnet direkt Demontage-Flow
- [ ] Vorgang mit Schritten zeigt Auswahl-Dialog (Demontage / Montage)

## UI-Skizze

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

## UI-Skizze: Auswahl-Dialog

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

## Technische Hinweise

- Room-Datenbank: `Reparaturvorgang` Entity mit Status-Feld (OFFEN, ARCHIVIERT)
- Compose: `LazyColumn` für die Liste
- Navigation: Compose Navigation zu F-002, F-003, F-004
- Auswahl-Dialog: `ModalBottomSheet` oder `AlertDialog` mit zwei Buttons
