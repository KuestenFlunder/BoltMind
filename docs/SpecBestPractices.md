# Spec Best Practices - BoltMind

## Grundprinzip

Specs sind die **Source of Truth**. Code dient der Spec, nicht umgekehrt.
Jede Spec muss so konkret sein, dass daraus direkt Issues und Tests ableitbar sind.

## Hierarchie

```
Feature-Spec (Ordner pro Feature, siehe "Spec-Ordner-Struktur")
├── User Story 1 → Akzeptanzkriterien (Given/When/Then)
├── User Story 2 → Akzeptanzkriterien (Given/When/Then)
└── User Story N → Akzeptanzkriterien (Given/When/Then)
```

Jede User Story kann zu einem eigenen Issue werden.

Der Regelfall ist der **Ordner** (README + Detail-Specs), nicht die Einzeldatei — wann welches,
steht unter [Wann Ordner, wann Einzeldatei?](#wann-ordner-wann-einzeldatei).

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
- UI-Framework- und Bibliotheks-Entscheidungen (Compose, Material3, Coil, Room etc.)
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
- **Issue-Titel**: `[F-XXX] Beschreibung (US-XXX.N)` — das Feature-Kuerzel steht vorn, die Story-ID
  in Klammern. Das ist die gelebte und gueltige Konvention (siehe `CLAUDE.md`, `docs/architecture.md`).
  Die frueheren F-001/F-002-Issues (#13–#20) nutzen noch `[US-XXX.N] Titel` — Altbestand, nicht nachahmen.
- **Issue-Body**: `## Kontext` → `## Spec-Referenz` (Pfad + Abschnitt) → `## Aufgaben` (Checkliste)
  → `## Akzeptanzkriterien` (Copy aus der Spec)
- **Labels**: Feature-Label (z.B. `F-001`)
- **Milestone**: Liefer-Welle (`R1`, `R2`, …), nicht Feature-Phase

### Zuschnitt: Vertical Slices

Ein Issue geht durch **alle Schichten** — Datenschicht, ViewModel, UI, Tests — und liefert fuer sich
genommen Nutzerwert. Nicht nach Schichten schneiden.

**Nicht mehr verwenden:** Die frueheren Milestones `F-XXX-A` (Datenschicht), `F-XXX-B` (ViewModel),
`F-XXX-C` (UI) waren ein Schichtenschnitt. Er hat sich nicht bewaehrt: eine Datenschicht ohne
Oberflaeche ist nicht abnehmbar, und die Issues einer Schicht altern gemeinsam, wenn sich die Spec
aendert. Milestones buendeln stattdessen Scheiben zu einer Liefer-Welle.

### Ausnahme: Issues ohne besitzende User Story

Manche Arbeit folgt aus `governance.md` oder `docs/CODING_RULES.md` statt aus einer User Story —
etwa ein Design-Token, eine Test-Infrastruktur oder eine projektweite Invariante. Eine US-Nummer zu
erfinden waere unehrlich, weil der Reviewer das Kriterium dann in keiner Spec findet.

Solche Issues tragen das Praefix **`[Governance]`** ohne US-Suffix, das Label `infra` und alle
beruehrten `F-XXX`-Label. Sie duerfen den Nutzerwert-Test nur ueberspringen, wenn **alle drei**
Bedingungen gelten:

1. Mindestens **zwei verschiedene Scheiben** brauchen sie — sonst gehoert die Arbeit in die eine Scheibe.
2. Sie sind **allein verifizierbar**: ein Test oder ein Gradle-Task wird gruen.
3. Der Body hat einen Abschnitt **`## Entblockt`**, der die abhaengigen Scheiben namentlich nennt.

## Von der Spec zum Test

Akzeptanzkriterien werden direkt zu Testnamen:
```
Given ein leerer Reparaturvorgang existiert
When  der Mechaniker den Vorgang antippt
Then  öffnet sich direkt der Demontage-Flow

→ Test: `oeffnet Demontage-Flow direkt wenn Vorgang keine Schritte hat`
```

## Spec-Ordner-Struktur (Feature-First + Service-Features + Komponenten-Module)

Komplexe Features werden als Ordner organisiert. Cross-Cutting Concerns werden zu eigenstaendigen Service-Features hochgestuft, gemeinsam genutzte UI-Bausteine zu eigenstaendigen Komponenten-Modulen.

Die Spec-Schreibregeln (diese Datei) liegen **eine Ebene ueber** den Specs, nicht in `docs/specs/`.

```
docs/
├── SpecBestPractices.md              # Diese Datei
└── specs/
    ├── governance.md                 # Projektweite Regeln (Sofort-Save, Debounce, DDD)
    │
    ├── F-001-uebersicht/
    │   ├── README.md
    │   └── uebersicht.md
    ├── F-002-vorgang-anlegen/
    │   ├── README.md
    │   └── anlegen.md
    │
    ├── F-003-demontage/              # Komplexes Feature → Ordner
    │   ├── README.md                 # Kontext, Domain-Konzepte, Entity-Definitionen
    │   ├── workflow.md               # State Machine, Transitions (eigener Aenderungsgrund)
    │   └── views/                    # View-Specs (UI + DB zusammen, gleicher Aenderungsgrund)
    │       └── schritt-ansicht.md
    │
    ├── F-004-montage/
    │   ├── README.md
    │   └── montage.md
    │
    ├── F-005-zeiterfassung/          # Service-Feature → Ordner
    │   ├── README.md                 # Kontext, Abgrenzung, Consumer-Uebersicht
    │   └── service.md                # Interface, Entity, Lifecycle
    │
    └── F-006-schritt-browser/        # Gemeinsames Komponenten-Modul (F-001, F-003, F-004)
        ├── README.md                 # Kontext, Modi, Consumer-Uebersicht
        └── browser.md                # User Stories, Interface
```

### Wann Ordner, wann Einzeldatei?

- **Einzeldatei:** Feature hat ≤1 View, keinen komplexen Workflow, passt in <200 Zeilen
- **Ordner:** Feature hat >1 View ODER einen eigenstaendigen Workflow ODER ist ein Service
  ODER ist ein gemeinsames Komponenten-Modul

### Service-Features

Cross-Cutting Concerns die mehrere Features durchschneiden, werden als **eigenstaendige Service-Features** behandelt:

1. Der Service beschreibt nur sich selbst (Interface, Entity, Lifecycle)
2. Der Service kennt keine Consumer-Features
3. Die **Consumer** beschreiben in ihren eigenen Specs, wie sie den Service nutzen
4. Keine Integration-Dateien im Service-Ordner — Abhaengigkeitsrichtung: Consumer → Service

### Gemeinsame Komponenten-Module

Neben Features und Service-Features gibt es einen dritten Typ: die **gemeinsame UI-Komponente**,
die mehrere Features identisch verwenden (Beispiel: F-006 Schritt-Browser, genutzt von F-001,
F-003 und F-004). Sie bekommt eine eigene `F-XXX`-Nummer und einen eigenen Ordner. Abgrenzung
zum Service-Feature:

| | Service-Feature (F-005) | Komponenten-Modul (F-006) |
|---|---|---|
| Eigene Tabelle / Datenhaltung | ja | nein |
| Eigenes ViewModel / Repository-Zugriff | ja | nein — zustandslos, State Hoisting |
| Kennt seine Consumer | nein | nein |
| Consumer beschreibt die Integration | ja, in seiner eigenen Spec | ja, in seiner eigenen Spec |

Fuer beide gilt dieselbe Abhaengigkeitsrichtung: **Consumer → Modul**. Das Modul beschreibt nur
sich selbst (Interface, Modi, Verhalten), nie wo seine Daten herkommen oder angezeigt werden.

### Pfeil-Konvention in Abhaengigkeits-Tabellen

In den `README.md`-Abhaengigkeitstabellen bedeutet der Pfeil immer dasselbe:

- `→ F-XXX` — **dieses** Feature nutzt oder ruft F-XXX
- `← F-XXX` — F-XXX nutzt oder ruft **dieses** Feature

Dieselbe Beziehung wird in beiden beteiligten Specs mit entgegengesetztem Pfeil notiert.

## Single Responsibility fuer Specs

Jede Spec-Datei hat **einen Grund sich zu aendern:**

| Spec-Typ | Aenderungsgrund | Beispiel |
|---|---|---|
| View-Spec | UI-Elemente oder DB-Interaktion dieser View aendern sich | schritt-ansicht.md: Neuer Aktions-Button oder neues Label-Flag |
| Workflow-Spec | Reihenfolge oder Bedingungen der Transitionen aendern sich | workflow.md: Neuer State, andere Transition |
| Service-Spec | Das Service-Interface oder die Entity aendert sich | service.md: Neuer Parameter, neues Feld |
| Komponenten-Spec | Interface, Modi oder Verhalten der geteilten Komponente aendern sich | browser.md: Neuer Modus, neuer Callback |
| README | Kontext, Domain-Konzepte oder Entity-Definitionen aendern sich | README.md: Neues Foto-Label |

**Faustregel:** Wenn eine Aenderung an der UI auch immer eine Aenderung an der DB-Interaktion nach sich zieht (oder umgekehrt), gehoeren sie in die gleiche Datei (View-Spec). Wenn ein Workflow sich unabhaengig von den Views aendern kann, gehoert er in eine eigene Datei.

## Anti-Patterns

- **Zu lang**: Spec wird so lang, dass beim Review nur überflogen wird
- **Zu technisch**: Stories beschreiben Implementierung statt Nutzerverhalten
- **Zu vage**: "System soll schnell sein" → nicht testbar
- **Gemischt**: Business-Intent und technische Details in derselben Sektion
- **Abhängige Stories**: Story B funktioniert nur wenn Story A fertig ist → Story aufteilen oder zusammenlegen
- **Dual-Purpose-Felder**: Ein DB-Feld dient zwei verschiedenen Zwecken (Workflow + Timer) → Eigene Tabelle/Entity
- **Service kennt Consumer**: Der Service beschreibt, wo seine Daten angezeigt werden → Consumer beschreibt das selbst
- **Kopierte Logik**: Gleiche Timer-Logik in F-003 und F-004 separat beschrieben → Service-Feature extrahieren
