# BoltMind - Architecture Overview

## 1. Einführung

### Vision

BoltMind ist eine Android-App für Kfz-Mechaniker in Autowerkstätten. Sie unterstützt beim systematischen Auseinander- und Zusammenbau von Fahrzeugkomponenten im Rahmen von Reparaturen.

### Kernidee

- Beim **Auseinanderbau** erstellt der Mechaniker eine Foto-Historie mit Teilreferenzen - Schritt für Schritt wird dokumentiert, welches Teil wo war und wo es abgelegt wird.
- Beim **Zusammenbau** wird die Historie rückwärts abgespielt, sodass nichts vergessen wird und jedes Teil wiedergefunden werden kann.
- Die App verwaltet **Ablageorte** für ausgebaute Teile, damit diese beim Zusammenbau schnell lokalisiert werden können.
- Ein eigenständiger **Timer-Service** (F-005) misst die Arbeitszeit pro Schritt. Der Service verwaltet eigene Daten (`ZeitMessung`-Tabelle) und wird von Consumer-Features (F-003, F-004) angesteuert.

### Zielgruppe

Kfz-Mechaniker in Autowerkstätten, die Reparaturen mit vielen Einzelteilen durchführen und eine visuelle Dokumentation des Demontageprozesses benötigen. Jeder Mechaniker nutzt sein eigenes Werkstatt-Handy.

## 2. Domäne

### Zentrale Begriffe

| Begriff | Beschreibung |
|---------|-------------|
| **Reparaturvorgang** | Ein Reparaturauftrag an einem Fahrzeug. Enthält Fahrzeugfoto, Auftragsnummer und optionale Beschreibung. Mehrere Vorgänge können gleichzeitig offen sein. |
| **Schritt** | Ein einzelner Demontage-Schritt. Hält beliebig viele Fotos (`SchrittFoto`), die über Label beschrieben werden. Kein fester Foto-Slot, kein Schritt-Typ. Die Granularität bestimmt der Mechaniker. Jeder Schritt hat eine fortlaufende Schrittnummer. |
| **SchrittFoto** | Ein einzelnes Foto eines Schritts mit Reihenfolge innerhalb des Schritts und drei kombinierbaren Labeln: Bauteil (Default), Übersicht, Ablageort. Wo eine einzelne Kategorie gebraucht wird, gilt die Priorität Ablageort > Übersicht > Bauteil. |
| **Ablageort** | Physischer Ort (Werkbank, Tisch etc.) wo ein ausgebautes Teil abgelegt wird. Wird als Foto-Label dokumentiert, nicht als eigener Schritt. Ob ein Schritt einen Ablageort hat, ist aus seinen Fotos ableitbar. Die Schrittnummer dient als Korrelation zwischen App und physischem Ablageort. |
| **ZeitMessung** | Zeitmessung mit Start/Stopp-Timestamps. Eigene Tabelle, verwaltet vom Timer-Service (F-005). Referenziert Schritte über `referenzId` + `referenzTyp`. |
| **Historie** | Chronologische Abfolge aller Schritte eines Reparaturvorgangs. Kann vorwärts (Demontage) und rückwärts (Montage) durchlaufen werden. |
| **Archiv** | Abgeschlossene Reparaturvorgänge werden archiviert und bleiben einsehbar. |

### Demontage-Flow (Auseinanderbau)

