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
ui/navigation/          # NavHost
ui/theme/               # Material3 Theme
```

Jedes Feature-Package enthält: `*Screen.kt`, `*ViewModel.kt`, `*UiState.kt`.

## Domain-Sprache (DDD)

Domain-Begriffe auf **Deutsch**, technische Begriffe auf **Englisch**:

| Domain (DE) | Bedeutung | Beispiel-Code |
|---|---|---|
| Reparaturvorgang | Repair job | `Reparaturvorgang.kt`, `ReparaturRepository` |
| Schritt | Disassembly step | `Schritt.kt`, `SchrittDao` |
| Ablageort | Storage location | `ablageortNummer: Int` |
| Fahrzeug | Vehicle | `fahrzeug: String` |
| Auftragsnummer | Order number | `auftragsnummer: String` |

Funktionsnamen für Domain-Events ebenfalls Deutsch: `onFotoAufgenommen()`, `onAblageortBestaetigt()`.

## Spec-driven Development

Feature-Specs in `docs/specs/F-XXX-*.md` sind Source of Truth. Issues referenzieren Specs via `[F-XXX]` im Titel. Immer Spec lesen bevor ein Feature implementiert wird.

## Coding Conventions

- **DI**: Koin (Module in `di/`)
- **State**: `MutableStateFlow<UiState>` im ViewModel, Screen erhält State + Callbacks
- **Persistence**: Room DB für Metadaten, Filesystem für Fotos. Sofort-Save bei jeder Aktion
- **Strings**: Alle UI-Strings in `res/values/strings.xml`, keine Hardcoded-Strings
- **Coroutines**: `viewModelScope` verwenden, kein `GlobalScope`
- **DB-Zugriffe**: Immer asynchron (suspend functions)
- **Compose**: State Hoisting, `@Preview` für jeden Screen

## TDD Workflow

Test-first nach Spec-Anforderungen:
1. Test schreiben der Akzeptanzkriterium abbildet
2. Implementieren bis Test grün
3. Refactorn

Testnamen beschreiben Spec-Verhalten: `sollte Reparaturvorgang mit Fahrzeug anlegen`.

## Git Workflow

Branch-Naming: `feature/F-XXX/kurze-beschreibung`
Commit-Prefix: `[F-XXX] Beschreibung`

## Key Documentation

- `docs/architecture.md` — Arc42-light Architektur-Übersicht
- `docs/CODING_RULES.md` — Vollständige Coding-Konventionen
- `docs/specs/` — Feature-Spezifikationen (F-001 bis F-005)

## Verbotene Patterns

- Business-Logik in Composables
- ViewModel > 200 LOC
- Synchrone DB-Calls auf Main-Thread
- Wildcard-Imports
- `GlobalScope`
