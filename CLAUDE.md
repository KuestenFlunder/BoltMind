# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
./gradlew assembleDebug          # Debug-Build
./gradlew test                   # Unit-Tests (JVM, JUnit 5)
./gradlew lint                   # Android Lint
```

Single test: `./gradlew test --tests "com.boltmind.app.ClassName"`

Build target: compileSdk 36, **minSdk 31**, targetSdk 36, Java 17, Gradle 8.14, AGP 8.10.1, Kotlin 2.1.10.

minSdk ist bewusst 31, nicht 26: das Design-System setzt durchgehend auf echten Hintergrund-Blur, und `RenderEffect` gibt es erst ab Android 12. Android 8 bis 11 werden nicht unterstuetzt. Siehe `docs/specs/design-system.md`, Abschnitt „Glas".

### Wichtig: Toolchain-Realität (Stand 2026-07-26)

| Erwartung | Realität |
|---|---|
| `./gradlew ktlintCheck` / `ktlintFormat` | **Existiert nicht.** Kein ktlint-Plugin in `build.gradle.kts` oder `libs.versions.toml`. |
| `./gradlew detekt` | **Existiert nicht.** Kein detekt-Plugin, kein `config/detekt/detekt.yml`. |
| `./gradlew connectedDebugAndroidTest` | **Läuft.** `app/src/androidTest/` enthält die Migrationstests. Braucht ein Gerät oder den Emulator (siehe unten). |
| Android SDK | **Installiert** unter `/opt/homebrew/share/android-commandlinetools`, `local.properties` ist gesetzt. Kein Android Studio nötig — Gradle-Wrapper genügt. |

Diese Punkte sind in `docs/CODING_RULES.md` als Soll beschrieben, aber noch nicht umgesetzt. Nicht so tun, als liefen die Checks — entweder Plugins einrichten oder den Schritt explizit als übersprungen melden.

### Test-Stack

JUnit 5 (`useJUnitPlatform()`), nicht JUnit 4. Verfügbar: `junit-jupiter-api/engine/params`, `mockito-kotlin`, `turbine`, `kotlinx-coroutines-test`, `koin-test-junit4`, `room-testing`.

## Architecture

MVVM mit feature-basierter Package-Struktur:

```
Screen (Stateless @Composable) → observes StateFlow
  ViewModel (State + Events, max 200 LOC) → calls
    Repository (Data Access) → queries
      Room DAO → SQLite
```

Package-Layout unter `com.boltmind.app/` (✅ = existiert, ⬜ = geplant):

```
di/                     ✅ AppModule.kt (Koin)
data/local/             ✅ BoltMindDatabase, Converters, ReparaturvorgangDao, SchrittDao
                        ⬜ SchrittFotoDao — Zielmodell F-003
data/model/             ✅ Reparaturvorgang, Schritt, SchrittTyp, VorgangStatus,
                           ReparaturvorgangMitAnzahl (@Embedded Projection)
                        ⬜ SchrittFoto — Zielmodell F-003; SchrittTyp entfällt dabei
data/repository/        ✅ ReparaturRepository
data/foto/              ✅ FotoManager — Filesystem-Handling, temp→permanent, EXIF-Stripping
feature/uebersicht/     ✅ F-001
feature/neuervorgang/   ✅ F-002
feature/demontage/      ✅ F-003 (Screen + ArbeitsphaseView, DemontageDialog, PreviewView)
feature/montage/        ⬜ F-004 — Route existiert, zeigt nur "kommt in F-004"
service/zeiterfassung/  ⬜ F-005 — nichts implementiert (kein ZeitMessung-Entity/DAO/Service)
ui/navigation/          ✅ BoltMindNavHost + BoltMindRoutes
ui/schrittbrowser/      ⬜ F-006 — SchrittBrowser, SchrittBrowserState,
                           SchrittThumbnailLeiste, SchrittFotoKarussell
ui/theme/               ✅ Color, Dimensions, Shape, Theme, Type, Glas, GlasRezepte (Dark-only)
ui/components/          ✅ BoltMindButton/Card/Dialog/TopBar, DebounceClick, FotoPreview,
                           PremiumEffects, SchrittNummer, StatusBadge