```
Mechaniker startet neuen Reparaturvorgang
  → Fahrzeugfoto aufnehmen (System-Kamera), dann Auftragsnummer erfassen (Beschreibung optional)
  → Schritt-Schleife (2 States: Kamera (transient) → Schritt-Ansicht):
      1. Schritt N startet → System-Kamera startet automatisch
      2. Foto aufgenommen → sofort als SchrittFoto persistiert (Label "Bauteil") → Schritt-Ansicht
      3. Schritt-Ansicht: Schrittnummer groß, Foto-Karussell dieses Schritts,
         drei Label-Checkboxen am sichtbaren Foto, Thumbnail-Leiste aller Schritte
      4. Aktionen: "Weiteres Foto"     → Kamera, Foto an DENSELBEN Schritt
                 / "Nächster Schritt"  → Schritt abschließen, N+1 anlegen, Kamera
                 / "Beenden"           → Schritt abschließen, zurück zur Übersicht (F-001)
  → Kamera-Abbruch: kein Foto angelegt, zurück in die Schritt-Ansicht
  → Der Ablageort ist ein Foto-Label, kein eigener Schritt und keine eigene View
  → Timer-Service (F-005) misst Arbeitszeit pro Schritt (Anker: Schritt-Ansicht)
```

### Montage-Flow (Zusammenbau)

```
Mechaniker öffnet Reparaturvorgang im Montage-Modus
  → Historie wird rückwärts angezeigt (letzter Schritt zuerst)
  → Pro Schritt: Foto-Karussell aller Fotos des Schritts (horizontal wischbar),
    Ablageort-Fotos über ihr Label erkennbar; Tap vergrößert ein Foto auf Vollbild
  → Horizontale Thumbnail-Leiste über alle Schritte (erstes Foto + Schrittnummer),
    Klick springt direkt zu diesem Schritt — keine Eingabe einer Schrittnummer
  → Mechaniker hakt erledigte Schritte ab
```

### Archiv-Durchblättern (F-001)

```
Mechaniker öffnet einen archivierten Reparaturvorgang
  → Schritt-Browser (F-006) im Nur-Lesen-Modus
  → Gleiche Thumbnail-Leiste und gleiches Foto-Karussell wie in F-003/F-004
  → Keine Bearbeitung: keine Kamera, keine Label-Änderung, kein Abhaken
```

Die Schritt-Navigation (Thumbnail-Leiste, Foto-Karussell, Vollbild) ist als gemeinsames Modul **F-006 Schritt-Browser** ausgelagert und wird von F-003 (Demontage), F-004 (Montage) und F-001 (Archiv-Detailansicht) genutzt — in drei Modi: **bearbeitbar** (F-003 Demontage: Label änderbar, Foto aufnehmen, Schritt-Aktionen), **lesend-mit-Aktionen** (F-004 Montage: Label sichtbar, aber nicht änderbar, kein Foto, Schritt-Aktionen wie "Eingebaut") und **nur-lesen** (F-001 Archiv: nichts änderbar, keine Aktionen). Label werden ausschließlich in der Demontage gesetzt.

### MVP vs. Final

| Aspekt | MVP | Final |
|--------|-----|-------|
| Ablageort-Dokumentation | Foto vom Ablageort | QR-Code-Sticker scannen |
| Datenhaltung | Lokal auf dem Gerät | Lokal + Sharing zwischen Mechanikern |
| Notizen pro Schritt | Nur Fotos mit Labeln (Bauteil/Übersicht/Ablageort) | Ggf. Text-/Sprachnotiz |
| Zeiterfassung | Timer-Service pro Schritt | Analyse-Dashboard |
| Kamera | System-Kamera via `ActivityResultContracts.TakePicture()` | Unverändert System-Kamera — eine eigene Kamera-Integration ist bewusst verworfen (siehe Governance, Abschnitt "Kamera") |

### Entschiedene Fragen

| Frage | Entscheidung |
|-------|-------------|
| Vorlagen für wiederkehrende Abläufe? | Nicht im Scope - Reparaturen sind zu individuell (mechaniker- und fahrzeugspezifisch) |
| Anbindung an Werkstatt-Software? | MVP: Standalone. Integration eventuell später |
| Sharing-Mechanismus? | MVP: keins. Probephase: Bluetooth. Vermarktung: Cloud (Firebase o.ä.) |
| Datensicherung bei Geräteverlust? | MVP: Kein Backup, Risiko akzeptiert |

