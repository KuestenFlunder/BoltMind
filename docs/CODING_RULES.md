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
│   │   └── SchrittDao.kt
│   ├── model/
│   │   ├── Reparaturvorgang.kt      # Room Entity
│   │   └── Schritt.kt              # Room Entity
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
| Ablageort | `ablageortNummer` | `storageLocationNumber` |
| Fahrzeug | `fahrzeug` | `vehicle` |
| Auftragsnummer | `auftragsnummer` | `orderNumber` |
| Beschreibung | `beschreibung` | `description` |

### Technische Begriffe: Englisch

Alles was nicht Domain ist, bleibt englisch:

```kotlin
// Domain: Deutsch
data class Reparaturvorgang(
    val id: Long = 0,
    val fahrzeug: String,
    val auftragsnummer: String,
    val beschreibung: String,
    val anzahlAblageorte: Int,
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

## Testing: TDD

### Strategie

1. **Test zuerst**: Vor jeder Implementierung wird der Test geschrieben
2. **Spec-describing Tests**: Testnamen beschreiben das erwartete Verhalten aus der Spec
3. **Unit Tests**: ViewModels und Repositories
4. **Integration Tests**: Room Database, Repository + DAO zusammen
5. **UI Tests**: Erst wenn die UI steht (nicht im MVP-Startfokus)

### Test-Struktur

```
app/src/
├── test/java/com/boltmind/app/           # Unit Tests
│   ├── feature/
│   │   ├── uebersicht/
│   │   │   └── UebersichtViewModelTest.kt
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

### Test-Naming

Tests beschreiben das Verhalten gemäß Spec:

```kotlin
class DemontageViewModelTest {
    @Test
    fun `schlaegt naechste Ablageort-Nummer automatisch vor`() { }

    @Test
    fun `speichert Schritt sofort in DB nach Ablageort-Bestaetigung`() { }

    @Test
    fun `Ablageort-Nummer macht Wrap-Around bei Erreichen des Maximums`() { }
}

class ReparaturRepositoryTest {
    @Test
    fun `loescht alle Schritte und Fotos bei Vorgang-Loeschung`() { }
}
```

## Compose Conventions

### State Hoisting

```kotlin
// ViewModel hält den State
class DemontageViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DemontageUiState())
    val uiState: StateFlow<DemontageUiState> = _uiState.asStateFlow()

    fun onFotoAufgenommen(photoPath: String) { ... }
    fun onAblageortBestaetigt() { ... }
    fun onAblageortGeaendert(nummer: String) { ... }
}

// Screen ist stateless
@Composable
fun DemontageScreen(
    uiState: DemontageUiState,
    onFotoAufgenommen: (String) -> Unit,
    onAblageortBestaetigt: () -> Unit,
    onAblageortGeaendert: (String) -> Unit,
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
                vorgeschlagenerAblageort = "7"
            ),
            onFotoAufgenommen = {},
            onAblageortBestaetigt = {},
            onAblageortGeaendert = {}
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
1. Issue wählen
2. Branch erstellen: feature/F-XXX/beschreibung
3. TDD: Test schreiben → Implementieren → Refactoren
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
