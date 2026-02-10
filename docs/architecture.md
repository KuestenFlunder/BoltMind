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
| **Schritt** | Ein einzelner Demontage-Schritt mit Bauteil-Foto (Zustand vor Ausbau) und SchrittTyp (AUSGEBAUT oder AM_FAHRZEUG). Bei AUSGEBAUT zusätzlich Ablageort-Foto. Die Granularität bestimmt der Mechaniker. Jeder Schritt hat eine fortlaufende Schrittnummer. |
| **Ablageort** | Physischer Ort (Werkbank, Tisch etc.) wo ein ausgebautes Teil abgelegt wird. Wird per Foto dokumentiert. Die Schrittnummer dient als Korrelation zwischen App und physischem Ablageort. |
| **SchrittTyp** | Unterscheidet ob ein Bauteil ausgebaut wurde (`AUSGEBAUT` → Ablageort-Foto) oder am Fahrzeug verbleibt (`AM_FAHRZEUG` → kein Ablageort). |
| **ZeitMessung** | Zeitmessung mit Start/Stopp-Timestamps. Eigene Tabelle, verwaltet vom Timer-Service (F-005). Referenziert Schritte über `referenzId` + `referenzTyp`. |
| **Historie** | Chronologische Abfolge aller Schritte eines Reparaturvorgangs. Kann vorwärts (Demontage) und rückwärts (Montage) durchlaufen werden. |
| **Archiv** | Abgeschlossene Reparaturvorgänge werden archiviert und bleiben einsehbar. |

### Demontage-Flow (Auseinanderbau)

```
Mechaniker startet neuen Reparaturvorgang
  → Fahrzeugfoto aufnehmen (System-Kamera), dann Auftragsnummer erfassen (Beschreibung optional)
  → Schritt-Schleife (4 Screens: Preview → Arbeitsphase → Dialog → Preview Ablageort):
      1. Bauteil-Foto aufnehmen (Zustand vor Ausbau) → Preview
      2. Mechaniker baut Teil aus (Arbeitsphase, keine App-Interaktion)
      3. Dialog: "Ausgebaut" (→ Ablageort fotografieren) oder "Am Fahrzeug" (→ nächster Schritt)
      4. Bei AUSGEBAUT: Ablageort-Foto aufnehmen → Preview → nächster Schritt
  → Timer-Service (F-005) misst Arbeitszeit pro Schritt
```

### Montage-Flow (Zusammenbau)

```
Mechaniker öffnet Reparaturvorgang im Montage-Modus
  → Historie wird rückwärts angezeigt (letzter Schritt zuerst)
  → Pro Schritt: Foto + zugehöriger Ablageort sichtbar
  → Mechaniker hakt erledigte Schritte ab
  → Fortschrittsanzeige zeigt verbleibende Schritte
```

### MVP vs. Final

| Aspekt | MVP | Final |
|--------|-----|-------|
| Ablageort-Dokumentation | Foto vom Ablageort | QR-Code-Sticker scannen |
| Datenhaltung | Lokal auf dem Gerät | Lokal + Sharing zwischen Mechanikern |
| Notizen pro Schritt | Nur Bauteil-Foto + Ablageort-Foto | Ggf. Text-/Sprachnotiz |
| Zeiterfassung | Timer-Service pro Schritt | Analyse-Dashboard |
| Kamera | System-Kamera via Intent | Eigene Kamera-Integration |

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
| 1 | **Bedienbarkeit unter Werkstatt-Bedingungen** | Mechaniker haben dreckige/ölige Hände, wenig Zeit, arbeiten im Stehen. Die App muss mit minimalen Interaktionen bedienbar sein - kein langes Tippen, große Buttons, schnelle Kamera. |
| 2 | **Zuverlässigkeit** | Fotos und Ablageort-Zuordnungen dürfen während eines laufenden Reparaturvorgangs niemals verloren gehen. Ein verlorener Schritt kann dazu führen, dass Teile nicht wiedergefunden werden. |
| 3 | **Performance** | Kamera muss sofort auslösen, Schrittübergänge ohne Wartezeit. Die App darf den Arbeitsfluss des Mechanikers nicht bremsen - jede Sekunde Verzögerung stört den Reparaturprozess. |

## 4. Cross-Cutting Concerns

### Persistenz

- **Metadaten** (Reparaturvorgang, Schritte, SchrittTyp): Room-Datenbank
- **Zeitmessungen** (ZeitMessung mit referenzId/referenzTyp): Eigene Room-Tabelle, verwaltet vom Timer-Service (F-005)
- **Fotos** (Bauteil-Fotos, Ablageort-Fotos, Fahrzeugfotos): Filesystem (App-interner Speicher), Pfad-Referenz in der DB
- **Speicherstrategie**: Sofort-Persistierung - jedes Foto und jeder Ablageort wird unmittelbar gespeichert, nicht erst am Schrittende. Kein Datenverlust bei App-Crash, Anruf oder Unterbrechung.

### Foto-Qualität & Speicherplatz

- Mittlere Qualität: Komprimiert, aber Details erkennbar (ca. 2-3 MB pro Foto)
- Ausreichend für Baugruppen-Erkennung und Schrauben-Identifikation
- Kompression beim Speichern, nicht bei der Vorschau

### App-Lifecycle

- Werkstatt-Umgebung: Unterbrechungen (Anrufe, Kollegen, Handy weglegen) sind der Normalfall
- Sofort-Save garantiert, dass kein halbfertiger Schritt verloren geht
- App-Neustart setzt nahtlos am letzten Stand fort

### Datenlöschung

- Mechaniker kann Reparaturvorgänge händisch löschen (offen und archiviert)
- Kaskadierend: Alle zugehörigen Schritte, Fotos und Metadaten werden mitgelöscht
- Bestätigungsdialog vor Löschung (Werkstatt-Bedingungen: kein versehentliches Löschen)

### Permission-Handling

- **Kamera**: System-Kamera via `ActivityResultContracts.TakePicture()` — keine CAMERA-Permission nötig, da die System-Kamera-App die Berechtigung selbst verwaltet
- **Speicher**: Fotos im App-internen Speicher (`filesDir`) — keine Speicher-Permission nötig
- Falls System-Kamera nicht verfügbar: Hinweis an den Nutzer

## 5. Randbedingungen

### Technisch

| Randbedingung | Beschreibung |
|---------------|-------------|
| Plattform | Android (Kotlin, Jetpack Compose) |
| Min SDK | API 26 (Android 8.0) |
| Kamera | System-Kamera via `ActivityResultContracts.TakePicture()` |
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

Feature-Specs werden in `docs/specs/` als Ordner organisiert:

```
docs/specs/
├── governance.md                     # Projektweite Regeln
├── SpecBestPractices.md              # Spec-Schreibregeln
├── F-001-uebersicht/                 # Feature-Ordner
│   ├── README.md                     # Intention, Abhängigkeiten (stabil)
│   └── uebersicht.md                # User Stories, Akzeptanzkriterien
├── F-003-demontage/                  # Komplexes Feature
│   ├── README.md                     # Kontext, Domain-Konzepte
│   ├── workflow.md                   # State Machine
│   └── views/                        # View-Specs
│       ├── preview.md
│       ├── arbeitsphase.md
│       └── dialog.md
└── F-005-zeiterfassung/              # Service-Feature
    ├── README.md                     # Kontext, Abgrenzung
    └── service.md                    # Interface, Entity, Lifecycle
```

GitHub Issues referenzieren Specs im Titel: `[F-001] Beschreibung`
