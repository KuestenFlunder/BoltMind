# F-001: Vorgangs-Übersicht

## Beschreibung

Der Startscreen der App zeigt eine Übersicht aller Reparaturvorgänge. Der Mechaniker kann zwischen offenen und archivierten Vorgängen wechseln, neue Vorgänge anlegen und bestehende löschen.

## Anforderungen

### Funktional

- Liste aller offenen Reparaturvorgänge als Startansicht
- Jeder Listeneintrag zeigt: Fahrzeug-Bezeichnung, Auftragsnummer, Erstellungsdatum, Anzahl Schritte
- Umschalten zwischen "Offen" und "Archiv" (Tabs oder Filter)
- Button zum Anlegen eines neuen Vorgangs (führt zu F-002)
- Tippen auf einen offenen Vorgang öffnet den Demontage-Flow (F-003) oder Montage-Flow (F-004)
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

## UI-Skizze

```
┌─────────────────────────────┐
│  BoltMind                   │
├──────────┬──────────────────┤
│  Offen   │   Archiv         │
├──────────┴──────────────────┤
│                             │
│  ┌─────────────────────┐    │
│  │ BMW 320d             │    │
│  │ Auftrag: #2024-0815  │    │
│  │ 12 Schritte · Heute  │    │
│  └─────────────────────┘    │
│                             │
│  ┌─────────────────────┐    │
│  │ VW Golf GTI          │    │
│  │ Auftrag: #2024-0712  │    │
│  │ 8 Schritte · Gestern │    │
│  └─────────────────────┘    │
│                             │
│                        [+]  │
└─────────────────────────────┘
```

## Technische Hinweise

- Room-Datenbank: `RepairJob` Entity mit Status-Feld (OPEN, ARCHIVED)
- Compose: `LazyColumn` für die Liste
- Navigation: Compose Navigation zu F-002, F-003, F-004
