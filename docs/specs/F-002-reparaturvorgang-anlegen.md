# F-002: Reparaturvorgang anlegen

## Beschreibung

Der Mechaniker erstellt einen neuen Reparaturvorgang über einen Initialdialog. Nach Eingabe der Pflichtfelder wird der Vorgang sofort in der Datenbank gespeichert und der Demontage-Flow (F-003) geöffnet.

## Anforderungen

### Funktional

- Eingabeformular mit vier Feldern:
  - **Fahrzeug-Bezeichnung** (Pflicht, Freitext) - z.B. "BMW 320d Blau"
  - **Auftragsnummer** (Pflicht, Freitext) - z.B. "#2024-0815"
  - **Beschreibung** (Pflicht, Freitext) - z.B. "Bremsen vorne wechseln"
  - **Anzahl Ablageorte** (Pflicht, Numpad) - z.B. "10". Definiert den Bereich für die Auto-Hochzählung im Demontage-Flow (F-003).
- Sofortige Speicherung in Room-DB beim Bestätigen
- Nach Speicherung: Direkter Übergang in den Demontage-Flow (F-003)
- Abbrechen kehrt zur Vorgangs-Übersicht (F-001) zurück

### Nicht-Funktional

- Große Eingabefelder, gut bedienbar mit Handschuhen/öligen Händen (Quality Goal #1)
- Speicherung sofort, kein Datenverlust (Quality Goal #2)
- Minimale Pflichtfelder, schnell ausfüllbar

## Akzeptanzkriterien

- [ ] Formular mit Fahrzeug, Auftragsnummer, Beschreibung, Anzahl Ablageorte wird angezeigt
- [ ] Alle vier Felder sind Pflicht, Validierung vor Speicherung
- [ ] Anzahl Ablageorte wird als Zahl eingegeben (Numpad)
- [ ] Vorgang wird sofort in der DB gespeichert
- [ ] Nach Speicherung öffnet sich der Demontage-Flow
- [ ] Abbrechen kehrt zur Übersicht zurück ohne zu speichern

## UI-Skizze

```
┌─────────────────────────────┐
│  ← Neuer Vorgang            │
├─────────────────────────────┤
│                             │
│  Fahrzeug                   │
│  ┌─────────────────────┐    │
│  │ BMW 320d Blau        │    │
│  └─────────────────────┘    │
│                             │
│  Auftragsnummer             │
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
│  Anzahl Ablageorte          │
│  ┌─────────────────────┐    │
│  │ 10                   │    │
│  └─────────────────────┘    │
│                             │
│  ┌─────────────────────┐    │
│  │     STARTEN          │    │
│  └─────────────────────┘    │
└─────────────────────────────┘
```

## Technische Hinweise

- Room Entity: `RepairJob(id, vehicle, orderNumber, description, storageLocationCount, status, createdAt)`
- Sofort-Insert in DB, dann Navigation zu F-003 mit `repairJobId`
