# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
./gradlew assembleDebug          # Debug-Build
./gradlew test                   # Unit-Tests
./gradlew connectedAndroidTest   # Instrumented Tests (Emulator/Gerät)
./gradlew lint                   # Android Lint
./gradlew ktlintCheck            # Kotlin Formatting Check
./gradlew ktlintFormat           # Auto-Format
./gradlew detekt                 # Static Analysis
```

Single test: `./gradlew test --tests "com.boltmind.app.ClassName.testName"`

Build target: compileSdk 36, minSdk 26, Java 17.

## Architecture

MVVM mit feature-basierter Package-Struktur:

```
Screen (Stateless @Composable) → observes StateFlow
  ViewModel (State + Events, max 200 LOC) → calls
    Repository (Data Access) → queries
      Room DAO → SQLite
```

Package-Layout unter `com.boltmind.app/`:

```
di/                     # Koin Module
data/local/             # Room Database, DAOs
data/model/             # Room Entities
data/repository/        # Repositories
feature/uebersicht/     # F-001: Vorgangs-Übersicht
feature/neuervorgang/   # F-002: Vorgang anlegen
feature/demontage/      # F-003: Demontage-Flow
feature/montage/        # F-004: Montage-Flow
service/zeiterfassung/  # F-005: Timer-Service
ui/navigation/          # NavHost
ui/theme/               # Material3 Theme
```

Jedes Feature-Package enthält: `*Screen.kt`, `*ViewModel.kt`, `*UiState.kt`.
Service-Packages enthalten: `*Service.kt`, `*Dao.kt`, Entity.

## Domain-Sprache (DDD)

Domain-Begriffe auf **Deutsch**, technische Begriffe auf **Englisch**:

| Domain (DE) | Bedeutung | Beispiel-Code |
|---|---|---|
| Reparaturvorgang | Repair job | `Reparaturvorgang.kt`, `ReparaturRepository` |
| Schritt | Disassembly step | `Schritt.kt`, `SchrittDao` |
| SchrittTyp | Step type (AUSGEBAUT/AM_FAHRZEUG) | `schrittTyp: SchrittTyp` |
| Bauteil-Foto | Component photo (before removal) | `bauteilFotoPfad: String` |
| Ablageort-Foto | Storage location photo | `ablageortFotoPfad: String?` |
| Fahrzeugfoto | Vehicle photo | `fahrzeugFotoPfad: String` |
| Auftragsnummer | Order number | `auftragsnummer: String` |
| ZeitMessung | Time measurement | `ZeitMessung.kt`, `ZeitMessungDao` |

Funktionsnamen für Domain-Events ebenfalls Deutsch: `onFotoAufgenommen()`, `onAblageortBestaetigt()`.

## Spec-driven Development

Feature-Specs in `docs/specs/F-XXX-name/` (Ordnerstruktur mit README.md + Detail-Specs) sind Source of Truth. Projektweite Regeln stehen in `docs/specs/governance.md`. Issues referenzieren Specs via `[F-XXX]` im Titel. Immer Spec lesen bevor ein Feature implementiert wird.

## Pflichtlektüre vor Code-Änderungen

**VOR jeder Implementierung** müssen gelesen werden:
1. `docs/CODING_RULES.md` — Vollständige Coding-Konventionen und Test-Struktur
2. `docs/specs/governance.md` — Projektweite Regeln (Sofort-Save, Foto-Handling, DDD, Service-Architektur)
3. `docs/specs/F-XXX-name/README.md` — Feature-Intention und Abhängigkeiten
4. `docs/specs/F-XXX-name/*.md` — Detail-Specs (User Stories, Workflow, Views)

## Coding Conventions

- **DI**: Koin (Module in `di/`)
- **State**: `MutableStateFlow<UiState>` im ViewModel, Screen erhält State + Callbacks
- **Persistence**: Room DB für Metadaten, Filesystem für Fotos. Sofort-Save bei jeder Aktion
- **Strings**: Alle UI-Strings in `res/values/strings.xml`, keine Hardcoded-Strings
- **Coroutines**: `viewModelScope` verwenden, kein `GlobalScope`
- **DB-Zugriffe**: Immer asynchron (suspend functions)
- **Compose**: State Hoisting, `@Preview` für jeden Screen

## TDD Workflow (verbindlich)

TDD ist keine Empfehlung, sondern Pflicht bei JEDER Code-Änderung (Feature, Bugfix, Refactoring).

**Zwingender Zyklus:**
```
1. RED:      Test schreiben/anpassen → ./gradlew test (muss fehlschlagen)
2. GREEN:    Implementieren          → ./gradlew test (muss grün sein)
3. REFACTOR: Aufräumen               → ./gradlew test + ktlintCheck + detekt (grün)
```

**Verboten:** Code ohne vorherigen Test schreiben. Tests deaktivieren/löschen um Build grün zu bekommen.

**Tests bilden User Stories ab:** Jede `US-XXX.Y` wird als `@Nested inner class` im Test abgebildet. Testnamen leiten sich aus den Given/When/Then-Akzeptanzkriterien der Spec ab. Details und Beispiele in `docs/CODING_RULES.md`.

## Git Workflow

Branch-Naming: `feature/F-XXX/kurze-beschreibung`
Commit-Prefix: `[F-XXX] Beschreibung`

## Key Documentation

- `docs/architecture.md` — Arc42-light Architektur-Übersicht
- `docs/CODING_RULES.md` — Vollständige Coding-Konventionen
- `docs/specs/governance.md` — Projektweite Regeln (Sofort-Save, DDD, Service-Architektur)
- `docs/specs/SpecBestPractices.md` — Spec-Schreibregeln und Ordner-Struktur
- `docs/specs/F-XXX-name/` — Feature-Spezifikationen als Ordner (F-001 bis F-005)

## Verbotene Patterns

- Business-Logik in Composables
- ViewModel > 200 LOC
- Synchrone DB-Calls auf Main-Thread
- Wildcard-Imports
- `GlobalScope`
