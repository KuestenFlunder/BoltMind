# BoltMind Coding Rules

## Architektur

### Pattern: MVVM (Model-View-ViewModel)

- **View** (Compose Screens): Stateless, empfängt State und Events vom ViewModel
- **ViewModel**: Hält UI-State als `StateFlow`, verarbeitet User-Aktionen, delegiert an Repository/UseCase
- **Model** (Data Layer): Room Entities, Repositories, DAOs

```
Screen (Composable)
    ↓ observes StateFlow
ViewModel
    ↓ calls
Repository
    ↓ queries/inserts
Room DAO → SQLite
```

### Package-Struktur: Feature-basiert

```
com.boltmind.app/
├── di/                              # Koin Module
│   └── AppModule.kt
├── data/
│   ├── local/
│   │   ├── BoltMindDatabase.kt      # Room Database
│   │   ├── ReparaturvorgangDao.kt
│   │   ├── SchrittDao.kt
│   │   └── ZeitMessungDao.kt        # Timer-Service DAO
│   ├── model/
│   │   ├── Reparaturvorgang.kt      # Room Entity
│   │   ├── Schritt.kt              # Room Entity (mit SchrittTyp)
│   │   └── ZeitMessung.kt          # Room Entity (Timer-Service)
│   └── repository/
│       └── ReparaturRepository.kt
├── feature/
│   ├── uebersicht/                  # F-001
│   │   ├── UebersichtScreen.kt
│   │   └── UebersichtViewModel.kt
│   ├── neuervorgang/                # F-002
│   │   ├── NeuerVorgangScreen.kt
│   │   └── NeuerVorgangViewModel.kt
│   ├── demontage/                   # F-003
│   │   ├── DemontageScreen.kt
│   │   └── DemontageViewModel.kt
│   └── montage/                     # F-004
│       ├── MontageScreen.kt
│       └── MontageViewModel.kt
├── service/
│   └── zeiterfassung/               # F-005: Timer-Service
│       └── ZeiterfassungService.kt
├── ui/
│   ├── navigation/
│   │   └── BoltMindNavHost.kt
│   └── theme/
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
├── MainActivity.kt
└── BoltMindApplication.kt
```

## Sprache & Naming (DDD)

### Domain-Begriffe: Deutsch

Domänen-Klassen und -Felder verwenden die deutsche Fachsprache aus der Architecture-Doku.

| Domain-Begriff | Klasse / Variable | Nicht verwenden |
|----------------|-------------------|-----------------|
| Reparaturvorgang | `Reparaturvorgang` | `RepairJob` |
| Schritt | `Schritt` | `Step` |
| SchrittTyp | `schrittTyp: SchrittTyp` | `stepType` |
| Bauteil-Foto | `bauteilFotoPfad` | `componentPhotoPath` |
| Ablageort-Foto | `ablageortFotoPfad` | `storageLocationPhotoPath` |
| Fahrzeugfoto | `fahrzeugFotoPfad` | `vehiclePhotoPath` |
| Auftragsnummer | `auftragsnummer` | `orderNumber` |
| Beschreibung | `beschreibung` | `description` |
| ZeitMessung | `ZeitMessung` | `TimeMeasurement` |

### Technische Begriffe: Englisch

Alles was nicht Domain ist, bleibt englisch:

```kotlin
// Domain: Deutsch
data class Reparaturvorgang(
    val id: Long = 0,
    val fahrzeugFotoPfad: String,
    val auftragsnummer: String,
    val beschreibung: String? = null,
    val status: VorgangStatus,
    val erstelltAm: Instant
)

// Technisch: Englisch
class ReparaturRepository(private val dao: ReparaturvorgangDao)
class DemontageViewModel(private val repository: ReparaturRepository) : ViewModel()
@Composable fun DemontageScreen(viewModel: DemontageViewModel)
```

### Naming Conventions

