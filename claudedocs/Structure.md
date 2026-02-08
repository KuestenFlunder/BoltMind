# BoltMind Architecture Overview

## Executive Summary

BoltMind ist eine Android-App für Kfz-Mechaniker, die den Demontage-/Montage-Prozess bei Reparaturen durch Foto-Historien mit Ablageort-Referenzen unterstützt. Frühe Projektphase: Boilerplate steht, Specs werden erarbeitet, keine Business-Logik implementiert.

**Key Statistics:**
- Kotlin Source Files: 5 (Boilerplate)
- Entities/Models: 0 (noch nicht implementiert)
- Screens/Components: 1 (Hello World)
- Tests: 0

---

## Technology Stack

| Layer | Technologie | Version |
|-------|------------|---------|
| **Sprache** | Kotlin | 2.1.10 |
| **UI** | Jetpack Compose + Material3 | BOM 2025.05.01 |
| **Kamera** | CameraX | 1.4.2 |
| **Image Loading** | Coil Compose | 2.7.0 |
| **Persistenz (geplant)** | Room | TBD |
| **Build** | Gradle (Kotlin DSL) | 8.14 |
| **AGP** | Android Gradle Plugin | 8.10.1 |
| **Min SDK** | API 26 (Android 8.0) | - |
| **Target/Compile SDK** | API 36 | - |
| **JVM** | Java 17 | - |

---

## Project Structure

```
BoltMind/
├── app/
│   ├── build.gradle.kts                    # App module config
│   ├── proguard-rules.pro                  # R8/ProGuard rules
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml         # Permissions: CAMERA, READ_MEDIA_IMAGES
│       │   ├── java/com/boltmind/app/
│       │   │   ├── BoltMindApplication.kt  # Application class (empty)
│       │   │   ├── MainActivity.kt         # Entry point, Compose host
│       │   │   └── ui/theme/
│       │   │       ├── Color.kt            # Color definitions
│       │   │       ├── Theme.kt            # Material3 theme + dynamic color
│       │   │       └── Type.kt             # Typography
│       │   └── res/
│       │       ├── drawable/               # Vector icons
│       │       ├── mipmap-anydpi-v26/      # Adaptive launcher icon
│       │       └── values/                 # strings, themes, colors
│       ├── test/                           # Unit tests (empty)
│       └── androidTest/                    # Instrumented tests (empty)
├── docs/
│   ├── architecture.md                     # Arc42-light: Vision, Domain, Quality Goals
│   └── specs/                              # Feature specs (F-XXX) & non-functional (NF-XXX)
├── gradle/
│   ├── libs.versions.toml                  # Version catalog (all dependencies)
│   └── wrapper/
├── build.gradle.kts                        # Root build config
├── settings.gradle.kts                     # Project settings
├── gradle.properties                       # Gradle JVM/Android settings
└── claudedocs/
    └── Structure.md                        # This file
```

---

## Domain Model (geplant, noch nicht implementiert)

```
┌──────────────────────────────────────┐
│        Reparaturvorgang              │
├──────────────────────────────────────┤
│ id, fahrzeug, auftragsnummer,        │
│ beschreibung, status, createdAt      │
└──────────────────────────────────────┘
         │ (OneToMany)
         ↓
┌──────────────────────────────────────┐
│            Schritt                   │
├──────────────────────────────────────┤
│ id, reparaturvorgangId, fotoPath,    │
│ ablageortNummer, reihenfolge,        │
│ startedAt, completedAt              │
└──────────────────────────────────────┘
```

**Geplante Entities:**

| Entity | Felder (MVP) | Beziehungen |
|--------|-------------|-------------|
| **Reparaturvorgang** | id, fahrzeug, auftragsnummer, beschreibung, status (offen/archiviert), createdAt | 1:N → Schritt |
| **Schritt** | id, reparaturvorgangId, fotoPath, ablageortNummer, reihenfolge, startedAt, completedAt | N:1 → Reparaturvorgang |

**Persistenz-Strategie:**
- Room DB: Metadaten (Reparaturvorgang, Schritt)
- Filesystem: Fotos (App-interner Speicher, Pfad in DB referenziert)
- Sofort-Persistierung bei jeder Aktion

---

## Flows

### Demontage (Auseinanderbau)
```
Neuer Vorgang → Initialdialog → [Foto → Ausbau(offline) → Ablageort-Eingabe]* → Abschluss
```

### Montage (Zusammenbau)
```
Vorgang öffnen → Rückwärts-Ansicht → [Foto+Ablageort anzeigen → Abhaken]* → Fertig → Archiv
```

---

## Quality Goals

| Prio | Ziel | Implikation |
|------|------|-------------|
| 1 | Bedienbarkeit (Werkstatt) | Große Buttons, wenig Tippen, schnelle Kamera |
| 2 | Zuverlässigkeit | Sofort-Save, kein Datenverlust bei Crash |
| 3 | Performance | Kamera sofort, keine Wartezeiten |

---

## Cross-Cutting Concerns

| Concern | Strategie |
|---------|-----------|
| Persistenz | Room DB + Filesystem, Sofort-Save |
| Foto-Qualität | Mittlere Kompression (~2-3 MB) |
| App-Lifecycle | Nahtloses Fortsetzen nach Unterbrechung |
| Löschung | Kaskadierend (Vorgang + Schritte + Fotos) |
| Permissions | Kamera = Pflicht, freundliche Erklärung |

---

## Spec-Workflow

- Specs: `docs/specs/F-XXX-name.md` (source of truth, auf `main`)
- Issues: Referenzieren Specs via `[F-XXX]` im Titel
- Sprints: GitHub Milestones
- Branching: Feature-Branches pro Issue

---

## Dependencies (libs.versions.toml)

### Production
| Gruppe | Artefakte |
|--------|-----------|
| Core | core-ktx, lifecycle-runtime-ktx, activity-compose |
| Compose | ui, ui-graphics, ui-tooling-preview, material3 (via BOM) |
| CameraX | camera-core, camera-camera2, camera-lifecycle, camera-view |
| Image | coil-compose |

### Noch nicht eingebunden (geplant)
| Bibliothek | Zweck |
|------------|-------|
| Room | Lokale Datenbank |
| Navigation Compose | Screen-Navigation |
| Hilt/Koin | Dependency Injection |

### Testing
| Artefakt | Scope |
|----------|-------|
| junit | Unit |
| androidx-junit, espresso | Instrumented |
| compose-ui-test-junit4 | Compose UI |

---

## Common Commands

```bash
# Build
ANDROID_HOME=~/Library/Android/sdk ./gradlew assembleDebug

# Tests
ANDROID_HOME=~/Library/Android/sdk ./gradlew test

# Lint
ANDROID_HOME=~/Library/Android/sdk ./gradlew lint
```

---

## File Locations

| Was | Pfad |
|-----|------|
| App Entry | `app/src/main/java/com/boltmind/app/MainActivity.kt` |
| Application | `app/src/main/java/com/boltmind/app/BoltMindApplication.kt` |
| Theme | `app/src/main/java/com/boltmind/app/ui/theme/` |
| Manifest | `app/src/main/AndroidManifest.xml` |
| Dependencies | `gradle/libs.versions.toml` |
| Architecture | `docs/architecture.md` |
| Specs | `docs/specs/` |

---

**Generated:** 2026-02-07
**Analysis Depth:** quick
**Project Phase:** Early (Boilerplate + Architecture, keine Business-Logik)