## 3. Quality Goals

| Priorität | Qualitätsziel | Motivation |
|-----------|--------------|------------|
| 1 | **Bedienbarkeit unter Werkstatt-Bedingungen** | Mechaniker haben dreckige/ölige Hände, wenig Zeit, arbeiten im Stehen. Die App muss mit minimalen Interaktionen bedienbar sein - kein langes Tippen, große Buttons, schnelle Kamera. Die verbindlichen Mindestmaße für Touch-Targets stehen in `docs/specs/governance.md`, Abschnitt "Touch-Targets". |
| 2 | **Zuverlässigkeit** | Fotos und Ablageort-Zuordnungen dürfen während eines laufenden Reparaturvorgangs niemals verloren gehen. Ein verlorener Schritt kann dazu führen, dass Teile nicht wiedergefunden werden. |
| 3 | **Performance** | Kamera muss sofort auslösen, Schrittübergänge ohne Wartezeit. Die App darf den Arbeitsfluss des Mechanikers nicht bremsen - jede Sekunde Verzögerung stört den Reparaturprozess. |

## 4. Cross-Cutting Concerns

### Persistenz

- **Metadaten** (Reparaturvorgang, Schritte, SchrittFoto inkl. Reihenfolge und Labeln): Room-Datenbank
- **Zeitmessungen** (ZeitMessung mit referenzId/referenzTyp): Eigene Room-Tabelle, verwaltet vom Timer-Service (F-005)
- **Fotos** (Schritt-Fotos, Fahrzeugfotos): Filesystem (App-interner Speicher), Pfad-Referenz in der DB
- **Speicherstrategie**: Sofort-Persistierung - jedes Foto und jede Label-Änderung wird unmittelbar gespeichert, nicht erst am Schrittende. Kein Datenverlust bei App-Crash, Anruf oder Unterbrechung.

### Foto-Qualität & Speicherplatz

- **Erwartungswert** (keine erzwingbare Vorgabe): mittlere Qualität, komprimiert, aber Details erkennbar — ca. 2-3 MB pro Foto
- Ausreichend für Baugruppen-Erkennung und Schrauben-Identifikation
- Auflösung und Kompression bestimmt die System-Kamera-App. Die App übernimmt die gelieferte Datei unverändert und komprimiert nicht nach; eine optionale Nachkompression ist in der Governance als offene Frage vermerkt (Governance, Abschnitt "Qualitaet")

### App-Lifecycle

- Werkstatt-Umgebung: Unterbrechungen (Anrufe, Kollegen, Handy weglegen) sind der Normalfall
- Sofort-Save garantiert, dass kein halbfertiger Schritt verloren geht
- App-Neustart setzt nahtlos am letzten Stand fort

### Datenlöschung

- Mechaniker kann Reparaturvorgänge händisch löschen (offen und archiviert)
- Kaskadierend: Alle zugehörigen Schritte, Fotos und Metadaten werden mitgelöscht
- Bestätigungsdialog vor Löschung (Werkstatt-Bedingungen: kein versehentliches Löschen)

### Permission-Handling

**Zielzustand:**

- **Kamera**: projektweit System-Kamera via `ActivityResultContracts.TakePicture()` + `FileProvider` — die CAMERA-Permission entfällt aus dem Manifest, da die System-Kamera-App die Berechtigung selbst verwaltet
- **Speicher**: Fotos im App-internen Speicher (`filesDir`) — keine Speicher-Permission nötig
- Falls System-Kamera nicht verfügbar: Hinweis an den Nutzer