| Artefakt | Konvention | Beispiel |
|----------|-----------|---------|
| Entity | PascalCase, Domänen-Name | `Reparaturvorgang`, `Schritt` |
| DAO | Entity + Dao | `ReparaturvorgangDao` |
| Repository | Domäne + Repository | `ReparaturRepository` |
| ViewModel | Feature + ViewModel | `DemontageViewModel` |
| Screen | Feature + Screen | `DemontageScreen` |
| State | Feature + UiState | `DemontageUiState` |
| Package | lowercase, Feature-Name | `feature.demontage` |

## Dependency Injection: Koin

```kotlin
// di/AppModule.kt
val appModule = module {
    single { BoltMindDatabase.create(get()) }
    single { get<BoltMindDatabase>().reparaturvorgangDao() }
    single { get<BoltMindDatabase>().schrittDao() }
    single { ReparaturRepository(get(), get()) }
    viewModel { UebersichtViewModel(get()) }
    viewModel { DemontageViewModel(get()) }
}
```

## Testing: TDD (verbindlich)

### Grundregel

Kein Produktivcode ohne vorherigen Test. TDD ist keine Empfehlung, sondern der verbindliche Entwicklungsflow für jede Code-Änderung.

### TDD-Zyklus (zwingend bei jeder Änderung)

```
1. RED:    Test schreiben / anpassen → Test muss fehlschlagen
           └─ ./gradlew test  (rot bestätigen)
2. GREEN:  Minimal implementieren bis Test grün
           └─ ./gradlew test  (grün bestätigen)
3. REFACTOR: Code aufräumen, Tests müssen grün bleiben
           └─ ./gradlew test + ./gradlew ktlintCheck + ./gradlew detekt
```

### Gilt für ALLE Änderungstypen

| Änderungstyp | Test-Aktion |
|--------------|-------------|
| Neues Feature | Neue Tests aus Spec-User-Stories ableiten (US-XXX.Y → @Nested class) |
| Bugfix | Erst Test schreiben der den Bug reproduziert, dann fixen |
| Refactoring | Bestehende Tests müssen vor UND nach dem Refactoring grün sein |
| Spec-Änderung | Betroffene Tests anpassen bevor Code angepasst wird |

### Verboten

- Produktivcode schreiben bevor der zugehörige Test existiert
- Tests nach der Implementierung schreiben ("test-after")
- Tests deaktivieren, löschen oder überspringen um Build grün zu bekommen
- Implementierung ohne abschließendes `./gradlew test`

### Test-Typen

1. **Unit Tests**: ViewModels und Repositories (Hauptfokus)
2. **Integration Tests**: Room Database, Repository + DAO zusammen
3. **UI Tests**: Erst wenn die UI steht (nicht im MVP-Startfokus)

### Test-Struktur

```
app/src/
├── test/java/com/boltmind/app/           # Unit Tests
│   ├── feature/
│   │   ├── uebersicht/
│   │   │   └── UebersichtViewModelTest.kt
│   │   ├── neuervorgang/
│   │   │   └── NeuerVorgangViewModelTest.kt
│   │   ├── demontage/
│   │   │   └── DemontageViewModelTest.kt
│   │   └── montage/
│   │       └── MontageViewModelTest.kt
│   └── data/repository/
│       └── ReparaturRepositoryTest.kt
├── androidTest/java/com/boltmind/app/    # Integration Tests
│   └── data/local/
│       ├── ReparaturvorgangDaoTest.kt
│       └── SchrittDaoTest.kt
```

### User-Story-Traceability in Tests

Jede User Story aus der Spec (`US-XXX.Y`) wird als `@Nested inner class` im zugehörigen Test abgebildet. So lässt sich jeder Test direkt auf eine User Story und deren Akzeptanzkriterien zurückverfolgen.

**Regeln:**

1. **1 User Story = 1 `@Nested inner class`** mit der US-ID im Namen
2. **1 Akzeptanzkriterium = 1 `@Test`** — Testname beschreibt das erwartete Verhalten
3. **Given/When/Then** aus der Spec als Kommentare im Test-Body (Arrange/Act/Assert)
4. **Spec ist Source of Truth** — Tests leiten sich aus den Akzeptanzkriterien ab, nicht umgekehrt

### Test-Naming

