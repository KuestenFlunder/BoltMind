# Spec Best Practices - BoltMind

## Grundprinzip

Specs sind die **Source of Truth**. Code dient der Spec, nicht umgekehrt.
Jede Spec muss so konkret sein, dass daraus direkt Issues und Tests ableitbar sind.

## Hierarchie

```
Feature-Spec (1 Markdown-Datei pro Feature/Workflow)
├── User Story 1 → Akzeptanzkriterien (Given/When/Then)
├── User Story 2 → Akzeptanzkriterien (Given/When/Then)
└── User Story N → Akzeptanzkriterien (Given/When/Then)
```

Jede User Story kann zu einem eigenen Issue werden.

## Spec-Datei Aufbau

```markdown
# F-XXX: Feature-Name

## Kontext
Warum existiert dieses Feature? Welches Problem löst es?
Wer ist der primäre Nutzer und in welcher Situation befindet er sich?

## User Stories

### US-XXX.1: Story-Titel
**Als** [Rolle]
**möchte ich** [Aktion/Fähigkeit]
**damit** [Nutzen/Ergebnis]

#### Akzeptanzkriterien
- **Given** [Ausgangszustand]
  **When** [Aktion des Nutzers]
  **Then** [Erwartetes Ergebnis]

- **Given** [Ausgangszustand]
  **When** [Aktion des Nutzers]
  **Then** [Erwartetes Ergebnis]

#### UI-Verhalten
[Nur wenn nötig: Skizze oder Beschreibung des UI-Verhaltens]

---

### US-XXX.2: Nächste Story
[...]

## Nicht-funktionale Anforderungen
- Performance, Bedienbarkeit, Zuverlässigkeit (Referenz auf Quality Goals)

## Technische Hinweise
- Getrennt von den Stories
- Datenmodell, Entity-Definitionen, technische Entscheidungen
- Hier darf "Wie" stehen, in den Stories nur "Was" und "Warum"

## Offene Fragen
- Explizit markiert mit [OFFEN], werden vor Implementierung geklärt
```

## User Story Regeln

### INVEST-Kriterien
Jede Story muss sein:
- **I**ndependent: Unabhängig von anderen Stories implementierbar
- **N**egotiable: Details verhandelbar, nicht in Stein gemeißelt
- **V**aluable: Liefert eigenständigen Nutzen für den User
- **E**stimable: Aufwand einschätzbar
- **S**mall: In einem Sprint umsetzbar
- **T**estable: Akzeptanzkriterien sind objektiv prüfbar

### Story-Formulierung
- **Als** [Rolle] - Wer will etwas? (z.B. "Als Mechaniker")
- **möchte ich** [Aktion] - Was will er tun? (konkrete Handlung)
- **damit** [Nutzen] - Warum? (Geschäftswert, nicht technischer Grund)

**Schlecht**: "Als User möchte ich einen Button damit ich klicken kann"
**Gut**: "Als Mechaniker möchte ich ein Foto vom Fahrzeug aufnehmen, damit ich den Vorgang in der Übersicht visuell wiederfinde"

### Was NICHT in die Story gehört
- Technische Implementierungsdetails (gehört in "Technische Hinweise")
- UI-Framework-Entscheidungen (Compose, CameraX etc.)
- Datenbank-Felder oder Entity-Definitionen

## Akzeptanzkriterien Regeln

### Given/When/Then Format (bevorzugt)
```
Given: Beschreibt den Ausgangszustand / die Vorbedingung
When:  Beschreibt die Aktion des Nutzers (ein konkreter Trigger)
Then:  Beschreibt das erwartete, beobachtbare Ergebnis
```

### Qualitätsmerkmale
- **Testbar**: Jedes Kriterium wird zu mindestens einem Test
- **Beobachtbar**: Ergebnis ist von außen sichtbar (UI-Zustand, DB-Zustand, Navigation)
- **Eindeutig**: Kein Interpretationsspielraum
- **Atomar**: Ein Kriterium testet eine Sache

### Beispiel
```markdown
- **Given** ein Reparaturvorgang mit 5 Demontage-Schritten existiert
  **When** der Mechaniker den Vorgang in der Übersicht antippt
  **Then** erscheint ein Auswahl-Dialog mit "Weiter demontieren" und "Montage starten"
```

## Nummerierung

- Feature: `F-XXX` (z.B. F-001, F-002)
- User Story: `US-XXX.N` (z.B. US-001.1, US-001.2)
- Akzeptanzkriterium: Implizit über Given/When/Then (wird im Test zum Methodennamen)

## Von der Spec zum Issue

Jede User Story wird zu einem GitHub Issue:
- **Issue-Titel**: `[US-XXX.N] Story-Titel`
- **Issue-Body**: Story-Text + Akzeptanzkriterien aus der Spec (Copy)
- **Labels**: Feature-Label (z.B. `F-001`)
- **Milestone**: Sprint-Zuordnung

## Von der Spec zum Test

Akzeptanzkriterien werden direkt zu Testnamen:
```
Given ein leerer Reparaturvorgang existiert
When  der Mechaniker den Vorgang antippt
Then  öffnet sich direkt der Demontage-Flow

→ Test: `oeffnet Demontage-Flow direkt wenn Vorgang keine Schritte hat`
```

## Anti-Patterns

- **Zu lang**: Spec wird so lang, dass beim Review nur überflogen wird
- **Zu technisch**: Stories beschreiben Implementierung statt Nutzerverhalten
- **Zu vage**: "System soll schnell sein" → nicht testbar
- **Gemischt**: Business-Intent und technische Details in derselben Sektion
- **Abhängige Stories**: Story B funktioniert nur wenn Story A fertig ist → Story aufteilen oder zusammenlegen
