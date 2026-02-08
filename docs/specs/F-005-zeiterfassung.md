# F-005: Zeiterfassung

## Beschreibung

Die App erfasst automatisch die Dauer jedes Demontage-Schritts. Die Zeiterfassung startet wenn die Kamera für einen neuen Schritt geöffnet wird und endet wenn der Ablageort bestätigt wird. Die Dauer wird dem Mechaniker sichtbar angezeigt.

## Anforderungen

### Funktional

- **Automatischer Start**: Zeiterfassung beginnt wenn Kamera für neuen Schritt öffnet
- **Automatischer Stopp**: Zeiterfassung endet wenn Ablageort bestätigt wird
- **Sichtbare Anzeige**: Laufende Zeit wird im Demontage-Flow (F-003) angezeigt
- **Gespeicherte Dauer**: Pro Schritt wird die Dauer in der DB gespeichert
- **Gesamtdauer**: Reparaturvorgang zeigt Gesamtdauer aller Schritte
- **Übersicht**: In der Vorgangs-Übersicht (F-001) wird die Gesamtdauer angezeigt

### Nicht-Funktional

- Zeiterfassung darf den Workflow nicht stören (kein manuelles Start/Stopp)
- Timer-Anzeige dezent, nicht ablenkend
- Timestamps mit Sekunden-Genauigkeit

## Akzeptanzkriterien

- [ ] Zeiterfassung startet automatisch bei Kamera-Öffnung
- [ ] Zeiterfassung stoppt automatisch bei Ablageort-Bestätigung
- [ ] Laufender Timer wird im Demontage-Flow angezeigt
- [ ] Dauer wird pro Schritt in der DB gespeichert (startedAt, completedAt)
- [ ] Gesamtdauer eines Vorgangs wird berechnet und angezeigt
- [ ] Gesamtdauer erscheint in der Vorgangs-Übersicht

## UI-Integration

### Im Demontage-Flow (F-003)
```
┌─────────────────────────────┐
│  ← Bremsen vorne    Schritt 5│
│                     ⏱ 02:34 │
├─────────────────────────────┤
│      [ Kamera-Vorschau ]    │
│              ...             │
```

### In der Vorgangs-Übersicht (F-001)
```
│  ┌─────────────────────┐    │
│  │ BMW 320d             │    │
│  │ #2024-0815           │    │
│  │ 12 Schritte · 45 min │    │
│  └─────────────────────┘    │
```

## Technische Hinweise

- Nutzt die bestehenden Felder `Step.startedAt` und `Step.completedAt`
- Dauer = `completedAt - startedAt` (berechnet, nicht extra gespeichert)
- Gesamtdauer = Summe aller Schritt-Dauern
- Pausen (App im Hintergrund) werden mitgezählt - bewusste Entscheidung, da der Mechaniker in der Zeit physisch arbeitet
- UI: `LaunchedEffect` mit `delay(1000)` für laufenden Timer