Konvention: `@Nested inner class` mit US-ID, `@Test` mit Verhaltensbeschreibung aus der Spec.

```kotlin
class UebersichtViewModelTest {

    @Nested
    inner class `US-001_1 Offene Vorgaenge anzeigen` {

        @Test
        fun `zeigt alle offenen Vorgaenge sortiert nach letzter Bearbeitung`() {
            // Given: offene Vorgänge existieren
            val vorgaenge = listOf(
                testVorgang(auftragsnummer = "ALT", updatedAt = gestern),
                testVorgang(auftragsnummer = "NEU", updatedAt = heute)
            )
            // When: Startscreen geladen
            val uiState = viewModel.uiState.value
            // Then: sortiert nach updatedAt DESC (neueste oben)
            assertEquals("NEU", uiState.vorgaenge.first().auftragsnummer)
        }

        @Test
        fun `zeigt Hinweis wenn keine offenen Vorgaenge existieren`() {
            // Given: keine offenen Vorgänge existieren
            // When: Startscreen geladen
            // Then: leerer Zustand mit Hinweis-Text
        }
    }

    @Nested
    inner class `US-001_2 Vorgang fuer Weiterarbeit oeffnen` {

        @Test
        fun `oeffnet direkt Demontage-Flow bei Vorgang mit 0 Schritten`() {
            // Given: offener Vorgang mit 0 Schritten (frisch angelegt)
            // When: Mechaniker tippt Vorgang an
            // Then: Demontage-Flow (F-003) öffnet sich direkt
        }

        @Test
        fun `zeigt Auswahl-Dialog bei Vorgang mit mindestens 1 Schritt`() {
            // Given: offener Vorgang mit mindestens 1 Schritt
            // When: Mechaniker tippt Vorgang an
            // Then: Dialog mit "Weiter demontieren" und "Montage starten"
        }
    }

    @Nested
    inner class `US-001_4 Vorgang loeschen` {

        @Test
        fun `zeigt Bestaetigungsdialog vor Loeschung`() {
            // Given: Löschen-Button ist sichtbar
            // When: Mechaniker tippt "Löschen"
            // Then: Bestätigungsdialog erscheint
        }

        @Test
        fun `loescht Vorgang mit allen Schritten und Fotos nach Bestaetigung`() {
            // Given: Bestätigungsdialog ist sichtbar
            // When: Mechaniker bestätigt "Löschen"
            // Then: Vorgang, Schritte und Fotos gelöscht
        }

        @Test
        fun `bricht Loeschung bei Abbrechen ab`() {
            // Given: Bestätigungsdialog ist sichtbar
            // When: Mechaniker wählt "Abbrechen"
            // Then: Vorgang bleibt erhalten
        }
    }
}

class NeuerVorgangViewModelTest {

    @Nested
    inner class `US-002_1 Fahrzeug fotografieren` {

        @Test
        fun `oeffnet Kamera sofort beim Start des Anlage-Flows`() {
            // Given: Mechaniker hat auf "+" getippt
            // When: Anlage-Flow startet
            // Then: Kamera öffnet sich im Vollbild
        }

        @Test
        fun `legt keinen Vorgang an bei Back ohne Foto`() {
            // Given: Kamera ist geöffnet
            // When: System-Back-Button gedrückt
            // Then: zurück zur Übersicht, kein Vorgang angelegt
        }
    }

    @Nested
    inner class `US-002_2 Auftragsdaten erfassen und Vorgang starten` {

        @Test
        fun `speichert Vorgang mit Foto und Auftragsnummer in DB`() {
            // Given: Foto aufgenommen, Auftragsnummer eingegeben
            // When: "Starten" getippt
            // Then: Vorgang in DB gespeichert, Demontage-Flow öffnet
        }

        @Test
        fun `zeigt Validierungsfehler bei fehlender Auftragsnummer`() {
            // Given: Formular angezeigt
            // When: "Starten" ohne Auftragsnummer
            // Then: Validierungsmeldung "Auftragsnummer ist erforderlich"
        }

        @Test
        fun `speichert Vorgang auch ohne Beschreibung`() {
            // Given: Foto und Auftragsnummer vorhanden, keine Beschreibung
            // When: "Starten" getippt
            // Then: Vorgang wird gespeichert (Beschreibung optional)
        }
    }

    @Nested
    inner class `US-002_3 Foto wiederholen` {

        @Test
        fun `ersetzt altes Foto durch neues nach Wiederholung`() {
            // Given: Foto-Preview wird angezeigt
            // When: "Bild wiederholen" → neues Foto aufgenommen
            // Then: neues Foto ersetzt altes, altes wird gelöscht
        }

        @Test
        fun `behaelt Formulardaten bei Foto-Wiederholung`() {
            // Given: Auftragsnummer und Beschreibung eingegeben
            // When: "Bild wiederholen" → Back
            // Then: Formulardaten sind erhalten
        }
    }
}

## Compose Conventions

### State Hoisting

```kotlin
// ViewModel hält den State
class DemontageViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DemontageUiState())
    val uiState: StateFlow<DemontageUiState> = _uiState.asStateFlow()

    fun onBauteilFotoAufgenommen(photoPath: String) { ... }
    fun onAblageortFotoAufgenommen(photoPath: String) { ... }
    fun onSchrittTypGewaehlt(typ: SchrittTyp) { ... }
}

