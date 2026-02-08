# F-004: Montage-Flow

## Beschreibung

Der Mechaniker öffnet einen Reparaturvorgang im Montage-Modus. Die Demontage-Schritte werden in umgekehrter Reihenfolge angezeigt (letzter Schritt zuerst). Pro Schritt sieht der Mechaniker das Foto und den zugehörigen Ablageort, um die Teile wiederzufinden und korrekt einzubauen. Erledigte Schritte werden abgehakt.

## Anforderungen

### Funktional

- **Rückwärts-Ansicht**: Schritte werden in umgekehrter Reihenfolge angezeigt (letzter Demontage-Schritt = erster Montage-Schritt)
- **Schritt-Anzeige**: Großes Foto + Ablageort-Nummer prominent sichtbar
- **Abhaken**: Mechaniker markiert erledigte Schritte als "eingebaut"
- **Fortschrittsanzeige**: Zeigt verbleibende Schritte (z.B. "Schritt 3 von 15, noch 12")
- **Navigation**: Blättern zwischen Schritten (vor/zurück), nicht nur linear
- **Suche nach Ablageort**: Mechaniker kann über Ablageort-Nummer den passenden Schritt finden
- **Abschluss**: Wenn alle Schritte abgehakt sind, kann der Vorgang archiviert werden

### Nicht-Funktional

- Foto muss groß und klar angezeigt werden (Details erkennbar)
- Ablageort-Nummer muss sofort sichtbar sein (kein Scrollen)
- Abhaken mit einem Tap (Quality Goal #1)

## Akzeptanzkriterien

- [ ] Schritte werden in umgekehrter Reihenfolge angezeigt
- [ ] Pro Schritt: Foto + Ablageort-Nummer sichtbar
- [ ] Schritte können als erledigt abgehakt werden
- [ ] Fortschrittsanzeige zeigt aktuelle Position und verbleibende Schritte
- [ ] Blättern zwischen Schritten möglich (nicht nur linear)
- [ ] Suche nach Ablageort-Nummer findet den passenden Schritt
- [ ] Nach Abhaken aller Schritte: Vorgang kann archiviert werden
- [ ] Archivierter Vorgang erscheint im Archiv-Tab (F-001)

## UI-Skizze

### Montage-Schritt
```
┌─────────────────────────────┐
│  ← Bremsen vorne            │
│    Schritt 3 von 15         │
│    ████████░░░░░░  (20%)    │
├─────────────────────────────┤
│                             │
│   ┌─────────────────────┐   │
│   │                     │   │
│   │   [Foto: Baugruppe] │   │
│   │                     │   │
│   └─────────────────────┘   │
│                             │
│   Ablageort: [ 7 ]         │
│                             │
│  ┌─────────────────────┐    │
│  │    ✓ EINGEBAUT       │    │
│  └─────────────────────┘    │
│                             │
│   ◄ Zurück    Weiter ►     │
└─────────────────────────────┘
```

### Alle Schritte erledigt
```
┌─────────────────────────────┐
│  ← Bremsen vorne            │
│    Alle 15 Schritte erledigt│
│    ████████████████ (100%)  │
├─────────────────────────────┤
│                             │
│         ✓                   │
│   Zusammenbau               │
│   abgeschlossen!            │
│                             │
│  ┌─────────────────────┐    │
│  │    ARCHIVIEREN       │    │
│  └─────────────────────┘    │
│                             │
└─────────────────────────────┘
```

## Technische Hinweise

- Query: `SELECT * FROM steps WHERE repairJobId = :id ORDER BY sequenceNumber DESC`
- Abhaken: `Step.completedInReassembly = true` (Boolean-Feld in Step-Entity)
- Fortschritt: `completedCount / totalCount`
- Ablageort-Suche: Filterung der Step-Liste nach `storageLocationNumber`
- Archivierung: `RepairJob.status = ARCHIVED`