> **Hinweis (Ist-Zustand):** Der Code weicht an **mehreren** Stellen vom Zielzustand ab, nicht nur bei der Permission:
>
> - **F-002 (Neuer Vorgang)** nutzt aktuell noch CameraX und deklariert die CAMERA-Permission. Nach der Umstellung auf die System-Kamera entfallen CAMERA-Permission und CameraX-Dependencies ersatzlos.
> - **F-003 (Demontage)** ist ebenfalls nicht am Zielzustand: die app-eigene Foto-Bestätigung existiert noch (`PreviewView.kt`), dazu `ArbeitsphaseView.kt` und `DemontageDialog.kt`; `DemontageUiState.kt` hält weiterhin die States `PREVIEW_BAUTEIL / ARBEITSPHASE / DIALOG / PREVIEW_ABLAGEORT`; `Schritt.kt` hält `typ`, `bauteilFotoPfad` und `ablageortFotoPfad`, `SchrittTyp.kt` existiert. Die DB steht auf Version 2 — `schritt_foto` und die Migration 2→3 (spezifiziert in `F-003-demontage/README.md`) fehlen.
>
> Diese Liste beschreibt den Ist-Zustand, nicht den Zielzustand. Der Zielzustand ist der oben stehende.

## 5. Randbedingungen

### Technisch

| Randbedingung | Beschreibung |
|---------------|-------------|
| Plattform | Android (Kotlin, Jetpack Compose) |
| Min SDK | API 26 (Android 8.0) |
| Kamera | Zielzustand: projektweit System-Kamera via `ActivityResultContracts.TakePicture()` + `FileProvider`, keine CAMERA-Permission, keine app-eigene Foto-Bestätigung. Ist-Zustand: F-002 nutzt noch CameraX, F-003 hat noch die app-eigene Foto-Bestätigung (`PreviewView`) — beides muss umgestellt werden (siehe Hinweis unter "Permission-Handling"). |
| UI Framework | Jetpack Compose mit Material3 |
| Datenhaltung MVP | Lokal auf dem Gerät |
| Geräte | Werkstatt-Handys (ein Gerät pro Mechaniker) |

### Organisatorisch

| Randbedingung | Beschreibung |
|---------------|-------------|
| Entwicklung | Spec-driven, iterativ in Sprints |
| Specs | Markdown-Dateien in `docs/specs/`, referenziert durch GitHub Issues |
| Versionierung | Git, GitHub |

## 6. Spec-Referenz

Feature-Specs werden in `docs/specs/` als Ordner organisiert. Die Spec-Schreibregeln liegen eine Ebene darüber in `docs/SpecBestPractices.md` (nicht in `docs/specs/`):

```
docs/
├── SpecBestPractices.md              # Spec-Schreibregeln
└── specs/
    ├── governance.md                 # Projektweite Regeln
    ├── F-001-uebersicht/             # Feature-Ordner
    │   ├── README.md                 # Intention, Abhängigkeiten (stabil)
    │   └── uebersicht.md             # User Stories, Akzeptanzkriterien
    ├── F-002-vorgang-anlegen/        # Feature-Ordner
    │   ├── README.md                 # Intention, Abhängigkeiten
    │   └── anlegen.md                # User Stories, Akzeptanzkriterien
    ├── F-003-demontage/              # Komplexes Feature
    │   ├── README.md                 # Kontext, Domain-Konzepte
    │   ├── workflow.md               # State Machine
    │   └── views/                    # View-Specs (eine Datei pro View)
    ├── F-004-montage/                # Feature-Ordner
    │   ├── README.md                 # Kontext, Abhängigkeiten
    │   └── montage.md                # User Stories, Akzeptanzkriterien
    ├── F-005-zeiterfassung/          # Service-Feature
    │   ├── README.md                 # Kontext, Abgrenzung
    │   └── service.md                # Interface, Entity, Lifecycle
    └── F-006-schritt-browser/        # Gemeinsames Modul (F-001, F-003, F-004)
        ├── README.md                 # Kontext, Nutzer-Features, Modi
        └── browser.md                # User Stories, Akzeptanzkriterien
```

GitHub Issues referenzieren Specs im Titel: `[F-001] Beschreibung`