// Screen ist stateless
@Composable
fun DemontageScreen(
    uiState: DemontageUiState,
    onBauteilFotoAufgenommen: (String) -> Unit,
    onAblageortFotoAufgenommen: (String) -> Unit,
    onSchrittTypGewaehlt: (SchrittTyp) -> Unit,
    modifier: Modifier = Modifier
)
```

### Preview

Jeder Screen bekommt eine `@Preview` mit realistischen Daten:

```kotlin
@Preview(showBackground = true)
@Composable
fun DemontageScreenPreview() {
    BoltMindTheme {
        DemontageScreen(
            uiState = DemontageUiState(
                schrittNummer = 5,
                bauteilFotoPfad = "/path/to/photo.jpg"
            ),
            onBauteilFotoAufgenommen = {},
            onAblageortFotoAufgenommen = {},
            onSchrittTypGewaehlt = {}
        )
    }
}
```

## Code-Qualität

### ktlint

- Automatische Formatierung nach Kotlin Coding Conventions
- Läuft als Gradle Task: `./gradlew ktlintCheck`
- Format: `./gradlew ktlintFormat`

### detekt

- Statische Code-Analyse: Complexity, Code Smells, Style
- Konfiguration in `config/detekt/detekt.yml`
- Läuft als Gradle Task: `./gradlew detekt`

## Git Workflow

### Branch-Naming

```
feature/F-XXX/kurze-beschreibung
```

Beispiele:
- `feature/F-001/room-db-setup`
- `feature/F-001/vorgangs-uebersicht-screen`
- `feature/F-003/kamera-integration`

### Commit-Messages

```
[F-XXX] Kurze Beschreibung

Optionale Details

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>
```

### Workflow

```
1. Issue wählen, Spec lesen
2. Branch erstellen: feature/F-XXX/beschreibung
3. TDD-Zyklus (pro Akzeptanzkriterium wiederholen):
   a) Test schreiben/anpassen → ./gradlew test (rot)
   b) Implementieren          → ./gradlew test (grün)
   c) Refactoren              → ./gradlew test + ktlintCheck + detekt (grün)
4. PR erstellen → Review → Merge nach main
5. Issue schließen
```

## Verbotene Patterns

- **Kein God-ViewModel**: Max 200 Zeilen pro ViewModel, bei mehr aufteilen
- **Kein Business-Logic in Composables**: Logik gehört ins ViewModel
- **Keine hardcoded Strings in UI**: Alles in `strings.xml`
- **Kein `GlobalScope`**: Immer `viewModelScope` oder strukturierte Coroutines
- **Keine synchronen DB-Aufrufe**: Room-Queries immer als `suspend` oder `Flow`
- **Keine Wildcart-Imports**: Explizite Imports