```

Jedes Feature-Package enthält: `*Screen.kt`, `*ViewModel.kt`, `*UiState.kt`.
Service-Packages enthalten: `*Service.kt`, `*Dao.kt`, Entity.
`ui/schrittbrowser/` (F-006) ist bewusst **kein** Feature-Package: geteilte, zustandslose Compose-Komponente ohne ViewModel, UiState oder DB-Zugriff — der Consumer liefert den State und reagiert auf Callbacks.

### Room-Datenbank

DB-Name `boltmind.db`, aktuell **Version 2** mit `MIGRATION_1_2` (Umbenennung `reihenfolge`→`schrittNummer`, `fotoPfad`→`bauteilFotoPfad`, neue Felder `typ`/`ablageortFotoPfad`). Schemas werden nach `app/schemas/` exportiert (`1.json`, `2.json`) — bei Entity-Änderungen **Migration schreiben**, kein `fallbackToDestructiveMigration`.

**Zielzustand ist Version 3** (spezifiziert in `docs/specs/F-003-demontage/README.md`): neue Tabelle `schritt_foto`, `schritt` verliert `typ`, `bauteilFotoPfad` und `ablageortFotoPfad`. `MIGRATION_2_3` ist noch nicht geschrieben, `3.json` noch nicht exportiert.

`Instant` wird via `Converters` als Epoch-Millis persistiert.

## Domain-Sprache (DDD)

Domain-Begriffe auf **Deutsch**, technische Begriffe auf **Englisch**:

| Domain (DE) | Bedeutung | Beispiel-Code |
|---|---|---|
| Reparaturvorgang | Repair job | `Reparaturvorgang.kt`, `ReparaturRepository` |
| Schritt | Disassembly step, hält N Fotos | `Schritt.kt`, `SchrittDao` |
| SchrittFoto | Ein einzelnes Foto eines Schritts (0..n), mit Reihenfolge und Labeln | `SchrittFoto.kt`, `SchrittFotoDao` |
| Foto-Label | Bauteil / Übersicht / Ablageort — unabhängig, kombinierbar, Default Bauteil | `istBauteil`, `istUebersicht`, `istAblageort` |
| Ablageort | Physischer Ablageort des ausgebauten Teils — ein **Foto-Label**, kein eigener Schritt und kein Schritt-Typ | `istAblageort: Boolean` |
| Fahrzeugfoto | Vehicle photo | `fahrzeugFotoPfad: String` |
| Auftragsnummer | Order number | `auftragsnummer: String` |
| ZeitMessung | Time measurement | `ZeitMessung.kt`, `ZeitMessungDao` |

Funktionsnamen für Domain-Events ebenfalls Deutsch: `onFotoAufgenommen()`, `onLabelGeaendert()`.

**Nicht mehr gültig:** `SchrittTyp` (AUSGEBAUT/AM_FAHRZEUG), `bauteilFotoPfad`, `ablageortFotoPfad` und feste Foto-Slots am Schritt. Sie entfallen ersatzlos (F-003 README, F-004 README, `governance.md`). Der Code hält sie noch — siehe Implementierungsstand, nicht als Vorbild verwenden.

## Spec-driven Development

### Die Kette: Spec → Issue → Test → Code

```
docs/specs/F-XXX-name/           Feature-Spec = Source of Truth
  README.md                      Intention, Problem, Lösung, Abhängigkeiten (stabil)
  <feature>.md | views/*.md      User Stories US-XXX.N + Given/When/Then-Akzeptanzkriterien
  workflow.md                    (nur komplexe Features) State Machine, Transitions
        ↓
GitHub Issue                     Titel: "[F-XXX] Beschreibung (US-XXX.N)", Body = Story + AKs (Copy),
                                 Label = Feature (z.B. F-001), Milestone = Feature-Phase
        ↓
Test                             1 User Story = 1 @Nested inner class `US-XXX_Y ...`
                                 1 Akzeptanzkriterium = 1 @Test (Name aus dem Then)
        ↓
Code                             Erst nach RED. Siehe TDD-Workflow unten.
```

**Regeln aus `docs/SpecBestPractices.md`:**
- Einzeldatei wenn Feature ≤1 View, kein komplexer Workflow, <200 Zeilen. Sonst Ordner.
- Jede Spec-Datei hat **einen** Änderungsgrund (View-Spec / Workflow-Spec / Service-Spec / Komponenten-Spec / README).
- View-Specs bündeln UI **und** DB-Interaktion derselben View — sie ändern sich gemeinsam.
- Service-Features (F-005) und gemeinsame Komponenten-Module (F-006) kennen ihre Consumer nicht. Die Consumer beschreiben die Integration in ihrer eigenen Spec. Abhängigkeitsrichtung immer Consumer → Modul.
- Offene Punkte werden explizit mit `[OFFEN]` markiert und vor der Implementierung geklärt.

### Issue-Tracker (github.com/KuestenFlunder/BoltMind)

Issues via `gh issue list`. Ein Issue ist eine **Vertical Slice**: es geht durch alle Schichten (Datenschicht → ViewModel → UI → Tests) und liefert für sich Nutzerwert. Milestones bündeln Scheiben zu Liefer-Wellen:

```
R0–R4                        Abgeschlossen und geschlossen (Stand 2026-07-27)
R5: Absicherung              Aufgaben aus #76–#93, die trotz geschlossenem Issue
                             offen blieben, plus dabei gefundene Defekte
R6: Offene Entscheidungen    Produktfragen im gebauten Stand — brauchen eine
                             Antwort, bevor Code entsteht
```

**Nicht mehr verwenden:** die früheren Schichten-Milestones `F-XXX-A/B/C` (Datenschicht / ViewModel / UI). Sie sind geschlossen. Eine Datenschicht ohne Oberfläche ist nicht abnehmbar, und die Issues einer Schicht altern gemeinsam, wenn sich die Spec ändert — genau das ist bei den F-004-Issues passiert.

**Titel-Konvention:** `[F-XXX] Beschreibung (US-XXX.N)`. Ausnahmen: die frühen F-001/F-002-Issues (#13–#20) nutzen noch `[US-XXX.N] Titel` (Altbestand), und Arbeit ohne besitzende User Story trägt `[Governance]` — siehe unten.

**Issue-Body-Struktur:** `## Kontext` → `## Spec-Referenz` (Datei + **Abschnittsüberschrift**, keine Zeilennummern — die rotten) → `## Aufgaben` (Checkliste) → `## Akzeptanzkriterien`. Bei Abhängigkeiten zusätzlich `## Blockiert von` / `## Entblockt`.

**Labels:** `F-001` … `F-006`, `spec`, `feature`, `infra`, `bug`, `enhancement` + GitHub-Defaults. Jedes Feature hat ein eigenes Label; Issues ohne Feature-Label sind ein Versehen.

### `[Governance]`-Issues

Manche Arbeit folgt aus `governance.md` oder `docs/CODING_RULES.md` statt aus einer User Story — Design-Token, Test-Infrastruktur, projektweite Invarianten. Eine US-Nummer zu erfinden wäre unehrlich, weil der Reviewer das Kriterium in keiner Spec fände.

Solche Issues tragen `[Governance]` ohne US-Suffix, das Label `infra` und alle berührten Feature-Label. Sie dürfen den Nutzerwert-Test nur überspringen, wenn **alle drei** gelten: mindestens zwei Scheiben brauchen sie, sie sind allein verifizierbar (ein Test oder Gradle-Task wird grün), und der Body nennt unter `## Entblockt` die abhängigen Scheiben namentlich. Sonst gehört die Arbeit in die eine Scheibe, die sie braucht.

### Produktentscheidungen, die älteren Ständen vorgehen

Diese Punkte wurden nach mehreren Spec-Überarbeitungen entschieden. Wenn ein älterer Text, ein Issue-Kommentar oder eine Erinnerung etwas anderes sagt, gilt das hier:

- **Ablageort ist ein Foto-Label, kein eigener Schritt.** Ein Schritt hält N Fotos (`SchrittFoto`), jedes mit den drei kombinierbaren Flags Bauteil / Übersicht / Ablageort, Default Bauteil. `SchrittTyp` ist ersatzlos gestrichen.
- **Schrittnummer und Fortschritt sind zwei verschiedene Dinge.** „Schritt 12" ist immer die Demontage-Nummer und wird nie umnummeriert — sie ist die Korrelation zum physischen Ablageort. Der Fortschritt heißt getrennt davon „3 von 15 eingebaut". Formulierungen wie „Schritt 5 von 15" vermischen beides und sind verboten.
- **Der Thumbnail-Sprung gehört F-006, Vor/Zurück dem Consumer** (entschieden am 2026-07-31, vorher lag beides bei F-006). Die drei Betriebsarten haben an derselben Stelle unterschiedliche Bedienelemente — die Demontage hat gar kein Vor/Zurück, die Montage nur „ZURÜCK", das Archiv zwei schlichte Pfeile. Ein einheitliches Vor/Zurück aus F-006 hätte daneben gestanden statt darin. Die Consumer hängen ihre Kreise in den Slot `bedienkreise`. Einen Sprung-Dialog mit Nummerneingabe gibt es nicht.
- **Horizontales Wischen** im Bildbereich wechselt das **Foto innerhalb des Schritts** (Karussell), nie den Schritt.
- **Label sind nur in der Demontage änderbar.** F-004 und das Archiv zeigen sie, ändern sie aber nicht.
- **Abschluss nur über den Abschluss-Screen.** Ist der letzte Schritt abgehakt, erscheint „Zusammenbau abgeschlossen!" mit „Archivieren"-Button. Back führt zum letzten Schritt zurück, **ohne** zu archivieren. Der „Weiter"-Button wird nicht zum Abschlussbutton — Archivieren nimmt den Vorgang aus der aktiven Liste und braucht mit Handschuhen eine Bestätigung.
- **Häkchen zurücknehmen nur mit Warndialog.** Bei Abbruch bleibt die Markierung.
- **Wiederholen löscht erst nach Erfolg.** Erst die Kamera starten, das alte Foto und seine Datei erst nach bestätigter Neuaufnahme löschen. Ein Kamera-Abbruch darf nie ein vorhandenes Foto vernichten.

**Warnung aus der Projekthistorie:** Die zwölf F-004-Issues #64–#75 wurden 21 Minuten vor dem Merge der überarbeiteten `montage.md` erstellt und beschrieben monatelang einen Stand, den es nicht mehr gab (Fortschrittsbalken, Numpad-Sprung, Perlenkette). Sie sind inzwischen geschlossen und durch #83–#85 ersetzt. Lehre: **Spec ist Source of Truth, nicht der Issue-Text.** Weicht ein Issue von der Spec ab, erst das Issue korrigieren.

## Pflichtlektüre vor Code-Änderungen

**VOR jeder Implementierung** müssen gelesen werden:
1. `docs/CODING_RULES.md` — Vollständige Coding-Konventionen und Test-Struktur
2. `docs/specs/governance.md` — Projektweite Regeln (Sofort-Save, Foto-Handling, DDD, Service-Architektur)
3. `docs/specs/F-XXX-name/README.md` — Feature-Intention und Abhängigkeiten
4. `docs/specs/F-XXX-name/*.md` — Detail-Specs (User Stories, Workflow, Views)

## Implementierungsstand (Stand 2026-07-31, PR #117)

Der Design-Prototyp ist umgesetzt. Alle sechs Features sind gebaut; die Reife
unterscheidet sich noch.

| Feature | Stand | Details |
|---|---|---|
| **F-001** Übersicht | ✅ | Tabs, Vorgangsliste mit Dauer im Archiv, Leerzustände, FAB, Auswahl- und Lösch-Sheet. Vier-Stufen-Datumsregel. Archiv-Detailansicht ist der Browser im Modus ARCHIV. |
| **F-002** Anlage | ✅ | System-Kamera, Pflicht-Auftragsnummer, Beschreibungsfeld, Direktstart in die Demontage. CameraX und `CAMERA`-Permission sind raus. |
| **F-003** Demontage | ✅ | Browser im Modus DEMONTAGE. Der Schritt beginnt mit der Kamera, nicht mit einer leeren Maske; ein Abbruch am frisch eröffneten Schritt rollt zurück. Am 2026-08-01 am Emulator gegen die Datenbank verifiziert. |
| **F-004** Montage | ✅ | Modus MONTAGE plus Abschluss-Screen. Am 2026-07-27 mit gesetzter Datenbank durchgespielt: Wiedereinstieg, Abhaken, Häkchen zurücknehmen, Archivieren. |
| **F-005** Zeiterfassung | ✅ | `ZeitMessung`, DAO, Service, Timer-Chip. Pausierbar — siehe unten. |
| **F-006** Schritt-Browser | ✅ | `ui/schrittbrowser/`, zustandslos, drei Betriebsarten. |

**Zwei Produktentscheidungen warten auf Bestätigung** (`docs/specs/design-system.md`, K-03/K-04):
Der Timer ist von Hand pausierbar, und die Montage misst ebenfalls. Beides kippt eine
zuvor bindende MVP-Antwort in `F-005/service.md`.

**Tests:** 207 JVM-Tests (`./gradlew test`) plus 13 instrumentierte Tests
(`./gradlew connectedDebugAndroidTest`) — vier Room-Migrationen und neun Compose-UI-Tests.
Das UI-Test-Harness steht seit #104; die Konventionen und die Animations-Falle stehen in
`docs/CODING_RULES.md`.

**Emulator:** AVD `boltmind36` (Android 16, arm64). Starten mit
`emulator -avd boltmind36 -gpu host`, danach `./gradlew installDebug`. Für ein echtes
Gerät ändert sich nur das Ziel — USB-Debugging genügt, Android 12 oder neuer.

**Offen:** achtzehn Issues, neun je Milestone. **R5** ist Arbeit ohne Entscheidungsbedarf —
darunter die Glas-Effekt-Abweichungen (#120), die gestauchte Montage-Bedienzeile (#124) und
die LOC-Grenze des `BrowserViewModel` (#126, inzwischen 340 Zeilen). Die Demontage-Sackgasse
nach Feierabend (#121) ist mit dem Kamera-Umbau vom 2026-08-01 erledigt: der Einstieg ohne
offenen Schritt legt jetzt einen an — Issue noch zu schließen.

**Neuer Befund vom 2026-08-01, noch ohne Issue:** die Android-Zurück-Geste verlässt die
Demontage zur Übersicht, obwohl `workflow.md` US-003.6 „Beenden" als einzigen Ausstieg
festlegt. Der einzige `BackHandler` sitzt im Vollbild (`SchrittBrowser.kt`), nicht auf
Ebene der Schritt-Ansicht.
**R6** sind Produktfragen, die eine Antwort brauchen, bevor Code entsteht — darunter das
Splash-Video und die zwei Timer-Entscheidungen aus #94.

Der Issue-Nachzug ist erledigt: #76–#96 wurden gegen den gebauten Stand geprüft, fünf
rückwirkende Issues (#99–#103) schließen die Lücken der Kette Spec → Issue → Test für
Arbeit, die in PR #98 ohne Issue entstand.

### Veraltete Doku

`claudedocs/Structure.md` stammt vom 2026-02-07 und behauptet „5 Kotlin-Dateien, 0 Entities, 0 Tests". Das ist überholt — nicht als Referenz verwenden. Gültig sind `docs/architecture.md`, `docs/CODING_RULES.md` und die Specs.

## Coding Conventions

- **DI**: Koin (Module in `di/`). ViewModels via `koinViewModel()`, Nav-Argumente via `SavedStateHandle` (siehe `DemontageViewModel`).
- **State**: `MutableStateFlow<UiState>` im ViewModel, Screen erhält State + Callbacks
- **Persistence**: Room DB für Metadaten, Filesystem für Fotos. Sofort-Save bei jeder Aktion
- **Strings**: Alle UI-Strings in `res/values/strings.xml`, keine Hardcoded-Strings
- **Coroutines**: `viewModelScope` verwenden, kein `GlobalScope`
- **DB-Zugriffe**: Immer asynchron (suspend functions)
- **Compose**: State Hoisting, `@Preview` für jeden Screen

### Kamera & Fotos

**Zielzustand (in `docs/specs/governance.md` verankert, gilt projektweit ohne Ausnahme):**

- Ausschließlich die **System-Kamera** via `ActivityResultContracts.TakePicture()` + `FileProvider`. Kein CameraX, keine app-eigene Kameraansicht.
- **Keine `CAMERA`-Permission** im Manifest — die System-Kamera-App verwaltet ihre Berechtigung selbst.
- **Keine app-eigene Foto-Bestätigung.** Die System-Kamera bestätigt selbst; danach hängt das Foto direkt am Kontext und wird sofort persistiert. Kein zusätzlicher Preview-Screen mit „Bestätigen"/„Wiederholen".
- „Wiederholen" existiert nur als Aktion **am bereits aufgenommenen Foto**: Foto löschen, Kamera erneut starten.
- **Kein `photos/temp/` mehr — projektweit, auch nicht in F-002.** Die System-Kamera schreibt direkt in die Zieldatei unter `photos/`. Bei Kamera-Abbruch oder verworfenem Anlage-Flow wird die Datei gelöscht.
- **Cleanup-Regel (governance.md):** beim App-Start werden Dateien in `photos/` gelöscht, auf die keine DB-Zeile verweist — geprüft gegen `SchrittFoto.pfad` **und** `Reparaturvorgang.fahrzeugFotoPfad`.
- Foto-Qualität ist bei einer fremden Kamera-App **nicht steuerbar**: ca. 2–3 MB sind ein Erwartungswert, keine erzwingbare Vorgabe. Die App übernimmt die gelieferte Datei wie sie ist.

**Ist-Zustand im Code:** entspricht dem Zielzustand. Beide Features nutzen die System-Kamera über `ActivityResultContracts.TakePicture()` + `FileProvider`; CameraX ist in keiner Gradle-Datei mehr, die `CAMERA`-Permission steht nicht im Manifest (dort nur ein Kommentar, warum sie fehlt). Die früher hier beschriebene Abweichung — CameraX in F-002, app-eigene Foto-Bestätigung in F-003 — ist erledigt (geprüft am 2026-08-01).

**Wer die Kamera startet:** in beiden Features das **ViewModel**, nicht die Route bzw. der Screen. Der Zustand trägt einen `KameraAuftrag`; die Route hängt einen `LaunchedEffect` daran. In F-003 trägt der Auftrag zusätzlich die **Ziel-Schritt-Id**, damit ein Foto nicht am betrachteten Schritt landet — der ist unmittelbar nach „NÄCHSTES TEIL" für einen Moment noch der eben abgeschlossene. Ein Tap darf die Kamera nie direkt starten: sonst läuft sie parallel zur Schritt-Anlage. Details in `docs/specs/F-003-demontage/workflow.md`, Abschnitt „Reihenfolge beim Schritt-Start".

`FotoManager` kapselt das Dateihandling und hängt noch am alten Zyklus:
- `photos/temp/` = unbestätigte Aufnahmen, wird bei App-Start via `BoltMindApplication.onCreate()` geleert — **entfällt im Zielzustand**
- `photos/` = Zielordner, aktuell per `renameTo()` aus `temp/` befüllt
- EXIF-Stripping (GPS + Zeitstempel), best-effort — bleibt, wandert an die Übernahme der Datei

### UI-Theme

Dark-only, **Orange `#FF7A1A` auf `#0B0C0D`**, Glasflächen über einem Mesh-Verlauf. Verbindliche Quelle ist `docs/specs/design-system.md` — dort stehen alle Farb-, Schrift- und Maßtoken sowie der Glas-Rezeptkatalog.

Das frühere Schema war Cyan `#00E5FF` auf Navy; die hier lange behauptete Farbe `#FF741F` war zu keinem Zeitpunkt im Code. Beides ist hinfällig.

Eigene Bausteine aus `ui/components/` statt roher Material3-Widgets verwenden — insbesondere `Modifier.boltKlick` (enthält die 300ms-Sperre aus der Governance), `GlasFlaeche`, `Rundbutton` und `BoltSheet`. Abstände und Touch-Targets kommen aus `ui/theme/Dimensions.kt`, nicht als Magic Numbers.

## TDD Workflow (verbindlich)

TDD ist keine Empfehlung, sondern Pflicht bei JEDER Code-Änderung (Feature, Bugfix, Refactoring).

**Zwingender Zyklus:**
```
1. RED:      Test schreiben/anpassen → ./gradlew test (muss fehlschlagen)
2. GREEN:    Implementieren          → ./gradlew test (muss grün sein)
3. REFACTOR: Aufräumen               → ./gradlew test (grün bleiben)
```

`ktlintCheck` und `detekt` existieren weiterhin nicht — nie behaupten, sie seien gelaufen. `./gradlew test` und `./gradlew connectedDebugAndroidTest` laufen dagegen beide und sind zu benutzen.

**Verboten:** Code ohne vorherigen Test schreiben. Tests deaktivieren/löschen um Build grün zu bekommen.

**Tests bilden User Stories ab:** Jede `US-XXX.Y` wird als `@Nested inner class` im Test abgebildet. Testnamen leiten sich aus den Given/When/Then-Akzeptanzkriterien der Spec ab. Details und Beispiele in `docs/CODING_RULES.md`.

## Git Workflow

Branch-Naming: `feature/F-XXX/kurze-beschreibung`
Commit-Prefix: `[F-XXX] Beschreibung`
Remote: `github.com/KuestenFlunder/BoltMind`, Default-Branch `main`, Merge über PRs.

CI: `.github/workflows/claude.yml` (@claude-Mentions) und `claude-code-review.yml` (automatischer Review bei jedem PR). **Keine** Build-/Test-Pipeline in CI — Tests laufen nur lokal.

## Key Documentation

- `docs/architecture.md` — Arc42-light Architektur-Übersicht (Vision, Domäne, Quality Goals, MVP-Abgrenzung)
- `docs/CODING_RULES.md` — Vollständige Coding-Konventionen und Test-Struktur
- `docs/specs/governance.md` — Projektweite Regeln (Sofort-Save, Debounce, Foto-Handling, DDD, Service-Architektur, Plattform-Randbedingungen)
- `docs/specs/design-system.md` — **verbindlich für alles Visuelle**: Farben, Typografie, Glas-Rezepte, Maße, und die zehn dokumentierten Abweichungen vom Design-Prototyp
- `docs/SpecBestPractices.md` — Spec-Schreibregeln, INVEST, Ordner-Struktur (liegt unter `docs/`, **nicht** in `docs/specs/`)
- `docs/specs/F-XXX-name/` — Feature-Spezifikationen als Ordner (F-001 bis F-006)
- `docs/specs/F-006-schritt-browser/` — gemeinsames UI-Modul (Thumbnail-Leiste, Foto-Karussell, Vollbild, Label-Checkboxen, Schritt-Navigation), genutzt von F-001, F-003 und F-004. Kein Service, kein eigenes ViewModel.

### Quality Goals (Priorität absteigend)

1. **Bedienbarkeit unter Werkstatt-Bedingungen** — Handschuhe, ölige Hände, Arbeit im Stehen. Große Touch-Targets (verbindliche Mindestmaße in `docs/specs/governance.md`), minimale Interaktionen, 300ms-Debounce global.
2. **Zuverlässigkeit** — Sofort-Save, kein Datenverlust bei Unterbrechung. Unterbrechungen sind der Normalfall.
3. **Performance** — Kamera sofort, Schrittübergänge ohne Wartezeit, kein Spinner beim Listenaufbau.

Bei Design-Entscheidungen in dieser Reihenfolge abwägen.

## Verbotene Patterns

- Business-Logik in Composables
- ViewModel > 200 LOC (gemessen: Browser 279, Uebersicht 146, NeuerVorgang 141, Abschluss 69 — `BrowserViewModel` reisst das Limit, weil es alle drei Betriebsarten bedient; offen als #126)
- Synchrone DB-Calls auf Main-Thread
- Wildcard-Imports
- `GlobalScope`
- Dual-Purpose-DB-Felder (ein Feld für Workflow *und* Timer) → eigene Tabelle
- `fallbackToDestructiveMigration` — Room-Migrationen werden von Hand geschrieben
